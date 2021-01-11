package io.zeta.metaspace.model.sync;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据采集任务定义
 */
@Data
public class SyncTaskDefinition {
    private String id;
    private String name;
    //创建者
    private String creator;
    //创建时间
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    //更新时间
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    private String description;

    private boolean enable;

    // 定时开始时间
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp cronStartTime;

    //定时结束时间
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp cronEndTime;

    //定时表达式
    private String crontab;

    //数据源id
    private String dataSourceId;

    private String dataSourceName;

    private String dataSourceType;

    //是否同步所有数据库
    private boolean syncAll;

    //同步指定的数据库
    private List<String> schemas;

    private String tenantId;

    private long total;
}
