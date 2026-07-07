package com.campus.evaluation.common.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 创建者 */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /** 更新者 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /** 逻辑删除标记（0=未删除, 1=已删除） */
    @TableLogic
    private Integer deleted;
}
