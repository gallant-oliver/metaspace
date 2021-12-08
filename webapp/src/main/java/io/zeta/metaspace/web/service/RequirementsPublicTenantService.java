package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.ResourceDTO;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.requirements.RequirementsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    
        if (CollectionUtils.isNotEmpty(requirements)) {
            return PageResult.empty(ResourceDTO.class);
        }
    
        List<ResourceDTO> resultList = requirements.stream()
                .map(requirement -> ResourceDTO.builder()
                        .requirementId(requirement.getGuid())
                        .version(requirement.getVersion())
                        .build()).collect(Collectors.toList());
    
        Set<String> requirementIds = resultList.stream()
                .map(ResourceDTO::getRequirementId)
                .collect(Collectors.toSet());
    
        // List<ResourcePO> resources =
    
    
        return null;
    }
}
