package io.zeta.metaspace.model.dataquality;

import java.util.List;

public class Report {
    public static class ReportRule {
        private int ruleType;
        private String ruleName;
        private String ruleInfo;
        private String ruleColumnName;
        private int ruleColumnType;
        private int ruleCheckType;
        private int ruleCheckExpression;
        private List<Double> ruleCheckThreshold;
        private String ruleCheckThresholdUnit;
        private Double reportRuleValue;
        private int reportRuleStatus;
        private int ruleResultId;

        public int getRuleResultId() {
            return ruleResultId;
        }

        public void setRuleResultId(int ruleResultId) {
            this.ruleResultId = ruleResultId;
        }

        public int getRuleColumnType() {
            return ruleColumnType;
        }

        public void setRuleColumnType(int ruleColumnType) {
            this.ruleColumnType = ruleColumnType;
        }

        public int getRuleType() {
            return ruleType;
        }

        public void setRuleType(int ruleType) {
            this.ruleType = ruleType;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public String getRuleColumnName() {
            return ruleColumnName;
        }

        public void setRuleColumnName(String ruleColumnName) {
            this.ruleColumnName = ruleColumnName;
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

        public String getRuleCheckThresholdUnit() {
            return ruleCheckThresholdUnit;
        }

        public String getRuleInfo() {
            return ruleInfo;
        }

        public void setRuleInfo(String ruleInfo) {
            this.ruleInfo = ruleInfo;
        }

        public List<Double> getRuleCheckThreshold() {
            return ruleCheckThreshold;
        }

        public void setRuleCheckThreshold(List<Double> ruleCheckThreshold) {
            this.ruleCheckThreshold = ruleCheckThreshold;
        }

        public Double getReportRuleValue() {
            return reportRuleValue;
        }

        public void setReportRuleValue(Double reportRuleValue) {
            this.reportRuleValue = reportRuleValue;
        }

        public void setRuleCheckThresholdUnit(String ruleCheckThresholdUnit) {
            this.ruleCheckThresholdUnit = ruleCheckThresholdUnit;
        }

        public int getReportRuleStatus() {
            return reportRuleStatus;
        }

        public void setReportRuleStatus(int reportRuleStatus) {
            this.reportRuleStatus = reportRuleStatus;
        }
    }
    private String reportId;
    private String reportName;
    private String source;
    private String templateName;
    private String buildType;
    private String periodCron;
    private String orangeAlerts;
    private String redAlerts;
    private String reportProduceDate;
    private List<ReportRule> rules;

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

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getPeriodCron() {
        return periodCron;
    }

    public void setPeriodCron(String periodCron) {
        this.periodCron = periodCron;
    }

    public String getOrangeAlerts() {
        return orangeAlerts;
    }

    public void setOrangeAlerts(String orangeAlerts) {
        this.orangeAlerts = orangeAlerts;
    }

    public String getRedAlerts() {
        return redAlerts;
    }

    public void setRedAlerts(String redAlerts) {
        this.redAlerts = redAlerts;
    }

    public String getReportProduceDate() {
        return reportProduceDate;
    }

    public void setReportProduceDate(String reportProduceDate) {
        this.reportProduceDate = reportProduceDate;
    }

    public List<ReportRule> getRules() {
        return rules;
    }

    public void setRules(List<ReportRule> rules) {
        this.rules = rules;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
