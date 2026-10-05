package com.campus.evaluation.school.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseEnrollmentVO {
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private Long courseId;
    private Long studentId;
    private Long classGroupId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
