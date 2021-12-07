package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsMqPO;

public interface RequirementsMqMapper {

    int deleteByPrimaryKey(String guid);

    int insert(RequirementsMqPO record);

    int insertSelective(RequirementsMqPO record);

    RequirementsMqPO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsMqPO record);

    int updateByPrimaryKey(RequirementsMqPO record);
}