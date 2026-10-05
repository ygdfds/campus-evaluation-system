package com.campus.evaluation.imports.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("imp_import_batch")
public class ImportBatch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private String importType;
    private String fileName;
    private Long fileId;
    private Integer totalCount;
    private Integer successCount;
    private Integer errorCount;
    private String status;
    private Long uploaderId;
    private String uploaderName;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
