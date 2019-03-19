package io.zeta.metaspace.model.result;

public class ReportResult {
    private String reportId;
    private String reportName;
    private String orangeAlerts;
    private String redAlerts;
    private String reportProduceDate;

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
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

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}
