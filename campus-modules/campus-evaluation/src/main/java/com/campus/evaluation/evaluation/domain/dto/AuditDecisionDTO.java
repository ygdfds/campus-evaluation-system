package com.campus.evaluation.evaluation.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "审核决定请求")
public class AuditDecisionDTO {

    @NotBlank(message = "驳回原因不能为空")
    @Size(min = 10, max = 300, message = "驳回原因需在 10-300 字之间")
    @Schema(description = "驳回原因（仅驳回时需要）")
    private String reason;
}
