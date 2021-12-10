package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.enums.ResourceState;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementIssuedPO;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.po.requirements.ResourcePO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.dao.TenantDAO;
import io.zeta.metaspace.web.dao.requirements.RequirementsMapper;
import io.zeta.metaspace.web.dao.sourceinfo.SourceInfoDAO;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequirementsPublicTenantService {

    @Autowired
    private RequirementsMapper requirementsMapper;
    @Autowired
    RequirementsService requirementsService;
    @Autowired
    private TableDAO tableDAO;
    @Autowired
    private ColumnDAO columnDAO;
    @Autowired
    private SourceInfoDAO sourceInfoDAO;

    @Autowired
    private RequirementColumnService columnService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private TenantDAO tenantDAO;

    /**
     * 查询数据表关联的已反馈需求下的资源
     */
    public PageResult<ResourceDTO> pagedResource(String tableId, Parameters parameters) {
        List<ResourcePO> resources = requirementsMapper.pagedResources(tableId, parameters);

        if (CollectionUtils.isEmpty(resources)) {
            return PageResult.empty(ResourceDTO.class);
        }

        // 查询数据表名
        String tableName = tableDAO.getTableNameByTableGuid(tableId);
        Assert.isTrue(StringUtils.isNotBlank(tableName), "数据表ID无效");

        long total = resources.get(0).getTotal();
        List<ResourceDTO> list = resources.stream()
                .map(po -> {
                    ResourceDTO dto = new ResourceDTO();
                    BeanUtils.copyProperties(po, dto, "type", "state");
                    // 处理枚举
                    dto.setState(ResourceState.parseByCode(po.getState()));
                    dto.setType(ResourceType.parseByCode(po.getType()));
                    return dto;
                })
                .peek(dto -> dto.setDataTableName(tableName))
                .collect(Collectors.toList());
        return new PageResult<>(total, list);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createdResource(RequirementDTO dto) {
        // 校验字段
        verifyRequirementDTO(dto);
        Assert.isTrue(StringUtils.isNotBlank(dto.getTenantId()), "需求关联的数据表所在的租户的ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(dto.getBusinessId()), "业务ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(dto.getTableId()), "需求关联的数据表ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(dto.getSourceId()), "数据源ID不能为空");
        Assert.isTrue(!isRequirementNameExist(dto.getName(), dto.getTenantId()), "需求名称已经存在!");
        Assert.isTrue(!isRequirementNumExist(dto.getNum(), dto.getTenantId()), "需求编码已经存在!");

        RequirementsPO po = RequirementsPO.builder()
                .guid(UUID.randomUUID().toString())
                .resourceType(dto.getResourceType().getCode())
                .aimingField(
                        JsonUtils.toJson(
                                Objects.isNull(dto.getTargetFieldIDs())
                                        ? Collections.emptyList()
                                        : dto.getTargetFieldIDs()))
                .status(1)
                .delete(0)
                .creator(AdminUtils.getUserData().getUserId())
                .createTime(DateUtils.currentTimestamp())
                .updateTime(DateUtils.currentTimestamp())
                .build();
        BeanUtils.copyProperties(dto, po, "guid", "resourceType");

        requirementsMapper.insert(po);

        // 存储过滤条件
        List<FilterConditionDTO> filterConditions = dto.getFilterConditions();
        columnService.batchInsert(po.getGuid(), po.getTableId(), filterConditions);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editedResource(RequirementDTO dto) {
        Assert.isTrue(StringUtils.isNotBlank(dto.getGuid()), "需求ID不能为空");
        verifyRequirementDTO(dto);

        RequirementsPO oldPo = requirementsMapper.getRequirementById(dto.getGuid());
        Assert.notNull(oldPo, "需求ID无效");

        Assert.isTrue(Objects.equals(oldPo.getName(), dto.getName())
                        || isRequirementNameExist(dto.getName(), oldPo.getTenantId()),
                "需求名称已经存在!");
        Assert.isTrue(Objects.equals(oldPo.getNum(), dto.getNum())
                        || isRequirementNameExist(dto.getNum(), oldPo.getTenantId()),
                "需求编码已经存在!");

        RequirementsPO newPo = RequirementsPO.builder()
                .guid(oldPo.getGuid())
                .resourceType(dto.getResourceType().getCode())
                .aimingField(
                        JsonUtils.toJson(
                                Objects.isNull(dto.getTargetFieldIDs())
                                        ? Collections.emptyList()
                                        : dto.getTargetFieldIDs()))
                .status(oldPo.getStatus())
                .updateTime(DateUtils.currentTimestamp())
                .build();
        BeanUtils.copyProperties(dto, newPo, "guid", "resourceType");

        requirementsMapper.updateByPrimaryKey(newPo);

        //  修改需求的过滤条件字段
        columnService.batchUpdate(oldPo.getGuid(), oldPo.getTableId(), dto.getFilterConditions());
    }

    private void verifyRequirementDTO(RequirementDTO dto) {
        Assert.isTrue(StringUtils.isNotBlank(dto.getName()), "需求名称不能为空");
        Assert.isTrue(StringUtils.isNotBlank(dto.getNum()), "需求编码不能为空");
        Assert.notNull(dto.getResourceType(), "资源类型不能为空");
    }

    private boolean isRequirementNameExist(String name, String tenantId) {
        return requirementsMapper.countRequirementByName(name, tenantId) > 0;
    }

    private boolean isRequirementNumExist(String num, String tenantId) {
        return requirementsMapper.countRequirementByName(num, tenantId) > 0;
    }

    /**
     * 根据数据表ID查询数据表的所有列.
     */
    public List<RequirementColumnDTO> queryColumnsByTableId(String tableId) {
        return columnDAO.getColumnInfoListByTableGuid(tableId)
                .stream()
                .map(v -> RequirementColumnDTO.builder()
                        .columnId(v.getColumnId())
                        .columnName(v.getColumnName())
                        .build())
                .collect(Collectors.toList());
    }

    public RequirementIssuedDTO queryIssuedInfo(String tableId, String sourceId) {
        RequirementIssuedPO po =
                Optional.ofNullable(sourceInfoDAO.queryIssuedInfo(tableId, sourceId))
                        .orElseThrow(() -> new AtlasBaseException(
                                String.format("数据表ID %s,数据源ID %s 无效,查询数据为空.",
                                        tableId,
                                        sourceId)));

        RequirementIssuedDTO dot = RequirementIssuedDTO.builder()
                .technicalCatalog(CategoryRelationUtils.getPath(po.getCategoryId(), po.getTenantId()))
                .tenantPermission(isOwnerHasTenantPermission(po.getTenantId(), po.getBusinessOwner()))
                .build();
        BeanUtils.copyProperties(po, dot);
        return dot;
    }

    /**
     * 判断业务负责人是否有租户权限
     */
    private boolean isOwnerHasTenantPermission(String tenantId, String businessOwner) {
        SecuritySearch search = new SecuritySearch();
        search.setTenantId(tenantId);
        search.setUserName(businessOwner);

        List<UserAndModule> userInfos = tenantService.getUserAndModule(0, -1, search).getLists();

        if (CollectionUtils.isEmpty(userInfos)) {
            return false;
        }

        return userInfos.parallelStream().anyMatch(v -> Objects.equals(v.getUserName(), businessOwner));
    }

    /**
     * 需求管理列表
     *
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

    /**
     * 查询需求处理结果及反馈结果
     */
    public FeedbackDetailBaseDTO getDetailBase(String id, Integer type) {
        Assert.isTrue(StringUtils.isNotBlank(id), "需求id为空");
        Assert.isTrue(null != type, "资源类型为空");
        FeedbackDetailBaseDTO baseDTO = new FeedbackDetailBaseDTO();
        DealDetailDTO dealDetailDTO = requirementsService.getDealDetail(id);
        baseDTO.setResult(dealDetailDTO.getResult());
        baseDTO.setUser(dealDetailDTO.getUser());
        baseDTO.setDescription(dealDetailDTO.getDescription());
        //TODO 待补充查询反馈结果
        return baseDTO;
    }
}
