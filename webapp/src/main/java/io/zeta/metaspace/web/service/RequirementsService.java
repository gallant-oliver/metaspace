package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.enums.FilterOperation;
import io.zeta.metaspace.model.enums.MessagePush;
import io.zeta.metaspace.model.enums.ProcessEnum;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.TableExtInfo;
import io.zeta.metaspace.model.po.requirements.*;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiHead;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.requirements.*;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.util.AdminUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.util.*;

import static io.zeta.metaspace.model.enums.MessagePush.NEED_AUDIT_DEAL_PEOPLE;
import static io.zeta.metaspace.model.enums.MessagePush.NEED_AUDIT_FINISH;

@Service
@Slf4j
public class RequirementsService {

    @Autowired
    private RequirementsApiMapper requirementsApiMapper;

    @Autowired
    private RequirementsDatabaseMapper requirementsDatabaseMapper;

    @Autowired
    private RequirementsMqMapper requirementsMqMapper;

    @Autowired
    private RequirementsMapper requirementsMapper;

    @Autowired
    private RequirementsResultMapper requirementsResultMapper;

    @Autowired
    private TableDAO tableDAO;

    @Autowired
    private RequirementsColumnMapper requirementsColumnMapper;

    @Autowired
    private ColumnDAO columnDAO;

    @Autowired
    DataShareDAO shareDAO;

    @Autowired
    ApproveGroupDAO approveGroupDAO;

    @Autowired
    MessageCenterService messageCenterService;

    @Autowired
    UserDAO userDAO;

    /**
     * ?????????????????????????????????????????????
     *
     * @param
     * @throws
     */
    public TableExtInfo getTableStatus(String tableId) {
        TableExtInfo tableExtInfo = tableDAO.getTableImportanceInfo(tableId);
        if (Objects.isNull(tableExtInfo)) {
            tableExtInfo = new TableExtInfo();
            tableExtInfo.setImportance(false);
            tableExtInfo.setSecurity(false);
        }
        return tableExtInfo;
    }

