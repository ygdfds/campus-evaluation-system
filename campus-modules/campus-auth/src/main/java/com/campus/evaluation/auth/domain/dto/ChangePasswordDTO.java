package com.campus.evaluation.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDTO {

    @NotBlank(message = "oldPassword is required")
    private String oldPassword;

    @NotBlank(message = "newPassword is required")
    @Size(min = 6, max = 64, message = "newPassword length must be 6-64")
    private String newPassword;

    @NotBlank(message = "confirmPassword is required")
    private String confirmPassword;
}
