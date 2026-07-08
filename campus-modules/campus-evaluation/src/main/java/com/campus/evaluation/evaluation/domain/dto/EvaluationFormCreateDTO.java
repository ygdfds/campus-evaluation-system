package com.campus.evaluation.evaluation.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "评价表单创建请求")
public class EvaluationFormCreateDTO {

    @NotBlank(message = "表单标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度需在 2-100 字之间")
    @Schema(description = "表单标题")
    private String title;

    @Size(max = 2000, message = "描述不能超过 2000 字")
    @Schema(description = "表单描述")
    private String description;

    @NotBlank(message = "表单类型不能为空")
    @Schema(description = "表单类型（course_evaluation / service_evaluation）")
    private String formType;

    @NotBlank(message = "评价对象类型不能为空")
    @Schema(description = "评价对象类型（course / service_item）")
    private String targetType;

    @NotNull(message = "评价对象 ID 不能为空")
    @Schema(description = "评价对象 ID")
    private Long targetId;

    @Schema(description = "封面文件 ID")
    private Long coverFileId;

    @Schema(description = "是否匿名评价", defaultValue = "true")
    private Boolean anonymousEnabled = true;

    @Schema(description = "是否启用评分", defaultValue = "false")
    private Boolean scoreEnabled = false;
}
