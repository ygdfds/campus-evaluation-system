package com.campus.evaluation.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "新增教职工DTO")
public class StaffUserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 80, message = "用户名长度 2-80")
    @Schema(description = "用户名")
    private String username;

    @Size(min = 6, max = 32, message = "密码长度 6-32")
    @Schema(description = "密码，不传则默认 123456")
    private String password;

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

    @Schema(description = "角色编码列表，如 [\"staff\",\"teaching_admin\"]")
    private List<String> roleCodes;
}
