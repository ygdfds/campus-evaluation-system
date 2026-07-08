package com.campus.evaluation.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "重置密码DTO")
public class ResetPasswordDTO {

    @Schema(description = "新密码，不传则默认 123456")
    private String newPassword;
}
