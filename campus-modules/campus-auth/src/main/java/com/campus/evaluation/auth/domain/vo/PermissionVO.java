package com.campus.evaluation.auth.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 权限信息 VO（/auth/permissions 返回）
 */
@Data
@Builder
public class PermissionVO {

    private List<String> roles;

    private List<String> permissions;
}
