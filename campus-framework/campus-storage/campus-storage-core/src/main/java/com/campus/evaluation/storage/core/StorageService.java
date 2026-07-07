package com.campus.evaluation.storage.core;

import java.io.InputStream;

/**
 * 存储服务核心接口
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param path      存储路径（相对路径）
     * @param data      文件字节数据
     * @param contentType 文件类型
     * @return 上传结果
     */
    FileUploadResult upload(String path, byte[] data, String contentType);

    /**
     * 上传文件（流）
     *
     * @param path        存储路径
     * @param inputStream 输入流
     * @param contentType 文件类型
     * @return 上传结果
     */
    FileUploadResult upload(String path, InputStream inputStream, String contentType);

    /**
     * 下载文件
     *
     * @param path 存储路径
     * @return 文件字节数据
     */
    byte[] download(String path);

    /**
     * 删除文件
     *
     * @param path 存储路径
     */
    void delete(String path);

    /**
     * 获取文件访问 URL
     *
     * @param path 存储路径
     * @return 访问 URL
     */
    String getUrl(String path);
}
