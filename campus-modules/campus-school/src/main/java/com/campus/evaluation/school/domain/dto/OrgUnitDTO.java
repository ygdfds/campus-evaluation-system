package com.campus.evaluation.school.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 组织新增/编辑 DTO（教学组织/服务组织通用）
 */
@Data
@Schema(description = "组织新增/编辑请求")
public class OrgUnitDTO {

    @Schema(description = "组织ID（编辑时传入）")
    private Long id;

    @NotBlank(message = "组织名称不能为空")
    @Size(max = 120, message = "组织名称最多120字")
    @Schema(description = "组织名称")
    private String name;

    @NotBlank(message = "组织编码不能为空")
    @Size(max = 64, message = "组织编码最多64字")
    @Schema(description = "组织编码")
    private String code;

    @NotBlank(message = "组织类型不能为空")
    @Schema(description = "组织类型")
    private String type;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "状态，默认 active")
    private String status = "active";
}
