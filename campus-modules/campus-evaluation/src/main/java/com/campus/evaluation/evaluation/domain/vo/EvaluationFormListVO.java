package com.campus.evaluation.evaluation.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "评价表单列表项")
public class EvaluationFormListVO {

    @Schema(description = "表单 ID")
    private Long id;

    @Schema(description = "表单标题")
    private String title;

    @Schema(description = "表单类型")
    private String formType;

    @Schema(description = "表单类型标签")
    private String formTypeLabel;

    @Schema(description = "评价对象类型")
    private String targetType;

    @Schema(description = "评价对象类型标签")
    private String targetTypeLabel;

    @Schema(description = "评价对象名称")
    private String targetName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "创建人 ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "窗口开始时间")
    private LocalDateTime windowStartTime;

    @Schema(description = "窗口结束时间")
    private LocalDateTime windowEndTime;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "封面文件 ID")
    private Long coverFileId;

    @Schema(description = "封面 URL")
    private String coverUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
