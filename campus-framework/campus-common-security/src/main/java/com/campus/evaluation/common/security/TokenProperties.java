package com.campus.evaluation.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Token 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "campus.token")
public class TokenProperties {

    /** Token 前缀 */
    private String prefix = "Bearer";

    /** Token 有效期（秒） */
    private long expireSeconds = 86400;

    /** Token 请求头名称 */
    private String headerName = "Authorization";
}
