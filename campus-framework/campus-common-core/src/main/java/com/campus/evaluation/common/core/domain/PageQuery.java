package com.campus.evaluation.common.core.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询参数
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前页码，默认 1 */
    @Min(value = 1, message = "页码最小值为1")
    private int pageNum = 1;

    /** 每页大小，默认 10 */
    @Min(value = 1, message = "每页条数最小值为1")
    @Max(value = 100, message = "每页条数最大值为100")
    private int pageSize = 10;
}
