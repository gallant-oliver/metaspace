package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.LineageTrace;
import io.zeta.metaspace.model.metadata.SimpleTaskNode;
import io.zeta.metaspace.model.metadata.TableInfoVo;
import io.zeta.metaspace.model.metadata.TableLineageInfo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author w
 */
public class MetaDateRelationalDateService {


    /**
     * 向上递归遍历血缘
     *
     * @param simpleTaskNodes        需要遍历的list（任务调度传递过来的数据）
     * @param tableInfoVo            outputTable当前层输出表集合（需要找到对应的上inputTable）
     * @param depth                  向上遍历的深度
     * @param lineageTraceSet        血缘关系
     * @param lineageEntities        对应表节点集合（sql与描述也封装为一个节点）
     * @param inputTableInfoVosCache 获取上级表inputTable集合（用于判定唯一guid）
     */
    public static void upTaskNode(List<SimpleTaskNode> simpleTaskNodes, List<TableInfoVo> tableInfoVo, int depth, Set<LineageTrace> lineageTraceSet,
                                  List<TableLineageInfo.LineageEntity> lineageEntities, List<TableInfoVo> inputTableInfoVosCache) {
        if (depth < 1) {
            return;
        }
        List<TableInfoVo> outputTableInfoVos = new ArrayList<>();

        for (TableInfoVo infoVo : tableInfoVo) {
            List<SimpleTaskNode> taskNodes = simpleTaskNodes.stream().filter(r -> {
                TableInfoVo outputTable = r.getOutputTable();
                if (outputTable != null && outputTable.getHost().equals(infoVo.getHost()) && outputTable.getPort().equals(infoVo.getPort())
                        && outputTable.getDatabase().equals(infoVo.getDatabase()) && outputTable.getTable().equals(infoVo.getTable())) {
                    return true;
                }
                return false;
            }).map(r -> {
                LineageTrace inLineageTrace = new LineageTrace();
                LineageTrace outLineageTrace = new LineageTrace();
                String tableGuid = UUID.randomUUID().toString();
                String descGuid = UUID.randomUUID().toString();

                if (!StringUtils.isEmpty(r.getInputTable()) && !StringUtils.isEmpty(r.getOutputTable())) {
                    r.getOutputTable().setGuid(infoVo.getGuid());
                    if (CollectionUtils.isNotEmpty(inputTableInfoVosCache)) {
                        List<TableInfoVo> inputCache = inputTableInfoVosCache.stream().filter(e -> {
                            if (e.getHost().equals(r.getInputTable().getHost()) && e.getPort().equals(r.getInputTable().getPort())
                                    && e.getDatabase().equals(r.getInputTable().getDatabase()) && e.getTable().equals(r.getInputTable().getTable())) {
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(inputCache)) {
                            r.getInputTable().setGuid(inputCache.get(0).getGuid());
                        } else {
                            r.getInputTable().setGuid(tableGuid);
                            inputTableInfoVosCache.add(r.getInputTable());
                        }
                    } else {
                        r.getInputTable().setGuid(tableGuid);
                        inputTableInfoVosCache.add(r.getInputTable());
                    }

                    // 血缘
                    inLineageTrace.setFromEntityId(tableGuid);
                    inLineageTrace.setToEntityId(descGuid);
                    lineageTraceSet.add(inLineageTrace);
                    outLineageTrace.setFromEntityId(descGuid);
                    outLineageTrace.setToEntityId(infoVo.getGuid());
                    lineageTraceSet.add(outLineageTrace);

                    setLineageTableInfoVo(r, lineageEntities, descGuid);

                }
                return r;
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(taskNodes)) {
                List<TableInfoVo> voList = taskNodes.stream().filter(MetaDataService.distinctByKey(SimpleTaskNode::getInputTable)).map(SimpleTaskNode::getInputTable).collect(Collectors.toList());
                outputTableInfoVos.addAll(voList);
            }
        }
        if (CollectionUtils.isNotEmpty(outputTableInfoVos)) {
            outputTableInfoVos = outputTableInfoVos.stream().filter(MetaDataService.distinctByKey(TableInfoVo::getGuid)).collect(Collectors.toList());
        }
        upTaskNode(simpleTaskNodes, outputTableInfoVos, --depth, lineageTraceSet, lineageEntities, inputTableInfoVosCache);
    }


    /**
     * 向上递归遍历血缘
     *
     * @param simpleTaskNodes         需要遍历的list（任务调度传递过来的数据）
     * @param tableInfoVo             inputTable当前层输入表集合（需要找到对应的上outputTable）
     * @param depth                   向上遍历的深度
     * @param lineageTraceSet         血缘关系
     * @param lineageEntities         对应表节点集合（sql与描述也封装为一个节点）
     * @param outputTableInfoVosCache 获取下级表outputTable集合（用于判定唯一guid）
     */
    public static void downTaskNode(List<SimpleTaskNode> simpleTaskNodes, List<TableInfoVo> tableInfoVo, int depth, Set<LineageTrace> lineageTraceSet,
                                    List<TableLineageInfo.LineageEntity> lineageEntities, List<TableInfoVo> outputTableInfoVosCache) {
        if (depth < 1) {
            return;
        }
        List<TableInfoVo> inputTableInfoVos = new ArrayList<>();

        for (TableInfoVo infoVo : tableInfoVo) {
            List<SimpleTaskNode> taskNodes = simpleTaskNodes.stream().filter(r -> {
                TableInfoVo inputTable = r.getInputTable();
                if (inputTable != null && inputTable.getHost().equals(infoVo.getHost()) && inputTable.getPort().equals(infoVo.getPort())
                        && inputTable.getDatabase().equals(infoVo.getDatabase()) && inputTable.getTable().equals(infoVo.getTable())) {
                    return true;
                }
                return false;
            }).map(r -> {
                LineageTrace inLineageTrace = new LineageTrace();
                LineageTrace outLineageTrace = new LineageTrace();
                String tableGuid = UUID.randomUUID().toString();
                String descGuid = UUID.randomUUID().toString();

                if (!StringUtils.isEmpty(r.getInputTable()) && !StringUtils.isEmpty(r.getOutputTable())) {
                    r.getInputTable().setGuid(infoVo.getGuid());
                    if (CollectionUtils.isNotEmpty(outputTableInfoVosCache)) {
                        List<TableInfoVo> outputCache = outputTableInfoVosCache.stream().filter(e -> {
                            if (e.getHost().equals(r.getOutputTable().getHost()) && e.getPort().equals(r.getOutputTable().getPort())
                                    && e.getDatabase().equals(r.getOutputTable().getDatabase()) && e.getTable().equals(r.getOutputTable().getTable())) {
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(outputCache)) {
                            r.getOutputTable().setGuid(outputCache.get(0).getGuid());
                        } else {
                            r.getOutputTable().setGuid(tableGuid);
                            outputTableInfoVosCache.add(r.getOutputTable());
                        }
                    } else {
                        r.getOutputTable().setGuid(tableGuid);
                        outputTableInfoVosCache.add(r.getOutputTable());
                    }

                    // 血缘
                    inLineageTrace.setFromEntityId(infoVo.getGuid());
                    inLineageTrace.setToEntityId(descGuid);
                    lineageTraceSet.add(inLineageTrace);
                    outLineageTrace.setFromEntityId(descGuid);
                    outLineageTrace.setToEntityId(tableGuid);
                    lineageTraceSet.add(outLineageTrace);

                    setLineageTableInfoVo(r, lineageEntities, descGuid);

                }
                return r;
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(taskNodes)) {
                List<TableInfoVo> voList = taskNodes.stream().filter(MetaDataService.distinctByKey(SimpleTaskNode::getOutputTable)).map(SimpleTaskNode::getOutputTable).collect(Collectors.toList());
                inputTableInfoVos.addAll(voList);
            }
        }
        if (CollectionUtils.isNotEmpty(inputTableInfoVos)) {
            inputTableInfoVos = inputTableInfoVos.stream().filter(MetaDataService.distinctByKey(TableInfoVo::getGuid)).collect(Collectors.toList());
        }
        downTaskNode(simpleTaskNodes, inputTableInfoVos, --depth, lineageTraceSet, lineageEntities, outputTableInfoVosCache);
    }

    /**
     * 对节点进行赋值
     *
     * @param r               当前节点关系
     * @param lineageEntities 存入的数据
     * @param descGuid        中间描述guid
     */
    public static void setLineageTableInfoVo(SimpleTaskNode r, List<TableLineageInfo.LineageEntity> lineageEntities, String descGuid) {
        // 添加输入类
        TableLineageInfo.LineageEntity inLineageEntity = new TableLineageInfo.LineageEntity();
        inLineageEntity.setGuid(r.getInputTable().getGuid());
        inLineageEntity.setTableName(r.getInputTable().getTable());
        inLineageEntity.setDbName(r.getInputTable().getDatabase());
        inLineageEntity.setTypeName(r.getInputTable().getType());
        inLineageEntity.setDisplayText(r.getInputTable().getTable());
        inLineageEntity.setProcess(false);
        lineageEntities.add(inLineageEntity);
        // 添加中间描述类
        TableLineageInfo.LineageEntity mediLineageEntity = new TableLineageInfo.LineageEntity();
        mediLineageEntity.setGuid(descGuid);
        mediLineageEntity.setTypeName(r.getOutputTable().getType() + "_process");
        mediLineageEntity.setProcess(true);
        if (!StringUtils.isEmpty(r.getSql())) {
            mediLineageEntity.setTableName(r.getSql());
            mediLineageEntity.setDisplayText(r.getSql());
        } else if (StringUtils.isEmpty(r.getSql()) && !StringUtils.isEmpty(r.getDesc())) {
            mediLineageEntity.setTableName(r.getDesc());
            mediLineageEntity.setDisplayText(r.getDesc());
        }
        lineageEntities.add(mediLineageEntity);
        // 添加输出类
        TableLineageInfo.LineageEntity outLineageEntity = new TableLineageInfo.LineageEntity();
        outLineageEntity.setGuid(r.getOutputTable().getGuid());
        outLineageEntity.setTableName(r.getOutputTable().getTable());
        outLineageEntity.setDbName(r.getOutputTable().getDatabase());
        outLineageEntity.setTypeName(r.getOutputTable().getType());
        outLineageEntity.setDisplayText(r.getOutputTable().getTable());
        outLineageEntity.setProcess(false);
        lineageEntities.add(outLineageEntity);
    }
}
