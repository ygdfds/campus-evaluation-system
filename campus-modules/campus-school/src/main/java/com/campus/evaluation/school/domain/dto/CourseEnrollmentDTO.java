package com.campus.evaluation.school.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "学生选课关系")
public class CourseEnrollmentDTO {
    @NotNull
    private Long courseId;
    @NotNull
    private Long studentId;
    private Long classGroupId;
    private String status = "active";
}
