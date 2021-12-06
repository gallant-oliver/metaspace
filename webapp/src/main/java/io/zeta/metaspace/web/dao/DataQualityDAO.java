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
import io.zeta.metaspace.model.dataquality.ReportError;
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.model.result.ReportResult;
import io.zeta.metaspace.model.result.TableColumnRules;
import io.zeta.metaspace.model.result.TemplateResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.SQLException;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/8 19:42
 */
public interface DataQualityDAO {

    /**
     * 添加模板(Template)
     * @param template
     * @return
     * @throws SQLException
     */
    @Insert("insert into template(templateId,tableId,buildType,periodCron,templateName,tableRulesNum,columnRulesNum,source,generateTime,tenantid)" +
            "values(#{template.templateId},#{template.tableId},#{template.buildType},#{template.periodCron},#{template.templateName},#{template.tableRulesNum},#{template.columnRulesNum},#{template.source},#{template.generateTime},#{tenantId})")
    public int insertTemplate(@Param("templates") Template template, @Param("tenantId") String tenantId) throws SQLException;

    /**
     * 添加模板规则
     * @param rule
     * @return
     * @throws SQLException
     */
    @Insert("insert into template_userrule(ruleId,ruleName,ruleInfo,ruleColumnName,ruleColumnType,ruleCheckType,ruleCheckExpression," +
            "ruleCheckThresholdUnit,templateId,dataType,ruleType,systemRuleId,generateTime)values(#{ruleId},#{ruleName},#{ruleInfo},#{ruleColumnName},#{ruleColumnType}," +
            "#{ruleCheckType},#{ruleCheckExpression},#{ruleCheckThresholdUnit},#{templateId},#{dataType},#{ruleType},#{systemRuleId},#{generateTime})")
    public int insertUserRule(UserRule rule) throws SQLException;

    /**
     * 更新模板开启时间
     * @param templateId
     * @param startTime
     * @return
     */
    @Insert("update template set startTime=#{startTime} where templateId=#{templateId}")
    public int updateTemplateStartTime(@Param("templateId") String templateId, @Param("startTime") String startTime);

    @Select("select count(*) from template where templateName=#{templateName} and tenantId=#{tenantId} and templateId!=#{templateId}")
    public int countTemplateName(@Param("templateName") String templateName,@Param("templateId") String templateId,@Param("tenantId")String tenantId);

    /**
     * 保存模板规则对应阈值
     * @param thresholdValue
     * @param ruleId
     * @return
     * @throws SQLException
     */
    @Insert("insert into template_userrule2threshold(thresholdValue,ruleId)values(#{thresholdValue},#{ruleId})")
    public int insertTemplateRuleThreshold(@Param("thresholdValue") double thresholdValue,@Param("ruleId") String ruleId) throws SQLException;

    /**
     * 保存报告规则对应阈值
     * @param thresholdValue
     * @param ruleId
     * @return
     * @throws SQLException
     */
    @Insert("insert into report_userrule2threshold(thresholdValue,ruleId)values(#{thresholdValue},#{ruleId})")
    public int insertReportThreshold(@Param("thresholdValue") double thresholdValue,@Param("ruleId") String ruleId) throws SQLException;

    /**
     * 更新模板状态
     * @param templateStatus
     * @param templateId
     * @return
     * @throws SQLException
     */
    @Update("update template set templateStatus=#{templateStatus} where templateId=#{templateId}")
    public int updateTemplateStatus(@Param("templateStatus") int templateStatus, @Param("templateId") String templateId) throws SQLException;

    /**
     * 更新模板
     * @param template
     * @return
     * @throws SQLException
     */
    @Update("update template set templateName=#{templateName},buildType=#{buildType},periodCron=#{periodCron}, " +
            "tableRulesNum=#{tableRulesNum},columnRulesNum=#{columnRulesNum} where templateId=#{templateId}")
    public int updateTemplate(Template template) throws SQLException;

    /**
     * 获取模板对应规则Id
     * @param templateId
     * @return
     * @throws SQLException
     */
    @Select("select ruleId from template_userrule where templateId=#{templateId}")
    public List<String> queryRuleIdByTemplateId(@Param("templateId") String templateId) throws SQLException;

