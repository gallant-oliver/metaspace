package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.DataTaskIdAndName;
import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface RuleDAO {

    @Insert(" insert into data_quality_rule_template(id,name,code,rule_type,description,creator,create_time,update_time,delete,scope,tenantId,sql,type,enable) " +
            " values(#{rule.id},#{rule.name},#{rule.code},#{rule.categoryId},#{rule.description},#{rule.creator},#{rule.createTime},#{rule.updateTime},#{rule.delete},2,#{tenantId},#{rule.sql},32,#{rule.enable})")
    public int insert(@Param("rule") Rule rule,@Param("tenantId")String tenantId);

    @Select("select unit from data_quality_rule_template where id=#{ruleTemplateId}")
    public String getRuleTemplateUnit(@Param("ruleTemplateId")String ruleTemplateId);

    @Update(" update data_quality_rule_template set " +
            " name=#{name},code=#{code},rule_type=#{categoryId},description=#{description},update_time=#{updateTime},sql=#{sql},enable=#{enable} " +
            " where id=#{id}")
    public int update(Rule rule);

    @Select({" select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete,a.check_threshold_unit as unit" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and id=#{id}"})
    public Rule getById(@Param("id") String id);

    @Select({" select a.id " ,
             " from data_quality_rule_template a where a.delete=false and a.code = #{code} and a.tenantid=#{tenantId}"})
    public List<Rule> getByCode(@Param("code") String code,@Param("tenantId") String tenantId);

    @Select({" select a.id " ,
             " from data_quality_rule_template a where a.delete=false and a.code = #{code} and a.tenantid=#{tenantId} and a.id!=#{id}"})
    public List<Rule> getByCodeV2(@Param("id") String id,@Param("code") String code,@Param("tenantId") String tenantId);

    @Select({" select a.id " ,
             " from data_quality_rule_template a where a.delete=false and a.name = #{name} and a.tenantid=#{tenantId}"})
    public List<Rule> getByName(@Param("name") String name,@Param("tenantId") String tenantId);

    @Select({" select a.id " ,
             " from data_quality_rule_template a where a.delete=false and a.name = #{name} and a.tenantid=#{tenantId} and a.id!=#{id}"})
    public List<Rule> getByNameV2(@Param("id") String id,@Param("name") String name,@Param("tenantId") String tenantId);

    @Select("select enable from data_quality_rule_template where id=#{id}")
    public Boolean getEnableStatusById(@Param("id") String id);

    @Select("update data_quality_rule_template set delete=true where id=#{id}")
    public void deleteById(@Param("id") String id);

    @Insert({" <script>",
             " update data_quality_rule_template set delete=true where id in ",
             " <foreach collection='idList' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public void deleteByIdList(@Param("idList") List<String> idList);


    @Select({"<script>",
             "select count(*)over() total,c.*, data_quality_rule_template.rule_type as ruleType from",
             " (select a.id,a.rule_template_id as ruleTemplateId,a.scope as scope,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete,a.check_threshold_unit as unit" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and a.category_id=#{categoryId} and a.tenantId=#{tenantId}) c",
             " join data_quality_rule_template on c.ruleTemplateId=data_quality_rule_template.id",
             " order by createTime desc",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<Rule> queryByCatetoryId(@Param("categoryId") String categoryId, @Param("params") Parameters params,@Param("tenantId")String tenantId);

    @Select({"<script>",
             " select count(*)over() total,c.*, data_quality_rule_template.rule_type as ruleType from",
             " (select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and a.tenantId=#{tenantId}",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (name like concat('%',#{params.query},'%') ESCAPE '/' or code like concat('%',#{params.query},'%') ESCAPE '/' ) ",
             " </if>",
             " )c join data_quality_rule_template on c.ruleTemplateId=data_quality_rule_template.id",
             " order by createTime desc",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<Rule> search(@Param("params") Parameters params,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select t.id,t.name from data_quality_task t join " +
            "(select s.task_id from data_quality_sub_task s join data_quality_sub_task_rule r on s.id=r.subtask_id where r.delete=false and r.ruleId in " +
            "<foreach collection='ids' item='id' index='index' separator=',' open='(' close=')'>" +
            "#{id}"+
            "</foreach>"+
            ") r " +
            " on r.task_id=t.id" +
            "</script>")
    public List<DataTaskIdAndName> getRuleUsed(@Param("ids") List<String> guids);

    @Update("update data_quality_rule_template set enable=#{status} where id=#{id} and tenantid=#{tenantId}")
    public int updateRuleStatus(@Param("id") String guid, @Param("status") Boolean status,@Param("tenantId")String tenantId);

    @Select("select id,name,scope,unit,description,delete,rule_type as ruleType from data_quality_rule_template")
    public List<RuleTemplate> getAllRuleTemplateList();

    @Select("select count(*) from data_quality_rule_template where rule_type=#{categoryId} and delete=false and tenantid=#{tenantId}")
    public Integer getCategoryObjectCount(@Param("categoryId") String guid,@Param("tenantId")String tenantId);

    @Select("select name from category where guid=#{categoryId} and tenantid=#{tenantId}")
    public String getCategoryName(@Param("categoryId") String guid,@Param("tenantId")String tenantId);

    @Select({" select name " ,
             " from data_quality_rule_template where delete=false and id=#{id} and tenantid = #{tenantId}"})
    public String getNameById(@Param("id") String id,@Param("tenantId") String tenantId);

    @Select({" select a.id,a.name,a.code,a.rule_type as categoryId,a.description,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete,a.unit as unit,type,scope" ,
             " from data_quality_rule_template a left join users b on a.creator=b.userid where a.delete=false and id=#{id} and tenantid=#{tenantId}"})
    public Rule getRuleTemplate(@Param("id") String id,@Param("tenantId")String tenantId);

    @Insert("<script>" +
            " insert into data_quality_rule_template(name,scope,unit,description,create_time,update_time,delete,id,rule_type,type,tenantid,creator,code,sql,enable) " +
            " values " +
            " <foreach collection='rules' item='rule' index='index' separator='),(' open='(' close=')'>" +
            " #{rule.name},#{rule.scope},#{rule.unit},#{rule.description},#{rule.createTime},#{rule.updateTime},#{rule.delete},#{rule.id},#{rule.categoryId},#{rule.type},#{tenantId},#{rule.creator},#{rule.code},#{rule.sql},#{rule.enable}"+
            " </foreach>"+
            "</script>")
    public int insertAll(@Param("rules") List<Rule> rules,@Param("tenantId")String tenantId);
}
