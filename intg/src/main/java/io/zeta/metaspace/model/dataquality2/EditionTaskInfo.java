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
 * @date 2019/8/14 19:15
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/14 19:15
 */
public class EditionTaskInfo {
    private String id;
    private String taskName;
    private String taskID;
    private Integer level;
    private String description;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp startTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp endTime;
    private String cronExpression;
    private List<EditionTaskInfo.SubTask> taskList;
    private List<WarningGroup> contentWarningNotificationIdList;
    private List<WarningGroup> executionWarningNotificationIdList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
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

    public List<WarningGroup> getContentWarningNotificationIdList() {
        return contentWarningNotificationIdList;
    }

    public void setContentWarningNotificationIdList(List<WarningGroup> contentWarningNotificationIdList) {
        this.contentWarningNotificationIdList = contentWarningNotificationIdList;
    }

    public List<WarningGroup> getExecutionWarningNotificationIdList() {
        return executionWarningNotificationIdList;
    }

    public void setExecutionWarningNotificationIdList(List<WarningGroup> executionWarningNotificationIdList) {
        this.executionWarningNotificationIdList = executionWarningNotificationIdList;
    }

    public static class SubTask {
        private String subTaskId;
        private Integer dataSourceType;
        private String sequence;
        private List<ObjectInfo> objectIdList;
        private List<EditionTaskInfo.SubTaskRule> subTaskRuleList;

        public String getSubTaskId() {
            return subTaskId;
        }

        public void setSubTaskId(String subTaskId) {
            this.subTaskId = subTaskId;
        }

        public Integer getDataSourceType() {
            return dataSourceType;
        }

        public void setDataSourceType(Integer dataSourceType) {
            this.dataSourceType = dataSourceType;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public List<ObjectInfo> getObjectIdList() {
            return objectIdList;
        }

        public void setObjectIdList(List<ObjectInfo> objectIdList) {
            this.objectIdList = objectIdList;
        }

        public List<SubTaskRule> getSubTaskRuleList() {
            return subTaskRuleList;
        }

        public void setSubTaskRuleList(List<SubTaskRule> subTaskRuleList) {
            this.subTaskRuleList = subTaskRuleList;
        }
    }

    public static class ObjectInfo {
        private Integer sequence;
        private String objectId;
        private String objectName;
        private String dbName;
        private String tableName;

        public Integer getSequence() {
            return sequence;
        }

        public void setSequence(Integer sequence) {
            this.sequence = sequence;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(String objectName) {
            this.objectName = objectName;
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
    }

    public static class SubTaskRule {
        private String subTaskRuleId;
        private String ruleId;
        private String ruleName;
        private Integer checkType;
        private String categoryId;
        private Integer checkExpression;
        private String checkThresholdUnit;
        private Float checkThresholdMinValue;
        private Float checkThresholdMaxValue;
        private Integer orangeWarningCheckType;
        private Integer orangeWarningCheckExpression;
        private Float orangeWarningCheckThresholdMinValue;
        private Float orangeWarningCheckThresholdMaxValue;

        private Integer redWarningCheckType;
        private Integer redWarningCheckExpression;
        private Float redWarningCheckThresholdMinValue;
        private Float redWarningCheckThresholdMaxValue;

        public String getSubTaskRuleId() {
            return subTaskRuleId;
        }

        public void setSubTaskRuleId(String subTaskRuleId) {
            this.subTaskRuleId = subTaskRuleId;
        }

        public String getRuleId() {
            return ruleId;
        }

        public void setRuleId(String ruleId) {
            this.ruleId = ruleId;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public Integer getCheckType() {
            return checkType;
        }

        public void setCheckType(Integer checkType) {
            this.checkType = checkType;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
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

        public Integer getOrangeWarningCheckType() {
            return orangeWarningCheckType;
        }

        public void setOrangeWarningCheckType(Integer orangeWarningCheckType) {
            this.orangeWarningCheckType = orangeWarningCheckType;
        }

        public Integer getOrangeWarningCheckExpression() {
            return orangeWarningCheckExpression;
        }

        public void setOrangeWarningCheckExpression(Integer orangeWarningCheckExpression) {
            this.orangeWarningCheckExpression = orangeWarningCheckExpression;
        }

        public Float getOrangeWarningCheckThresholdMinValue() {
            return orangeWarningCheckThresholdMinValue;
        }

        public void setOrangeWarningCheckThresholdMinValue(Float orangeWarningCheckThresholdMinValue) {
            this.orangeWarningCheckThresholdMinValue = orangeWarningCheckThresholdMinValue;
        }

        public Float getOrangeWarningCheckThresholdMaxValue() {
            return orangeWarningCheckThresholdMaxValue;
        }

        public void setOrangeWarningCheckThresholdMaxValue(Float orangeWarningCheckThresholdMaxValue) {
            this.orangeWarningCheckThresholdMaxValue = orangeWarningCheckThresholdMaxValue;
        }

        public Integer getRedWarningCheckType() {
            return redWarningCheckType;
        }

        public void setRedWarningCheckType(Integer redWarningCheckType) {
            this.redWarningCheckType = redWarningCheckType;
        }

        public Integer getRedWarningCheckExpression() {
            return redWarningCheckExpression;
        }

        public void setRedWarningCheckExpression(Integer redWarningCheckExpression) {
            this.redWarningCheckExpression = redWarningCheckExpression;
        }

        public Float getRedWarningCheckThresholdMinValue() {
            return redWarningCheckThresholdMinValue;
        }

        public void setRedWarningCheckThresholdMinValue(Float redWarningCheckThresholdMinValue) {
            this.redWarningCheckThresholdMinValue = redWarningCheckThresholdMinValue;
        }

        public Float getRedWarningCheckThresholdMaxValue() {
            return redWarningCheckThresholdMaxValue;
        }

        public void setRedWarningCheckThresholdMaxValue(Float redWarningCheckThresholdMaxValue) {
            this.redWarningCheckThresholdMaxValue = redWarningCheckThresholdMaxValue;
        }
    }

    public static class WarningGroup {
        private String warningGroupId;
        private String warningGroupName;

        public String getWarningGroupId() {
            return warningGroupId;
        }

        public void setWarningGroupId(String warningGroupId) {
            this.warningGroupId = warningGroupId;
        }

        public String getWarningGroupName() {
            return warningGroupName;
        }

        public void setWarningGroupName(String warningGroupName) {
            this.warningGroupName = warningGroupName;
        }
    }

}