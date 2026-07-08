package com.campus.evaluation.evaluation.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评价题目实体
 */
@Data
@TableName("eval_question")
public class EvaluationQuestion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long schoolId;

    private Long formId;

    /** 题目类型：rating / single / multiple / text */
    private String type;

    private String title;

    private Boolean required;

    private BigDecimal maxScore;

    private Integer minLength;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
