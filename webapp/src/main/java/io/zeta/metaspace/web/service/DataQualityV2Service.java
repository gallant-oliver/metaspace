package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dataquality.DataType;
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
import java.util.ArrayList;
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
        List<TableColumnRules.ColumnsRule> columnsRules = new ArrayList<>();
        List<TableColumnRules.SystemRule> tableSystemRules = dataQualityV2DAO.getTableSystemRules(RuleType.TABLE.code, buildType);
        addCheckRules(tableSystemRules);
        ColumnQuery columnQuery = new ColumnQuery();
        columnQuery.setGuid(tableId);
        List<Column> columns = metadataService.getColumnInfoById(columnQuery, true);
        for (Column column : columns) {
            TableColumnRules.ColumnsRule columnsRule = new TableColumnRules.ColumnsRule();
            String columnId = column.getColumnId();
            String columnName = column.getColumnName();
            String type = column.getType();
            columnsRule.setRuleColumnId(columnId);
            columnsRule.setRuleColumnName(columnName);
            columnsRule.setRuleColumnType(type);
            DataType datatype = getDatatype(type);
            List<TableColumnRules.SystemRule> columnSystemRules = dataQualityV2DAO.getColumnSystemRules(RuleType.COLUMN.code, datatype.getCode(), buildType);
            addCheckRules(columnSystemRules);
            columnsRule.setColumnRules(columnSystemRules);
            columnsRules.add(columnsRule);
        }
        tableColumnRules.setTableRules(tableSystemRules);
        tableColumnRules.setColumnsRules(columnsRules);
        return tableColumnRules;
}

    private void addCheckRules(List<TableColumnRules.SystemRule> tableSystemRules) throws SQLException {
        for (TableColumnRules.SystemRule tableSystemRule : tableSystemRules) {
            int ruleId = tableSystemRule.getRuleId();
            List<Integer> checktypes = dataQualityV2DAO.getChecktypes(ruleId);
            tableSystemRule.setRuleAllowCheckType(checktypes);
        }
    }

    private DataType getDatatype(String type) {
        ArrayList<String> numeric = new ArrayList<>();
        numeric.add("tinyint");
        numeric.add("smallint");
        numeric.add("int");
        numeric.add("integer");
        numeric.add("bigint");
        numeric.add("float");
        numeric.add("double");
        numeric.add("double precision");
        numeric.add("decimal");
        numeric.add("numeric");
        if (numeric.contains(type))
            return DataType.NUMERIC;

        return DataType.UNNUMERIC;
    }

}
