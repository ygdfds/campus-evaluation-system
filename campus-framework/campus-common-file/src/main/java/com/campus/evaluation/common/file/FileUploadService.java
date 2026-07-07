package com.campus.evaluation.common.file;

import com.campus.evaluation.storage.core.FileUploadResult;
import com.campus.evaluation.storage.core.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传服务（封装业务层文件上传）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final StorageService storageService;

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param dir  存储子目录
     * @return 上传结果
     */
    public FileUploadResult upload(MultipartFile file, String dir) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storagePath = dir + "/" + UUID.randomUUID() + extension;
        try {
            FileUploadResult result = storageService.upload(storagePath, file.getInputStream(), file.getContentType());
            result.setOriginalFilename(originalFilename);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
}
