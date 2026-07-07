package com.campus.evaluation.auth.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应
 */
@Data
@Builder
public class LoginResponse {

    private String tokenName;

    private String token;

    private Long expiresIn;

    private Long userId;

    private String username;

    private String realName;

    private String userType;

    private Long tenantId;

    private Long schoolId;

    private List<String> roles;

    private List<String> permissions;

    private String avatarUrl;
}
