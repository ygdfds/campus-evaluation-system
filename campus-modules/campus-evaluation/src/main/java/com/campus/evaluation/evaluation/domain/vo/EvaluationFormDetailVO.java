package com.campus.evaluation.evaluation.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "评价表单详情")
public class EvaluationFormDetailVO {

    @Schema(description = "表单 ID")
    private Long id;

    @Schema(description = "表单标题")
    private String title;

    @Schema(description = "表单描述")
    private String description;

    @Schema(description = "表单类型")
    private String formType;

    @Schema(description = "表单类型标签")
    private String formTypeLabel;

    @Schema(description = "评价对象类型")
    private String targetType;

    @Schema(description = "评价对象类型标签")
    private String targetTypeLabel;

    @Schema(description = "评价对象 ID")
    private Long targetId;

    @Schema(description = "评价对象名称")
    private String targetName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "是否匿名评价")
    private Boolean anonymousEnabled;

    @Schema(description = "是否启用评分")
    private Boolean scoreEnabled;

    @Schema(description = "创建人 ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "封面文件 ID")
    private Long coverFileId;

    @Schema(description = "封面 URL")
    private String coverUrl;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "题目列表")
    private List<EvaluationQuestionVO> questions;

    @Schema(description = "窗口信息")
    private EvaluationWindowVO window;

    @Schema(description = "审核记录")
    private List<EvaluationAuditVO> auditRecords;
}
