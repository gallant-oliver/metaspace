package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.RequirementsResultDTO;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.po.requirements.RequirementsResultPO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.web.dao.SourceInfoDeriveTableInfoDAO;
import io.zeta.metaspace.web.dao.requirements.RequirementsApiMapper;
import io.zeta.metaspace.web.dao.requirements.RequirementsMapper;
import io.zeta.metaspace.web.dao.requirements.RequirementsResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RequirementsService {

    @Autowired
    private RequirementsApiMapper requirementsApiMapper;

    @Autowired
    private RequirementsMapper requirementsMapper;

    @Autowired
    private RequirementsResultMapper requirementsResultMapper;

    @Autowired
    private SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDAO;

    /**
     * 需求下发
     *
     * @param
     * @throws
     */
    public void grant(String guid) {
        RequirementsPO requirementsPO = new RequirementsPO();
        // 1、待下发  2、已下发（待处理）  3、已处理（未反馈） 4、已反馈  -1、退回
        requirementsPO.setStatus(2);
        requirementsPO.setGuid(guid);
        requirementsMapper.updateByPrimaryKeySelective(requirementsPO);
    }

    /**
     * 需求删除
     *
     * @param
     * @throws
     */
    public void deleteRequirements(List<String> guids) {
        requirementsMapper.deleteByGuids(guids);
    }

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
    public void handle(RequirementsResultDTO resultDTO) {
        List<String> guids = resultDTO.getGuids();
        if (CollectionUtils.isNotEmpty(guids)) {
            List<RequirementsResultPO> resultPOS = new ArrayList<>();
            RequirementsResultPO result = resultDTO.getResult();
            for (String guid : guids) {
                RequirementsResultPO resultPO = new RequirementsResultPO();
                BeanUtils.copyProperties(result, resultPO);
                resultPO.setGuid(guid);

                resultPOS.add(resultPO);
            }

            requirementsResultMapper.

        }
    }
}
