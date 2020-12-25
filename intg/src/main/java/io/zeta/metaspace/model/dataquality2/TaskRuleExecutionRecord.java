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

import lombok.Data;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/1 18:21
 */
@Data
public class TaskRuleExecutionRecord {
    private Integer objectType;
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
}
