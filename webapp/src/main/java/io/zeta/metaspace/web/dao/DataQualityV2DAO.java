package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.result.TemplateResult;
import org.apache.ibatis.annotations.Select;

import java.sql.SQLException;
import java.util.List;

public interface DataQualityV2DAO {
    //report template
    @Select("select tb.templateid templateid,tb.orangealerts orangealerts, tb.redalerts redalerts, tb.reportid reportid,tb.reportname reportname,template.buildtype buildtype,template.periodcron periodcron,template.templatename templatename,template.templatestatus templatestatus,template.starttime starttime,template.tablerulesnum tablerulesnum,template.columnrulesnum columnrulesnum from (select templateid,orangealerts,redalerts,reportid,reportname,MAX(reportproducedate) OVER (PARTITION BY templateid) from report) as tb,template where tb.templateid = template.templateid")
    public List<TemplateResult> getTemplateResults( String tableId) throws SQLException;
    @Select("select * from report")
    public List<Report> getReport(String reportId) throws SQLException;

}
