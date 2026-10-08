package com.campus.evaluation.review.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("rv_appeal_request")
public class AppealRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private String appealNo;
    private Long submissionId;
    private Long formId;
    private String targetType;
    private Long targetId;
    private Long appellantUserId;
    private String appealType;
    private String reason;
    private String evidenceFileIds;
    private String status;
    private String priority;
    private Long handlerId;
    private String handleResult;
    private String handleComment;
    private LocalDateTime submittedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime resolvedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
