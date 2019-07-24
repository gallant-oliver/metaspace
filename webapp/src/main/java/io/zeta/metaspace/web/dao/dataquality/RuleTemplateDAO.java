package io.zeta.metaspace.web.dao.dataquality;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RuleTemplateDAO {


    @Select({" select count(1) from data_quality_rule_template where delete=false and category_id=#{categoryId} "})
    public long countByCategoryId(@Param("categoryId") String categoryId);

}
