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
 * @date 2019/7/24 17:28
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 17:28
 */
public class TaskInfo {
    private String taskName;
    private Integer level;
    private String description;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp startTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp endTime;
    private String cronExpression;
    private List<SubTask> taskList;
    private List<String> contentWarningNotificationIdList;
    private List<String> executionWarningNotificationIdList;
    private String pool;

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
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

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public List<SubTask> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<SubTask> taskList) {
        this.taskList = taskList;
    }

    public List<String> getContentWarningNotificationIdList() {
        return contentWarningNotificationIdList;
    }

    public void setContentWarningNotificationIdList(List<String> contentWarningNotificationIdList) {
        this.contentWarningNotificationIdList = contentWarningNotificationIdList;
    }

    public List<String> getExecutionWarningNotificationIdList() {
        return executionWarningNotificationIdList;
    }

    public void setExecutionWarningNotificationIdList(List<String> executionWarningNotificationIdList) {
        this.executionWarningNotificationIdList = executionWarningNotificationIdList;
    }

    @Data
    public static class SubTask {
        private Integer dataSourceType;
        private List<String> objectIdList;
        private List<SubTaskRule> subTaskRuleList;
        private int ruleScope;
    }

    public static class SubTaskRule {
        private String ruleId;
        private Integer checkType;
        private Integer checkExpression;
        private String checkThresholdUnit;
        private Float checkThresholdMinValue;
        private Float checkThresholdMaxValue;
        private List<Warning> warnings;

        public String getRuleId() {
            return ruleId;
        }

        public void setRuleId(String ruleId) {
            this.ruleId = ruleId;
        }

        public Integer getCheckType() {
            return checkType;
        }

        public void setCheckType(Integer checkType) {
            this.checkType = checkType;
        }

        public Integer getCheckExpression() {
            return checkExpression;
        }

        public void setCheckExpression(Integer checkExpression) {
            this.checkExpression = checkExpression;
        }


        public String getCheckThresholdUnit() {
            return checkThresholdUnit;
        }

        public void setCheckThresholdUnit(String checkThresholdUnit) {
            this.checkThresholdUnit = checkThresholdUnit;
        }

        public Float getCheckThresholdMinValue() {
            return checkThresholdMinValue;
        }

        public void setCheckThresholdMinValue(Float checkThresholdMinValue) {
            this.checkThresholdMinValue = checkThresholdMinValue;
        }

        public Float getCheckThresholdMaxValue() {
            return checkThresholdMaxValue;
        }

        public void setCheckThresholdMaxValue(Float checkThresholdMaxValue) {
            this.checkThresholdMaxValue = checkThresholdMaxValue;
        }

        public List<Warning> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<Warning> warnings) {
            this.warnings = warnings;
        }
    }

    public static class Warning {
        private Integer warningType;
        private Integer warningCheckType;
        private Integer warningCheckExpression;
        private Float warningCheckThresholdMinValue;
        private Float warningCheckThresholdMaxValue;

        public Integer getWarningType() {
            return warningType;
        }

        public void setWarningType(Integer warningType) {
            this.warningType = warningType;
        }

        public Integer getWarningCheckType() {
            return warningCheckType;
        }

        public void setWarningCheckType(Integer warningCheckType) {
            this.warningCheckType = warningCheckType;
        }

        public Integer getWarningCheckExpression() {
            return warningCheckExpression;
        }

        public void setWarningCheckExpression(Integer warningCheckExpression) {
            this.warningCheckExpression = warningCheckExpression;
        }

        public Float getWarningCheckThresholdMinValue() {
            return warningCheckThresholdMinValue;
        }

        public void setWarningCheckThresholdMinValue(Float warningCheckThresholdMinValue) {
            this.warningCheckThresholdMinValue = warningCheckThresholdMinValue;
        }

        public Float getWarningCheckThresholdMaxValue() {
            return warningCheckThresholdMaxValue;
        }

        public void setWarningCheckThresholdMaxValue(Float warningCheckThresholdMaxValue) {
            this.warningCheckThresholdMaxValue = warningCheckThresholdMaxValue;
        }

    }
}
