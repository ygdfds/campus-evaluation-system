package com.campus.evaluation.auth.service;

import com.campus.evaluation.auth.domain.dto.ChangePasswordDTO;
import com.campus.evaluation.auth.domain.dto.LoginRequest;
import com.campus.evaluation.auth.domain.vo.CurrentUserVO;
import com.campus.evaluation.auth.domain.vo.LoginResponse;
import com.campus.evaluation.auth.domain.vo.PermissionVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * 认证授权服务接口
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * 登出
     */
    void logout();

    /**
     * 获取当前用户信息
     */
    CurrentUserVO getCurrentUser();

    /**
     * 获取当前用户权限信息
     */
    PermissionVO getPermissions();

    void changePassword(ChangePasswordDTO request);

    Map<String, Object> updateCurrentUser(Map<String, Object> payload);

    List<Map<String, Object>> getLoginLogs();
}