    /**
     * 删除模板
     * @param templateId
     * @return
     * @throws SQLException
     */
    @Delete("delete from template where templateId=#{templateId}")
    public int delTemplate(@Param("templateId") String templateId) throws SQLException;

    /**
     * 删除模板规则
     * @param templateId
     * @return
     * @throws SQLException
     */
    @Delete("delete from template_userrule where templateId=#{templateId}")
    public int delRuleByTemplateId(@Param("templateId") String templateId) throws SQLException;

    /**
     * 删除规则对应阈值
     * @param ruleId
     * @return
     * @throws SQLException
     */
    @Delete("delete from template_userrule2threshold where ruleId=#{ruleId}")
    public int deleteThresholdByRuleId(@Param("ruleId") String ruleId) throws SQLException;

    /**
     * 根据Id查询模板
     * @param templateId
     * @return
     * @throws SQLException
     */
    @Select("select * from template where templateId=#{templateId}")
    public Template queryTemplateById(@Param("templateId") String templateId) throws SQLException;

    /**
     * 查询模板对应规则
     * @param templateId
     * @return
     * @throws SQLException
     */
    @Select("select * from template_userrule where templateId=#{templateId} order by generateTime")
    public List<UserRule> queryTemplateUserRuleById(@Param("templateId") String templateId) throws SQLException;

    /**
     * 查询模板规则对应阈值
     * @param ruleId
     * @return
     * @throws SQLException
     */
    @Select("select thresholdvalue from template_userrule2threshold where ruleId=#{ruleId} order by thresholdvalue asc")
    public List<Double> queryTemplateThresholdByRuleId(@Param("ruleId") String ruleId) throws SQLException;

    /**
     * 查询报告规则对应阈值
     * @param ruleId
     * @return
     * @throws SQLException
     */
    @Select("select thresholdvalue from report_userrule2threshold where ruleId=#{ruleId} order by thresholdvalue asc")
    public List<Double> queryReportThresholdByRuleId(@Param("ruleId") String ruleId) throws SQLException;

    /**
     * 添加报告
     * @param report
     * @return
     */
    @Insert("insert into report(reportId,reportName,templateId,templateName,periodCron,source,buildType,reportProduceDate)values(#{reportId}," +
            "#{reportName},#{templateId},#{templateName},#{periodCron},#{source},#{buildType},#{reportProduceDate})")
    public int insertReport(Report report);

    /**
     * 插入报告规则结果
     * @param reportId
     * @param rule
     * @throws SQLException
     */
    @Insert("insert into report_userrule(ruleId,templateRuleId,reportId,ruleType,ruleName,ruleInfo,ruleColumnName,ruleColumnType,ruleCheckType,ruleCheckExpression," +
            "ruleCheckThresholdUnit,reportRuleValue,reportRuleStatus,refValue,generateTime)values(#{rule.ruleId},#{rule.templateRuleId},#{reportId},#{rule.ruleType},#{rule.ruleName},#{rule.ruleInfo},#{rule.ruleColumnName}," +
            "#{rule.ruleColumnType},#{rule.ruleCheckType},#{rule.ruleCheckExpression},#{rule.ruleCheckThresholdUnit},#{rule.reportRuleValue},#{rule.reportRuleStatus},#{rule.refValue},#{rule.generateTime})")
    public void insertRuleReport(@Param("reportId")String reportId, @Param("rule")Report.ReportRule rule) throws SQLException;

    /**
     * 根据Id查询报告
     * @param reportId
     * @return
     */
    @Select("select * from report where reportId=#{reportId}")
    public ReportResult getReportResult(@Param("reportId") String reportId);

    /**
     * 查询报告规则
     * @param reportId
     * @return
     */
    @Select("select * from report_userrule where reportId=#{reportId} order by generateTime")
    public List<Report.ReportRule> getReportRuleList(@Param("reportId") String reportId);

    /**
     * 查询报告名称
     * @param reportId
     * @return
     */
    @Select("select reportName from report where reportId=#{reportId}")
    public String getReportName(@Param("reportId") String reportId);

