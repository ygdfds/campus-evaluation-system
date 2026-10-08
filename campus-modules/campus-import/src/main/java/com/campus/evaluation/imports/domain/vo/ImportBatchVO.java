package com.campus.evaluation.imports.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImportBatchVO {
    private Long id;
    private String importType;
    private String fileName;
    private Integer totalCount;
    private Integer successCount;
    private Integer errorCount;
    private String status;
    private Long uploaderId;
    private String uploaderName;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
