package com.campus.evaluation.evaluation.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "风险检查项")
public class RiskItemVO {

    @Schema(description = "风险级别（blocker / warning）")
    private String level;

    @Schema(description = "风险描述")
    private String message;
}
