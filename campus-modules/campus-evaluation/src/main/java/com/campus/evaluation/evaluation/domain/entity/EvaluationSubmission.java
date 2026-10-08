package com.campus.evaluation.evaluation.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("eval_submission")
public class EvaluationSubmission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long schoolId;

    private Long formId;

    private Long windowId;

    private Long evaluatorUserId;

    @TableField("evaluator_hash")
    private String evaluatorHash;

    private String targetType;

    private Long targetId;

    private BigDecimal overallScore;

    private Boolean anonymous;

    private LocalDateTime submittedAt;

    private LocalDateTime modifiableUntil;

    private LocalDateTime lockedAt;

    private String status;

    private String reviewStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
