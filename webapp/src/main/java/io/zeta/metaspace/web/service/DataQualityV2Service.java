package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.result.ReportResult;
import io.zeta.metaspace.model.result.TableColumnRules;
import io.zeta.metaspace.model.result.TemplateResult;
import io.zeta.metaspace.web.dao.DataQualityV2DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.PathParam;
import java.sql.SQLException;
import java.util.List;

@Service
public class DataQualityV2Service {
    @Autowired
    private DataQualityV2DAO dataQualityV2DAO;
    public List<TemplateResult> getTemplates (String tableId) throws SQLException {
        List<TemplateResult> templateResults = dataQualityV2DAO.getTemplateResults(tableId);
        return  templateResults;
    };
    public List<ReportResult> getReports(String templateId,int offect, int limit) throws SQLException {
        List<ReportResult> reports = dataQualityV2DAO.getReports(templateId, offect, limit);
        return reports;
    }
    public Report getReport(String reportId) throws SQLException {
        List<Report> reports = dataQualityV2DAO.getReport(reportId);
        for (Report report : reports) {
            List<Report.ReportRule> reportRule = dataQualityV2DAO.getReportRule(reportId);
            for (Report.ReportRule rule : reportRule) {
                int ruleResultId = rule.getRuleResultId();
                List<Double> reportThresholdValue =  dataQualityV2DAO.getReportThresholdValue(ruleResultId);
                rule.setRuleCheckThreshold(reportThresholdValue);
            }
            report.setRules(reportRule);
        }
        return reports.get(0);
    }
    public TableColumnRules getRules( int tableId,  int buildType) throws SQLException {
        TableColumnRules tableColumnRules = new TableColumnRules();
        dataQualityV2DAO.getSystemRules(buildType,1);
        return tableColumnRules;
    }
}
