package com.campus.evaluation.auth.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "用户详情VO")
public class UserDetailVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "学校ID")
    private Long schoolId;

    @Schema(description = "头像文件ID")
    private Long avatarFileId;

    @Schema(description = "角色编码列表")
    private List<String> roleCodes;

    @Schema(description = "角色名称列表")
    private List<String> roleNames;

    @Schema(description = "工号")
    private String staffNo;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "教学组织ID")
    private Long teachingOrgId;

    @Schema(description = "教学组织名称")
    private String teachingOrgName;

    @Schema(description = "服务组织ID")
    private Long serviceOrgId;

    @Schema(description = "服务组织名称")
    private String serviceOrgName;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "是否必须修改密码")
    private Boolean mustChangePassword;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
