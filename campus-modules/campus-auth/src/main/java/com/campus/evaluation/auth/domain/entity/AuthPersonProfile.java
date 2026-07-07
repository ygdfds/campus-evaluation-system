package com.campus.evaluation.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人员档案实体
 */
@Data
@TableName("auth_person_profile")
public class AuthPersonProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long userId;

    private String realName;

    /** student/staff/school_admin/system_admin */
    private String roleType;

    private String noWork;

    private String noStudent;

    private String gender;

    private String officePhone;

    private String intro;

    private Long avatarFileId;

    private Long orgUnitId;

    private String departmentName;

    private String className;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
