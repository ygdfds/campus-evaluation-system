package com.campus.evaluation.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("msg_staff_notification_preference")
public class StaffNotificationPreference {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private Long userId;
    private Boolean feedbackNotice;
    private Boolean evaluationNotice;
    private Boolean appealNotice;
    private Boolean reportWarningNotice;
    private Boolean systemNotice;
}
