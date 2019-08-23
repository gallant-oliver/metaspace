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
 * @date 2019/8/8 14:57
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/8 14:57
 */
public class WarningInfo {

    private String executionId;
    private String taskName;
    private String executeTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp closeTime;
    private String closer;
    private Integer warningStatus;
    private List<SubTaskWarning> subTaskList;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }


    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
    }

    public Timestamp getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Timestamp closeTime) {
        this.closeTime = closeTime;
    }

    public String getCloser() {
        return closer;
    }

    public void setCloser(String closer) {
        this.closer = closer;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    public List<SubTaskWarning> getSubTaskList() {
        return subTaskList;
    }

    public void setSubTaskList(List<SubTaskWarning> subTaskList) {
        this.subTaskList = subTaskList;
    }

    public static class SubTaskWarning {
        private String subTaskId;
        private Integer ruleType;
        private Integer sequence;
        private List<SubTaskRuleWarning> subTaskList;

        public String getSubTaskId() {
            return subTaskId;
        }

        public void setSubTaskId(String subTaskId) {
            this.subTaskId = subTaskId;
        }

        public Integer getRuleType() {
            return ruleType;
        }

        public void setRuleType(Integer ruleType) {
            this.ruleType = ruleType;
        }

        public Integer getSequence() {
            return sequence;
        }

        public void setSequence(Integer sequence) {
            this.sequence = sequence;
        }

        public List<SubTaskRuleWarning> getSubTaskList() {
            return subTaskList;
        }

        public void setSubTaskList(List<SubTaskRuleWarning> subTaskList) {
            this.subTaskList = subTaskList;
        }
    }


    public static class SubTaskRuleWarning {
        private String ruleName;
        private String dbName;
        private String tableName;
        private String columnName;
        private String warningMessage;
        private Integer warningType;
        private Float result;
        private String unit;
        private String objectId;


        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getWarningMessage() {
            return warningMessage;
        }

        public void setWarningMessage(String warningMessage) {
            this.warningMessage = warningMessage;
        }

        public Integer getWarningType() {
            return warningType;
        }

        public void setWarningType(Integer warningType) {
            this.warningType = warningType;
        }

        public Float getResult() {
            return result;
        }

        public void setResult(Float result) {
            this.result = result;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }
    }
}
