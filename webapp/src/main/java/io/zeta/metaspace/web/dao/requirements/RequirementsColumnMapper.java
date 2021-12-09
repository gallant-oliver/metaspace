package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsColumnPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface RequirementsColumnMapper {
    int deleteByPrimaryKey(String guid);
    
    int insert(RequirementsColumnPO record);
    
    void batchInsert(@Param("list") List<RequirementsColumnPO> pos);
    
    RequirementsColumnPO selectByPrimaryKey(String guid);
    
    int updateByPrimaryKeySelective(RequirementsColumnPO record);
    
    int updateByPrimaryKey(RequirementsColumnPO record);
    
    void deleteByRequirementId(String requirementId);
    
    List<RequirementsColumnPO> getByRequirementId(String requirementId);
    
}