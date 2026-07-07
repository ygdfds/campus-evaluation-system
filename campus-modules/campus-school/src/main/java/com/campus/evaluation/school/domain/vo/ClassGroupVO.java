package com.campus.evaluation.school.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClassGroupVO {
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private Long teachingOrgId;
    private String teachingOrgName;
    private String gradeName;
    private String className;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
