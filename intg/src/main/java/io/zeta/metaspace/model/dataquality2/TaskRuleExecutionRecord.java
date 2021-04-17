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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/1 18:21
 */
@Data
public class TaskRuleExecutionRecord {
    private Integer scope;
    private Integer taskType;
    private String subTaskRuleId;
    private String subtaskId;
    private Integer subTaskSequence;
    private String executionId;
    private String ruleExecutionId;
    private String ruleName;
    private String description;
    private String dataSourceName;
    private String dbName;
    private String tableName;
    private String objectId;
    private String objectName;
    private Integer checkType;
    private Integer checkExpression;
    private Float result;
    private String resultString;
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
    private String checkThresholdUnit;
    private Boolean filing;
    @JsonIgnore
    private Timestamp createTime;
    private String tableId;

    public TaskRuleExecutionRecord(){

    }

    public TaskRuleExecutionRecord(TaskRuleExecutionRecord taskRuleExecutionRecord){
        this.scope = taskRuleExecutionRecord.scope;
        this.taskType = taskRuleExecutionRecord.taskType;
        this.subTaskRuleId = taskRuleExecutionRecord.subTaskRuleId;
        this.subtaskId = taskRuleExecutionRecord.subtaskId;
        this.subTaskSequence = taskRuleExecutionRecord.subTaskSequence;
        this.executionId = taskRuleExecutionRecord.executionId;
        this.ruleExecutionId = taskRuleExecutionRecord.ruleExecutionId;
        this.ruleName = taskRuleExecutionRecord.ruleName;
        this.description = taskRuleExecutionRecord.description;
        this.dataSourceName = taskRuleExecutionRecord.dataSourceName;
        this.dbName = taskRuleExecutionRecord.dbName;
        this.tableName = taskRuleExecutionRecord.tableName;
        this.objectId = taskRuleExecutionRecord.objectId;
        this.objectName = taskRuleExecutionRecord.objectName;
        this.checkType = taskRuleExecutionRecord.checkType;
        this.checkExpression = taskRuleExecutionRecord.checkExpression;
        this.result = taskRuleExecutionRecord.result;
        this.checkMinValue = taskRuleExecutionRecord.checkMinValue;
        this.checkMaxValue = taskRuleExecutionRecord.checkMaxValue;
        this.checkStatus = taskRuleExecutionRecord.checkStatus;
        this.orangeWarningCheckType = taskRuleExecutionRecord.orangeWarningCheckType;
        this.orangeWarningCheckExpression = taskRuleExecutionRecord.orangeWarningCheckExpression;
        this.orangeWarningMinValue = taskRuleExecutionRecord.orangeWarningMinValue;
        this.orangeWarningMaxValue = taskRuleExecutionRecord.orangeWarningMaxValue;
        this.orangeCheckStatus = taskRuleExecutionRecord.orangeCheckStatus;
        this.redWarningCheckType = taskRuleExecutionRecord.redWarningCheckType;
        this.redWarningCheckExpression = taskRuleExecutionRecord.redWarningCheckExpression;
        this.redWarningMinValue = taskRuleExecutionRecord.redWarningMinValue;
        this.redWarningMaxValue = taskRuleExecutionRecord.redWarningMaxValue;
        this.redCheckStatus = taskRuleExecutionRecord.redCheckStatus;
        this.checkThresholdUnit = taskRuleExecutionRecord.checkThresholdUnit;
        this.filing = taskRuleExecutionRecord.filing;
        this.createTime = taskRuleExecutionRecord.createTime;
        this.resultString = taskRuleExecutionRecord.resultString;
    }
}
