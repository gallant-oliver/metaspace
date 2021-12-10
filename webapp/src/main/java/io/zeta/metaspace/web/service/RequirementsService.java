package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.enums.FilterOperation;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.metadata.TableExtInfo;
import io.zeta.metaspace.model.po.requirements.RequirementsColumnPO;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.po.requirements.RequirementsResultPO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.dao.requirements.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

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

        RequirementsApiDetailDTO api = null;
        RequirementsDatabaseDetailDTO database = null;
        RequirementsMqDetailDTO mq = null;

        //资源类型 1：API 2：中间库 3：消息队列
        switch (resourceType){
            case 1:
                api = requirementsApiMapper.selectByRequirementId(requirementId);
                break;
            case 2:
                database = requirementsDatabaseMapper.selectByRequirementId(requirementId);
                break;
            case 3:
                mq = requirementsMqMapper.selectByRequirementId(requirementId);
                break;
            default:
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"资源类型错误：1-API 2-中间库 3-消息队列");
        }

        result.setApi(api);
        result.setDatabase(database);
        result.setMq(mq);

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
}
