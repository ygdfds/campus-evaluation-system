package com.campus.evaluation.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "编辑教职工DTO")
public class StaffUserUpdateDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 80, message = "真实姓名最长 80")
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "工号")
    private String staffNo;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "教学组织ID")
    private Long teachingOrgId;

    @Schema(description = "服务组织ID")
    private Long serviceOrgId;

    @Schema(description = "头像文件ID")
    private Long avatarFileId;
}
