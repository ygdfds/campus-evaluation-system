package com.campus.evaluation.storage.minio;

import com.campus.evaluation.storage.core.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 存储配置（预留骨架，暂不实现完整功能）
 * TODO: 后续引入 MinIO SDK 后实现 MinioStorageService
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class MinioConfig {
    // 预留，后续实现
}
