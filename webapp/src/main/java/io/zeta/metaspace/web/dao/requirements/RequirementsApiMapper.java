package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsApiPO;

public interface RequirementsApiMapper {

    int deleteByPrimaryKey(String guid);

    int insert(RequirementsApiPO record);

    int insertSelective(RequirementsApiPO record);

    RequirementsApiPO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsApiPO record);

    int updateByPrimaryKey(RequirementsApiPO record);
}