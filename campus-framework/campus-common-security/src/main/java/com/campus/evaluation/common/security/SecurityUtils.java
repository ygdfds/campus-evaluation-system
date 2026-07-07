package com.campus.evaluation.common.security;

import cn.dev33.satoken.stp.StpUtil;

import java.util.Collections;
import java.util.List;

/**
 * 安全工具类
 * <p>
 * 基于 Sa-Token 实现，获取当前登录用户信息
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /** Sa-Token Session 中存储 LoginUser 的 key */
    public static final String LOGIN_USER_KEY = "loginUser";

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return (LoginUser) StpUtil.getSession().get(LOGIN_USER_KEY);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户真实姓名
     */
    public static String getRealName() {
        LoginUser user = getLoginUser();
        return user != null ? user.getRealName() : null;
    }

    /**
     * 获取当前租户ID
     */
    public static Long getTenantId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getTenantId() : null;
    }

    /**
     * 获取当前学校ID
     */
    public static Long getSchoolId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getSchoolId() : null;
    }

    /**
     * 获取当前用户角色列表
     */
    public static List<String> getRoles() {
        LoginUser user = getLoginUser();
        return user != null ? user.getRoles() : Collections.emptyList();
    }

    /**
     * 获取当前用户权限列表
     */
    public static List<String> getPermissions() {
        LoginUser user = getLoginUser();
        return user != null ? user.getPermissions() : Collections.emptyList();
    }

    /**
     * 判断是否已登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 判断当前用户是否拥有指定角色
     */
    public static boolean hasRole(String roleCode) {
        return getRoles().contains(roleCode);
    }

    /**
     * 判断当前用户是否拥有指定权限
     */
    public static boolean hasPermission(String permissionCode) {
        return getPermissions().contains(permissionCode);
    }
}
