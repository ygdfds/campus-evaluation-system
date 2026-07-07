package com.campus.evaluation.storage.core;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 文件上传结果
 */
@Data
public class FileUploadResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件访问 URL */
    private String url;

    /** 存储路径（相对路径或 key） */
    private String path;

    /** 原始文件名 */
    private String originalFilename;

    /** 文件大小（字节） */
    private long size;

    /** 文件 MIME 类型 */
    private String contentType;
}
