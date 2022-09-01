package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 任务规则执行结果
 *
 * @author w
 */
@Data
public class SubTaskExecuteRule {
    /**
     * 当前子任务执行规则ID
     */
    private String id;
    /**
     * 当前子任务实例执行ID
     */
    private String taskExecuteId;
    /**
     * 所属任务id
     */
    private String taskId;
    /**
     * 所属子任务id
     */
    private String subtaskId;
    /**
     * 当前子任务执行结果
     */
    private String subtaskRuleId;
    /**
     * 所属子任务对象id
     */
    private String subtaskObjectId;
    /**
     * 当前子任务执行结果
     */
    private Float result;
    /**
     * 当前子任务执行时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp executeTime;
}
