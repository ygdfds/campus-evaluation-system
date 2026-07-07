package com.campus.evaluation.school.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "课程新增/编辑请求")
@Data
public class CourseDTO {

    @NotNull(message = "教学组织ID不能为空")
    @Schema(description = "教学组织ID")
    private Long teachingOrgId;

    @NotBlank(message = "课程编码不能为空")
    @Size(max = 64, message = "课程编码最多64字")
    @Schema(description = "课程编码")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 160, message = "课程名称最多160字")
    @Schema(description = "课程名称")
    private String courseName;

    @NotBlank(message = "学期不能为空")
    @Schema(description = "学期")
    private String term;

    @Schema(description = "开始时间")
    private LocalDateTime startAt;

    @Schema(description = "结束时间")
    private LocalDateTime endAt;
}
