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
 * @date 2019/1/7 21:03
 */
package io.zeta.metaspace.model.dataquality;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/7 21:03
 */
public class Template {

    private String tableId;
    private String templateId;
    private int buildType;
    private String periodCron;
    private String templateName;
    private int tableRuleNum;
    private int columnRuleNum;
    private List<UserRule> rules;

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public int getBuildType() {
        return buildType;
    }

    public void setBuildType(int buildType) {
        this.buildType = buildType;
    }

    public String getPeriodCron() {
        return periodCron;
    }

    public void setPeriodCron(String periodCron) {
        this.periodCron = periodCron;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public int getTableRuleNum() {
        return tableRuleNum;
    }

    public void setTableRuleNum(int tableRuleNum) {
        this.tableRuleNum = tableRuleNum;
    }

    public int getColumnRuleNum() {
        return columnRuleNum;
    }

    public void setColumnRuleNum(int columnRuleNum) {
        this.columnRuleNum = columnRuleNum;
    }

    public List<UserRule> getRules() {
        return rules;
    }

    public void setRules(List<UserRule> rules) {
        this.rules = rules;
    }
}
