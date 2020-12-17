package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.Report2RuleTemplate;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RuleParameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

public interface RuleTemplateDAO {


    @Select({" select count(1) from data_quality_rule_template where delete=false and rule_type=#{ruleType} and tenantid=#{tenantId}"})
    public long countByCategoryId(@Param("ruleType") String ruleType,@Param("tenantId")String tenantId);

    @Select({"<script>",
             "select id,name,scope,unit,description,delete,create_time as createTime,rule_type as ruleType,code,",
             " count(*)over() as total",
             " from data_quality_rule_template",
             " where rule_type=#{ruleType} and tenantid=#{tenantId} " +
             " <if test='params.enable != null'>",
             " and enable=#{params.enable}  ",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<RuleTemplate> getRuleTemplateByCategoryId(@Param("ruleType")String ruleType, @Param("params") RuleParameters params,@Param("tenantId")String tenantId);


    @Select({"<script>",
             " select count(*)over() total,id,name,scope,unit,description,delete,rule_type as ruleType,create_time as createTime,code from data_quality_rule_template" ,
             " where tenantid=#{tenantId} " +
             "<if test=\"params.query != null and params.query!=''\">",
             " and (name like '%${params.query}%' ESCAPE '/' or description like '%${params.query}%' ESCAPE '/' )  " +
             " <if test='params.enable != null'>",
             " and enable=#{params.enable} ",
             " </if>",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<RuleTemplate> searchRuleTemplate(@Param("params") RuleParameters params,@Param("tenantId")String tenantId);

    @Insert({" <script>",
             " insert into report2ruletemplate(rule_template_id,data_quality_execute_id,creator,create_time)values",
             " <foreach collection='ruleTypeList' item='ruleTypeId' index='index'  separator=','>",
             " (#{ruleTypeId},#{executeId},#{creator},#{createTime})",
             " </foreach>",
             " </script>"})
    public int addReport2RuleTemplate(@Param("executeId")String executeId, @Param("ruleTypeList")List<String> ruleTypeList, @Param("creator")String creator, @Param("createTime")Timestamp createTime);

    @Select({" <script>",
            " select count(*)over() total,c.*,users.username as creatorName,ROW_NUMBER () OVER (ORDER BY createTime desc) AS number from",
            " (select b.*,data_quality_task.name as taskName,data_quality_task.delete as delete from",
            " (select a.*,data_quality_task_execute.task_id as taskId from ",
            " (select rule_template_id as ruleTemplateId,data_quality_execute_id as executeId,",
            " report2ruletemplate.creator as creatorId,report2ruletemplate.create_time as createTime,rule_type as ruleTypeId",
            " from report2ruletemplate join data_quality_rule_template on data_quality_rule_template.id=report2ruletemplate.rule_template_id",
            " where rule_template_id=#{templateId} and tenantid=#{tenantId}) a",
            " join data_quality_task_execute",
            " on data_quality_task_execute.id=a.executeId)b",
            " join data_quality_task on data_quality_task.id=b.taskId where data_quality_task.tenantid=#{tenantId}) c",
            " join users on users.account=c.creatorId",
            " order by createTime desc",
            " <if test='params.limit != null and params.limit != -1'>",
            " limit #{params.limit} offset #{params.offset}",
            " </if>",
            " </script>"})
    public List<Report2RuleTemplate> getReportByRuleType(@Param("templateId")String templateId, @Param("params") Parameters params,@Param("tenantId")String tenantId);
}
