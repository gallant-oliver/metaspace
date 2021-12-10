package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsColumnPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface RequirementsColumnMapper {
    int deleteByPrimaryKey(String guid);

    int insert(RequirementsColumnPO record);

    int insertSelective(RequirementsColumnPO record);

    RequirementsColumnPO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsColumnPO record);

    int updateByPrimaryKey(RequirementsColumnPO record);

    List<RequirementsColumnPO> selectByRequirementId(@Param("requirementId")String requirementId);
}