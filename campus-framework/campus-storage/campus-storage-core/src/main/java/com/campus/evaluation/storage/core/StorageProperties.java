package com.campus.evaluation.storage.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 存储配置属性
 */
@Data
@ConfigurationProperties(prefix = "campus.storage")
public class StorageProperties {

    /** 存储类型：local / minio */
    private String type = "local";

    /** 本地存储根目录 */
    private String localPath = "./upload/campus-evaluation";

    /** 访问 URL 前缀 */
    private String urlPrefix = "/upload";

    /** MinIO 端点（预留） */
    private String minioEndpoint;

    /** MinIO 访问密钥（预留） */
    private String minioAccessKey;

    /** MinIO 密钥（预留） */
    private String minioSecretKey;

    /** MinIO 桶名（预留） */
    private String minioBucket = "campus-evaluation";
}
