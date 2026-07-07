package com.campus.evaluation.school.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 组织树节点 VO（教学组织/服务组织通用）
 */
@Data
public class OrgTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String type;
    private String status;
    private List<OrgTreeVO> children;
}
