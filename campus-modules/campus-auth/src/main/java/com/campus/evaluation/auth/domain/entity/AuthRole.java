package com.campus.evaluation.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("auth_role")
public class AuthRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String roleCode;

    private String roleName;

    /** platform/tenant/school/org */
    private String scopeType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