    /**
     * ????????????
     *
     * @param
     * @throws
     */
    @Transactional(rollbackFor = Exception.class)
    public void handle(RequirementsHandleDTO resultDTO, String tenantId) {
        List<String> guids = resultDTO.getGuids();
        if (CollectionUtils.isNotEmpty(guids)) {
            // ????????????
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp currentTime = new Timestamp(currentTimeMillis);

            List<RequirementsResultPO> resultPOS = new ArrayList<>();
            DealDetailDTO result = resultDTO.getResult();
            List<String> needNameList = new ArrayList<>();
            for (String requirementsId : guids) {
                RequirementsResultPO resultPO = new RequirementsResultPO();
                BeanUtils.copyProperties(result, resultPO);
                resultPO.setRequirementsId(requirementsId);
                resultPO.setType(result.getResult().shortValue());

                String guid = UUID.randomUUID().toString();
                resultPO.setGuid(guid);

                resultPO.setCreateTime(currentTime);
                resultPO.setUpdateTime(currentTime);

                resultPOS.add(resultPO);

                // ???????????????????????????????????????????????????
                RequirementsPO po = requirementsMapper.getRequirementById(requirementsId);
                if (po != null) {
                    if (StringUtils.isNotEmpty(po.getName())) {
                        needNameList.add(po.getName());
                    }
                }

            }

            requirementsResultMapper.batchInsert(resultPOS);

            // ?????????????????????1????????????  2???????????????????????????  3??????????????????????????? 4????????????  -1?????????
            Integer type = result.getResult(); // 1?????????2??????
            if (type == 1) {
                requirementsMapper.batchUpdateStatusByIds(guids, 3);

                // ?????????????????????????????????
                String userId = resultDTO.getResult().getUserId();
                if (StringUtils.isNotEmpty(userId)) {
                    List<String> userEmailList = userDAO.getUsersEmailByIds(new ArrayList<>(Arrays.asList(userId)));
                    MessageEntity message = null;
                    for (String needName : needNameList) {
                        message = new MessageEntity(NEED_AUDIT_DEAL_PEOPLE.type, MessagePush.getFormattedMessageName(NEED_AUDIT_DEAL_PEOPLE.name, needName), NEED_AUDIT_DEAL_PEOPLE.module, ProcessEnum.PROCESS_APPROVED_NOT_DEAL.code);
                        if (CollectionUtils.isNotEmpty(userEmailList)){
                            for (String userEmail : userEmailList) {
                                message.setCreateUser(userEmail);
                                messageCenterService.addMessage(message, tenantId);
                            }
                        }

                    }

                }


            } else {
                requirementsMapper.batchUpdateStatusByIds(guids, -1);
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
        FeedbackResultDTO result = new FeedbackResultDTO();
        result.setResourceType(resourceType);

        //???????????? 1???API 2???????????? 3???????????????
        switch (resourceType) {
            case 1:
                RequirementsApiPO apiInfo = requirementsApiMapper.selectByRequirementId(requirementId);
                RequirementsApiDetailDTO api = new RequirementsApiDetailDTO();
                if (Objects.nonNull(apiInfo)) {
                    BeanUtils.copyProperties(apiInfo, api);
                    api.setStatus("up".equalsIgnoreCase(apiInfo.getStatus()) ? "??????" : "??????");
                }
                result.setApi(api);
                break;
            case 2:
                RequirementsDatabasePO databaseInfo = requirementsDatabaseMapper.selectByRequirementId(requirementId);
                RequirementsDatabaseDetailDTO database = new RequirementsDatabaseDetailDTO();
                if (Objects.nonNull(databaseInfo)) {
                    BeanUtils.copyProperties(databaseInfo, database);
                    database.setStatus(databaseInfo.getStatus() == 1 ? "??????" : "??????");
                }
                result.setDatabase(database);
                break;
            case 3:
                RequirementsMqPO mqInfo = requirementsMqMapper.selectByRequirementId(requirementId);
                RequirementsMqDetailDTO mq = new RequirementsMqDetailDTO();
                if (Objects.nonNull(mqInfo)) {
                    BeanUtils.copyProperties(mqInfo, mq);
                    mq.setStatus(mqInfo.getStatus() == 1 ? "??????" : "??????");
                }
                result.setMq(mq);
                break;
            default:
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????1-API 2-????????? 3-????????????");
        }

        return result;
    }

    /**
     * ????????????
     *
     * @param
     * @throws
     */
    public RequirementDTO getRequirementById(String requirementId) {
        RequirementDTO result = null;
        RequirementsPO requirementsPO = requirementsMapper.selectByPrimaryKey(requirementId);
        if (Objects.nonNull(requirementsPO)) {
            result = new RequirementDTO();
            BeanUtils.copyProperties(requirementsPO, result);

            result.setResourceType(ResourceType.parseByCode(requirementsPO.getResourceType()));

            // ????????????????????????
            String aimingField = requirementsPO.getAimingField();
            Gson gson = new Gson();
            List<String> columnIds = gson.fromJson(aimingField, List.class);
            if (CollectionUtils.isNotEmpty(columnIds)) {
                List<Column> columns = columnDAO.queryColumns(columnIds);
                result.setTargetFields(columns);
            }

            result.setTargetFieldIDs(columnIds);

            List<FilterConditionDTO> filterConditions = null;

            // ????????????????????????
            List<RequirementsColumnPO> filterColumnInfos = requirementsColumnMapper.selectByRequirementId(requirementId);
            filterConditions = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(filterColumnInfos)) {
                for (RequirementsColumnPO filterColumnInfo : filterColumnInfos) {
                    FilterConditionDTO filterCondition = new FilterConditionDTO();
                    BeanUtils.copyProperties(filterColumnInfo, filterCondition);
                    filterCondition.setOperation(FilterOperation.parseByDesc(filterColumnInfo.getOperator()));
                    filterConditions.add(filterCondition);
                }
            }

            result.setFilterConditions(filterConditions);
        }


        return result;
    }

    public DealDetailDTO getDealDetail(String id) {
        return requirementsResultMapper.queryDealDetail(id);
    }

    public List<ApiCateDTO> getCategoryApis(String projectId, String categoryId, String search, String tenantId) {
        if (StringUtils.isBlank(projectId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????id????????????");
        }
        Parameters parameters = new Parameters();
        parameters.setLimit(-1);
        List<ApiCateDTO> apiList = new ArrayList<>();
        String query = search;
        if (StringUtils.isNotBlank(query)) {
            parameters.setQuery(query.replaceAll("_", "/_").replaceAll("%", "/%"));
        }
        if (StringUtils.isBlank(categoryId)) {
            categoryId = null;
        }

        List<ApiHead> apiHeads;
        try {
            apiHeads = shareDAO.searchApi(parameters, projectId, categoryId, null, null, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????API???????????????" + e.getMessage());
        }
        if (CollectionUtils.isNotEmpty(apiHeads)) {
            for (ApiHead head : apiHeads) {
                ApiCateDTO dto = new ApiCateDTO();
                dto.setId(head.getId());
                dto.setName(head.getName());
                dto.setCategoryId(head.getCategoryId());
                dto.setCategoryName(head.getCategoryName());
                apiList.add(dto);
            }
        }
        return apiList;
    }

    public List<ApiCateDTO> getCategories(String projectId, String search, String tenantId) {
        List<ApiCateDTO> cateList;
        try {
            cateList = shareDAO.getCategories(projectId, tenantId, search);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????" + e.getMessage());
        }
        return cateList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void feedback(RequirementsFeedbackCommit commitInput) {
        //??????????????????
        String requirementsId = commitInput.getRequirementsId();
        Integer resourceType = commitInput.getResourceType();
        Assert.isTrue(StringUtils.isNotBlank(requirementsId), "??????id????????????");
        Assert.notNull(resourceType, "????????????????????????");

        if (resourceType == ResourceType.API.getCode()) {
            RequirementsApiCommitDTO apiInput = commitInput.getApi();
            Assert.notNull(apiInput, "API??????????????????");
            String projectId = apiInput.getProjectId();
            String categoryId = apiInput.getCategoryId();
            String apiId = apiInput.getApiId();
            Assert.isTrue(StringUtils.isNotBlank(projectId), "??????id????????????");
            Assert.isTrue(StringUtils.isNotBlank(apiId), "API id????????????");

            RequirementsApiPO record = new RequirementsApiPO();
            record.setGuid(UUID.randomUUID().toString());
            record.setRequirementsId(requirementsId);
            record.setProjectId(projectId);
            record.setCategoryId(categoryId);
            record.setApiId(apiId);
            record.setDescription(apiInput.getDescription());
            Timestamp now = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
            record.setCreateTime(now);
            record.setUpdateTime(now);
            requirementsApiMapper.insert(record);
        }

        String userId = AdminUtils.getUserData().getUserId();
        if (resourceType == ResourceType.TABLE.getCode()) {
            RequirementsDatabaseCommitDTO databaseInput = commitInput.getDatabase();
            Assert.notNull(databaseInput, "???????????????????????????");
            String middleType = databaseInput.getMiddleType();
            String database = databaseInput.getDatabase();
            String tableNameEn = databaseInput.getTableNameEn();
            String tableNameCh = databaseInput.getTableNameCh();
            Integer status = databaseInput.getStatus();
            Assert.isTrue(StringUtils.isNotBlank(middleType), "???????????????????????????");
            Assert.isTrue(StringUtils.isNotBlank(database), "???????????????????????????");
            Assert.isTrue(StringUtils.isNotBlank(tableNameEn), "?????????????????????????????????");
            Assert.isTrue(StringUtils.isNotBlank(tableNameCh), "?????????????????????????????????");
            Assert.notNull(status, "??????????????????");

            RequirementsDatabasePO record = new RequirementsDatabasePO();
            record.setGuid(UUID.randomUUID().toString());
            record.setRequirementsId(requirementsId);
            record.setMiddleType(middleType);
            record.setDatabase(database);
            record.setTableNameEn(tableNameEn);
            record.setTableNameCh(tableNameCh);
            record.setTableNameCh(tableNameCh);
            record.setStatus(status);
            record.setDescription(databaseInput.getDescription());
            Timestamp now = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
            record.setCreateTime(now);
            record.setUpdateTime(now);
            record.setCreator(userId);
            requirementsDatabaseMapper.insert(record);
        }

        if (resourceType == ResourceType.MESSAGE_QUEUE.getCode()) {
            RequirementsMqCommitDTO mqInput = commitInput.getMq();
            Assert.notNull(mqInput, "??????????????????????????????");
            String mqNameEn = mqInput.getMqNameEn();
            String mqNameCh = mqInput.getMqNameCh();
            String format = mqInput.getFormat();
            Integer status = mqInput.getStatus();
            Assert.isTrue(StringUtils.isNotBlank(mqNameEn), "????????????????????????????????????");
            Assert.isTrue(StringUtils.isNotBlank(mqNameCh), "????????????????????????????????????");
            Assert.isTrue(StringUtils.isNotBlank(format), "??????????????????");
            Assert.notNull(status, "??????????????????");

            RequirementsMqPO record = new RequirementsMqPO();
            record.setGuid(UUID.randomUUID().toString());
            record.setRequirementsId(requirementsId);
            record.setMqNameEn(mqNameEn);
            record.setMqNameCh(mqNameCh);
            record.setFormat(format);
            record.setStatus(status);
            record.setDescription(mqInput.getDescription());
            Timestamp now = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
            record.setCreateTime(now);
            record.setUpdateTime(now);
            record.setCreator(userId);
            requirementsMqMapper.insert(record);
        }

        // ?????????????????????1????????????  2???????????????????????????  3??????????????????????????? 4????????????  -1?????????
        List<String> guids = new ArrayList<>();
        guids.add(requirementsId);
        requirementsMapper.batchUpdateStatusByIds(guids, 4);

        // ?????????????????????????????????
        RequirementsPO requirementById = requirementsMapper.getRequirementById(requirementsId);
        String creator = requirementById.getCreator();
        if (StringUtils.isNotEmpty(creator)){
            List<String> userEmailList = userDAO.getUsersEmailByIds(new ArrayList<>(Arrays.asList(creator)));
            MessageEntity message = new MessageEntity(NEED_AUDIT_FINISH.type, MessagePush.getFormattedMessageName(NEED_AUDIT_FINISH.name, requirementById.getName()), NEED_AUDIT_FINISH.module, ProcessEnum.PROCESS_APPROVED_FEEDBACK.code);
            if (CollectionUtils.isNotEmpty(userEmailList)){
                for (String userEmail : userEmailList){
                    message.setCreateUser(userEmail);
                    messageCenterService.addMessage(message, requirementById.getTenantId());
                }
            }
        }

    }

    /**
     * ??????????????????
     *
     * @param requireListParam
     * @param tenantId
     * @return
     */
    public PageResult getHandleListPage(RequireListParam requireListParam, String tenantId) {
        PageResult pageResult = new PageResult();
        List<RequirementsListDTO> requirementsListDTOList = new ArrayList<>();
        try {
            User user = AdminUtils.getUserData();
            if (StringUtils.isBlank(requireListParam.getOrder())) {
                requireListParam.setOrder("asc");
            }
            List<RequirementsPO> requirementsPOList;
            if (requireListParam.getStatus() != null) {
                requirementsPOList = requirementsMapper.selectHandleListByStatusPage(user.getUserId(), tenantId, requireListParam);
            } else {
                requirementsPOList = requirementsMapper.selectHandleListPage(user.getUserId(), tenantId, requireListParam);
            }
            if (CollectionUtils.isEmpty(requirementsPOList)) {
                pageResult.setTotalSize(0);
                pageResult.setCurrentSize(0);
                pageResult.setOffset(0);
                pageResult.setLists(requirementsListDTOList);
                return pageResult;
            }
            for (RequirementsPO requirementsPO : requirementsPOList) {
                RequirementsListDTO requirementsListDTO = new RequirementsListDTO();
                BeanUtils.copyProperties(requirementsPO, requirementsListDTO);
                requirementsListDTO.setResourceTypeName(ResourceType.getValue(requirementsListDTO.getResourceType()));
                if ((requirementsListDTO.getStatus().equals(CommonConstant.REQUIREMENTS_STATUS_TWO))) {
                    requirementsListDTO.setStatusName("?????????");
                } else {
                    requirementsListDTO.setStatusName("?????????");
                }
                requirementsListDTOList.add(requirementsListDTO);
            }
            pageResult.setTotalSize(requirementsListDTOList.get(0).getTotal());
            pageResult.setCurrentSize(requirementsListDTOList.size());
            pageResult.setOffset(requireListParam.getOffset());
            pageResult.setLists(requirementsListDTOList);
        } catch (Exception e) {
            log.error("getHandleListPage exception is {}", e);
        }
        return pageResult;
    }

    /**
     * ????????????????????????
     *
     * @param requireListParam
     * @param tenantId
     * @return
     */
    public PageResult getReturnListPage(RequireListParam requireListParam, String tenantId) {
        PageResult pageResult = new PageResult();
        List<RequirementsListDTO> requirementsListDTOList = new ArrayList<>();
        try {
            User user = AdminUtils.getUserData();
            if (StringUtils.isBlank(requireListParam.getOrder())) {
                requireListParam.setOrder("asc");
            }
            List<RequirementsPO> requirementsPOList = requirementsMapper.selectReturnListPage(user.getUserId(), tenantId, requireListParam);
            if (CollectionUtils.isEmpty(requirementsPOList)) {
                pageResult.setTotalSize(0);
                pageResult.setCurrentSize(0);
                pageResult.setOffset(0);
                pageResult.setLists(requirementsListDTOList);
                return pageResult;
            }
            for (RequirementsPO requirementsPO : requirementsPOList) {
                RequirementsListDTO requirementsListDTO = new RequirementsListDTO();
                BeanUtils.copyProperties(requirementsPO, requirementsListDTO);
                requirementsListDTO.setResourceTypeName(ResourceType.getValue(requirementsListDTO.getResourceType()));
                if ((requirementsListDTO.getStatus().equals(CommonConstant.REQUIREMENTS_STATUS_THREE))) {
                    requirementsListDTO.setStatusName("?????????");
                } else {
                    requirementsListDTO.setStatusName("?????????");
                }
                requirementsListDTOList.add(requirementsListDTO);
            }
            pageResult.setTotalSize(requirementsListDTOList.get(0).getTotal());
            pageResult.setCurrentSize(requirementsListDTOList.size());
            pageResult.setOffset(requireListParam.getOffset());
            pageResult.setLists(requirementsListDTOList);
        } catch (Exception e) {
            log.error("getReturnListPage exception is {}", e);
        }
        return pageResult;
    }
}
