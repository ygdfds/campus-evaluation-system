package com.campus.evaluation.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改用户状态DTO")
public class ChangeUserStatusDTO {

    @NotBlank(message = "状态不能为空")
    @Schema(description = "状态：active / disabled / locked", allowableValues = {"active", "disabled", "locked"})
    @Pattern(regexp = "active|disabled|locked", message = "status must be active, disabled or locked")
    private String status;
}
