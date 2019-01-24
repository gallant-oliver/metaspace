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
 * @date 2019/1/8 19:42
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.model.result.ReportResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.DELETE;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/8 19:42
 */
public interface DataQualityDAO {

    @Insert("insert into template(templateId,tableId,buildType,periodCron,templateName,tableRulesNum,columnRulesNum,source)" +
            "values(#{templateId},#{tableId},#{buildType},#{periodCron},#{templateName},#{tableRulesNum},#{columnRulesNum},#{source})")
    public int insertTemplate(Template template) throws SQLException;

    @Insert("insert into template_userrule(ruleId,ruleName,ruleInfo,ruleColumnName,ruleColumnType,ruleCheckType,ruleCheckExpression," +
            "ruleCheckThresholdUnit,templateId,dataType,ruleType,systemRuleId)values(#{ruleId},#{ruleName},#{ruleInfo},#{ruleColumnName},#{ruleColumnType}," +
            "#{ruleCheckType},#{ruleCheckExpression},#{ruleCheckThresholdUnit},#{templateId},#{dataType},#{ruleType},#{systemRuleId})")
    public int insertUserRule(UserRule rule) throws SQLException;

    @Insert("update template set startTime=#{startTime} where templateId=#{templateId}")
    public int updateTemplateStartTime(@Param("templateId") String templateId, @Param("startTime") String startTime);

    @Insert("insert into template_userrule2threshold(thresholdValue,ruleId)values(#{thresholdValue},#{ruleId})")
    public int insertTemplateThreshold(@Param("thresholdValue") double thresholdValue,@Param("ruleId") String ruleId) throws SQLException;

    @Insert("insert into report_userrule2threshold(thresholdValue,ruleId)values(#{thresholdValue},#{ruleId})")
    public int insertReportThreshold(@Param("thresholdValue") double thresholdValue,@Param("ruleId") String ruleId) throws SQLException;

    @Update("update template set templateStatus=#{templateStatus} where templateId=#{templateId}")
    public int updateTemplateStatus(@Param("templateStatus") int templateStatus, @Param("templateId") String templateId) throws SQLException;

    @Update("update template set templateName=#{templateName},buildType=#{buildType},periodCron=#{periodCron}, " +
            "tableRulesNum=#{tableRulesNum},columnRulesNum=#{columnRulesNum} where templateId=#{templateId}")
    public int updateTemplate(Template template) throws SQLException;

    @Select("select ruleId from template_userrule where templateId=#{templateId}")
    public List<String> queryRuleIdByTemplateId(@Param("templateId") String templateId) throws SQLException;

    @Delete("delete from template where templateId=#{templateId}")
    public int delTemplate(@Param("templateId") String templateId) throws SQLException;

    @Delete("delete from template_userrule where templateId=#{templateId}")
    public int delRuleByTemplateId(@Param("templateId") String templateId) throws SQLException;

    @Delete("delete from template_userrule2threshold where ruleId=#{ruleId}")
    public int deleteThresholdByRuleId(@Param("ruleId") String ruleId) throws SQLException;

    @Select("select * from template where templateId=#{templateId}")
    public Template queryTemplateById(@Param("templateId") String templateId) throws SQLException;

    @Select("select * from template_userrule where templateId=#{templateId}")
    public List<UserRule> queryTemplateUserRuleById(@Param("templateId") String templateId) throws SQLException;

    @Select("select thresholdvalue from template_userrule2threshold where ruleId=#{ruleId} order by thresholdvalue asc")
    public List<Double> queryTemplateThresholdByRuleId(@Param("ruleId") String ruleId) throws SQLException;

    @Select("select thresholdvalue from report_userrule2threshold where ruleId=#{ruleId} order by thresholdvalue asc")
    public List<Double> queryReportThresholdByRuleId(@Param("ruleId") String ruleId) throws SQLException;


    @Insert("insert into report(reportId,reportName,templateId,templateName,periodCron,source,buildType,reportProduceDate)values(#{reportId}," +
            "#{reportName},#{templateId},#{templateName},#{periodCron},#{source},#{buildType},#{reportProduceDate})")
    public int insertReport(Report report);

    @Insert("insert into report_userrule(ruleId,templateRuleId,reportId,ruleType,ruleName,ruleInfo,ruleColumnName,ruleColumnType,ruleCheckType,ruleCheckExpression," +
            "ruleCheckThresholdUnit,reportRuleValue,reportRuleStatus,refValue)values(#{rule.ruleId},#{rule.templateRuleId},#{reportId},#{rule.ruleType},#{rule.ruleName},#{rule.ruleInfo},#{rule.ruleColumnName}," +
            "#{rule.ruleColumnType},#{rule.ruleCheckType},#{rule.ruleCheckExpression},#{rule.ruleCheckThresholdUnit},#{rule.reportRuleValue},#{rule.reportRuleStatus},#{rule.refValue})")
    public void insertRuleReport(@Param("reportId")String reportId, @Param("rule")Report.ReportRule rule) throws SQLException;

    @Select("select * from report where reportId=#{reportId}")
    public ReportResult getReportResult(@Param("reportId") String reportId);

    @Select("select * from report_userrule where reportId=#{reportId}")
    public List<Report.ReportRule> getReport(@Param("reportId") String reportId);

    @Select("select reportName from report where reportId=#{reportId}")
    public String getReportName(@Param("reportId") String reportId);

    @Select("select * from report_ruleresult where reportId=(select reportId from report where templateId=#{templateId})")
    public List<Report> getReportByTemplateId(@Param("templateId") String templateId);

    @Select("select source from template where templateId=#{templateId}")
    public String querySourceByTemplateId(@Param("templateId") String templateId);

    @Select("select refValue from report_userrule where templateRuleId=#{templateRuleId} and reportId in (select reportId from report where templateId=#{templateId} order by reportproducedate desc limit 1  offset 1)")
    public Double getLastTableRowNum(@Param("templateId") String templateId,@Param("templateRuleId") String templateRuleId);

    @Select("select refValue from report_userrule where templateRuleId=#{templateRuleId} and reportId in (select reportId from report where templateId=#{templateId} order by reportproducedate desc limit 1)")
    public long getLastValue(@Param("templateId") String templateId,@Param("templateRuleId") String templateRuleId);

    @Update("update report set (orangeAlerts,redAlerts) = ((select case when count(*)=null then 0 else count(*) end as re from report_userrule where reportid=#{reportId} and reportRuleStatus=1)," +
            "(select case when count(*)=null then 0 else count(*) end as re from report_userrule where reportid=#{reportId} and reportRuleStatus=2)) where reportid=#{reportId}")
    public int updateAlerts(@Param("reportId") String reportId);

    @Select("select periodCron from template where templateId=#{templateId}")
    public String getCronByTemplateId(@Param("templateId") String templateId);

    @Insert("insert into template2qrtz_job(templateId,qrtz_job)values(#{templateId},#{qrtz_job})")
    public int insertTemplate2Qrtz_Trigger(@Param("templateId") String templateId, @Param("qrtz_job") String qrtz_job);

    @Select("select * from template where templateId = (select templateId from template2qrtz_job where qrtz_job=#{qrtz_job})")
    public Template getTemplateByJob(@Param("qrtz_job") String qrtz_job);
}
