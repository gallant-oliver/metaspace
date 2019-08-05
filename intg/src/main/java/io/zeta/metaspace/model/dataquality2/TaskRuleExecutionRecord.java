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
    private String subTaskRuleId;
    private String executionId;
    private String ruleExecutionId;
    private String ruleName;
    private String description;
    private String dbGuid;
    private String dbName;
    private String tableGuid;
    private String tableName;
    private String objectId;
    private String objectName;
    private Integer checkType;
    private Integer checkExpression;
    private Float result;
    private Float checkMinValue;
    private Float checkMaxValue;
    private Integer checkStatus;
    private Integer orangeWarningCheckType;
    private Integer orangeWarningCheckExpression;
    private Float orangeWarningMinValue;
    private Float orangeWarningMaxValue;
    private Integer orangeCheckStatus;
    private Integer redWarningCheckType;
    private Integer redWarningCheckExpression;
    private Float redWarningMinValue;
    private Float redWarningMaxValue;
    private Integer redCheckStatus;

    public Integer getObjectType() {
        return objectType;
    }

    public void setObjectType(Integer objectType) {
        this.objectType = objectType;
    }

    public String getSubTaskRuleId() {
        return subTaskRuleId;
    }

    public void setSubTaskRuleId(String subTaskRuleId) {
        this.subTaskRuleId = subTaskRuleId;
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

    public Float getResult() {
        return result;
    }

    public void setResult(Float result) {
        this.result = result;
    }

    public Float getCheckMinValue() {
        return checkMinValue;
    }

    public void setCheckMinValue(Float checkMinValue) {
        this.checkMinValue = checkMinValue;
    }

    public Float getCheckMaxValue() {
        return checkMaxValue;
    }

    public void setCheckMaxValue(Float checkMaxValue) {
        this.checkMaxValue = checkMaxValue;
    }

    public Integer getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(Integer checkStatus) {
        this.checkStatus = checkStatus;
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

    public Integer getOrangeCheckStatus() {
        return orangeCheckStatus;
    }

    public void setOrangeCheckStatus(Integer orangeCheckStatus) {
        this.orangeCheckStatus = orangeCheckStatus;
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

    public Integer getRedCheckStatus() {
        return redCheckStatus;
    }

    public void setRedCheckStatus(Integer redCheckStatus) {
        this.redCheckStatus = redCheckStatus;
    }
}
