package com.campus.evaluation.message.domain.entity;

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
@TableName("msg_notification")
public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private Long senderUserId;
    private Long receiverUserId;
    private String targetRoles;
    private String type;
    @TableField("biz_type")
    private String businessType;
    private String title;
    private String content;
    private String priority;
    private String readStatus;
    private String link;
    private Long bizId;
    private LocalDateTime readAt;
    private String tag;
    private Long coverFileId;
    private LocalDateTime publishTime;
    private String status;
    private String noticeType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
