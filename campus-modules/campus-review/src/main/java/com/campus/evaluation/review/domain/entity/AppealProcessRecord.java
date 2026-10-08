package com.campus.evaluation.review.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("rv_appeal_process_record")
public class AppealProcessRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private Long appealId;
    private Long operatorId;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String content;
    private LocalDateTime createdAt;
}
