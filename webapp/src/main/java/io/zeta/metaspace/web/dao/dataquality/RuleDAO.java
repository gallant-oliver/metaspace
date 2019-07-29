package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RuleDAO {

    @Insert(" insert into data_quality_rule(id,rule_template_id,name,code,category_id,enable,description,check_type,check_expression_type,check_threshold_min_value,check_threshold_max_value,creator,create_time,update_time,delete) " +
            " values(#{id},#{ruleTemplateId},#{name},#{code},#{categoryId},#{enable},#{description},#{checkType},#{checkExpressionType},#{checkThresholdMinValue},#{checkThresholdMaxValue},#{creator},#{createTime},#{updateTime},#{delete})")
    public int insert(Rule rule);

    @Insert(" update data_quality_rule set " +
            " rule_template_id=#{ruleTemplateId},name=#{name},code=#{code},category_id=#{categoryId},enable=#{enable},description=#{description},check_type=#{checkType},check_expression_type=#{checkExpressionType},check_threshold_min_value=#{checkThresholdMinValue},check_threshold_max_value=#{checkThresholdMaxValue},update_time=#{updateTime}")
    public int update(Rule rule);

    @Select({" select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and id=#{id}"})
    public Rule getById(@Param("id") String id);

    @Select({" select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and a.code = #{code} "})
    public List<Rule> getByCode(@Param("code") String code);

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
             " select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and a.category_id=#{categoryId} ",
             " <if test='params != null'>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </if>",
             " </script>"})
    public List<Rule> queryByCatetoryId(@Param("categoryId") String categoryId, @Param("params") Parameters params);


    @Select({"<script>",
             " select count(1) from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false and a.category_id=#{categoryId} ",
             " </script>"})
    public long countByByCatetoryId(@Param("categoryId") String categoryId);

    @Select({"<script>",
             " select a.id,a.rule_template_id as ruleTemplateId,a.name,a.code,a.category_id as categoryId,a.enable,a.description,a.check_type as checkType,a.check_expression_type as checkExpressionType,a.check_threshold_min_value as checkThresholdMinValue,a.check_threshold_max_value as checkThresholdMaxValue,b.username as creator,a.create_time as createTime,a.update_time as updateTime,a.delete" ,
             " from data_quality_rule a inner join users b on a.creator=b.userid where a.delete=false ",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (name like '%${params.query}%' ESCAPE '/' or code like '%${params.query}%' ESCAPE '/' ) ",
             " </if>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
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

}
