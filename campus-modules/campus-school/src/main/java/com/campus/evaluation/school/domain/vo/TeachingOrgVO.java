package com.campus.evaluation.school.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教学组织 VO
 */
@Data
@Builder
public class TeachingOrgVO {
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private Long parentId;
    private String name;
    private String code;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
