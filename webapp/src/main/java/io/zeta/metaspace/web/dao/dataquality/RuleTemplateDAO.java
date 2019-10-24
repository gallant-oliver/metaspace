package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.Report2RuleType;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

public interface RuleTemplateDAO {


    @Select({" select count(1) from data_quality_rule_template where delete=false and rule_type=#{ruleType} "})
    public long countByCategoryId(@Param("ruleType") Integer ruleType);

    @Select({"<script>",
             "select id,name,scope,unit,description,delete,create_time as createTime,rule_type as ruleType,",
             " count(*)over() as total",
             " from data_quality_rule_template",
             " where rule_type=#{ruleType}",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<RuleTemplate> getRuleTemplateByCategoryId(@Param("ruleType")Integer ruleType, @Param("params") Parameters params);


    @Select({"<script>",
             " select count(*)over() total,id,name,scope,unit,description,delete,rule_type as ruleType,create_time as createTime from data_quality_rule_template" ,
             " <if test=\"params.query != null and params.query!=''\">",
             " where (name like '%${params.query}%' ESCAPE '/' or description like '%${params.query}%' ESCAPE '/' ) ",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<RuleTemplate> searchRuleTemplate(@Param("params") Parameters params);

    @Insert({" <script>",
             " insert into report2ruletype(rule_type_id,data_quality_execute_id,creator,create_time)values",
             " <foreach collection='ruleTypeList' item='ruleTypeId' index='index'  separator=','>",
             " (#{ruleTypeId},#{executeId},#{creator},#{createTime})",
             " </foreach>",
             " </script>"})
    public int addReport2RuleType(@Param("executeId")String executeId, @Param("ruleTypeList")List<String> ruleTypeList, @Param("creator")String creator, @Param("createTime")Timestamp createTime);

    @Select({" <script>",
            " select count(*)over() total,b.*,users.username as creatorName,ROW_NUMBER () OVER (ORDER BY createTime desc) AS number from",
            " (select a.*,data_quality_task.name as taskName from",
            " (select report2ruletype.rule_type_id as ruleTypeId,report2ruletype.data_quality_execute_id as executeId,report2ruletype.creator as creatorId,",
            " report2ruletype.create_time as createTime,data_quality_task_execute.task_id as taskId from report2ruletype join data_quality_task_execute",
            " on data_quality_task_execute.id=report2ruletype.data_quality_execute_id where report2ruletype.rule_type_id='0') a",
            " join data_quality_task on data_quality_task.id=a.taskId) b",
            " join users on users.account=b.creatorId",
            " order by createTime desc",
            " <if test='params.limit != null and params.limit != -1'>",
            " limit #{params.limit} offset #{params.offset}",
            " </if>",
            " </script>"})
    public List<Report2RuleType> getReportByRuleType(@Param("ruleType")String ruleType,@Param("params") Parameters params);
}
