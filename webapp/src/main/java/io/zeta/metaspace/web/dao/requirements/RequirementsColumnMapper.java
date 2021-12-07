package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsColumnPO;


public interface RequirementsColumnMapper {
    int deleteByPrimaryKey(String guid);

    int insert(RequirementsColumnPO record);

    int insertSelective(RequirementsColumnPO record);

    RequirementsColumnPO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsColumnPO record);

    int updateByPrimaryKey(RequirementsColumnPO record);
}