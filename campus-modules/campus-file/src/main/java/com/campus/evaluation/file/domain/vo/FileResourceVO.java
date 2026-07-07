package com.campus.evaluation.file.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件资源 VO
 */
@Data
@Builder
@Schema(description = "文件资源信息")
public class FileResourceVO {

    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "学校ID")
    private Long schoolId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "访问URL")
    private String url;

    @Schema(description = "文件大小（字节）")
    private Long size;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "上传人ID")
    private Long uploaderId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
