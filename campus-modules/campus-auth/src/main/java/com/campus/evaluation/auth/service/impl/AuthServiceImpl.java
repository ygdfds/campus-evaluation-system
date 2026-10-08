package com.campus.evaluation.auth.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.auth.domain.dto.ChangePasswordDTO;
import com.campus.evaluation.auth.domain.dto.LoginRequest;
import com.campus.evaluation.auth.domain.entity.AuthLoginLog;
import com.campus.evaluation.auth.domain.entity.AuthPermission;
import com.campus.evaluation.auth.domain.entity.AuthPersonProfile;
import com.campus.evaluation.auth.domain.entity.AuthRole;
import com.campus.evaluation.auth.domain.entity.AuthUserAccount;
import com.campus.evaluation.auth.domain.vo.CurrentUserVO;
import com.campus.evaluation.auth.domain.vo.LoginResponse;
import com.campus.evaluation.auth.domain.vo.PermissionVO;
import com.campus.evaluation.auth.mapper.AuthLoginLogMapper;
import com.campus.evaluation.auth.mapper.AuthPermissionMapper;
import com.campus.evaluation.auth.mapper.AuthPersonProfileMapper;
import com.campus.evaluation.auth.mapper.AuthRoleMapper;
import com.campus.evaluation.auth.mapper.AuthUserAccountMapper;
import com.campus.evaluation.auth.service.AuthService;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.LoginUser;
import com.campus.evaluation.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_SYSTEM_ADMIN = "system_admin";
    private static final String ROLE_SCHOOL_ADMIN = "school_admin";
    private static final String ROLE_STAFF = "staff";
    private static final String ROLE_STUDENT = "student";

    private final AuthUserAccountMapper userAccountMapper;
    private final AuthRoleMapper roleMapper;
    private final AuthPermissionMapper permissionMapper;
    private final AuthPersonProfileMapper personProfileMapper;
    private final AuthLoginLogMapper loginLogMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String password = request.getPassword();
        String ip = getClientIp(httpRequest);
        String device = httpRequest.getHeader("User-Agent");

        AuthUserAccount account = userAccountMapper.selectOne(
                new LambdaQueryWrapper<AuthUserAccount>()
                        .eq(AuthUserAccount::getUsername, username)
                        .eq(AuthUserAccount::getDeleted, 0)
        );

        if (account == null) {
            saveLoginLog(null, null, ip, device, "fail");
            throw invalidCredentials();
        }
        if (!"active".equals(account.getStatus())) {
            saveLoginLog(account.getId(), account.getTenantId(), ip, device, "fail");
            throw new BusinessException(422, "Account is disabled or locked", "USER_DISABLED");
        }
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            saveLoginLog(account.getId(), account.getTenantId(), ip, device, "fail");
            throw invalidCredentials();
        }

        List<AuthRole> roles = roleMapper.selectRolesByUserId(account.getId());
        List<AuthPermission> permissions = permissionMapper.selectPermissionsByUserId(account.getId());
        List<String> roleCodes = roles.stream().map(AuthRole::getRoleCode).collect(Collectors.toList());
        List<String> permissionCodes = permissions.stream().map(AuthPermission::getPermissionCode).collect(Collectors.toList());

        AuthPersonProfile profile = personProfileMapper.selectOne(
                new LambdaQueryWrapper<AuthPersonProfile>()
                        .eq(AuthPersonProfile::getUserId, account.getId())
                        .eq(AuthPersonProfile::getDeleted, 0)
        );
        String roleType = resolveRoleType(profile, roleCodes);
        Scope scope = resolveScope(account, roleType);

        LoginUser loginUser = buildLoginUser(account, profile, roleType, scope, roleCodes, permissionCodes);
        SaLoginModel loginModel = new SaLoginModel()
                .setIsLastingCookie(Boolean.TRUE.equals(request.getRememberMe()));
        StpUtil.login(account.getId(), loginModel);
        loginUser.setExpiresIn(StpUtil.getTokenTimeout());
        StpUtil.getSession().set(SecurityUtils.LOGIN_USER_KEY, loginUser);

        account.setLastLoginAt(LocalDateTime.now());
        userAccountMapper.updateById(account);
        saveLoginLog(account.getId(), account.getTenantId(), ip, device, "success");

        log.info("User login success: username={}, userId={}, tenantId={}, roleType={}",
                username, account.getId(), scope.tenantId(), roleType);

        return LoginResponse.builder()
                .tokenName(StpUtil.getTokenName())
                .token(StpUtil.getTokenValue())
                .expiresIn(loginUser.getExpiresIn())
                .userId(account.getId())
                .username(account.getUsername())
                .realName(loginUser.getRealName())
                .userType(roleType)
                .roleType(roleType)
                .tenantId(scope.tenantId())
                .schoolId(scope.schoolId())
                .roles(roleCodes)
                .permissions(permissionCodes)
                .avatarUrl(loginUser.getAvatarUrl())
                .build();
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            String username = SecurityUtils.getUsername();
            StpUtil.logout();
            log.info("User logout: username={}", username);
        }
    }

    @Override
    public CurrentUserVO getCurrentUser() {
        LoginUser loginUser = requireLoginUser();
        AuthUserAccount account = userAccountMapper.selectById(loginUser.getUserId());
        return CurrentUserVO.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .realName(loginUser.getRealName())
                .userType(loginUser.getUserType())
                .roleType(loginUser.getRoleType() != null ? loginUser.getRoleType() : loginUser.getUserType())
                .tenantId(loginUser.getTenantId())
                .schoolId(loginUser.getSchoolId())
                .avatarUrl(loginUser.getAvatarUrl())
                .avatarFileId(account != null ? account.getAvatarFileId() : null)
                .phone(account != null ? account.getPhone() : null)
                .email(account != null ? account.getEmail() : null)
                .status(account != null ? account.getStatus() : null)
                .createdAt(account != null ? account.getCreatedAt() : null)
                .updatedAt(account != null ? account.getUpdatedAt() : null)
                .lastLoginAt(account != null ? account.getLastLoginAt() : null)
                .roles(loginUser.getRoles())
                .permissions(loginUser.getPermissions())
                .mustChangePassword(loginUser.getMustChangePassword())
                .build();
    }

    @Override
    public PermissionVO getPermissions() {
        LoginUser loginUser = requireLoginUser();
        return PermissionVO.builder()
                .roles(loginUser.getRoles())
                .permissions(loginUser.getPermissions())
                .build();
    }

    @Override
    public void changePassword(ChangePasswordDTO request) {
        LoginUser loginUser = requireLoginUser();
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new BusinessException(422, "Password confirmation does not match", "PASSWORD_CONFIRM_MISMATCH");
        }
        if (Objects.equals(request.getOldPassword(), request.getNewPassword())) {
            throw new BusinessException(422, "New password must be different from the old password", "PASSWORD_REUSED");
        }

        AuthUserAccount account = userAccountMapper.selectById(loginUser.getUserId());
        if (account == null || Objects.equals(account.getDeleted(), 1)) {
            throw new BusinessException(401, "Login expired", "UNAUTHORIZED");
        }
        if (!"active".equals(account.getStatus())) {
            StpUtil.logout();
            throw new BusinessException(422, "Account is disabled or locked", "USER_DISABLED");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new BusinessException(400, "Old password is incorrect", "OLD_PASSWORD_INVALID");
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        account.setMustChangePassword(false);
        userAccountMapper.updateById(account);

        loginUser.setMustChangePassword(false);
        StpUtil.getSession().set(SecurityUtils.LOGIN_USER_KEY, loginUser);
    }

    @Override
    public Map<String, Object> updateCurrentUser(Map<String, Object> payload) {
        LoginUser loginUser = requireLoginUser();
        AuthUserAccount account = userAccountMapper.selectById(loginUser.getUserId());
        AuthPersonProfile profile = personProfileMapper.selectOne(new LambdaQueryWrapper<AuthPersonProfile>()
                .eq(AuthPersonProfile::getUserId, loginUser.getUserId())
                .eq(AuthPersonProfile::getTenantId, loginUser.getTenantId()));
        if (account == null || profile == null) {
            throw new BusinessException(404, "User profile not found", "USER_PROFILE_NOT_FOUND");
        }
        if (payload.containsKey("phone")) {
            String phone = payload.get("phone") == null ? null : String.valueOf(payload.get("phone")).trim();
            if (phone != null && !phone.isBlank() && userAccountMapper.existsByPhone(phone, account.getId()) > 0) {
                throw new BusinessException(409, "Phone number is already in use", "PHONE_DUPLICATE");
            }
            account.setPhone(phone);
        }
        if (payload.containsKey("email")) {
            account.setEmail(payload.get("email") == null ? null : String.valueOf(payload.get("email")).trim());
        }
        if (payload.containsKey("avatarFileId")) {
            account.setAvatarFileId(asLong(payload.get("avatarFileId")));
            profile.setAvatarFileId(account.getAvatarFileId());
        }
        if (payload.containsKey("officePhone")) {
            profile.setOfficePhone(asString(payload.get("officePhone")));
        }
        if (payload.containsKey("intro")) {
            profile.setIntro(asString(payload.get("intro")));
        }
        account.setUpdatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.updateById(account);
        personProfileMapper.updateById(profile);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", account.getId());
        result.put("username", account.getUsername());
        result.put("phone", account.getPhone());
        result.put("email", account.getEmail());
        result.put("avatarFileId", account.getAvatarFileId());
        result.put("officePhone", profile.getOfficePhone());
        result.put("intro", profile.getIntro());
        return result;
    }

    @Override
    public List<Map<String, Object>> getLoginLogs() {
        LoginUser loginUser = requireLoginUser();
        return loginLogMapper.selectList(new LambdaQueryWrapper<AuthLoginLog>()
                        .eq(AuthLoginLog::getTenantId, loginUser.getTenantId())
                        .eq(AuthLoginLog::getUserId, loginUser.getUserId())
                        .orderByDesc(AuthLoginLog::getCreatedAt)
                        .last("LIMIT 20"))
                .stream()
                .map(log -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", log.getId());
                    item.put("user_id", log.getUserId());
                    item.put("tenant_id", log.getTenantId());
                    item.put("ip", log.getIp());
                    item.put("device", log.getDevice());
                    item.put("location", log.getLocation());
                    item.put("result", log.getResult());
                    item.put("created_at", log.getCreatedAt());
                    return item;
                })
                .toList();
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long asLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            throw new BusinessException(400, "Invalid numeric value");
        }
    }

    private LoginUser buildLoginUser(
            AuthUserAccount account,
            AuthPersonProfile profile,
            String roleType,
            Scope scope,
            List<String> roleCodes,
            List<String> permissionCodes
    ) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(account.getId());
        loginUser.setUsername(account.getUsername());
        loginUser.setRealName(profile != null ? profile.getRealName() : null);
        loginUser.setUserType(roleType);
        loginUser.setRoleType(roleType);
        loginUser.setTenantId(scope.tenantId());
        loginUser.setSchoolId(scope.schoolId());
        loginUser.setAvatarUrl(null);
        loginUser.setRoles(roleCodes);
        loginUser.setPermissions(permissionCodes);
        loginUser.setMustChangePassword(account.getMustChangePassword());
        return loginUser;
    }

    private String resolveRoleType(AuthPersonProfile profile, List<String> roleCodes) {
        if (profile != null && StringUtils.hasText(profile.getRoleType())) {
            return profile.getRoleType();
        }
        if (roleCodes.contains(ROLE_SYSTEM_ADMIN)) return ROLE_SYSTEM_ADMIN;
        if (roleCodes.contains(ROLE_SCHOOL_ADMIN)) return ROLE_SCHOOL_ADMIN;
        if (roleCodes.contains(ROLE_STAFF)) return ROLE_STAFF;
        if (roleCodes.contains(ROLE_STUDENT)) return ROLE_STUDENT;
        throw new BusinessException(403, "Account has no supported role", "ACCOUNT_ROLE_INVALID");
    }

    private Scope resolveScope(AuthUserAccount account, String roleType) {
        Long tenantId = account.getTenantId();
        if (ROLE_SYSTEM_ADMIN.equals(roleType)) {
            return new Scope(tenantId, tenantId != null ? userAccountMapper.selectSchoolIdByTenantId(tenantId) : null);
        }
        if (tenantId == null) {
            throw new BusinessException(403, "Account is not bound to a tenant", "ACCOUNT_SCOPE_INVALID");
        }
        if (userAccountMapper.countActiveTenant(tenantId) == 0) {
            throw new BusinessException(403, "Tenant is disabled or unavailable", "TENANT_DISABLED");
        }
        Long schoolId = userAccountMapper.selectSchoolIdByTenantId(tenantId);
        if (schoolId == null) {
            throw new BusinessException(403, "Account is not bound to a school profile", "ACCOUNT_SCOPE_INVALID");
        }
        return new Scope(tenantId, schoolId);
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(401, "Login required or expired", "UNAUTHORIZED");
        }
        return loginUser;
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(400, "Invalid username or password", "INVALID_CREDENTIALS");
    }

    private void saveLoginLog(Long userId, Long tenantId, String ip, String device, String result) {
        try {
            AuthLoginLog loginLog = new AuthLoginLog();
            loginLog.setUserId(userId != null ? userId : 0L);
            loginLog.setTenantId(tenantId);
            loginLog.setIp(ip);
            loginLog.setDevice(device != null && device.length() > 250 ? device.substring(0, 250) : device);
            loginLog.setResult(result);
            loginLog.setCreatedAt(LocalDateTime.now());
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("Failed to write login log", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private record Scope(Long tenantId, Long schoolId) {
    }
}
