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
 * @date 2019/1/7 20:58
 */
package io.zeta.metaspace.model.dataquality;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/7 20:58
 */
public class UserRule {
    private String ruleId;
    private String ruleName;
    private String ruleInfo;
    private int ruleType;
    private String ruleColumnName;
    private String ruleColumnType;
    private int ruleCheckType;
    private int ruleCheckExpression;
    private long ruleCheckThreshold;
    private String ruleCheckThresholdUnit;

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

    public String getRuleInfo() {
        return ruleInfo;
    }

    public void setRuleInfo(String ruleInfo) {
        this.ruleInfo = ruleInfo;
    }

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleColumnName() {
        return ruleColumnName;
    }

    public void setRuleColumnName(String ruleColumnName) {
        this.ruleColumnName = ruleColumnName;
    }

    public String getRuleColumnType() {
        return ruleColumnType;
    }

    public void setRuleColumnType(String ruleColumnType) {
        this.ruleColumnType = ruleColumnType;
    }

    public int getRuleCheckType() {
        return ruleCheckType;
    }

    public void setRuleCheckType(int ruleCheckType) {
        this.ruleCheckType = ruleCheckType;
    }

    public int getRuleCheckExpression() {
        return ruleCheckExpression;
    }

    public void setRuleCheckExpression(int ruleCheckExpression) {
        this.ruleCheckExpression = ruleCheckExpression;
    }

    public long getRuleCheckThreshold() {
        return ruleCheckThreshold;
    }

    public void setRuleCheckThreshold(long ruleCheckThreshold) {
        this.ruleCheckThreshold = ruleCheckThreshold;
    }

    public String getRuleCheckThresholdUnit() {
        return ruleCheckThresholdUnit;
    }

    public void setRuleCheckThresholdUnit(String ruleCheckThresholdUnit) {
        this.ruleCheckThresholdUnit = ruleCheckThresholdUnit;
    }
}
