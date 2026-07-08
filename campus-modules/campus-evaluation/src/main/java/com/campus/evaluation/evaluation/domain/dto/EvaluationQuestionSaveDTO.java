package com.campus.evaluation.evaluation.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "题目配置保存请求")
public class EvaluationQuestionSaveDTO {

    @NotEmpty(message = "至少需要 1 个题目")
    @Valid
    @Schema(description = "题目列表")
    private List<QuestionItem> questions;

    @Data
    @Schema(description = "题目项")
    public static class QuestionItem {

        @NotBlank(message = "题目标题不能为空")
        @Size(max = 500, message = "题目标题不能超过 500 字")
        @Schema(description = "题目标题")
        private String title;

        @NotBlank(message = "题目类型不能为空")
        @Schema(description = "题目类型（rating / single / multiple / text）")
        private String questionType;

        @Schema(description = "是否必填", defaultValue = "true")
        private Boolean required = true;

        @Schema(description = "排序序号")
        private Integer sortOrder;

        @Schema(description = "评分题最大分值（1-10）")
        private Integer maxScore;

        @Valid
        @Schema(description = "选项列表")
        private List<OptionItem> options;
    }

    @Data
    @Schema(description = "选项项")
    public static class OptionItem {

        @NotBlank(message = "选项文本不能为空")
        @Schema(description = "选项文本")
        private String label;

        @Schema(description = "排序序号")
        private Integer sortOrder;
    }
}
