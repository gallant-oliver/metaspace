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
    @Select("select report.templateid templateid,report.orangealerts, report.redalerts, report.reportid,report.reportname,report.alert,temp.buildtype buildtype,temp.periodcron periodcron,temp.templatename templatename,temp.templatestatus,temp.starttime,temp.tablerulesnum,temp.columnrulesnum,MAX(reportproducedate) OVER (PARTITION BY report.templateid)\n" +
            "from\n" +
            "(select templateid,buildtype,periodcron,templatename,templatestatus,starttime,tablerulesnum,columnrulesnum\n" +
            "from template \n" +
            "where tableid = #{tableId}) as temp,report \n" +
            "where report.templateid=temp.templateid")
    public List<TemplateResult> getTemplateResults( String tableId) throws SQLException;
    @Select("select reportid,reportname,source,templatename,periodcron,buildtype,reportproducedate,redalerts,orangealerts from report where reportid = #{reportId}")
    public List<Report> getReport(String reportId) throws SQLException;
    @Select("select ruletype,rulename,ruleinfo,rulecolumnname,rulecolumntype,rulechecktype,rulecheckexpression,rulecheckthresholdunit,reportrulevalue,reportrulestatus,ruleid from report_ruleresult where reportid = #{reportId}")
    public List<Report.ReportRule> getReportRule(String reportId) throws SQLException;
    @Select("select report_threshold_value from report_threshold_value where ruleid = #{ruleResultId} order by report_threshold_value asc")
    public List<Double> getReportThresholdValue(String ruleResultId) throws SQLException;
    @Select("select reportid,reportname,orangealerts,redalerts,reportproducedate from report where templateid = #{templateId} orderby reportproducedate desc limit #{offect},#{limit}")
    public List<ReportResult> getReports(String templateId,int offect,int limit) throws SQLException;
    @Select("select systemrule.ruleid,rulename,ruleinfo,ruletype,rulecheckthresholdunit from systemrule,rule2datatype,rule2buildtype where systemrule.ruleid=rule2datatype.ruleid  and systemrule.ruleid=rule2buildtype.ruleid and buildtype=#{buildtype} and datatype=#{datatype} and ruletype=#{ruletype}")
    public List<TableColumnRules.SystemRule> getColumnSystemRules(int ruleType,int dataType,int buildType) throws SQLException;
    @Select("select systemrule.ruleid,rulename,ruleinfo,ruletype,rulecheckthresholdunit from systemrule,rule2buildtype where systemrule.ruleid=rule2buildtype.ruleid and buildtype=#{buildtype} and ruletype=#{ruletype}")
    public List<TableColumnRules.SystemRule> getTableSystemRules(int ruleType,int buildType) throws SQLException;
    @Select("select checktype from rule2checktype where ruleid = #{ruleId}")
    public List<Integer> getChecktypes(int ruleId) throws SQLException;

}
