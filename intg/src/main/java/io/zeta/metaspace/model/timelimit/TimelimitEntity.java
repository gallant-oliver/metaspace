package io.zeta.metaspace.model.timelimit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.net.ntp.TimeStamp;

import java.sql.Timestamp;

@Data
public class TimelimitEntity {

    /**
     * 时间限定唯一ID
     */
    private String id;

    /**
     * 时间限定版本
     */

    private int version;

    /**
     * 时间限定版本
     */

    private int total;

    /**
     * 时间限定名称
     */
    private String name;

    /**
     * desc
     */

    private String desc;

    /**
     * 时间限定类型
     */

    private String grade;

    /**
     * 开始时间
     */

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp startTime;
    /**
     * 结束时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp endTime;

    /**
     * 创建人Id
     */
    private String creator;

    /**
     * 更新人Id
     */
    private String updater;

    /**
     * 时间限定状态
     */

    private String state; //初始化为新建

    /**
     * 审批组ID
     */

    private String appreveId;

    /**
     * delete flag
     */

    private boolean delete = false; // 初始化为未删除

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    private String tenantId;


}
