package com.campus.evaluation.evaluation.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "评价题目 VO")
public class EvaluationQuestionVO {

    @Schema(description = "题目 ID")
    private Long id;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目类型")
    private String questionType;

    @Schema(description = "题目类型标签")
    private String questionTypeLabel;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "最大分值")
    private BigDecimal maxScore;

    @Schema(description = "选项列表")
    private List<OptionVO> options;

    @Data
    @Builder
    @Schema(description = "选项 VO")
    public static class OptionVO {
        @Schema(description = "选项 ID")
        private Long id;

        @Schema(description = "选项文本")
        private String label;

        @Schema(description = "排序序号")
        private Integer sortOrder;
    }
}
