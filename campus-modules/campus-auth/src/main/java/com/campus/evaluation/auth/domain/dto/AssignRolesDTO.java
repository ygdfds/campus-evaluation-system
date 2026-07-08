package com.campus.evaluation.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分配角色DTO")
public class AssignRolesDTO {

    @NotEmpty(message = "角色编码列表不能为空")
    @Schema(description = "角色编码列表")
    private List<String> roleCodes;
}
