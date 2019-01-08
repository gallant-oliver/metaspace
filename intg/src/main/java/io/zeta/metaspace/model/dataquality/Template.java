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
    private List<UserRule> rules;
    private String templateStatus;
    private String startTime;
    private int tableRulesNum;
    private int columnRulesNum;
    private int orangeAlerts;
    private int redAlerts;

    public List<UserRule> getRules() {
        return rules;
    }

    public void setRules(List<UserRule> rules) {
        this.rules = rules;
    }

    public String getTemplateStatus() {
        return templateStatus;
    }

    public void setTemplateStatus(String templateStatus) {
        this.templateStatus = templateStatus;
    }

    public int getTableRulesNum() {
        return tableRulesNum;
    }

    public void setTableRulesNum(int tableRulesNum) {
        this.tableRulesNum = tableRulesNum;
    }

    public int getColumnRulesNum() {
        return columnRulesNum;
    }

    public void setColumnRulesNum(int columnRulesNum) {
        this.columnRulesNum = columnRulesNum;
    }

    public int getOrangeAlerts() {
        return orangeAlerts;
    }

    public void setOrangeAlerts(int orangeAlerts) {
        this.orangeAlerts = orangeAlerts;
    }

    public int getRedAlerts() {
        return redAlerts;
    }

    public void setRedAlerts(int redAlerts) {
        this.redAlerts = redAlerts;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
