package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.ResourceDTO;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.requirements.RequirementsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RequirementsPublicTenantService {
    
    @Autowired
    private RequirementsMapper requirementsMapper;
    
    
    /**
     * 查询数据表关联的已反馈需求下的资源
     */
    public PageResult<ResourceDTO> pagedResource(String tableId, Parameters parameters) {
        List<RequirementsPO> requirements = requirementsMapper.pagedAlreadyFeedbackRequirements(tableId, parameters);
        // Map<String,String>
        
        return null;
    }
}
