package com.campus.evaluation.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "修改用户状态DTO")
public class ChangeUserStatusDTO {

    @NotBlank(message = "状态不能为空")
    @Schema(description = "状态：enabled / disabled", allowableValues = {"enabled", "disabled"})
    private String status;
}
