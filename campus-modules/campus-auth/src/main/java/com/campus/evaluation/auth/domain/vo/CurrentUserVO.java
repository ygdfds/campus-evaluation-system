package com.campus.evaluation.auth.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

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

    private String roleType;

    private Long tenantId;

    private Long schoolId;

    private String avatarUrl;

    private Long avatarFileId;

    private String phone;

    private String email;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    private List<String> roles;

    private List<String> permissions;

    private Boolean mustChangePassword;
}
