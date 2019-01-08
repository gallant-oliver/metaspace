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
 * @date 2019/1/7 20:54
 */
package io.zeta.metaspace.model.dataquality;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/7 20:54
 */
public class SystemRule {
    private String systemRuleId;
    private String ruleName;
    private String ruleInfo;
    private List<Integer> ruleAllowCheckType;
    private String ruleCheckThresholdUnit;

    public String getSystemRuleId() {
        return systemRuleId;
    }

    public void setSystemRuleId(String systemRuleId) {
        this.systemRuleId = systemRuleId;
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

    public List<Integer> getRuleAllowCheckType() {
        return ruleAllowCheckType;
    }

    public void setRuleAllowCheckType(List<Integer> ruleAllowCheckType) {
        this.ruleAllowCheckType = ruleAllowCheckType;
    }

    public String getRuleCheckThresholdUnit() {
        return ruleCheckThresholdUnit;
    }

    public void setRuleCheckThresholdUnit(String ruleCheckThresholdUnit) {
        this.ruleCheckThresholdUnit = ruleCheckThresholdUnit;
    }
}
