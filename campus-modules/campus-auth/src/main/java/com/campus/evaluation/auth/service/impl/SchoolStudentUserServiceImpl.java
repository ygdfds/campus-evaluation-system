package com.campus.evaluation.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.auth.domain.dto.ChangeUserStatusDTO;
import com.campus.evaluation.auth.domain.dto.ResetPasswordDTO;
import com.campus.evaluation.auth.domain.dto.StudentUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.StudentUserUpdateDTO;
import com.campus.evaluation.auth.domain.entity.AuthPersonProfile;
import com.campus.evaluation.auth.domain.entity.AuthRole;
import com.campus.evaluation.auth.domain.entity.AuthUserAccount;
import com.campus.evaluation.auth.domain.entity.AuthUserRole;
import com.campus.evaluation.auth.domain.vo.StudentUserVO;
import com.campus.evaluation.auth.domain.vo.UserDetailVO;
import com.campus.evaluation.auth.mapper.AuthPersonProfileMapper;
import com.campus.evaluation.auth.mapper.AuthRoleMapper;
import com.campus.evaluation.auth.mapper.AuthUserAccountMapper;
import com.campus.evaluation.auth.mapper.AuthUserRoleMapper;
import com.campus.evaluation.auth.service.SchoolStudentUserService;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolStudentUserServiceImpl implements SchoolStudentUserService {

    private final AuthUserAccountMapper userAccountMapper;
    private final AuthPersonProfileMapper personProfileMapper;
    private final AuthRoleMapper roleMapper;
    private final AuthUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${campus.security.default-password:123456}")
    private String defaultPassword;

    @Override
    public PageResult<StudentUserVO> list(String keyword, String status, Long classId, String grade,
                                           int pageNum, int pageSize) {
        Long tenantId = requireTenantId();

        LambdaQueryWrapper<AuthPersonProfile> profileWrapper = new LambdaQueryWrapper<AuthPersonProfile>()
                .eq(AuthPersonProfile::getTenantId, tenantId)
                .eq(AuthPersonProfile::getRoleType, "student")
                .eq(classId != null, AuthPersonProfile::getClassId, classId)
                .like(keyword != null && !keyword.isEmpty(), AuthPersonProfile::getRealName, keyword);

        List<AuthPersonProfile> profiles = personProfileMapper.selectList(profileWrapper);
        if (profiles.isEmpty()) {
            return new PageResult<>(0, List.of(), pageNum, pageSize);
        }

        // 按年级过滤（需要查 class 表）
        if (grade != null && !grade.isEmpty()) {
            profiles = profiles.stream()
                    .filter(p -> {
                        if (p.getClassId() == null) return false;
                        String classGrade = userAccountMapper.selectClassGrade(p.getClassId());
                        return grade.equals(classGrade);
                    })
                    .collect(Collectors.toList());
            if (profiles.isEmpty()) {
                return new PageResult<>(0, List.of(), pageNum, pageSize);
            }
        }

        List<Long> userIds = profiles.stream().map(AuthPersonProfile::getUserId).collect(Collectors.toList());
        final List<AuthPersonProfile> finalProfiles = profiles;

        LambdaQueryWrapper<AuthUserAccount> wrapper = new LambdaQueryWrapper<AuthUserAccount>()
                .in(AuthUserAccount::getId, userIds)
                .eq(status != null && !status.isEmpty(), AuthUserAccount::getStatus,
                        "enabled".equals(status) ? "active" : status);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(AuthUserAccount::getUsername, keyword)
                    .or().like(AuthUserAccount::getPhone, keyword)
                    .or().like(AuthUserAccount::getEmail, keyword)
                    .or().in(AuthUserAccount::getId, userIds));
        }
        wrapper.orderByAsc(AuthUserAccount::getId);

        Page<AuthUserAccount> page = userAccountMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<StudentUserVO> records = page.getRecords().stream()
                .map(acc -> toStudentVO(acc, findProfile(finalProfiles, acc.getId())))
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public UserDetailVO getById(Long id) {
        Long tenantId = requireTenantId();
        AuthUserAccount account = getAccountInTenant(id, tenantId);
        AuthPersonProfile profile = getProfileByUserId(id);
        validateIsStudent(profile);

        List<AuthRole> roles = roleMapper.selectRolesByUserId(id);

        return UserDetailVO.builder()
                .id(account.getId()).username(account.getUsername()).realName(profile.getRealName())
                .userType(profile.getRoleType()).phone(account.getPhone()).email(account.getEmail())
                .status(account.getStatus()).tenantId(account.getTenantId())
                .schoolId(userAccountMapper.selectSchoolIdByTenantId(tenantId))
                .avatarFileId(account.getAvatarFileId())
                .roleCodes(roles.stream().map(AuthRole::getRoleCode).collect(Collectors.toList()))
                .roleNames(roles.stream().map(AuthRole::getRoleName).collect(Collectors.toList()))
                .studentNo(profile.getNoStudent())
                .classId(profile.getClassId())
                .className(profile.getClassId() != null
                        ? userAccountMapper.selectClassName(profile.getClassId()) : null)
                .grade(profile.getClassId() != null
                        ? userAccountMapper.selectClassGrade(profile.getClassId()) : null)
                .teachingOrgName(profile.getClassId() != null ? resolveClassTeachingOrgName(profile.getClassId()) : null)
                .mustChangePassword(account.getMustChangePassword())
                .lastLoginAt(account.getLastLoginAt())
                .createdAt(account.getCreatedAt()).updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public StudentUserVO create(StudentUserCreateDTO dto) {
        Long tenantId = requireTenantId();

        if (userAccountMapper.existsByUsername(dto.getUsername(), null) > 0) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()
                && userAccountMapper.existsByPhone(dto.getPhone(), null) > 0) {
            throw new BusinessException(409, "手机号已被使用");
        }
        if (userAccountMapper.countClassGroup(dto.getClassId(), tenantId) == 0) {
            throw new BusinessException(400, "班级不存在或不属于当前租户");
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
        String className = userAccountMapper.selectClassName(dto.getClassId());
        AuthPersonProfile profile = new AuthPersonProfile();
        profile.setTenantId(tenantId);
        profile.setUserId(account.getId());
        profile.setRealName(dto.getRealName());
        profile.setRoleType("student");
        profile.setNoStudent(dto.getStudentNo());
        profile.setClassId(dto.getClassId());
        profile.setClassName(className);
        profile.setAvatarFileId(dto.getAvatarFileId());
        personProfileMapper.insert(profile);

        // 3. 绑定 student 角色
        AuthRole studentRole = roleMapper.selectRoleByCodeAndTenant("student", tenantId);
        if (studentRole == null) {
            throw new BusinessException(500, "student 角色不存在");
        }
        AuthUserRole userRole = new AuthUserRole();
        userRole.setTenantId(tenantId);
        userRole.setUserId(account.getId());
        userRole.setRoleId(studentRole.getId());
        userRoleMapper.insert(userRole);

        return toStudentVO(account, profile);
    }

    @Override
    @Transactional
    public StudentUserVO update(Long id, StudentUserUpdateDTO dto) {
        Long tenantId = requireTenantId();
        AuthUserAccount account = getAccountInTenant(id, tenantId);
        AuthPersonProfile profile = getProfileByUserId(id);
        validateIsStudent(profile);

        if (dto.getPhone() != null && !dto.getPhone().isEmpty()
                && userAccountMapper.existsByPhone(dto.getPhone(), id) > 0) {
            throw new BusinessException(409, "手机号已被使用");
        }
        if (dto.getClassId() != null && userAccountMapper.countClassGroup(dto.getClassId(), tenantId) == 0) {
            throw new BusinessException(400, "班级不存在或不属于当前租户");
        }

        account.setPhone(dto.getPhone());
        account.setEmail(dto.getEmail());
        account.setAvatarFileId(dto.getAvatarFileId());
        userAccountMapper.updateById(account);

        profile.setRealName(dto.getRealName());
        profile.setNoStudent(dto.getStudentNo());
        profile.setAvatarFileId(dto.getAvatarFileId());
        if (dto.getClassId() != null) {
            profile.setClassId(dto.getClassId());
            profile.setClassName(userAccountMapper.selectClassName(dto.getClassId()));
        }
        personProfileMapper.updateById(profile);

        return toStudentVO(account, profile);
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
        validateIsStudent(profile);

        account.setStatus("enabled".equals(dto.getStatus()) ? "active" : "disabled");
        userAccountMapper.updateById(account);
    }

    @Override
    public void resetPassword(Long id, ResetPasswordDTO dto) {
        Long tenantId = requireTenantId();
        AuthUserAccount account = getAccountInTenant(id, tenantId);
        AuthPersonProfile profile = getProfileByUserId(id);
        validateIsStudent(profile);

        String pwd = (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty())
                ? dto.getNewPassword() : defaultPassword;
        account.setPasswordHash(passwordEncoder.encode(pwd));
        account.setMustChangePassword(true);
        userAccountMapper.updateById(account);
    }

    // ==================== 私有方法 ====================

    private String resolveClassTeachingOrgName(Long classId) {
        Long teachingOrgId = userAccountMapper.selectClassTeachingOrgId(classId);
        return teachingOrgId != null ? userAccountMapper.selectTeachingOrgName(teachingOrgId) : null;
    }

    private StudentUserVO toStudentVO(AuthUserAccount acc, AuthPersonProfile profile) {
        List<AuthRole> roles = roleMapper.selectRolesByUserId(acc.getId());
        Long classId = profile != null ? profile.getClassId() : null;
        return StudentUserVO.builder()
                .id(acc.getId()).username(acc.getUsername())
                .realName(profile != null ? profile.getRealName() : null)
                .userType(profile != null ? profile.getRoleType() : null)
                .phone(acc.getPhone()).email(acc.getEmail())
                .status(acc.getStatus()).tenantId(acc.getTenantId())
                .schoolId(userAccountMapper.selectSchoolIdByTenantId(acc.getTenantId()))
                .avatarFileId(acc.getAvatarFileId())
                .roleCodes(roles.stream().map(AuthRole::getRoleCode).collect(Collectors.toList()))
                .roleNames(roles.stream().map(AuthRole::getRoleName).collect(Collectors.toList()))
                .studentNo(profile != null ? profile.getNoStudent() : null)
                .classId(classId)
                .className(classId != null ? userAccountMapper.selectClassName(classId) : null)
                .grade(classId != null ? userAccountMapper.selectClassGrade(classId) : null)
                .teachingOrgName(classId != null ? resolveClassTeachingOrgName(classId) : null)
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

    private void validateIsStudent(AuthPersonProfile profile) {
        if (!"student".equals(profile.getRoleType())) {
            throw new BusinessException(400, "该用户不是学生");
        }
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }
}
