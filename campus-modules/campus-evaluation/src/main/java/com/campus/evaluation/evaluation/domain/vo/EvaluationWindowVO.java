package com.campus.evaluation.evaluation.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "评价窗口 VO")
public class EvaluationWindowVO {

    @Schema(description = "窗口 ID")
    private Long id;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "窗口状态")
    private String status;

    @Schema(description = "窗口状态标签")
    private String statusLabel;

    @Schema(description = "窗口说明")
    private String description;
}
