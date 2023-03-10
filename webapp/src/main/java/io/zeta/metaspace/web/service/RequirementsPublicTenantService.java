package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.enums.MessagePush;
import io.zeta.metaspace.model.enums.ProcessEnum;
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
     * ???????????????????????????????????????????????????
     */
    public PageResult<ResourceDTO> pagedResource(String tableId, Parameters parameters) {
        List<ResourcePO> resources = requirementsMapper.pagedResources(tableId, parameters);

        if (CollectionUtils.isEmpty(resources)) {
            return PageResult.empty(ResourceDTO.class);
        }

        // ??????????????????
        String tableName = tableDAO.getTableNameByTableGuid(tableId);
        Assert.isTrue(StringUtils.isNotBlank(tableName), "?????????ID??????");

        long total = resources.get(0).getTotal();
        List<ResourceDTO> list = resources.stream()
                .map(po -> {
                    ResourceDTO dto = new ResourceDTO();
                    BeanUtils.copyProperties(po, dto, "type", "state");
                    // ????????????
                    dto.setState(ResourceState.parseByCode(po.getState()));
                    dto.setType(ResourceType.parseByCode(po.getType()));
                    return dto;
                })
                .peek(dto -> dto.setDataTableName(tableName))
                .collect(Collectors.toList());
        return new PageResult<>(total, list);
    }

    /**
     * ????????????
     *
     * @param
     * @throws
     */
    @Transactional(rollbackFor = Exception.class)
    public void grant(String requirementId) {
        RequirementsPO requirementsPO = new RequirementsPO();
        // 1????????????  2???????????????????????????  3??????????????????????????? 4????????????  -1?????????
        requirementsPO.setStatus(2);
        requirementsPO.setGuid(requirementId);
        requirementsMapper.updateByPrimaryKeySelective(requirementsPO);

        RequirementsPO result = requirementsMapper.getRequirementById(requirementId);
        String tableId = result.getTableId();
        String sourceId = result.getSourceId();
        RequirementIssuedPO po =
                Optional.ofNullable(sourceInfoDAO.queryIssuedInfo(tableId, sourceId))
                        .orElseThrow(() -> new AtlasBaseException(
                                String.format("?????????ID %s,?????????ID %s ??????,??????????????????.",
                                        tableId,
                                        sourceId)));

        // ?????????????????????????????????
        String tenantId = result.getTenantId();
        if (po != null && StringUtils.isNotBlank(po.getUserId())){
            List<String> userEmailList = userDAO.getUsersEmailByIds(new ArrayList<>(Arrays.asList(po.getUserId())));
            MessageEntity message =  new MessageEntity(NEED_AUDIT_START_MANAGER.type, MessagePush.getFormattedMessageName(NEED_AUDIT_START_MANAGER.name, result.getName()), NEED_AUDIT_START_MANAGER.module, ProcessEnum.PROCESS_APPROVED_NOT_DEAL.code);
            if (CollectionUtils.isNotEmpty(userEmailList)){
                for (String userEmail : userEmailList) {
                    message.setCreateUser(userEmail);
                    messageCenterService.addMessage(message, tenantId);
                }
            }
        }

    }

    /**
     * ??????????????????
     *
     * @param
     * @throws
     */
    public FeedbackResultDTO getFeedbackResult(String requirementId, Integer resourceType) {
        FeedbackResultDTO result = requirementsService.getFeedbackResult(requirementId, resourceType);

        // ??????????????????
        result.setHandle(requirementsService.getDealDetail(requirementId));

        return result;
    }

    /**
     * ????????????
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
        // ????????????
        verifyRequirementDTO(dto);
        Assert.isTrue(StringUtils.isNotBlank(dto.getTenantId()), "??????????????????????????????????????????ID????????????");
        Assert.isTrue(StringUtils.isNotBlank(dto.getBusinessId()), "??????ID????????????");
        Assert.isTrue(StringUtils.isNotBlank(dto.getTableId()), "????????????????????????ID????????????");
        Assert.isTrue(StringUtils.isNotBlank(dto.getSourceId()), "?????????ID????????????");
        Assert.isTrue(!isRequirementNameExist(dto.getName(), dto.getTenantId()), "????????????????????????!");
        Assert.isTrue(!isRequirementNumExist(dto.getNum(), dto.getTenantId()), "????????????????????????!");

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

        // ??????????????????
        List<FilterConditionDTO> filterConditions = dto.getFilterConditions();
        columnService.batchInsert(po.getGuid(), po.getTableId(), filterConditions);
        return po.getGuid();
    }

    @Transactional(rollbackFor = Exception.class)
    public void editedRequirement(RequirementDTO dto) {
        Assert.isTrue(StringUtils.isNotBlank(dto.getGuid()), "??????ID????????????");
        verifyRequirementDTO(dto);

        RequirementsPO oldPo = requirementsMapper.getRequirementById(dto.getGuid());
        Assert.notNull(oldPo, "??????ID??????");

        // ????????????????????????true;????????????????????????????????????????????????
        Assert.isTrue(Objects.equals(oldPo.getName(), dto.getName())
                        || !isRequirementNameExist(dto.getName(), oldPo.getTenantId()),
                "????????????????????????!");
        Assert.isTrue(Objects.equals(oldPo.getNum(), dto.getNum())
                        || !isRequirementNumExist(dto.getNum(), oldPo.getTenantId()),
                "????????????????????????!");

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
        //  ?????????????????????????????????
        columnService.batchUpdate(oldPo.getGuid(), oldPo.getTableId(), dto.getFilterConditions());
    }

    private void verifyRequirementDTO(RequirementDTO dto) {
        Assert.isTrue(StringUtils.isNotBlank(dto.getName()), "????????????????????????");
        Assert.isTrue(StringUtils.isNotBlank(dto.getNum()), "????????????????????????");
        Assert.notNull(dto.getResourceType(), "????????????????????????");
    }

    private boolean isRequirementNameExist(String name, String tenantId) {
        return requirementsMapper.countRequirementByName(name, tenantId) > 0;
    }

    private boolean isRequirementNumExist(String num, String tenantId) {
        return requirementsMapper.countRequirementByNum(num, tenantId) > 0;
    }

    /**
     * ???????????????ID???????????????????????????.
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
                                String.format("?????????ID %s,?????????ID %s ??????,??????????????????.",
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
     * ??????????????????????????????????????????
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
     * ??????????????????
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
                    requirementsListDTO.setStatusName("?????????");
                } else if ("2,3".contains(String.valueOf(requirementsListDTO.getStatus()))) {
                    requirementsListDTO.setStatusName("?????????");
                } else {
                    requirementsListDTO.setStatusName("?????????");
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
     * ???????????????????????????????????????
     */
    public FeedbackDetailBaseDTO getDetailBase(String id, Integer type) {
        Assert.isTrue(StringUtils.isNotBlank(id), "??????id??????");
        Assert.isTrue(null != type, "??????????????????");
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
