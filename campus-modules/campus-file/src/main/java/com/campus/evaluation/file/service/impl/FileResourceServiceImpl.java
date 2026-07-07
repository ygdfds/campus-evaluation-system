package com.campus.evaluation.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.file.FileUploadService;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.file.domain.entity.FileResource;
import com.campus.evaluation.file.domain.vo.FileResourceVO;
import com.campus.evaluation.file.mapper.FileResourceMapper;
import com.campus.evaluation.file.service.FileResourceService;
import com.campus.evaluation.storage.core.FileUploadResult;
import com.campus.evaluation.storage.core.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件资源服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileResourceServiceImpl implements FileResourceService {

    private final FileResourceMapper fileResourceMapper;
    private final FileUploadService fileUploadService;
    private final StorageService storageService;

    /** 允许上传的文件类型白名单 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "webp", "gif",
            "pdf", "doc", "docx", "xls", "xlsx"
    );

    @Override
    public FileResourceVO upload(MultipartFile file, String bizType) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "不支持的文件类型: " + extension);
        }

        // 调用存储服务
        String dir = "files/" + (bizType != null ? bizType : "general");
        FileUploadResult uploadResult = fileUploadService.upload(file, dir);

        // 写入数据库
        FileResource entity = new FileResource();
        entity.setTenantId(SecurityUtils.getTenantId());
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setObjectKey(uploadResult.getPath());
        entity.setFileName(originalFilename);
        entity.setMimeType(file.getContentType());
        entity.setUrl(uploadResult.getUrl());
        entity.setUploaderId(SecurityUtils.getUserId());
        entity.setSize(file.getSize());
        entity.setBizType(bizType);
        fileResourceMapper.insert(entity);

        return toVO(entity);
    }

    @Override
    public FileResourceVO getById(Long id) {
        FileResource entity = getEntityById(id);
        checkAccess(entity);
        return toVO(entity);
    }

    @Override
    public byte[] download(Long id) {
        FileResource entity = getEntityById(id);
        checkAccess(entity);
        return storageService.download(entity.getObjectKey());
    }

    @Override
    public byte[] preview(Long id) {
        FileResource entity = getEntityById(id);
        // 预览不做租户校验（公开访问），后续可根据 bizType 区分
        return storageService.download(entity.getObjectKey());
    }

    @Override
    public void delete(Long id) {
        FileResource entity = getEntityById(id);
        checkAccess(entity);
        // 仅软删除数据库记录，不删除物理文件
        fileResourceMapper.deleteById(id);
    }

    private FileResource getEntityById(Long id) {
        FileResource entity = fileResourceMapper.selectOne(
                new LambdaQueryWrapper<FileResource>()
                        .eq(FileResource::getId, id)
        );
        if (entity == null) {
            throw new BusinessException(404, "文件不存在");
        }
        return entity;
    }

    /**
     * 校验当前用户是否有权访问文件
     */
    private void checkAccess(FileResource entity) {
        Long tenantId = SecurityUtils.getTenantId();
        if (entity.getTenantId() != null && tenantId != null
                && !entity.getTenantId().equals(tenantId)) {
            throw new BusinessException(403, "无权访问该文件");
        }
    }

    private FileResourceVO toVO(FileResource e) {
        return FileResourceVO.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .schoolId(e.getSchoolId())
                .fileName(e.getFileName())
                .mimeType(e.getMimeType())
                .url(e.getUrl())
                .size(e.getSize())
                .bizType(e.getBizType())
                .bizId(e.getBizId())
                .uploaderId(e.getUploaderId())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
