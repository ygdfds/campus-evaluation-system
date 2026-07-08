package com.campus.evaluation.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.auth.domain.dto.AssignRolesDTO;
import com.campus.evaluation.auth.domain.dto.ChangeUserStatusDTO;
import com.campus.evaluation.auth.domain.dto.ResetPasswordDTO;
import com.campus.evaluation.auth.domain.dto.StaffUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.StaffUserUpdateDTO;
import com.campus.evaluation.auth.domain.entity.AuthPersonProfile;
import com.campus.evaluation.auth.domain.entity.AuthRole;
import com.campus.evaluation.auth.domain.entity.AuthUserAccount;
import com.campus.evaluation.auth.domain.entity.AuthUserRole;
import com.campus.evaluation.auth.domain.vo.StaffUserVO;
import com.campus.evaluation.auth.domain.vo.UserDetailVO;
import com.campus.evaluation.auth.mapper.AuthPersonProfileMapper;
import com.campus.evaluation.auth.mapper.AuthRoleMapper;
import com.campus.evaluation.auth.mapper.AuthUserAccountMapper;
import com.campus.evaluation.auth.mapper.AuthUserRoleMapper;
import com.campus.evaluation.auth.service.SchoolStaffUserService;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolStaffUserServiceImpl implements SchoolStaffUserService {

    private static final Set<String> STAFF_ALLOWED_ROLES = Set.of(
            "staff", "teaching_admin", "service_admin", "feedback_handler", "form_publisher"
    );
    private static final Set<String> STAFF_FORBIDDEN_ROLES = Set.of(
            "system_admin", "school_admin", "student"
    );

    private final AuthUserAccountMapper userAccountMapper;
    private final AuthPersonProfileMapper personProfileMapper;
    private final AuthRoleMapper roleMapper;
    private final AuthUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${campus.security.default-password:123456}")
    private String defaultPassword;

    @Override
    public PageResult<StaffUserVO> list(String keyword, String status, Long teachingOrgId,
                                         Long serviceOrgId, String roleCode, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();

        LambdaQueryWrapper<AuthPersonProfile> profileWrapper = new LambdaQueryWrapper<AuthPersonProfile>()
                .eq(AuthPersonProfile::getTenantId, tenantId)
                .eq(AuthPersonProfile::getRoleType, "staff")
                .eq(teachingOrgId != null, AuthPersonProfile::getTeachingOrgId, teachingOrgId)
                .eq(serviceOrgId != null, AuthPersonProfile::getServiceOrgId, serviceOrgId)
                .like(keyword != null && !keyword.isEmpty(), AuthPersonProfile::getRealName, keyword);

        List<AuthPersonProfile> profiles = personProfileMapper.selectList(profileWrapper);
        if (profiles.isEmpty()) {
            return new PageResult<>(0, List.of(), pageNum, pageSize);
        }

        List<Long> userIds = profiles.stream().map(AuthPersonProfile::getUserId).collect(Collectors.toList());

        // 如果有 roleCode 过滤，需要查 auth_user_role
        if (roleCode != null && !roleCode.isEmpty()) {
            AuthRole targetRole = roleMapper.selectRoleByCodeAndTenant(roleCode, tenantId);
            if (targetRole != null) {
                List<AuthUserRole> urs = userRoleMapper.selectList(
                        new LambdaQueryWrapper<AuthUserRole>()
                                .eq(AuthUserRole::getRoleId, targetRole.getId())
                                .in(AuthUserRole::getUserId, userIds));
                Set<Long> matchedIds = urs.stream().map(AuthUserRole::getUserId).collect(Collectors.toSet());
                userIds = userIds.stream().filter(matchedIds::contains).collect(Collectors.toList());
                if (userIds.isEmpty()) {
                    return new PageResult<>(0, List.of(), pageNum, pageSize);
                }
            } else {
                return new PageResult<>(0, List.of(), pageNum, pageSize);
            }
        }

        final List<Long> finalUserIds = userIds;

        LambdaQueryWrapper<AuthUserAccount> wrapper = new LambdaQueryWrapper<AuthUserAccount>()
                .in(AuthUserAccount::getId, finalUserIds)
                .eq(status != null && !status.isEmpty(), AuthUserAccount::getStatus,
                        "enabled".equals(status) ? "active" : status);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(AuthUserAccount::getUsername, keyword)
                    .or().like(AuthUserAccount::getPhone, keyword)
                    .or().like(AuthUserAccount::getEmail, keyword)
                    .or().in(AuthUserAccount::getId, finalUserIds));
        }
        wrapper.orderByAsc(AuthUserAccount::getId);

        Page<AuthUserAccount> page = userAccountMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<StaffUserVO> records = page.getRecords().stream()
                .map(acc -> toStaffVO(acc, findProfile(profiles, acc.getId())))
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public UserDetailVO getById(Long id) {
        Long tenantId = requireTenantId();
        AuthUserAccount account = getAccountInTenant(id, tenantId);
        AuthPersonProfile profile = getProfileByUserId(id);
        validateIsStaff(profile);

        List<AuthRole> roles = roleMapper.selectRolesByUserId(id);

        return UserDetailVO.builder()
                .id(account.getId()).username(account.getUsername()).realName(profile.getRealName())
                .userType(profile.getRoleType()).phone(account.getPhone()).email(account.getEmail())
                .status(account.getStatus()).tenantId(account.getTenantId())
                .schoolId(userAccountMapper.selectSchoolIdByTenantId(tenantId))
                .avatarFileId(account.getAvatarFileId())
                .roleCodes(roles.stream().map(AuthRole::getRoleCode).collect(Collectors.toList()))
                .roleNames(roles.stream().map(AuthRole::getRoleName).collect(Collectors.toList()))
                .staffNo(profile.getNoWork())
                .teachingOrgId(profile.getTeachingOrgId())
                .teachingOrgName(profile.getTeachingOrgId() != null
                        ? userAccountMapper.selectTeachingOrgName(profile.getTeachingOrgId()) : null)
                .serviceOrgId(profile.getServiceOrgId())
                .serviceOrgName(profile.getServiceOrgId() != null
                        ? userAccountMapper.selectServiceOrgName(profile.getServiceOrgId()) : null)
                .mustChangePassword(account.getMustChangePassword())
                .lastLoginAt(account.getLastLoginAt())
                .createdAt(account.getCreatedAt()).updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public StaffUserVO create(StaffUserCreateDTO dto) {
        Long tenantId = requireTenantId();

        if (userAccountMapper.existsByUsername(dto.getUsername(), null) > 0) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()
                && userAccountMapper.existsByPhone(dto.getPhone(), null) > 0) {
            throw new BusinessException(409, "手机号已被使用");
        }
        if (dto.getTeachingOrgId() != null
                && userAccountMapper.countTeachingOrg(dto.getTeachingOrgId(), tenantId) == 0) {
            throw new BusinessException(400, "教学组织不存在或不属于当前租户");
        }
        if (dto.getServiceOrgId() != null
                && userAccountMapper.countServiceOrg(dto.getServiceOrgId(), tenantId) == 0) {
            throw new BusinessException(400, "服务组织不存在或不属于当前租户");
        }

        // 确定角色列表
        List<String> roleCodes = new ArrayList<>();
        roleCodes.add("staff"); // 必须包含 staff
        if (dto.getRoleCodes() != null) {
            for (String code : dto.getRoleCodes()) {
                if (!"staff".equals(code) && STAFF_ALLOWED_ROLES.contains(code)) {
                    roleCodes.add(code);
                }
                if (STAFF_FORBIDDEN_ROLES.contains(code)) {
                    throw new BusinessException(400, "不允许分配角色: " + code);
                }
            }
        }

        String pwd = (dto.getPassword() != null && !dto.getPassword().isEmpty())
                ? dto.getPassword() : defaultPassword;

        // 1. 插入 account
        AuthUserAccount account = new AuthUserAccount();
        account.setTenantId(tenantId);
        account.setUsername(dto.getUsername());
        account.setPasswordHash(passwordEncoder.encode(pwd));
        account.setPhone(dto.getPhone());
        account.setEmail(dto.getEmail());
        account.setStatus("active");
        account.setMustChangePassword(true);
        account.setAvatarFileId(dto.getAvatarFileId());
        userAccountMapper.insert(account);

        // 2. 插入 profile
        AuthPersonProfile profile = new AuthPersonProfile();
        profile.setTenantId(tenantId);
        profile.setUserId(account.getId());
        profile.setRealName(dto.getRealName());
        profile.setRoleType("staff");
        profile.setNoWork(dto.getStaffNo());
        profile.setTeachingOrgId(dto.getTeachingOrgId());
        profile.setServiceOrgId(dto.getServiceOrgId());
        profile.setAvatarFileId(dto.getAvatarFileId());
        personProfileMapper.insert(profile);

        // 3. 绑定角色
        for (String code : roleCodes) {
            AuthRole role = roleMapper.selectRoleByCodeAndTenant(code, tenantId);
            if (role == null) {
                throw new BusinessException(500, "角色不存在: " + code);
            }
            AuthUserRole userRole = new AuthUserRole();
            userRole.setTenantId(tenantId);
            userRole.setUserId(account.getId());
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        }

        return toStaffVO(account, profile);
    }

    @Override
    @Transactional
    public StaffUserVO update(Long id, StaffUserUpdateDTO dto) {
        Long tenantId = requireTenantId();
        AuthUserAccount account = getAccountInTenant(id, tenantId);
        AuthPersonProfile profile = getProfileByUserId(id);
        validateIsStaff(profile);

        if (dto.getPhone() != null && !dto.getPhone().isEmpty()
                && userAccountMapper.existsByPhone(dto.getPhone(), id) > 0) {
            throw new BusinessException(409, "手机号已被使用");
        }
        if (dto.getTeachingOrgId() != null
                && userAccountMapper.countTeachingOrg(dto.getTeachingOrgId(), tenantId) == 0) {
            throw new BusinessException(400, "教学组织不存在或不属于当前租户");
        }
        if (dto.getServiceOrgId() != null
                && userAccountMapper.countServiceOrg(dto.getServiceOrgId(), tenantId) == 0) {
            throw new BusinessException(400, "服务组织不存在或不属于当前租户");
        }

        account.setPhone(dto.getPhone());
        account.setEmail(dto.getEmail());
        account.setAvatarFileId(dto.getAvatarFileId());
        userAccountMapper.updateById(account);

        profile.setRealName(dto.getRealName());
        profile.setNoWork(dto.getStaffNo());
        profile.setTeachingOrgId(dto.getTeachingOrgId());
        profile.setServiceOrgId(dto.getServiceOrgId());
        profile.setAvatarFileId(dto.getAvatarFileId());
        personProfileMapper.updateById(profile);

        return toStaffVO(account, profile);
    }

    @Override
    public void changeStatus(Long id, ChangeUserStatusDTO dto) {
        Long tenantId = requireTenantId();
        Long currentUserId = SecurityUtils.getUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException(400, "不能停用当前登录账号");
        }
        AuthUserAccount account = getAccountInTenant(id, tenantId);
        AuthPersonProfile profile = getProfileByUserId(id);
        validateIsStaff(profile);

        account.setStatus("enabled".equals(dto.getStatus()) ? "active" : "disabled");
        userAccountMapper.updateById(account);
    }

    @Override
    public void resetPassword(Long id, ResetPasswordDTO dto) {
        Long tenantId = requireTenantId();
        AuthUserAccount account = getAccountInTenant(id, tenantId);
        AuthPersonProfile profile = getProfileByUserId(id);
        validateIsStaff(profile);

        String pwd = (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty())
                ? dto.getNewPassword() : defaultPassword;
        account.setPasswordHash(passwordEncoder.encode(pwd));
        account.setMustChangePassword(true);
        userAccountMapper.updateById(account);
    }

    @Override
    @Transactional
    public void assignRoles(Long id, AssignRolesDTO dto) {
        Long tenantId = requireTenantId();
        AuthPersonProfile profile = getProfileByUserId(id);
        validateIsStaff(profile);
        getAccountInTenant(id, tenantId); // 验证归属

        // 校验角色范围
        Set<String> newRoles = new HashSet<>(dto.getRoleCodes());
        newRoles.add("staff"); // 必须保留 staff
        for (String code : dto.getRoleCodes()) {
            if (STAFF_FORBIDDEN_ROLES.contains(code)) {
                throw new BusinessException(400, "不允许分配角色: " + code);
            }
            if (!STAFF_ALLOWED_ROLES.contains(code)) {
                throw new BusinessException(400, "无效角色: " + code);
            }
        }

        // 删除旧角色绑定（物理删除，避免逻辑删除后唯一键冲突）
        userRoleMapper.physicalDeleteByUserId(id);

        // 重新绑定
        for (String code : newRoles) {
            AuthRole role = roleMapper.selectRoleByCodeAndTenant(code, tenantId);
            if (role == null) {
                throw new BusinessException(500, "角色不存在: " + code);
            }
            AuthUserRole userRole = new AuthUserRole();
            userRole.setTenantId(tenantId);
            userRole.setUserId(id);
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        }
    }

    // ==================== 私有方法 ====================

    private StaffUserVO toStaffVO(AuthUserAccount acc, AuthPersonProfile profile) {
        List<AuthRole> roles = roleMapper.selectRolesByUserId(acc.getId());
        return StaffUserVO.builder()
                .id(acc.getId()).username(acc.getUsername())
                .realName(profile != null ? profile.getRealName() : null)
                .userType(profile != null ? profile.getRoleType() : null)
                .phone(acc.getPhone()).email(acc.getEmail())
                .status(acc.getStatus()).tenantId(acc.getTenantId())
                .schoolId(userAccountMapper.selectSchoolIdByTenantId(acc.getTenantId()))
                .avatarFileId(acc.getAvatarFileId())
                .roleCodes(roles.stream().map(AuthRole::getRoleCode).collect(Collectors.toList()))
                .roleNames(roles.stream().map(AuthRole::getRoleName).collect(Collectors.toList()))
                .staffNo(profile != null ? profile.getNoWork() : null)
                .teachingOrgId(profile != null ? profile.getTeachingOrgId() : null)
                .teachingOrgName(profile != null && profile.getTeachingOrgId() != null
                        ? userAccountMapper.selectTeachingOrgName(profile.getTeachingOrgId()) : null)
                .serviceOrgId(profile != null ? profile.getServiceOrgId() : null)
                .serviceOrgName(profile != null && profile.getServiceOrgId() != null
                        ? userAccountMapper.selectServiceOrgName(profile.getServiceOrgId()) : null)
                .createdAt(acc.getCreatedAt()).updatedAt(acc.getUpdatedAt())
                .build();
    }

    private AuthPersonProfile findProfile(List<AuthPersonProfile> profiles, Long userId) {
        return profiles.stream().filter(p -> p.getUserId().equals(userId)).findFirst().orElse(null);
    }

    private AuthUserAccount getAccountInTenant(Long id, Long tenantId) {
        AuthUserAccount account = userAccountMapper.selectById(id);
        if (account == null || !tenantId.equals(account.getTenantId())) {
            throw new BusinessException(404, "用户不存在");
        }
        return account;
    }

    private AuthPersonProfile getProfileByUserId(Long userId) {
        AuthPersonProfile profile = personProfileMapper.selectOne(
                new LambdaQueryWrapper<AuthPersonProfile>().eq(AuthPersonProfile::getUserId, userId));
        if (profile == null) throw new BusinessException(404, "用户档案不存在");
        return profile;
    }

    private void validateIsStaff(AuthPersonProfile profile) {
        if (!"staff".equals(profile.getRoleType())) {
            throw new BusinessException(400, "该用户不是教职工");
        }
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }
}
