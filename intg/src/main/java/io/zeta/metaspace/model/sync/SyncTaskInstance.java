package io.zeta.metaspace.model.sync;

import lombok.Data;
import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 数据采集任务实例，记录任务运行信息
 */
@Data
public class SyncTaskInstance {
    private String id;
    //定义 Id
    private String definitionId;
    private String name;
    //执行者名称
    private String executor;
    //启动时间
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp startTime;
    //更新时间
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    //时长单位秒
    private long duration;

    private Status status;
    private String log;

    private long total;

    public enum Status {
        NEW, RUN, SUCCESS, FAIL
    }
}
