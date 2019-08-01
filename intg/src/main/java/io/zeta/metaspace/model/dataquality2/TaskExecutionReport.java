// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/8/1 9:45
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/1 9:45
 */
public class TaskExecutionReport {
    private String taskId;
    private String taskName;
    private Integer level;
    private String description;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp startTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp endTime;
    private Integer orangeWarningTotalCount;
    private Integer redWarningTotalCount;
    private Integer errorTotalCount;
    private Integer executeCount;
    private List<ExecutionRecord> executionRecordList;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getOrangeWarningTotalCount() {
        return orangeWarningTotalCount;
    }

    public void setOrangeWarningTotalCount(Integer orangeWarningTotalCount) {
        this.orangeWarningTotalCount = orangeWarningTotalCount;
    }

    public Integer getRedWarningTotalCount() {
        return redWarningTotalCount;
    }

    public void setRedWarningTotalCount(Integer redWarningTotalCount) {
        this.redWarningTotalCount = redWarningTotalCount;
    }

    public Integer getExecuteCount() {
        return executeCount;
    }

    public void setExecuteCount(Integer executeCount) {
        this.executeCount = executeCount;
    }

    public Integer getErrorTotalCount() {
        return errorTotalCount;
    }

    public void setErrorTotalCount(Integer errorTotalCount) {
        this.errorTotalCount = errorTotalCount;
    }

    public List<ExecutionRecord> getExecutionRecordList() {
        return executionRecordList;
    }

    public void setExecutionRecordList(List<ExecutionRecord> executionRecordList) {
        this.executionRecordList = executionRecordList;
    }

    public static class ExecutionRecord {
        private String executionId;
        private Integer orangeWarningCount;
        private Integer redWarningCount;
        private Integer errorCount;
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
        private Timestamp executeTime;
        private String number;

        public String getExecutionId() {
            return executionId;
        }

        public void setExecutionId(String executionId) {
            this.executionId = executionId;
        }

        public Integer getOrangeWarningCount() {
            return orangeWarningCount;
        }

        public void setOrangeWarningCount(Integer orangeWarningCount) {
            this.orangeWarningCount = orangeWarningCount;
        }

        public Integer getRedWarningCount() {
            return redWarningCount;
        }

        public void setRedWarningCount(Integer redWarningCount) {
            this.redWarningCount = redWarningCount;
        }

        public Integer getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(Integer errorCount) {
            this.errorCount = errorCount;
        }

        public Timestamp getExecuteTime() {
            return executeTime;
        }

        public void setExecuteTime(Timestamp executeTime) {
            this.executeTime = executeTime;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }
    }
}
