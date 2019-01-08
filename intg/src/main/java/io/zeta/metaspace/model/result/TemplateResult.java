package io.zeta.metaspace.model.result;

import io.zeta.metaspace.model.dataquality.UserRule;

import java.util.List;

public class TemplateResult {
    private String templateId;
    private int buildType;
    private String periodCron;
    private String templateName;
    private String templateStatus;
    private String startTime;
    private int tableRulesNum;
    private int columnRulesNum;
    private int orangeAlerts;
    private int redAlerts;
    private String reportId;
    private String reportName;

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

    public String getTemplateStatus() {
        return templateStatus;
    }

    public void setTemplateStatus(String templateStatus) {
        this.templateStatus = templateStatus;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
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

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
}
