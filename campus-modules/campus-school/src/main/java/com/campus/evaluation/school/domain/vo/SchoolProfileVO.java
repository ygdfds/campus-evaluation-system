package com.campus.evaluation.school.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校资料 VO
 */
@Data
@Builder
public class SchoolProfileVO {

    private Long id;
    private Long tenantId;
    private String schoolName;
    private String address;
    private String website;
    private Long logoFileId;
    private String logoUrl;
    private Long coverFileId;
    private String coverUrl;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
