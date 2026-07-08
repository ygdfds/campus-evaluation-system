package com.campus.evaluation.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "编辑学生DTO")
public class StudentUserUpdateDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 80, message = "真实姓名最长 80")
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "头像文件ID")
    private Long avatarFileId;
}
