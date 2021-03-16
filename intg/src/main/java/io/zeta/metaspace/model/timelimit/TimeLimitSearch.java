package io.zeta.metaspace.model.timelimit;

import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.commons.net.ntp.TimeStamp;

import java.sql.Timestamp;
import java.util.List;

/**
 * 基于通过查询类派生出的时间限定查询类
 */
public class TimeLimitSearch extends Parameters {

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
