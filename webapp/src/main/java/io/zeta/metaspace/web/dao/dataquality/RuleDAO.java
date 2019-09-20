package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface RuleDAO {

    @Insert(" insert into data_quality_rule(id,rule_template_id,name,code,category_id,enable,description,check_type,check_expression_type,check_threshold_min_value,check_threshold_max_value,creator,create_time,update_time,delete,check_threshold_unit,scope) " +
            " values(#{id},#{ruleTemplateId},#{name},#{code},#{categoryId},#{enable},#{description},#{checkType},#{checkExpressionType},#{checkThresholdMinValue},#{checkThresholdMaxValue},#{creator},#{createTime},#{updateTime},#{delete},#{unit},#{scope})")
    public int insert(Rule rule);

    @Select("select unit from data_quality_rule_template where id=#{ruleTemplateId}")
    public String getRuleTemplateUnit(@Param("ruleTemplateId")String ruleTemplateId);

    @Update(" update data_quality_rule set " +
            " rule_template_id=#{ruleTemplateId},name=#{name},code=#{code},category_id=#{categoryId},enable=#{enable},description=#{description},check_type=#{checkType},check_expression_type=#{checkExpressionType},check_threshold_min_value=#{checkThresholdMinValue},check_threshold_max_value=#{checkThresholdMaxValue},update_time=#{updateTime}" +
            " where id=#{id}")
    public int update(Rule rule);

    @Select({" select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete,a.check_threshold_unit as unit" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and id=#{id}"})
    public Rule getById(@Param("id") String id);

    @Select({" select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and a.code = #{code} "})
    public List<Rule> getByCode(@Param("code") String code);

    @Select("select enable from data_quality_rule where id=#{id}")
    public Boolean getEnableStatusById(@Param("id") String id);

    @Select("update data_quality_rule set delete=true where id=#{id}")
    public void deleteById(@Param("id") String id);

    @Insert({" <script>",
             " update data_quality_rule set delete=true where id in ",
             " <foreach collection='idList' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public void deleteByIdList(@Param("idList") List<String> idList);


    @Select({"<script>",
             "select c.*, data_quality_rule_template.rule_type as ruleType from",
             " (select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete,a.check_threshold_unit as unit" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and a.category_id=#{categoryId}) c",
             " join data_quality_rule_template on c.ruleTemplateId=data_quality_rule_template.id",
             " order by createTime desc",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<Rule> queryByCatetoryId(@Param("categoryId") String categoryId, @Param("params") Parameters params);


    @Select({"<script>",
             " select count(1) from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and a.category_id=#{categoryId} ",
             " </script>"})
    public long countByByCatetoryId(@Param("categoryId") String categoryId);

    @Select({"<script>",
             " select c.*, data_quality_rule_template.rule_type as ruleType from",
             " (select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (name like '%${params.query}%' ESCAPE '/' or code like '%${params.query}%' ESCAPE '/' ) ",
             " </if>",
             " )c join data_quality_rule_template on c.ruleTemplateId=data_quality_rule_template.id",
             " order by createTime desc",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<Rule> search(@Param("params") Parameters params);

    @Select({"<script>",
             " select count(1) from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false ",
             " <if test=\"query != null and query!=''\">",
             " and (name like '%${query}%' ESCAPE '/' or code like '%${query}%' ESCAPE '/' ) ",
             " </if>",
             " </script>"})
    public long countBySearch(@Param("query") String query);

    @Select("select count(*) from data_quality_sub_task_rule where ruleId=#{id}")
    public int getRuleUsedCount(@Param("id") String guid);

    @Update("update data_quality_rule set enable=#{status} where id=#{id}")
    public int updateRuleStatus(@Param("id") String guid, @Param("status") Boolean status);

    @Select("select id,name,scope,unit,description,delete,rule_type as ruleType from data_quality_rule_template")
    public List<RuleTemplate> getAllRuleTemplateList();

    @Select("select count(*) from data_quality_rule where category_id=#{categoryId} and delete=false")
    public Integer getCategoryObjectCount(@Param("categoryId") String guid);

    @Select("select name from category where guid=#{categoryId}")
    public String getCategoryName(@Param("categoryId") String guid);

}
