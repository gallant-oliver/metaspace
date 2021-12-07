package io.zeta.metaspace.web.dao.requirements;


import io.zeta.metaspace.model.po.requirements.RequirementsDatabasePO;

public interface RequirementsDatabaseMapper {

    int deleteByPrimaryKey(String guid);

    int insert(RequirementsDatabasePO record);

    int insertSelective(RequirementsDatabasePO record);

    RequirementsDatabasePO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsDatabasePO record);

    int updateByPrimaryKey(RequirementsDatabasePO record);
}