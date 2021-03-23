package io.zeta.metaspace.model.dto.indices;

import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.List;

public class PageQueryDTO {
    /**
     * 目录列表
     */
    private List<String> indexFieldIds;
    /**
     * 指标类型 （1 原子，2 派生，3 复合，4 跨类型，默认4）
     */
    private int indexType;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp startTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp endTime;
    /**
     * true：核心指标，false：核心指标+非核心指标
     */
    private boolean central;
    /**
     * 搜索内容（指标名或指标标识）
     */
    private String searchContent;
    /**
     * 指标状态(1 新建，2 已发布，3 已下线，4 审核中)
     */
    private int indexState;
    /**
     * 更新时间排序方向（asc ，desc）
     */
    private String order;
    /**
     * 偏移量
     */
    private int offset;
    /**
     * 页大小
     */
    private int limit;

    public int getIndexType() {
        return indexType;
    }

    public void setIndexType(int indexType) {
        this.indexType = indexType;
    }


    public boolean isCentral() {
        return central;
    }

    public void setCentral(boolean central) {
        this.central = central;
    }

    public String getSearchContent() {
        return searchContent;
    }

    public void setSearchContent(String searchContent) {
        this.searchContent = searchContent;
    }

    public int getIndexState() {
        return indexState;
    }

    public void setIndexState(int indexState) {
        this.indexState = indexState;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public List<String> getIndexFieldIds() {
        return indexFieldIds;
    }

    public void setIndexFieldIds(List<String> indexFieldIds) {
        this.indexFieldIds = indexFieldIds;
    }
}
