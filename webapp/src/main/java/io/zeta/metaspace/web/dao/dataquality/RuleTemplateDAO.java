package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.Report2RuleTemplate;
import io.zeta.metaspace.model.dataquality2.ReportArchivedPath;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RuleParameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public interface RuleTemplateDAO {
    
    @Select({" select count(1) from data_quality_rule_template where delete=false and rule_type=#{ruleType} and tenantid=#{tenantId}"})
    long countByCategoryId(@Param("ruleType") String ruleType, @Param("tenantId") String tenantId);
    
    @Select({
            "<script>",
            "SELECT r.id,",
            "       r.name,",
            "       r.scope,",
            "       r.unit,",
            "       r.description,",
            "       r.delete,",
            "       r.create_time,",
            "       r.rule_type,",
            "       r.code,",
            "       r.enable,",
            "       r.sql,",
            "       r.type,",
            "       d.number         AS data_standard_code,",
            "       d.name           AS data_standard_name,",
            "       COUNT(*) OVER () AS total ",
            "FROM data_quality_rule_template r",
            "        LEFT JOIN data_standard d",
            "                   ON d.number = r.data_standard_code",
            "                       AND d.tenantid = #{tenantId}",
            "                       AND d.delete = FALSE",
            "                       AND d.version = 0",
            "where r.rule_type=#{ruleType} ",
            "and r.tenantid=#{tenantId} ",
            "and r.delete=false",
            " <if test='params.enable != null'>",
            "   and r.enable=#{params.enable}  ",
            " </if>",
            " <if test='params.limit != null and params.limit != -1'>",
            " limit #{params.limit} offset #{params.offset}",
            " </if>",
            " </script>"
    })
    List<RuleTemplate> getRuleTemplateByCategoryId(@Param("ruleType") String ruleType,
                                                   @Param("params") RuleParameters params,
                                                   @Param("tenantId") String tenantId);

    @Select({
            "<script>",
            "SELECT r.id,",
            "       r.name,",
            "       r.scope,",
            "       r.unit,",
            "       r.description,",
            "       r.delete,",
            "       r.create_time,",
            "       r.rule_type,",
            "       r.code,",
            "       r.enable,",
            "       r.sql,",
            "       r.type,",
            "       d.number         AS data_standard_code,",
            "       d.name           AS data_standard_name,",
            "       COUNT(*) OVER () AS total ",
            "FROM data_standard d ",
            "        LEFT JOIN data_quality_rule_template r",
            "                   ON d.number = r.data_standard_code",
            "                       AND d.tenantid = r.tenantId",
            "                       AND d.delete = FALSE",
            "                       AND d.version = 0",
            "where d.id=#{dataStandardId} ",
            "and r.delete=false",
            " <if test='params.limit != null and params.limit >= 0 and params.offset != null and params.offset >= 0 '>",
            " limit #{params.limit} offset #{params.offset}",
            " </if>",
            " </script>"
    })
    List<RuleTemplate> getRuleTemplateByDataStandardId(@Param("dataStandardId") String dataStandardId,
                                                       @Param("params") RuleParameters params);
    
    @Select({
            "<script>",
            "SELECT r.id,",
            "       r.name,",
            "       r.scope,",
            "       r.unit,",
            "       r.description,",
            "       r.delete,",
            "       r.create_time,",
            "       r.rule_type,",
            "       r.code,",
            "       r.enable,",
            "       r.sql,",
            "       r.type,",
            "       d.number         AS data_standard_code,",
            "       d.name           AS data_standard_name,",
            "       COUNT(*) OVER () AS total ",
            "FROM data_quality_rule_template r",
            "        LEFT JOIN data_standard d",
            "                   ON d.number = r.data_standard_code",
            "                       AND d.tenantid = #{tenantId}",
            "                       AND d.delete = FALSE ",
            "                       AND d.version = 0",
            " where r.tenantid=#{tenantId} and r.delete=false ",
            "<if test=\"params.query != null and params.query!=''\">",
            " and (r.name like concat('%',#{params.query},'%')  ESCAPE '/' or r.description like concat('%',#{params.query},'%')  ESCAPE '/'  or r.id like concat('%',#{params.query},'%')  ESCAPE '/' ) ",
            "<if test='params.enable != null'>",
            " and r.enable=#{params.enable} ",
            "</if>",
            "</if>",
            "<if test='params.limit != null and params.limit != -1'>",
            " limit #{params.limit} offset #{params.offset}",
            "</if>",
            " </script>"
    })
    List<RuleTemplate> searchRuleTemplate(@Param("params") RuleParameters params,
                                          @Param("tenantId") String tenantId) throws SQLException;
    
    @Insert({" <script>",
            " insert into report2ruletemplate(rule_template_id,data_quality_execute_id,creator,create_time)values",
            " <foreach collection='ruleTypeList' item='ruleTypeId' index='index'  separator=','>",
            " (#{ruleTypeId},#{executeId},#{creator},#{createTime})",
            " </foreach>",
            " </script>"})
    int addReport2RuleTemplate(@Param("executeId") String executeId, @Param("ruleTypeList") List<String> ruleTypeList, @Param("creator") String creator, @Param("createTime") Timestamp createTime);
    
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
    List<Report2RuleTemplate> getReportByRuleType(@Param("templateId") String templateId, @Param("params") Parameters params, @Param("tenantId") String tenantId);

    /**
     * 获取任务归档路径列表
     * @param executeIds 任务列表id
     * @param tenantId 当前执行的租户id
     * @return 任务归档路径实体
     */
    @Select({" <script>",
            " select A.*,",
                    " data_quality_rule_template.name AS ruleTemplateName, ",
                    " data_quality_rule_template.rule_type AS ruleType,",
                    " ( ",
                    " case  ",
                    "  when rule_type = 'rule_1' then concat('表体积/',name) ",
                    "  when rule_type = 'rule_2' then concat('空值校验/',name) ",
                    "  when rule_type = 'rule_3' then concat('唯一值校验/',name) ",
                    "  when rule_type = 'rule_4' then concat('重复值校验/',name) ",
                    "  when rule_type = 'rule_5' then concat('数值型校验/',name) ",
                    "  when rule_type = 'rule_6' then concat('一致性校验/',name) ",
                    "  else '其他' end ",
                    " ) AS archivedPath ",
                    " FROM ( ",
                    " SELECT ",
                    "  rule_template_id AS ruleTemplateId, ",
                    "  data_quality_execute_id AS executeId, ",
                    "  report2ruletemplate.creator AS creatorId, ",
                    "  report2ruletemplate.create_time AS createTime ",
                    " FROM ",
                    "  report2ruletemplate ",
                    " JOIN data_quality_task_execute ON data_quality_task_execute.ID = report2ruletemplate.data_quality_execute_id  ",
                    " WHERE ",
                    "  data_quality_execute_id in ",
                    " <foreach collection='executeIds' item='executeId' index='index'  separator=',' open='(' close=')' >",
                    "   #{executeId}",
                    " </foreach>",
                    " )A JOIN data_quality_rule_template ON data_quality_rule_template.ID = A.ruleTemplateId AND tenantid =#{tenantId} ",
            " </script>"})
    List<ReportArchivedPath> getArchivedPathByExecuteId(@Param("executeIds") List<String> executeIds, @Param("tenantId") String tenantId);
}
