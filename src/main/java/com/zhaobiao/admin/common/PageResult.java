package com.zhaobiao.admin.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "分页结果。页码从 1 开始，后端会把 pageNum<=0 修正为 1，并把 pageSize<=0 修正为 10；招标列表 pageSize 最大 50。")
public class PageResult<T> {

    @Schema(description = "当前页码，从 1 开始", example = "1")
    private int pageNum;

    @Schema(description = "每页条数", example = "10")
    private int pageSize;

    @Schema(description = "符合条件的总记录数", example = "128")
    private long total;

    @Schema(description = "总页数", example = "13")
    private int totalPages;

    @Schema(description = "当前页数据列表")
    private List<T> list;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
