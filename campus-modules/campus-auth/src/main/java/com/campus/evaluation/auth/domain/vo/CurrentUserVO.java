package com.campus.evaluation.auth.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 当前用户信息 VO（/auth/me 返回）
 */
@Data
@Builder
public class CurrentUserVO {

    private Long userId;

    private String username;

    private String realName;

    private String userType;

    private Long tenantId;

    private Long schoolId;

    private String avatarUrl;

    private List<String> roles;

    private List<String> permissions;
}
