package com.campus.evaluation.auth.service;

import com.campus.evaluation.auth.domain.dto.LoginRequest;
import com.campus.evaluation.auth.domain.vo.CurrentUserVO;
import com.campus.evaluation.auth.domain.vo.LoginResponse;
import com.campus.evaluation.auth.domain.vo.PermissionVO;
import jakarta.servlet.http.HttpServletRequest;

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
}
