package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.RequireListParam;
import io.zeta.metaspace.model.dto.requirements.RequirementsListDTO;
import io.zeta.metaspace.model.dto.requirements.ResourceDTO;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.TenantDAO;
import io.zeta.metaspace.web.dao.requirements.RequirementsMapper;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.util.AdminUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequirementsPublicTenantService {

    @Autowired
    private RequirementsMapper requirementsMapper;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private TenantDAO tenantDAO;


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
     * 需求管理列表
     * @param requireListParam
     * @return
     */
    public PageResult getListByCreatorPage(RequireListParam requireListParam) {
        PageResult pageResult = new PageResult();
        List<RequirementsListDTO> requirementsListDTOList = new ArrayList<>();
        try {
            User user = AdminUtils.getUserData();
            List<RequirementsPO> requirementsPOList = requirementsMapper.selectListByCreatorPage(user.getUserId(), requireListParam);
            if (CollectionUtils.isEmpty(requirementsPOList)) {
                pageResult.setTotalSize(0);
                pageResult.setCurrentSize(0);
                pageResult.setOffset(0);
                pageResult.setLists(requirementsListDTOList);
                return pageResult;
            }
            String tenantId = requirementsPOList.get(0).getTenantId();
            String name = tenantDAO.selectNameById(tenantId);

            List<String> categoryList = new ArrayList<>();
            requirementsPOList.stream().forEach(requirementsPO -> categoryList.add(requirementsPO.getBusinessCategoryId()));
            Set<CategoryEntityV2> categoryEntityV2s = categoryDAO.selectPathByGuidAndCategoryType(categoryList, tenantId, CommonConstant.BUSINESS_CATEGORY_TYPE);
            Map<String, String> map = categoryEntityV2s.stream().collect(Collectors.toMap(CategoryEntityV2::getGuid, CategoryEntityV2::getPath));
            for (RequirementsPO requirementsPO : requirementsPOList) {
                RequirementsListDTO requirementsListDTO = new RequirementsListDTO();
                BeanUtils.copyProperties(requirementsPO, requirementsListDTO);
                requirementsListDTO.setResourceTypeName(ResourceType.getValue(requirementsListDTO.getResourceType()));
                if (requirementsListDTO.getStatus().equals(1)) {
                    requirementsListDTO.setStatusName("待下发");
                } else if ("2,3".contains(String.valueOf(requirementsListDTO.getStatus()))) {
                    requirementsListDTO.setStatusName("已下发");
                } else {
                    requirementsListDTO.setStatusName("已反馈");
                }
                String path = map.get(requirementsListDTO.getBusinessCategoryId());
                if (StringUtils.isNotBlank(path)) {
                    requirementsListDTO.setCategoryPath(name + "/" + path);
                }
                requirementsListDTOList.add(requirementsListDTO);
            }
            pageResult.setTotalSize(requirementsListDTOList.get(0).getTotal());
            pageResult.setCurrentSize(requirementsListDTOList.size());
            pageResult.setOffset(requireListParam.getOffset());
            pageResult.setLists(requirementsListDTOList);
        } catch (Exception e) {
            log.error("getListByCreatorPage  exception is {}", e);
        }
        return pageResult;
    }

}
