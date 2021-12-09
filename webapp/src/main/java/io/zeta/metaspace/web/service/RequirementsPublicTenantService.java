package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.requirements.RequirementsApiMapper;
import io.zeta.metaspace.web.dao.requirements.RequirementsDatabaseMapper;
import io.zeta.metaspace.web.dao.requirements.RequirementsMapper;
import io.zeta.metaspace.web.dao.requirements.RequirementsMqMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
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

    @Autowired
    private RequirementsApiMapper requirementsApiMapper;

    @Autowired
    private RequirementsDatabaseMapper requirementsDatabaseMapper;

    @Autowired
    private RequirementsMqMapper requirementsMqMapper;

    @Autowired
    private RequirementsService requirementsService;
    
    
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

    /**
     * 需求下发
     *
     * @param
     * @throws
     */
    public void grant(String requirementId) {
        RequirementsPO requirementsPO = new RequirementsPO();
        // 1、待下发  2、已下发（待处理）  3、已处理（未反馈） 4、已反馈  -1、退回
        requirementsPO.setStatus(2);
        requirementsPO.setGuid(requirementId);
        requirementsMapper.updateByPrimaryKeySelective(requirementsPO);
    }

    public FeedbackResultDTO getFeedbackResult(String requirementId, Integer resourceType) {
        FeedbackResultDTO result = requirementsService.getFeedbackResult(requirementId, resourceType);
        // 查询处理结果
        result.setHandle(null);

        return result;
    }

    /**
     * 删除需求
     *
     * @param
     * @throws
     */
    public void deleteRequirements(List<String> guids) {
        requirementsMapper.deleteByGuids(guids);
    }
}
