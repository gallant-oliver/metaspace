package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.enums.ResourceType;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.po.requirements.RequirementsResultPO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.web.dao.SourceInfoDeriveTableInfoDAO;
import io.zeta.metaspace.web.dao.requirements.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
    private SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDAO;

    /**
     * 需求关联表是否为重要表、保密表
     *
     * @param
     * @throws
     */
    public SourceInfoDeriveTableInfo getTableStatus(String tableId) {
        return sourceInfoDeriveTableInfoDAO.getByNameAndDbGuid(tableId, null);
    }

    /**
     * 需求处理
     *
     * @param
     * @throws
     */
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
        }
    }

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

    public RequirementDTO getRequirementById(String requirementId) {
        RequirementDTO result = null;
        RequirementsPO requirementsPO = requirementsMapper.selectByPrimaryKey(requirementId);
        if (Objects.nonNull(requirementsPO)) {
            result = new RequirementDTO();
            BeanUtils.copyProperties(requirementsPO, result);

            result.setResourceType(ResourceType.parseByCode(requirementsPO.getResourceType()));

            //查询目标字段信息
            String f = requirementsPO.getAimingField();
        }


        return result;
    }
}
