package com.campus.evaluation.evaluation.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表单发布审核记录实体
 */
@Data
@TableName("eval_form_publish_audit")
public class EvaluationFormPublishAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long schoolId;

    private Long formId;

    /** 操作类型：publish */
    private String action;

    /** 状态：pending / approved / rejected */
    private String status;

    /** 提交人 */
    private Long requestedBy;

    private LocalDateTime requestedAt;

    /** 提交人角色 */
    private String submitterRole;

    /** 提交原因 */
    private String submitReason;

    /** 审核人 */
    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    /** 审核意见/驳回原因 */
    private String reviewComment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
