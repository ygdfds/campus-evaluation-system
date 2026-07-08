package com.campus.evaluation.evaluation.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价表单实体
 */
@Data
@TableName("eval_form")
public class EvaluationForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long schoolId;

    /** 表单类型（course_evaluation / service_evaluation） */
    private String type;

    private String title;

    private String description;

    private Long coverFileId;

    /** 发布者/创建者 */
    private Long publisherId;

    /** 发布范围 */
    private String publishScope;

    /** 匿名评价 */
    private Boolean anonymous;

    /** 评分功能 */
    private Boolean scoreEnabled;

    private Long teachingOrgId;

    private Long serviceOrgId;

    private Long courseId;

    private Long serviceItemId;

    /** 状态：draft / pending / published / rejected / closed */
    private String status;

    private LocalDateTime publishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
