package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.result.ReportResult;
import io.zeta.metaspace.model.result.TableColumnRules;
import io.zeta.metaspace.model.result.TemplateResult;
import org.apache.ibatis.annotations.Select;

import java.sql.SQLException;
import java.util.List;

public interface DataQualityV2DAO {
    //report template
    @Select("select tb.templateid templateid,tb.orangealerts orangealerts, tb.redalerts redalerts, tb.reportid reportid,tb.reportname reportname,template.buildtype buildtype,template.periodcron periodcron,template.templatename templatename,template.templatestatus templatestatus,template.starttime starttime,template.tablerulesnum tablerulesnum,template.columnrulesnum columnrulesnum from (select templateid,orangealerts,redalerts,reportid,reportname,MAX(reportproducedate) OVER (PARTITION BY templateid) from report) as tb,template where tb.templateid = template.templateid and template.tableid = #{tableId}")
    public List<TemplateResult> getTemplateResults( String tableId) throws SQLException;
    @Select("select reportid,reportname,source,templatename,periodcron,buildtype,reportproducedate,redalerts,orangealerts from report where reportid = #{reportId}")
    public List<Report> getReport(String reportId) throws SQLException;
    @Select("select ruletype,rulename,ruleinfo,rulecolumnname,rulecolumntype,rulechecktype,rulecheckexpression,rulecheckthresholdunit,reportrulevalue,reportrulestatus,ruleresultid from report_ruleresult where reportid = #{reportId}")
    public List<Report.ReportRule> getReportRule(String reportId) throws SQLException;
    @Select("select report_threshold_value from report_threshold_value where ruleresultid = #{ruleResultId} order by report_threshold_value asc")
    public List<Double> getReportThresholdValue(int ruleResultId) throws SQLException;
    @Select("select reportid,reportname,orangealerts,redalerts,reportproducedate from report where templateid = #{templateId} orderby reportproducedate desc limit #{offect},#{limit}")
    public List<ReportResult> getReports(String templateId,int offect,int limit) throws SQLException;
    @Select("select rulename,ruleinfo,ruleid from systemrule where buildtype=#{buildType} and ruleType=#{ruleType}")
    public List<TableColumnRules.SystemRule> getSystemRules(int buildType, int ruleType) throws SQLException;
    @Select("select datatype from rule2datatype where ruleid = #{ruleId}")
    public List<Integer> getDatatypes(int ruleId) throws SQLException;
    @Select("select checktype from rule2checktype where ruleid = #{ruleId}")
    public List<Integer> getRuletypes(int ruleId) throws SQLException;
}
