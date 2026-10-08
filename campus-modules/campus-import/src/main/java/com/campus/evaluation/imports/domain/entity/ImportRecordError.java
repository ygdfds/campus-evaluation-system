package com.campus.evaluation.imports.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("imp_import_record_error")
public class ImportRecordError {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long batchId;
    private Integer rowNumber;
    private String fieldName;
    private String errorReason;
    private String originalValue;
    private LocalDateTime createdAt;
}
