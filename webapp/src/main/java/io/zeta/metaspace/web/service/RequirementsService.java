package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.enums.FilterOperation;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.TableExtInfo;
import io.zeta.metaspace.model.po.requirements.RequirementsColumnPO;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.po.requirements.RequirementsResultPO;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.dao.requirements.*;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsApiPO;
import io.zeta.metaspace.model.po.requirements.RequirementsDatabasePO;
import io.zeta.metaspace.model.po.requirements.RequirementsMqPO;
import io.zeta.metaspace.model.share.ApiHead;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    /**
     * 需求关联表是否为重要表、保密表
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
     * 需求处理
     *
     * @param
     * @throws
     */
    @Transactional(rollbackFor=Exception.class)
    public void handle(RequirementsHandleDTO resultDTO) {
        List<String> guids = resultDTO.getGuids();
        if (CollectionUtils.isNotEmpty(guids)) {
            // 创建时间
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp currentTime = new Timestamp(currentTimeMillis);

            List<RequirementsResultPO> resultPOS = new ArrayList<>();
            RequirementsResultPO result = resultDTO.getResult();
            for (String requirementsId : guids) {
                RequirementsResultPO resultPO = new RequirementsResultPO();
                BeanUtils.copyProperties(result, resultPO);
                resultPO.setRequirementsId(requirementsId);

                String guid = UUID.randomUUID().toString();
                resultPO.setGuid(guid);

                resultPO.setCreateTime(currentTime);
                resultPO.setUpdateTime(currentTime);

                resultPOS.add(resultPO);
            }

            requirementsResultMapper.batchInsert(resultPOS);

            // 更新需求状态：1、待下发  2、已下发（待处理）  3、已处理（未反馈） 4、已反馈  -1、退回
            Short type = result.getType(); // 1同意；2拒绝
            if (type == 1) {
                requirementsMapper.batchUpdateStatusByIds(guids, 3);
            }
            else {
                requirementsMapper.batchUpdateStatusByIds(guids, -1);
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
        FeedbackResultDTO result = new FeedbackResultDTO();

        //资源类型 1：API 2：中间库 3：消息队列
        switch (resourceType){
            case 1:
                RequirementsApiPO apiInfo = requirementsApiMapper.selectByRequirementId(requirementId);
                RequirementsApiDetailDTO api = new RequirementsApiDetailDTO();
                BeanUtils.copyProperties(apiInfo, api);
                api.setStatus("up".equalsIgnoreCase(apiInfo.getStatus()) ? "上线" : "下线");
                result.setApi(api);
                break;
            case 2:
                RequirementsDatabasePO databaseInfo = requirementsDatabaseMapper.selectByRequirementId(requirementId);
                RequirementsDatabaseDetailDTO database = new RequirementsDatabaseDetailDTO();
                BeanUtils.copyProperties(databaseInfo, database);
                database.setStatus(databaseInfo.getStatus() == 1 ? "上线" : "下线");
                result.setDatabase(database);
                break;
            case 3:
                RequirementsMqPO mqInfo = requirementsMqMapper.selectByRequirementId(requirementId);
                RequirementsMqDetailDTO mq = new RequirementsMqDetailDTO();
                BeanUtils.copyProperties(mqInfo, mq);
                mq.setStatus(mqInfo.getStatus() == 1 ? "上线" : "下线");
                result.setMq(mq);
                break;
            default:
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"资源类型错误：1-API 2-中间库 3-消息队列");
        }

        return result;
    }

    /**
     * 需求详情
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

            // 查询目标字段信息
            String aimingField = requirementsPO.getAimingField();
            Gson gson = new Gson();
            List<String> columnIds = gson.fromJson(aimingField, List.class);
            if (CollectionUtils.isNotEmpty(columnIds)) {
                List<String> columnNames = columnDAO.queryColumnNames(columnIds);
                result.setTargetFieldNames(columnNames);
            }

            List<String> filterFieldNames = null;
            List<FilterConditionDTO> filterConditions = null;

            // 查询过滤字段信息
            List<RequirementsColumnPO> filterColumnInfos = requirementsColumnMapper.selectByRequirementId(requirementId);
            if (CollectionUtils.isNotEmpty(filterColumnInfos)) {
                filterFieldNames = filterColumnInfos.stream().map(RequirementsColumnPO :: getColumnName).collect(Collectors.toList());

                filterConditions = new ArrayList<>();
                for (RequirementsColumnPO filterColumnInfo : filterColumnInfos) {
                    FilterConditionDTO filterCondition = new FilterConditionDTO();
                    BeanUtils.copyProperties(filterColumnInfo, filterCondition);
                    filterCondition.setOperation(FilterOperation.parseByDesc(filterColumnInfo.getOperator()));
                    filterConditions.add(filterCondition);
                }
            }

            result.setFilterFieldNames(filterFieldNames);
            result.setFilterConditions(filterConditions);
        }


        return result;
    }

    public DealDetailDTO getDealDetail(String id) {
        return requirementsResultMapper.queryDealDetail(id);
    }

    public List<ApiCateDTO> getCateategoryApis(String projectId, String categoryId, String search, String tenantId) {
        if (StringUtils.isBlank(projectId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "项目id不能为空");
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询API列表异常：" + e.getMessage());
        }
        if (CollectionUtils.isNotEmpty(apiHeads)) {
            for (ApiHead head : apiHeads) {
                ApiCateDTO dto = new ApiCateDTO();
                dto.setId(head.getId());
                dto.setName(head.getName());
                apiList.add(dto);
            }
        }
        return apiList;
    }

    public List<ApiCateDTO> getCateategories(String projectId, String search, String tenantId) {
        List<ApiCateDTO> cateList;
        try {
            cateList = shareDAO.getCategories(projectId, tenantId, search);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询项目目录列表异常：" + e.getMessage());
        }
        return cateList;
    }


    public void feedback(RequirementsFeedbackCommit commitInput) {
        //必输参数校验
        String requirementsId = commitInput.getRequirementsId();
        Integer resourceType = commitInput.getResourceType();

        if (StringUtils.isBlank(requirementsId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "需求id不能为空");
        }

        if (Objects.isNull(resourceType)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "资源类型不能为空");
        }

        if (resourceType == ResourceType.API.getCode()) {
            RequirementsApiCommitDTO apiInput = commitInput.getApi();
            if (null == apiInput) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API入参为空");
            }
            String projectId = apiInput.getProjectId();
            String categoryId = apiInput.getCategoryId();
            String apiId = apiInput.getApiId();
            if (StringUtils.isBlank(projectId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "项目id为空");
            }
            if (StringUtils.isBlank(apiId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API id为空");
            }
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
            if (null == databaseInput) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "中间库入参不能为空");
            }
            String middleType = databaseInput.getMiddleType();
            String database = databaseInput.getDatabase();
            String tableNameEn = databaseInput.getTableNameEn();
            String tableNameCh = databaseInput.getTableNameCh();
            Integer status = databaseInput.getStatus();
            if (StringUtils.isBlank(middleType)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "中间库类型不能为空");
            }
            if (StringUtils.isBlank(database)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库名称不能为空");
            }
            if (StringUtils.isBlank(tableNameEn)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据表英文名称不能为空");
            }
            if (StringUtils.isBlank(tableNameCh)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据表中文名称不能为空");
            }
            if (null == status) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "状态不能为空");
            }

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
            if (null == mqInput) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "消息队列入参不能为空");
            }
            String mqNameEn = mqInput.getMqNameEn();
            String mqNameCh = mqInput.getMqNameCh();
            String format = mqInput.getFormat();
            Integer status = mqInput.getStatus();
            if (StringUtils.isBlank(mqNameEn)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "消息队列英文名称不能为空");
            }
            if (StringUtils.isBlank(mqNameCh)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "消息队列中文名称不能为空");
            }
            if (StringUtils.isBlank(format)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "格式不能为空");
            }
            if (null == status) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "状态不能为空");
            }

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


    }
}
