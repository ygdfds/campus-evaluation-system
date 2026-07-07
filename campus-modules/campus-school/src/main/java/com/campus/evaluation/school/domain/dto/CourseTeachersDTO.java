package com.campus.evaluation.school.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "课程教师维护请求")
@Data
public class CourseTeachersDTO {

    @NotEmpty(message = "教师ID列表不能为空")
    @Schema(description = "教师用户ID列表")
    private List<Long> teacherIds;
}
