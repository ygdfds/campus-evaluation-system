package com.campus.evaluation.imports.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImportRecordErrorVO {
    private Long id;
    private Long batchId;
    private Integer rowNumber;
    private String fieldName;
    private String errorReason;
    private String originalValue;
    private LocalDateTime createdAt;
}
