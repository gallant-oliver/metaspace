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
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.DELETE;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/8 19:42
 */
public interface DataQualityDAO {

    @Insert("insert into template(templateId,tableId,buildType,periodCron,templateName,tableRulesNum,columnRulesNum)" +
            "values(#{templateId},#{tableId},#{buildType},#{periodCron},#{templateName},#{tableRulesNum},#{columnRulesNum})")
    public int insertTemplate(Template template) throws SQLException;

    @Insert("insert into template_userrule(ruleId,ruleName,ruleInfo,ruleColumnName,ruleColumnType,ruleCheckType,ruleCheckExpression," +
            "ruleCheckThresholdUnit,templateId,dataType,ruleType)values(#{ruleId},#{ruleName},#{ruleInfo},#{ruleColumnName},#{ruleColumnType}," +
            "#{ruleCheckType},#{ruleCheckExpression},#{ruleCheckThresholdUnit},#{templateId},#{dataType},#{ruleType})")
    public int insertUserRule(UserRule rule) throws SQLException;

    @Insert("insert into template_userrule2threshold(thresholdValue,ruleId)values(#{thresholdValue},#{ruleId})")
    public int insertThreshold(@Param("thresholdValue") double thresholdValue,@Param("ruleId") String ruleId) throws SQLException;

    @Update("update template set templateStatus=#{templateStatus} where templateId=#{templateId}")
    public int updateTemplateStatus(@Param("templateStatus") int templateStatus, @Param("templateId") String templateId) throws SQLException;

    @Update("update template set templateName=#{templateName},buildType=#{buildType},periodCron=#{periodCron}, " +
            "tableRulesNum=#{tableRulesNum},columnRulesNum=#{columnRulesNum} where templateId=#{templateId}")
    public int updateTemplate(Template template) throws SQLException;

    @Update("update template_userrule set ruleName=#{ruleName},ruleInfo=#{ruleInfo},ruleColumnName=#{ruleColumnName},ruleColumnType=#{ruleColumnType}," +
            "ruleCheckType=#{ruleCheckType},ruleCheckExpression=#{ruleCheckExpression},ruleCheckThresholdUnit=#{ruleCheckThresholdUnit} where templateId=#{templateId}")
    public int updateUserRule(UserRule userRule) throws SQLException;

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

    @Insert("insert into report_ruleresult(ruleResultId,reportId,ruleType,ruleName,ruleInfo,ruleColumnName,ruleColumnType,ruleCheckType,ruleCheckExpression," +
            "ruleCheckThresholdUnit,reportRuleValue,reportRuleStatus)values(#{ruleResultId},#{reportId},#{ruleType},#{ruleName},#{ruleInfo},#{ruleColumnName}," +
            "#{ruleColumnType},#{ruleCheckType},#{ruleCheckExpression},#{ruleCheckThresholdUnit},#{reportRuleValue},#{reportRuleStatus})")
    public void insertRuleReport(Report.ReportRule rule) throws SQLException;

    @Select("select * from report where reportId=#{reportId}")
    public ReportResult getReportResult(@Param("reportId") String reportId);

    @Select("select * from report_userrule where reportId=#{reportId}")
    public List<Report.ReportRule> getReport(@Param("reportId") String reportId);

    @Select("select reportName from report where reportId=#{reportId}")
    public String getReportName(@Param("reportId") String reportId);

    @Select("select * from report_ruleresult where reportId=(select reportId from report where templateId=#{templateId})")
    public List<Report> getReportByTemplateId(@Param("templateId") String templateId);
}
