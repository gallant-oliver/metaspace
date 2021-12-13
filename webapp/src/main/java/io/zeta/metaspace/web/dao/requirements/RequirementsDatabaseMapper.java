package io.zeta.metaspace.web.dao.requirements;


import io.zeta.metaspace.model.dto.requirements.RequirementsDatabaseDetailDTO;
import io.zeta.metaspace.model.po.requirements.RequirementsDatabasePO;
import org.apache.ibatis.annotations.Param;

public interface RequirementsDatabaseMapper {

    int deleteByPrimaryKey(String guid);

    int insert(RequirementsDatabasePO record);

    int insertSelective(RequirementsDatabasePO record);

    RequirementsDatabasePO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsDatabasePO record);

    int updateByPrimaryKey(RequirementsDatabasePO record);

    RequirementsDatabaseDetailDTO selectByRequirementId(@Param("requirementId") String requirementId);
}