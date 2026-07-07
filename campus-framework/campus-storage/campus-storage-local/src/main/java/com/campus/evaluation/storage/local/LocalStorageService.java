package com.campus.evaluation.storage.local;

import com.campus.evaluation.storage.core.FileUploadResult;
import com.campus.evaluation.storage.core.StorageProperties;
import com.campus.evaluation.storage.core.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储实现
 */
@Slf4j
@Service
@EnableConfigurationProperties(StorageProperties.class)
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;

    public LocalStorageService(StorageProperties properties) {
        this.properties = properties;
        // 确保存储目录存在
        try {
            Files.createDirectories(Paths.get(properties.getLocalPath()));
        } catch (IOException e) {
            log.error("创建本地存储目录失败: {}", e.getMessage());
        }
    }

    @Override
    public FileUploadResult upload(String path, byte[] data, String contentType) {
        try {
            Path fullPath = resolvePath(path);
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, data);
            return buildResult(path, data.length, contentType);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + path, e);
        }
    }

    @Override
    public FileUploadResult upload(String path, InputStream inputStream, String contentType) {
        try {
            Path fullPath = resolvePath(path);
            Files.createDirectories(fullPath.getParent());
            long size = Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);
            return buildResult(path, size, contentType);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + path, e);
        }
    }

    @Override
    public byte[] download(String path) {
        try {
            return Files.readAllBytes(resolvePath(path));
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Files.deleteIfExists(resolvePath(path));
        } catch (IOException e) {
            throw new RuntimeException("文件删除失败: " + path, e);
        }
    }

    @Override
    public String getUrl(String path) {
        return properties.getUrlPrefix() + "/" + path;
    }

    private Path resolvePath(String path) {
        return Paths.get(properties.getLocalPath()).resolve(path);
    }

    private FileUploadResult buildResult(String path, long size, String contentType) {
        FileUploadResult result = new FileUploadResult();
        result.setPath(path);
        result.setUrl(getUrl(path));
        result.setSize(size);
        result.setContentType(contentType);
        return result;
    }
}
