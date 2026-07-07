package com.campus.evaluation.common.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 登录用户信息
 * <p>
 * 登录成功后写入 Sa-Token Session，作为全局用户上下文
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 用户类型（system_admin/school_admin/staff/student） */
    private String userType;

    /** 租户ID */
    private Long tenantId;

    /** 学校ID */
    private Long schoolId;

    /** 头像URL */
    private String avatarUrl;

    /** 角色码列表 */
    private List<String> roles;

    /** 权限码列表 */
    private List<String> permissions;

    /** Token 有效期（秒） */
    private Long expiresIn;
}
