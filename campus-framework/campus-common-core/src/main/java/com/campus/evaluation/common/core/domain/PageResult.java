package com.campus.evaluation.common.core.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> records;

    /** 当前页码 */
    private int pageNum;

    /** Formal API contract field; pageNum remains for compatibility. */
    private int page;

    /** 每页大小 */
    private int pageSize;

    /** 总页数 */
    private int totalPages;

    public PageResult() {}

    public PageResult(long total, List<T> records, int pageNum, int pageSize) {
        this.total = total;
        this.records = records;
        this.pageNum = pageNum;
        this.page = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
        this.page = pageNum;
    }

    public void setPage(int page) {
        this.page = page;
        this.pageNum = page;
    }
}
