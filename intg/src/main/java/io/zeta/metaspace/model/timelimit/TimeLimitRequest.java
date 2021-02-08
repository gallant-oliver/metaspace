package io.zeta.metaspace.model.timelimit;


import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 时间限定request
 */
@Data
public class TimeLimitRequest {

    /**
     * ID
     */

    private String id;

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

    /**
     * version
     */

    private int version;

    /**
     * ids 批量操作的ID
     */

    private List<String> ids;

    /**
     * 操作类型 1.发布 2.下线 3.删除
     */

    private String type;



    public Timestamp getStartTimeTimestamp(){
        return new Timestamp(Long.parseLong(this.startTime));
    }

    public Timestamp getEndTimeTimestamp(){
        return new Timestamp(Long.parseLong(this.endTime));
    }
}
