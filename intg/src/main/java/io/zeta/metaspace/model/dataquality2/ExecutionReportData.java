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
 * @date 2019/8/26 10:25
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/26 10:25
 */
@Data
public class ExecutionReportData {
    private TaskBasicInfo basicInfo;
    private TaskCheckResultCount checkResultCount;
    private ImprovingSuggestion suggestion;
    private List<SubTaskRecord> ruleCheckResult;

    @Data
    public static class TaskBasicInfo {
        private String taskId;
        private String executionId;
        private String taskName;
        private String taskNumber;
        private Integer level;
        private String cronExpression;
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
        private Timestamp executeTime;
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
        private Timestamp startTime;
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
        private Timestamp endTime;
        private String pool;
    }

    public static class TaskCheckResultCount {
        private long tableRulePassedNumber;
        private long tableRuleNoPassedNumber;
        private long columnRulePassedNumber;
        private long columnRuleNoPassedNumber;

        private long orangeWarningNumber;
        private long totalOrangeWarningRuleNumber;
        private long redWarningNumber;
        private long totalRedWarningRuleNumber;
        private long errorRuleNumber;
        private long totalRuleNumber;

        public long getTableRulePassedNumber() {
            return tableRulePassedNumber;
        }

        public void setTableRulePassedNumber(long tableRulePassedNumber) {
            this.tableRulePassedNumber = tableRulePassedNumber;
        }

        public long getTableRuleNoPassedNumber() {
            return tableRuleNoPassedNumber;
        }

        public void setTableRuleNoPassedNumber(long tableRuleNoPassedNumber) {
            this.tableRuleNoPassedNumber = tableRuleNoPassedNumber;
        }

        public long getColumnRulePassedNumber() {
            return columnRulePassedNumber;
        }

        public void setColumnRulePassedNumber(long columnRulePassedNumber) {
            this.columnRulePassedNumber = columnRulePassedNumber;
        }

        public long getColumnRuleNoPassedNumber() {
            return columnRuleNoPassedNumber;
        }

        public void setColumnRuleNoPassedNumber(long columnRuleNoPassedNumber) {
            this.columnRuleNoPassedNumber = columnRuleNoPassedNumber;
        }

        public long getOrangeWarningNumber() {
            return orangeWarningNumber;
        }

        public void setOrangeWarningNumber(long orangeWarningNumber) {
            this.orangeWarningNumber = orangeWarningNumber;
        }

        public long getTotalOrangeWarningRuleNumber() {
            return totalOrangeWarningRuleNumber;
        }

        public void setTotalOrangeWarningRuleNumber(long totalOrangeWarningRuleNumber) {
            this.totalOrangeWarningRuleNumber = totalOrangeWarningRuleNumber;
        }

        public long getRedWarningNumber() {
            return redWarningNumber;
        }

        public void setRedWarningNumber(long redWarningNumber) {
            this.redWarningNumber = redWarningNumber;
        }

        public long getTotalRedWarningRuleNumber() {
            return totalRedWarningRuleNumber;
        }

        public void setTotalRedWarningRuleNumber(long totalRedWarningRuleNumber) {
            this.totalRedWarningRuleNumber = totalRedWarningRuleNumber;
        }

        public long getErrorRuleNumber() {
            return errorRuleNumber;
        }

        public void setErrorRuleNumber(long errorRuleNumber) {
            this.errorRuleNumber = errorRuleNumber;
        }

        public long getTotalRuleNumber() {
            return totalRuleNumber;
        }

        public void setTotalRuleNumber(long totalRuleNumber) {
            this.totalRuleNumber = totalRuleNumber;
        }
    }

    public static class ImprovingSuggestion {
        private List<String> tableQuestion;
        private List<String> columnQuestion;

        public List<String> getTableQuestion() {
            return tableQuestion;
        }

        public void setTableQuestion(List<String> tableQuestion) {
            this.tableQuestion = tableQuestion;
        }

        public List<String> getColumnQuestion() {
            return columnQuestion;
        }

        public void setColumnQuestion(List<String> columnQuestion) {
            this.columnQuestion = columnQuestion;
        }
    }

    public static class TaskRuleCheckResult {
        private String executeRuleId;
        private String subTaskId;
        private String ruleName;
        private String description;
        private Integer checkType;
        private Integer checkExpressionType;
        private Float result;
        private Integer checkStatus;

        public String getExecuteRuleId() {
            return executeRuleId;
        }

        public void setExecuteRuleId(String executeRuleId) {
            this.executeRuleId = executeRuleId;
        }

        public String getSubTaskId() {
            return subTaskId;
        }

        public void setSubTaskId(String subTaskId) {
            this.subTaskId = subTaskId;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getCheckType() {
            return checkType;
        }

        public void setCheckType(Integer checkType) {
            this.checkType = checkType;
        }

        public Integer getCheckExpressionType() {
            return checkExpressionType;
        }

        public void setCheckExpressionType(Integer checkExpressionType) {
            this.checkExpressionType = checkExpressionType;
        }

        public Float getResult() {
            return result;
        }

        public void setResult(Float result) {
            this.result = result;
        }

        public Integer getCheckStatus() {
            return checkStatus;
        }

        public void setCheckStatus(Integer checkStatus) {
            this.checkStatus = checkStatus;
        }
    }
}
