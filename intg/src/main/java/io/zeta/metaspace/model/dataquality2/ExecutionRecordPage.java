package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 任务报告详情分页
 * @author w
 */
@Data
public class ExecutionRecordPage {
    /**
     * 执行记录id
     */
    private String executionId;
    /**
     * 橙色告警
     */
    private Integer orangeWarningCount;
    /**
     * 红色告警
     */
    private Integer redWarningCount;
    private Integer errorCount;
    private Integer generalWarningCount;
    /**
     * 质量结果
     */
    private String checkResult;
    /**
     * 执行时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp executeTime;
    /**
     * 任务实例
     */
    private String number;
    /**
     * 报告规定路径
     */
    private String archivePath;
    /**
     * 总条数
     */
    @JsonIgnore
    private Integer total;
}
