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
    private Integer pageNum;

    /** Formal API contract field; pageNum remains for old clients. */
    @Min(value = 1, message = "页码最小值为1")
    private Integer page;

    /** 每页大小，默认 10 */
    @Min(value = 1, message = "每页条数最小值为1")
    @Max(value = 100, message = "每页条数最大值为100")
    private int pageSize = 20;

    public int getPage() {
        if (page != null && page > 0) {
            return page;
        }
        return pageNum != null && pageNum > 0 ? pageNum : 1;
    }

    public void setPage(int page) {
        this.page = page;
        this.pageNum = page;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
}
