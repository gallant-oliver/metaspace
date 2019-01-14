package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.dataquality.RuleType;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.ColumnQuery;
import io.zeta.metaspace.model.result.ReportResult;
import io.zeta.metaspace.model.result.TableColumnRules;
import io.zeta.metaspace.model.result.TemplateResult;
import io.zeta.metaspace.web.dao.DataQualityV2DAO;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class DataQualityV2Service {
    @Autowired
    private DataQualityV2DAO dataQualityV2DAO;
    @Autowired
    private MetaDataService metadataService;

    public List<TemplateResult> getTemplates(String tableId) throws SQLException {
        List<TemplateResult> templateResults = dataQualityV2DAO.getTemplateResults(tableId);
        return templateResults;
    }

    ;

    public List<ReportResult> getReports(String templateId, int offect, int limit) throws SQLException {
        List<ReportResult> reports = dataQualityV2DAO.getReports(templateId, offect, limit);
        return reports;
    }

    public Report getReport(String reportId) throws SQLException {
        List<Report> reports = dataQualityV2DAO.getReport(reportId);
        for (Report report : reports) {
            List<Report.ReportRule> reportRule = dataQualityV2DAO.getReportRule(reportId);
            for (Report.ReportRule rule : reportRule) {
                String ruleResultId = rule.getRuleResultId();
                List<Double> reportThresholdValue = dataQualityV2DAO.getReportThresholdValue(ruleResultId);
                rule.setRuleCheckThreshold(reportThresholdValue);
            }
            report.setRules(reportRule);
        }
        return reports.get(0);
    }

    public TableColumnRules getRules(String tableId, int buildType) throws SQLException, AtlasBaseException {
        TableColumnRules tableColumnRules = new TableColumnRules();
        ColumnQuery columnQuery = new ColumnQuery();
        columnQuery.setGuid(tableId);
        List<Column> columns = metadataService.getColumnInfoById(columnQuery, true);

        List<TableColumnRules.SystemRule> systemRules = dataQualityV2DAO.getSystemRules(RuleType.TABLE.code);
        for (TableColumnRules.SystemRule systemRule : systemRules) {
            TableColumnRules.SystemRule tableRule = new TableColumnRules.SystemRule();
            int systemRuleId = systemRule.getSystemRuleId();
            List<Integer> buildtypes = dataQualityV2DAO.getBuildtypes(systemRuleId);
            if (buildtypes.contains(buildType)) {
                List<Integer> checktypes = dataQualityV2DAO.getChecktypes(systemRuleId);
                systemRule.setRuleAllowCheckType(checktypes);
            }
        }
        return tableColumnRules;
    }

}
