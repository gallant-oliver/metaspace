package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsResultPO;

public interface RequirementsResultMapper {

    int deleteByPrimaryKey(String guid);

    int insert(RequirementsResultPO record);

    int insertSelective(RequirementsResultPO record);

    RequirementsResultPO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsResultPO record);

    int updateByPrimaryKey(RequirementsResultPO record);
}