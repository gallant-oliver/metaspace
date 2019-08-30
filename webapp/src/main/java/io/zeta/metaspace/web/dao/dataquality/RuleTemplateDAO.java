package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RuleTemplateDAO {


    @Select({" select count(1) from data_quality_rule_template where delete=false and category_id=#{categoryId} "})
    public long countByCategoryId(@Param("categoryId") String categoryId);

    @Select("select id,name,scope,unit,description,delete,category_id as categoryId,create_time as createTime,type as ruleType from data_quality_rule_template where category_id=#{categoryId}")
    public List<RuleTemplate> getRuleTemplateByCategoryId(@Param("categoryId")String categoryId);


    @Select({"<script>",
             " select id,name,scope,unit,description,delete,category_id as categoryId,create_time as createTime,type as ruleType from data_quality_rule_template" ,
             " <if test=\"params.query != null and params.query!=''\">",
             " where (name like '%${params.query}%' ESCAPE '/' or description like '%${params.query}%' ESCAPE '/' ) ",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<RuleTemplate> searchRuleTemplate(@Param("params") Parameters params);

    @Select({"<script>",
             " select count(1) from data_quality_rule_template" ,
             " <if test=\"params.query != null and params.query!=''\">",
             " where (name like '%${params.query}%' ESCAPE '/' or description like '%${params.query}%' ESCAPE '/' ) ",
             " </if>",
             " </script>"})
    public long coutSearchRuleTemplate(@Param("params") Parameters params);

}
