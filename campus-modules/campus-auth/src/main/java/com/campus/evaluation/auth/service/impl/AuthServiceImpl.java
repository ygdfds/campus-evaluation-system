package com.campus.evaluation.auth.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证授权服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

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

        // 1. 查询用户账号
        AuthUserAccount account = userAccountMapper.selectOne(
                new LambdaQueryWrapper<AuthUserAccount>()
                        .eq(AuthUserAccount::getUsername, username)
        );

        if (account == null) {
            // 用户不存在，记录失败日志（userId=0 表示未知用户）
            saveLoginLog(null, null, ip, device, "fail");
            throw new BusinessException(400, "账号或密码错误");
        }

        // 2. 校验账号状态
        if (!"active".equals(account.getStatus())) {
            saveLoginLog(account.getId(), account.getTenantId(), ip, device, "fail");
            throw new BusinessException(400, "账号或密码错误");
        }

        // 3. 校验租户状态（平台账号 tenantId 为 NULL 时跳过）
        Long tenantId = account.getTenantId();
        Long schoolId = null;
        if (tenantId != null) {
            int tenantCount = userAccountMapper.countActiveTenant(tenantId);
            if (tenantCount == 0) {
                saveLoginLog(account.getId(), tenantId, ip, device, "fail");
                throw new BusinessException(400, "账号或密码错误");
            }
            schoolId = userAccountMapper.selectSchoolIdByTenantId(tenantId);
        }

        // 4. BCrypt 密码校验
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            saveLoginLog(account.getId(), tenantId, ip, device, "fail");
            throw new BusinessException(400, "账号或密码错误");
        }

        // 5. 查询角色和权限
        List<AuthRole> roles = roleMapper.selectRolesByUserId(account.getId());
        List<AuthPermission> permissions = permissionMapper.selectPermissionsByUserId(account.getId());

        List<String> roleCodes = roles.stream()
                .map(AuthRole::getRoleCode)
                .collect(Collectors.toList());
        List<String> permissionCodes = permissions.stream()
                .map(AuthPermission::getPermissionCode)
                .collect(Collectors.toList());

        // 6. 查询人员档案获取 realName 和 userType
        String realName = null;
        String userType = null;
        AuthPersonProfile profile = personProfileMapper.selectOne(
                new LambdaQueryWrapper<AuthPersonProfile>()
                        .eq(AuthPersonProfile::getUserId, account.getId())
        );
        if (profile != null) {
            realName = profile.getRealName();
            userType = profile.getRoleType();
        }

        // 7. 构建 LoginUser 并存入 Sa-Token Session
        long expiresIn = StpUtil.getTokenTimeout();

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(account.getId());
        loginUser.setUsername(account.getUsername());
        loginUser.setRealName(realName);
        loginUser.setUserType(userType);
        loginUser.setTenantId(tenantId);
        loginUser.setSchoolId(schoolId);
        loginUser.setAvatarUrl(null); // 头像 URL 暂不查询 file_resource
        loginUser.setRoles(roleCodes);
        loginUser.setPermissions(permissionCodes);
        loginUser.setExpiresIn(expiresIn);
        loginUser.setMustChangePassword(account.getMustChangePassword());

        // 8. Sa-Token 登录
        SaLoginModel loginModel = new SaLoginModel()
                .setIsLastingCookie(request.getRememberMe() != null && request.getRememberMe());
        StpUtil.login(account.getId(), loginModel);
        StpUtil.getSession().set(SecurityUtils.LOGIN_USER_KEY, loginUser);

        // 获取实际 token 信息
        String token = StpUtil.getTokenValue();
        String tokenName = StpUtil.getTokenName();
        long actualExpiresIn = StpUtil.getTokenTimeout();
        loginUser.setExpiresIn(actualExpiresIn);

        // 9. 更新最后登录时间
        account.setLastLoginAt(LocalDateTime.now());
        userAccountMapper.updateById(account);

        // 10. 记录成功登录日志
        saveLoginLog(account.getId(), tenantId, ip, device, "success");

        log.info("用户登录成功: username={}, userId={}, tenantId={}", username, account.getId(), tenantId);

        return LoginResponse.builder()
                .tokenName(tokenName)
                .token(token)
                .expiresIn(actualExpiresIn)
                .userId(account.getId())
                .username(account.getUsername())
                .realName(realName)
                .userType(userType)
                .tenantId(tenantId)
                .schoolId(schoolId)
                .roles(roleCodes)
                .permissions(permissionCodes)
                .avatarUrl(null)
                .build();
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            String username = SecurityUtils.getUsername();
            StpUtil.logout();
            log.info("用户登出: username={}", username);
        }
    }

    @Override
    public CurrentUserVO getCurrentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        return CurrentUserVO.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .realName(loginUser.getRealName())
                .userType(loginUser.getUserType())
                .tenantId(loginUser.getTenantId())
                .schoolId(loginUser.getSchoolId())
                .avatarUrl(loginUser.getAvatarUrl())
                .roles(loginUser.getRoles())
                .permissions(loginUser.getPermissions())
                .mustChangePassword(loginUser.getMustChangePassword())
                .build();
    }

    @Override
    public PermissionVO getPermissions() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        return PermissionVO.builder()
                .roles(loginUser.getRoles())
                .permissions(loginUser.getPermissions())
                .build();
    }

    // ==================== 私有方法 ====================

    /**
     * 保存登录日志
     */
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
            log.error("写入登录日志失败", e);
        }
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
