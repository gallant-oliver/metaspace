package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.enums.MessagePush;
import io.zeta.metaspace.model.enums.ResourceState;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementIssuedPO;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.po.requirements.ResourcePO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.Tenant;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.requirements.RequirementsMapper;
import io.zeta.metaspace.web.dao.sourceinfo.SourceInfoDAO;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.service.fileinfo.FileInfoService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.JsonUtils;
import javassist.compiler.ast.ASTList;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.zeta.metaspace.model.enums.MessagePush.NEED_AUDIT_START_MANAGER;

@Service
@Slf4j
public class RequirementsPublicTenantService {

    @Autowired
    private RequirementsMapper requirementsMapper;

    @Autowired
    private RequirementsService requirementsService;


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
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private FileInfoService fileInfoService;

    @Autowired
    ApproveGroupDAO approveGroupDAO;

    @Autowired
    MessageCenterService messageCenterService;

    @Autowired
    UserDAO userDAO;

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

    /**
     * 需求下发
     *
     * @param
     * @throws
     */
    @Transactional(rollbackFor = Exception.class)
    public void grant(String requirementId) {
        RequirementsPO requirementsPO = new RequirementsPO();
        // 1、待下发  2、已下发（待处理）  3、已处理（未反馈） 4、已反馈  -1、退回
        requirementsPO.setStatus(2);
        requirementsPO.setGuid(requirementId);
        requirementsMapper.updateByPrimaryKeySelective(requirementsPO);

        RequirementsPO result = requirementsMapper.getRequirementById(requirementId);
        String tableId = result.getTableId();
        String sourceId = result.getSourceId();
        RequirementIssuedPO po =
                Optional.ofNullable(sourceInfoDAO.queryIssuedInfo(tableId, sourceId))
                        .orElseThrow(() -> new AtlasBaseException(
                                String.format("数据表ID %s,数据源ID %s 无效,查询数据为空.",
                                        tableId,
                                        sourceId)));

        // 审核消息推送业务负责人
        String tenantId = result.getTenantId();
        if (po != null && StringUtils.isNotBlank(po.getUserId())){
            List<String> userEmailList = userDAO.getUsersEmailByIds(new ArrayList<>(Arrays.asList(po.getUserId())));
            MessageEntity message =  new MessageEntity(NEED_AUDIT_START_MANAGER.type, MessagePush.getFormattedMessageName(NEED_AUDIT_START_MANAGER.name, result.getName()), NEED_AUDIT_START_MANAGER.module);
            for (String userEmail : userEmailList) {
                message.setCreateUser(userEmail);
                messageCenterService.addMessage(message, tenantId);
            }
        }

    }

    /**
     * 需求反馈结果
     *
     * @param
     * @throws
     */
    public FeedbackResultDTO getFeedbackResult(String requirementId, Integer resourceType) {
        FeedbackResultDTO result = requirementsService.getFeedbackResult(requirementId, resourceType);

        // 查询处理结果
        result.setHandle(requirementsService.getDealDetail(requirementId));

        return result;
    }

    /**
     * 删除需求
     *
     * @param
     * @throws
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRequirements(List<String> guids) {
        requirementsMapper.deleteByGuids(guids);
    }

    @Transactional(rollbackFor = Exception.class)
    public String createdRequirement(RequirementDTO dto) throws IOException {
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
        fileInfoService.createFileuploadRecord(dto.getFilePath(), dto.getFileName());
        return po.getGuid();
    }

    @Transactional(rollbackFor = Exception.class)
    public void editedRequirement(RequirementDTO dto) {
        Assert.isTrue(StringUtils.isNotBlank(dto.getGuid()), "需求ID不能为空");
        verifyRequirementDTO(dto);

        RequirementsPO oldPo = requirementsMapper.getRequirementById(dto.getGuid());
        Assert.notNull(oldPo, "需求ID无效");

        // 名称未修改直接为true;名称修改了，查库判断名称是否存在
        Assert.isTrue(Objects.equals(oldPo.getName(), dto.getName())
                        || !isRequirementNameExist(dto.getName(), oldPo.getTenantId()),
                "需求名称已经存在!");
        Assert.isTrue(Objects.equals(oldPo.getNum(), dto.getNum())
                        || !isRequirementNumExist(dto.getNum(), oldPo.getTenantId()),
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
        fileInfoService.createFileuploadRecord(dto.getFilePath(), dto.getFileName());
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
        return requirementsMapper.countRequirementByNum(num, tenantId) > 0;
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
            List<String> categoryList = new ArrayList<>();
            Set<String> tenantIdS = new HashSet<>();
            requirementsPOList.stream().forEach(requirementsPO -> {
                categoryList.add(requirementsPO.getBusinessCategoryId());
                tenantIdS.add(requirementsPO.getTenantId());
            });
            List<Tenant> tenants = tenantDAO.selectListByTenantId(tenantIdS);
            Map<String, String> mapTenant = tenants.stream().collect(Collectors.toMap(Tenant::getTenantId, Tenant::getProjectName));
            Set<CategoryEntityV2> categoryEntityV2s = categoryDAO.selectPathByGuidAndTenantList(categoryList, tenantIdS, CommonConstant.BUSINESS_CATEGORY_TYPE);
            Map<String, String> map = categoryEntityV2s.stream().collect(Collectors.toMap(CategoryEntityV2::getGuid, CategoryEntityV2::getPath));
            for (RequirementsPO requirementsPO : requirementsPOList) {
                RequirementsListDTO requirementsListDTO = new RequirementsListDTO();
                BeanUtils.copyProperties(requirementsPO, requirementsListDTO);
                requirementsListDTO.setResourceTypeName(ResourceType.getValue(requirementsListDTO.getResourceType()));
                if (requirementsListDTO.getStatus().equals(CommonConstant.REQUIREMENTS_STATUS_ONE)) {
                    requirementsListDTO.setStatusName("待下发");
                } else if ("2,3".contains(String.valueOf(requirementsListDTO.getStatus()))) {
                    requirementsListDTO.setStatusName("已下发");
                } else {
                    requirementsListDTO.setStatusName("已反馈");
                }
                String path = map.get(requirementsListDTO.getBusinessCategoryId());
                if (StringUtils.isNotBlank(path)) {
                    requirementsListDTO.setCategoryPath(mapTenant.get(requirementsListDTO.getTenantId()) + "/" + path.replace(",", "/").replace("\"", "").replace("{", "").replace("}", ""));
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

        FeedbackResultDTO feedbackResultDTO = requirementsService.getFeedbackResult(id, type);
        baseDTO.setApi(feedbackResultDTO.getApi());
        baseDTO.setDatabase(feedbackResultDTO.getDatabase());
        baseDTO.setMq(feedbackResultDTO.getMq());
        return baseDTO;
    }

}
