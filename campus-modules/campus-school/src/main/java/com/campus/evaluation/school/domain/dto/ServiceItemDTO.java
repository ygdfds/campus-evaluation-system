package com.campus.evaluation.school.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "服务项目新增/编辑请求")
@Data
public class ServiceItemDTO {

    @NotNull(message = "服务组织ID不能为空")
    @Schema(description = "服务组织ID")
    private Long serviceOrgId;

    @NotBlank(message = "服务项目名称不能为空")
    @Size(max = 160, message = "服务项目名称最多160字")
    @Schema(description = "服务项目名称")
    private String name;

    @Schema(description = "封面文件ID")
    private Long coverFileId;

    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "服务类型")
    private String type;

    @Schema(description = "状态，默认 active")
    private String status = "active";
}
