package io.zeta.metaspace.model.timelimit;

import java.sql.Timestamp;
import java.util.List;

/**
 * 基于通过查询类派生出的时间限定查询类
 */
public class TimeLimitSearch {

    public Timestamp getStartTime() {
        return startTime == null ? null :new Timestamp(Long.valueOf(startTime));
    }

    public Timestamp getEndTime() {
        return endTime == null ? null : new Timestamp(Long.valueOf(endTime));
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    private String startTime; //查询时间戳

    private String endTime; //查询时间戳

    private List<String> status;
    /**
     * 时间限定ID
     */
    private String id;

    private String query;

    private int offset;

    private int limit;

    private String sortBy;

    private String order;

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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}
