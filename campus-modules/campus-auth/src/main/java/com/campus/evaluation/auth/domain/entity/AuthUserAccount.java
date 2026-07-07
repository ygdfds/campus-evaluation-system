package com.campus.evaluation.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户账号实体
 */
@Data
@TableName("auth_user_account")
public class AuthUserAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String username;

    private String passwordHash;

    private String phone;

    private String email;

    /** active/disabled/locked */
    private String status;

    private Boolean mustChangePassword;

    private LocalDateTime lastLoginAt;

    private Long avatarFileId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
