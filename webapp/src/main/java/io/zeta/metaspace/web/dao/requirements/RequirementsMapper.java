package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsPO;


public interface RequirementsMapper {

    int deleteByPrimaryKey(String guid);

    int insert(RequirementsPO record);

    int insertSelective(RequirementsPO record);

    RequirementsPO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsPO record);

    int updateByPrimaryKey(RequirementsPO record);
}