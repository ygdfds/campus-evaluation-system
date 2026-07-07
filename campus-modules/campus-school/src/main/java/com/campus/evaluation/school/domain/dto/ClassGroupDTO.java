package com.campus.evaluation.school.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "班级新增/编辑请求")
@Data
public class ClassGroupDTO {

    @NotNull(message = "教学组织ID不能为空")
    @Schema(description = "所属教学组织ID")
    private Long teachingOrgId;

    @Schema(description = "年级")
    private String gradeName;

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 120, message = "班级名称最多120字")
    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "状态，默认 active")
    private String status = "active";
}
