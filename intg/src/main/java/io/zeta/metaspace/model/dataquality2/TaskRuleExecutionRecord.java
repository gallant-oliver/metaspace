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
 * @date 2019/8/1 18:21
 */
package io.zeta.metaspace.model.dataquality2;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/1 18:21
 */
public class TaskRuleExecutionRecord {
    private Integer objectType;
    private String executionId;
    private String ruleExecutionId;
    private String ruleName;
    private String description;
    private String dbGuid;
    private String dbName;
    private String tableGuid;
    private String tableName;
    private String columnId;
    private String columnName;
    private Integer orangeWarningCheckType;
    private Integer orangeWarningcheckExpression;
    private Float orangeWarningMinValue;
    private Float orangeWarningMaxValue;
    private Integer redWarningCheckType;
    private Integer redWarningcheckExpression;
    private Float redWarningMinValue;
    private Float redWarningMaxValue;
    private Float result;
    private Integer checkStatus;

    public Integer getObjectType() {
        return objectType;
    }

    public void setObjectType(Integer objectType) {
        this.objectType = objectType;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getRuleExecutionId() {
        return ruleExecutionId;
    }

    public void setRuleExecutionId(String ruleExecutionId) {
        this.ruleExecutionId = ruleExecutionId;
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

    public String getDbGuid() {
        return dbGuid;
    }

    public void setDbGuid(String dbGuid) {
        this.dbGuid = dbGuid;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getOrangeWarningCheckType() {
        return orangeWarningCheckType;
    }

    public void setOrangeWarningCheckType(Integer orangeWarningCheckType) {
        this.orangeWarningCheckType = orangeWarningCheckType;
    }

    public Integer getOrangeWarningcheckExpression() {
        return orangeWarningcheckExpression;
    }

    public void setOrangeWarningcheckExpression(Integer orangeWarningcheckExpression) {
        this.orangeWarningcheckExpression = orangeWarningcheckExpression;
    }

    public Float getOrangeWarningMinValue() {
        return orangeWarningMinValue;
    }

    public void setOrangeWarningMinValue(Float orangeWarningMinValue) {
        this.orangeWarningMinValue = orangeWarningMinValue;
    }

    public Float getOrangeWarningMaxValue() {
        return orangeWarningMaxValue;
    }

    public void setOrangeWarningMaxValue(Float orangeWarningMaxValue) {
        this.orangeWarningMaxValue = orangeWarningMaxValue;
    }

    public Integer getRedWarningCheckType() {
        return redWarningCheckType;
    }

    public void setRedWarningCheckType(Integer redWarningCheckType) {
        this.redWarningCheckType = redWarningCheckType;
    }

    public Integer getRedWarningcheckExpression() {
        return redWarningcheckExpression;
    }

    public void setRedWarningcheckExpression(Integer redWarningcheckExpression) {
        this.redWarningcheckExpression = redWarningcheckExpression;
    }

    public Float getRedWarningMinValue() {
        return redWarningMinValue;
    }

    public void setRedWarningMinValue(Float redWarningMinValue) {
        this.redWarningMinValue = redWarningMinValue;
    }

    public Float getRedWarningMaxValue() {
        return redWarningMaxValue;
    }

    public void setRedWarningMaxValue(Float redWarningMaxValue) {
        this.redWarningMaxValue = redWarningMaxValue;
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
