package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.requirements.FilterConditionDTO;
import io.zeta.metaspace.model.po.requirements.RequirementsColumnPO;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.requirements.RequirementsColumnMapper;
import io.zeta.metaspace.web.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
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
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(String requirementId, String tableId, List<FilterConditionDTO> filterConditions) {
        Assert.isTrue(StringUtils.isNotBlank(requirementId), "需求ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID不能为空");
    
        if (CollectionUtils.isEmpty(filterConditions)) {
            columnMapper.deleteByRequirementId(requirementId);
            return;
        }
    
        Set<RequirementsColumnPO> toUpdateSet = new HashSet<>();
        Set<RequirementsColumnPO> toInsertSet = new HashSet<>();
        Map<String, RequirementsColumnPO> oldColumnId2PoMap = columnMapper.getByRequirementId(requirementId)
                .stream()
                .collect(Collectors.toMap(RequirementsColumnPO::getColumnId, Function.identity()));
        Set<String> newColumnIdsSet = filterConditions.stream()
                .filter(Objects::nonNull)
                .map(filter -> RequirementsColumnPO.builder()
                        .guid(UUID.randomUUID().toString())
                        .requirementsId(requirementId)
                        .tableId(tableId)
                        .columnId(filter.getColumnId())
                        .operator(filter.getOperation().getDesc())
                        .sampleData(filter.getSampleData())
                        .description(filter.getDescription())
                        .createTime(DateUtils.currentTimestamp())
                        .updateTime(DateUtils.currentTimestamp())
                        .delete(0)
                        .build())
                .peek(po -> ObjectUtils.isTrueThenElseThen(po.getColumnId(),
                        oldColumnId2PoMap::containsKey,
                        v -> {
                            RequirementsColumnPO oldOne = oldColumnId2PoMap.get(v);
                            // 内容对比依赖复写的equals
                            if (!Objects.equals(po, oldOne)) {
                                // 回写guid
                                po.setGuid(oldOne.getGuid());
                                toUpdateSet.add(po);
                            }
                        },
                        v -> toInsertSet.add(po)
                ))
                .map(RequirementsColumnPO::getColumnId)
                .collect(Collectors.toSet());
    
        Set<String> toDeleteIdsSet = oldColumnId2PoMap.entrySet()
                .stream()
                .filter(entry -> !newColumnIdsSet.contains(entry.getKey()))
                .map(entry -> entry.getValue().getGuid())
                .collect(Collectors.toSet());
    
        // 写库
        ObjectUtils.isTrueThen(toDeleteIdsSet,
                CollectionUtils::isNotEmpty,
                v -> columnMapper.batchDeleteByPrimaryKey(v));
        ObjectUtils.isTrueThen(toInsertSet,
                CollectionUtils::isNotEmpty,
                v -> columnMapper.batchInsert(v));
        ObjectUtils.isTrueThen(toUpdateSet,
                CollectionUtils::isNotEmpty,
                v -> columnMapper.batchUpdate(v));
    }
}
