package com.campus.evaluation.file.domain.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 文件资源实体
 */
@Data
@TableName("file_resource")
public class FileResource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，平台文件可为空 */
    private Long tenantId;

    /** 学校ID */
    private Long schoolId;

    /** 对象存储 Key */
    private String objectKey;

    /** 文件名 */
    private String fileName;

    /** MIME 类型 */
    private String mimeType;

    /** 访问 URL */
    private String url;

    /** 上传人ID */
    private Long uploaderId;

    /** 文件大小（字节） */
    private Long size;

    /** 业务类型 */
    private String bizType;

    /** 业务ID */
    private Long bizId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
