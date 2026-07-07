package com.campus.evaluation.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 */
@Data
@TableName("auth_login_log")
public class AuthLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long userId;

    private String ip;

    private String device;

    private String location;

    /** success/fail */
    private String result;

    private LocalDateTime createdAt;
}
