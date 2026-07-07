package com.campus.evaluation.school.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ServiceItemVO {
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private Long serviceOrgId;
    private String serviceOrgName;
    private String name;
    private Long coverFileId;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
