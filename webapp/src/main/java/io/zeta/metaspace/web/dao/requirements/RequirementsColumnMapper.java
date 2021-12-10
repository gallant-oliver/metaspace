package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsColumnPO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;


public interface RequirementsColumnMapper {
    
    RequirementsColumnPO selectByPrimaryKey(String guid);
    
    List<RequirementsColumnPO> getByRequirementId(String requirementId);
    
    int insert(RequirementsColumnPO record);
    
    void batchInsert(@Param("list") Collection<RequirementsColumnPO> pos);
    
    int updateByPrimaryKeySelective(RequirementsColumnPO record);
    
    int updateByPrimaryKey(RequirementsColumnPO record);

    List<RequirementsColumnPO> selectByRequirementId(@Param("requirementId")String requirementId);
    
    void batchUpdate(@Param("list") Collection<RequirementsColumnPO> toInsertSet);
    
    void deleteByRequirementId(String requirementId);
    
    int deleteByPrimaryKey(String guid);
    
    void batchDeleteByPrimaryKey(@Param("list") Collection<String> toDeleteIdsSet);
    
}