    /**
     * 查询模板报告列表
     * @param templateId
     * @return
     */
    @Select("select * from report_ruleresult where reportId=(select reportId from report where templateId=#{templateId})")
    public List<Report> getReportByTemplateId(@Param("templateId") String templateId);

    /**
     * 查询模板所属库表
     * @param templateId
     * @return
     */
    @Select("select source from template where templateId=#{templateId}")
    public String querySourceByTemplateId(@Param("templateId") String templateId);

    /**
     *
     * @param templateId
     * @param templateRuleId
     * @return
     */
    @Select("select refValue from report_userrule where templateRuleId=#{templateRuleId} and reportId in (select reportId from report where templateId=#{templateId} order by reportproducedate desc limit 1  offset 1)")
    public Double getLastTableRowNum(@Param("templateId") String templateId,@Param("templateRuleId") String templateRuleId);

    /**
     * 获取库中最新值
     * @param templateId
     * @param templateRuleId
     * @return
     */
    @Select("select refValue from report_userrule where templateRuleId=#{templateRuleId} and reportId in (select reportId from report where templateId=#{templateId} order by reportproducedate desc limit 1)")
    public Double getLastValue(@Param("templateId") String templateId,@Param("templateRuleId") String templateRuleId);

    /**
     * 更新报告告警数量
     * @param reportId
     * @return
     */
    @Update("update report set (orangeAlerts,redAlerts) = ((select case when count(*)=null then 0 else count(*) end as re from report_userrule where reportid=#{reportId} and reportRuleStatus=1)," +
            "(select case when count(*)=null then 0 else count(*) end as re from report_userrule where reportid=#{reportId} and reportRuleStatus=2)) where reportid=#{reportId}")
    public int updateAlerts(@Param("reportId") String reportId);

    /**
     * 更新报告告警状态
     * @param reportId
     * @param alert
     * @return
     */
    @Update("update report set alert=#{alert} where reportId=#{reportId}")
    public int updateAlertStatus(@Param("reportId") String reportId,@Param("alert") int alert);

    /**
     * 根据templateId查询模板定时周期
     * @param templateId
     * @return
     */
    @Select("select periodCron from template where templateId=#{templateId}")
    public String getCronByTemplateId(@Param("templateId") String templateId);

    /**
     * 插入桥接表，保存定时任务与TemplateId
     * @param templateId
     * @param jobName
     * @return
     */
    @Insert("insert into template2qrtz_job(templateId,qrtz_job)values(#{templateId},#{qrtz_job})")
    public int insertTemplate2QrtzTrigger(@Param("templateId") String templateId, @Param("qrtz_job") String jobName);

    @Delete("delete from template2qrtz_job where templateId=#{templateId}")
    public int deleteTemplate2QrtzByTemplateId(@Param("templateId") String templateId);

    /**
     * 根据quartzName查询Template
     * @param jobName
     * @return
     */
    @Select("select * from template where templateId = (select templateId from template2qrtz_job where qrtz_job=#{qrtz_job})")
    public Template getTemplateByJob(@Param("qrtz_job") String jobName);

    @Select("select qrtz_job from template2qrtz_job where templateId=#{templateId}")
    public String getJobByTemplateId(@Param("templateId") String templateId);

    @Update("update template set finishedPercent=#{finishedPercent} where templateId=#{templateId}")
    public Integer updateFinishedPercent(@Param("templateId") String templateId, @Param("finishedPercent") Float finishedPercent);

    @Select("select finishedPercent from template where templateId=#{templateId}")
    public Float getFinishedPercent(@Param("templateId") String templateId);

    @Select("select templateStatus from template where templateId=#{templateId}")
    public Integer getTemplateStatus(@Param("templateId") String templateId);

    @Select("select * from template where templateId=#{templateId}")
    public Template getTemplate(@Param("templateId") String templateId);

    @Insert("insert into report_error(errorId,templateId,reportId,ruleId,content,generateTime,retryCount)values(#{errorId},#{templateId},#{reportId},#{ruleId},#{content},#{generateTime},#{retryCount})")
    public int insertReportError(ReportError error);

