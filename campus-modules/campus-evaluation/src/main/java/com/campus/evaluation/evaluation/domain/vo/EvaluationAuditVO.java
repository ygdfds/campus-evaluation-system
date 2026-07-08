package com.campus.evaluation.evaluation.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "审核记录 VO")
public class EvaluationAuditVO {

    @Schema(description = "审核 ID")
    private Long id;

    @Schema(description = "表单 ID")
    private Long formId;

    @Schema(description = "表单标题")
    private String formTitle;

    @Schema(description = "审核状态")
    private String status;

    @Schema(description = "审核状态标签")
    private String statusLabel;

    @Schema(description = "提交人 ID")
    private Long submitterId;

    @Schema(description = "提交人姓名")
    private String submitterName;

    @Schema(description = "提交人角色")
    private String submitterRole;

    @Schema(description = "提交时间")
    private LocalDateTime requestedAt;

    @Schema(description = "提交原因")
    private String submitReason;

    @Schema(description = "审核人 ID")
    private Long reviewerId;

    @Schema(description = "审核人姓名")
    private String reviewerName;

    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;

    @Schema(description = "审核意见/驳回原因")
    private String reviewComment;

    @Schema(description = "表单类型")
    private String formType;

    @Schema(description = "表单类型标签")
    private String formTypeLabel;
}
