package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.FilterConditionDTO;
import io.zeta.metaspace.model.po.requirements.RequirementsColumnPO;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.requirements.RequirementsColumnMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 需求管理 - 过滤条件
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-08
 */
@Slf4j
@Service
public class RequirementColumnService {
    @Autowired
    private RequirementsColumnMapper columnMapper;
    
    /**
     * 批量插入需求的过滤条件
     */
    public void batchInsert(String requirementId, String tableId, List<FilterConditionDTO> filterConditions) {
        Assert.isTrue(StringUtils.isNotBlank(requirementId), "需求ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID不能为空");
        if (CollectionUtils.isEmpty(filterConditions)) {
            log.warn("需求 {} 的过滤条件为空,不执行插入操作.", requirementId);
            return;
        }
        
        List<RequirementsColumnPO> pos = filterConditions.stream()
                .map(v -> RequirementsColumnPO.builder()
                        .guid(UUID.randomUUID().toString())
                        .requirementsId(requirementId)
                        .tableId(tableId)
                        .columnId(v.getColumnId())
                        .operator(v.getOperation().getDesc())
                        .sampleData(v.getSampleData())
                        .description(v.getDescription())
                        .createTime(DateUtils.currentTimestamp())
                        .updateTime(DateUtils.currentTimestamp())
                        .delete(0)
                        .build())
                .collect(Collectors.toList());
        
        columnMapper.batchInsert(pos);
    }
    
    /**
     * 批量更新需求的过滤条件
     * <p>
     * 1.查询以前关联的字段
     * <p>
     * 2.对比出有变化的条目: 需要新增的列;需要删除的列;需要更新的列
     * <p>
     */
    public void batchUpdate(String requirementId, String tableId, List<FilterConditionDTO> filterConditions) {
        Assert.isTrue(StringUtils.isNotBlank(requirementId), "需求ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID不能为空");
        
        if (CollectionUtils.isEmpty(filterConditions)) {
            columnMapper.deleteByRequirementId(requirementId);
            return;
        }
        
        List<RequirementsColumnPO> oldColumnPos = columnMapper.getByRequirementId(requirementId);
    }
}
