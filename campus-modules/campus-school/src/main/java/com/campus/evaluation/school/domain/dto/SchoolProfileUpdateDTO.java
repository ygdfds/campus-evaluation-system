package com.campus.evaluation.school.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学校资料更新 DTO
 */
@Data
@Schema(description = "学校资料更新请求")
public class SchoolProfileUpdateDTO {

    @Size(max = 180, message = "学校名称最多180字")
    @Schema(description = "学校名称")
    private String name;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "官网")
    private String website;

    @Schema(description = "Logo文件ID")
    private Long logoFileId;

    @Schema(description = "封面文件ID")
    private Long coverFileId;

    @Size(max = 2000, message = "简介最多2000字")
    @Schema(description = "学校简介")
    private String intro;
}
