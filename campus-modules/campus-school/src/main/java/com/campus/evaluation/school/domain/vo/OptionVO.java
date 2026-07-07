package com.campus.evaluation.school.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉选项 VO（通用轻量结构）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionVO {
    private Long id;
    private String label;
    private Object value;
    private String extra;
}
