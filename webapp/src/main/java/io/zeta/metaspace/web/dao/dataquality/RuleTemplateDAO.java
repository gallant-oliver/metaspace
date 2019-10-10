package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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


}