    @Select("select * from (select reportproducedate,temp.templateid templateid,orangealerts, redalerts,reportid,reportname,alert,temp.buildtype buildtype,temp.periodcron periodcron,temp.templatename templatename,templatestatus,starttime,tablerulesnum,columnrulesnum,generatetime,MAX(reportproducedate) OVER (PARTITION BY report.templateid) maxtime\n" +
            "from\n" +
            "(select templateid,buildtype,periodcron,templatename,templatestatus,starttime,tablerulesnum,columnrulesnum,generatetime " +
            "from template \n" +
            "where tableid = #{tableId} and tenantid=#{tenantId} order by generatetime desc) as temp left join report \n" +
            "on report.templateid=temp.templateid) temp2re where maxtime=reportproducedate or reportproducedate is null order by generatetime desc")
    public List<TemplateResult> getTemplateResults(@Param("tableId") String tableId,@Param("tenantId") String tenantId) throws SQLException;
    @Select("select reportid,reportname,source,templatename,periodcron,buildtype,reportproducedate,redalerts,orangealerts from report where reportid = #{reportId}")
    public List<Report> getReport(String reportId) throws SQLException;
    @Select("select ruletype,rulename,ruleinfo,rulecolumnname,rulecolumntype,rulechecktype,rulecheckexpression,rulecheckthresholdunit,reportrulevalue,reportrulestatus,ruleid,generateTime from report_userrule where reportid = #{reportId} order by generateTime")
    public List<Report.ReportRule> getReportRule(String reportId) throws SQLException;
    @Select("select thresholdvalue from report_userrule2threshold where ruleid = #{ruleId} order by thresholdvalue asc")
    public List<Double> getReportThresholdValue(String ruleId) throws SQLException;


    @Select({"<script>",
             " select count(*)over() total,reportid,reportname,orangealerts,redalerts,reportproducedate from report,template",
             " where template.templateId=#{templateId} and template.tableId=#{tableId} and report.templateid = template.templateId order by reportproducedate desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<ReportResult> getReports(@Param("tableId") String tableId, @Param("templateId") String templateId,@Param("offset") int offset,@Param("limit") int limit) throws SQLException;


    @Select({"<script>",
             " select count(*)over() total,reportid,reportname,orangealerts,redalerts,reportproducedate from report,template",
             " where template.tableId=#{tableId} and report.templateid = template.templateId and tenantid=#{tenantId}order by reportproducedate desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<ReportResult> getReportsByTableGuid(@Param("tableId") String tableId,@Param("offset") int offset,@Param("limit") int limit,@Param("tenantId")String tenantId) throws SQLException;


    @Select("select systemrule.ruleid,rulename,ruleinfo,ruletype,rulecheckthresholdunit from systemrule,rule2datatype,rule2buildtype where systemrule.ruleid=rule2datatype.ruleid  and systemrule.ruleid=rule2buildtype.ruleid and buildtype=#{buildType} and datatype=#{dataType} and ruletype=#{ruleType} order by ruleid")
    public List<TableColumnRules.SystemRule> getColumnSystemRules(@Param("ruleType") int ruleType, @Param("dataType") int dataType, @Param("buildType") int buildType) throws SQLException;
    @Select("select systemrule.ruleid,rulename,ruleinfo,ruletype,rulecheckthresholdunit from systemrule,rule2buildtype where systemrule.ruleid=rule2buildtype.ruleid and buildtype=#{buildType} and ruletype=#{ruleType} order by ruleid")
    public List<TableColumnRules.SystemRule> getTableSystemRules(@Param("ruleType") int ruleType,@Param("buildType") int buildType) throws SQLException;
    @Select("select checktype from rule2checktype where ruleid = #{ruleId}")
    public List<Integer> getChecktypes(int ruleId) throws SQLException;

    @Select("select * from report_error where templateId=#{templateId} order by generateTime desc limit 1")
    public ReportError getLastError(@Param("templateId") String templateId);

    @Delete("delete from report_error where templateId=#{templateId}")
    public int deleteReportError(@Param("templateId") String templateId);
}
