package com.campus.evaluation.evaluation.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价窗口实体
 */
@Data
@TableName("eval_window")
public class EvaluationWindow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long schoolId;

    private Long formId;

    /** 窗口类型 */
    private String type;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer modifiableHours;

    /** 状态：scheduled / active / ended / closed */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
