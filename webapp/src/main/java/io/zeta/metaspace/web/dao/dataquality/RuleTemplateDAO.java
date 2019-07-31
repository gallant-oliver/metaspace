package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RuleTemplateDAO {


    @Select({" select count(1) from data_quality_rule_template where delete=false and category_id=#{categoryId} "})
    public long countByCategoryId(@Param("categoryId") String categoryId);

    @Select("select id,name,scope,unit,description,delete,category_id as categoryId from data_quality_rule_template where category_id=#{categoryId}")
    public List<RuleTemplate> getRuleTemplateByCategoryId(@Param("categoryId")String categoryId);

}
