package io.zeta.metaspace.model.timelimit;


import lombok.Data;

import java.sql.Timestamp;

/**
 * 时间限定request
 */
@Data
public class TimeLimitRequest {

    /*
      时间限定名称
     */
    private String name;

    /*
     时间限定标识
     */

    private String mark;

    /*
     * 粒度
     */

    private String grade;

    /**
     * startTime
     */

    private String startTime;

    /**
     * endTime
     */

    private String endTime;

    /**
     * 审批组ID
     */

    private String appreveId;

    /**
     * desc
     */

    private String desc;


    public Timestamp getStartTimeTimestamp(){
        return new Timestamp(Long.parseLong(this.startTime));
    }

    public Timestamp getEndTimeTimestamp(){
        return new Timestamp(Long.parseLong(this.endTime));
    }
}
