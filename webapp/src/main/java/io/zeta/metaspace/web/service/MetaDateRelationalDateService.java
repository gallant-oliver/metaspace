package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.LineageTrace;
import io.zeta.metaspace.model.metadata.SimpleTaskNode;
import io.zeta.metaspace.model.metadata.TableInfoVo;
import io.zeta.metaspace.model.metadata.TableLineageInfo;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
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
     * @param taskNodeCache          记录遍历过的节点缓存（用于判定是否存在死循环）
     */
    public static void upTaskNode(List<SimpleTaskNode> simpleTaskNodes, List<TableInfoVo> tableInfoVo, int depth, Set<LineageTrace> lineageTraceSet,
                                  List<TableLineageInfo.LineageEntity> lineageEntities, List<TableInfoVo> inputTableInfoVosCache, List<SimpleTaskNode> taskNodeCache) {
        if (depth < 1) {
            return;
        }
        List<TableInfoVo> outputTableInfoVos = new ArrayList<>();

        for (TableInfoVo infoVo : tableInfoVo) {
            // 一个节点对应一个详细描述
            String descGuid = UUID.randomUUID().toString();
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

                // 判断是否出现死循环血缘
                judgeInfiniteLoop(taskNodeCache, r);

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

                    if (!judgeAlike(taskNodeCache, r)) {
                        // 添加缓存
                        taskNodeCache.add(r);
                        // 血缘
                        inLineageTrace.setFromEntityId(r.getInputTable().getGuid());
                        inLineageTrace.setToEntityId(descGuid);
                        lineageTraceSet.add(inLineageTrace);
                        outLineageTrace.setFromEntityId(descGuid);
                        outLineageTrace.setToEntityId(infoVo.getGuid());
                        lineageTraceSet.add(outLineageTrace);

                        setLineageTableInfoVo(r, lineageEntities, descGuid);
                    }

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
        upTaskNode(simpleTaskNodes, outputTableInfoVos, --depth, lineageTraceSet, lineageEntities, inputTableInfoVosCache, taskNodeCache);
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
     * @param taskNodeCache           记录遍历过的节点缓存（用于判定是否存在死循环）
     */
    public static void downTaskNode(List<SimpleTaskNode> simpleTaskNodes, List<TableInfoVo> tableInfoVo, int depth, Set<LineageTrace> lineageTraceSet,
                                    List<TableLineageInfo.LineageEntity> lineageEntities, List<TableInfoVo> outputTableInfoVosCache, List<SimpleTaskNode> taskNodeCache) {
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

                // 判断是否出现死循环血缘
                judgeInfiniteLoop(taskNodeCache, r);

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
                            descGuid = outputCache.get(0).getDescGuid();
                        } else {
                            r.getOutputTable().setDescGuid(descGuid);
                            r.getOutputTable().setGuid(tableGuid);
                            outputTableInfoVosCache.add(r.getOutputTable());
                        }
                    } else {
                        r.getOutputTable().setDescGuid(descGuid);
                        r.getOutputTable().setGuid(tableGuid);
                        outputTableInfoVosCache.add(r.getOutputTable());
                    }

                    if (!judgeAlike(taskNodeCache, r)) {
                        // 添加缓存
                        taskNodeCache.add(r);
                        // 血缘
                        inLineageTrace.setFromEntityId(infoVo.getGuid());
                        inLineageTrace.setToEntityId(descGuid);
                        lineageTraceSet.add(inLineageTrace);
                        outLineageTrace.setFromEntityId(descGuid);
                        outLineageTrace.setToEntityId(r.getOutputTable().getGuid());
                        lineageTraceSet.add(outLineageTrace);

                        setLineageTableInfoVo(r, lineageEntities, descGuid);
                    }

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
        downTaskNode(simpleTaskNodes, inputTableInfoVos, --depth, lineageTraceSet, lineageEntities, outputTableInfoVosCache, taskNodeCache);
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
        } else {
            mediLineageEntity.setTableName("");
            mediLineageEntity.setDisplayText("");
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

    /**
     * 判断是否出现死循环血缘
     *
     * @param taskNodeCache  缓存出现过的节点
     * @param simpleTaskNode 当前节点
     */
    public static void judgeInfiniteLoop(List<SimpleTaskNode> taskNodeCache, SimpleTaskNode simpleTaskNode) {
        if (CollectionUtils.isNotEmpty(taskNodeCache)) {
            List<SimpleTaskNode> nodes = taskNodeCache.stream().filter(t -> {
                TableInfoVo outTable = t.getOutputTable();
                TableInfoVo inTable = t.getInputTable();
                TableInfoVo inputTable = simpleTaskNode.getInputTable();
                TableInfoVo outputTable = simpleTaskNode.getOutputTable();
                if (outTable != null && inTable != null && outputTable != null && inputTable != null) {
                    if (outTable.getHost().equals(inputTable.getHost()) && outTable.getPort().equals(inputTable.getPort())
                            && outTable.getDatabase().equals(inputTable.getDatabase()) && outTable.getTable().equals(inputTable.getTable())
                            && inTable.getHost().equals(outputTable.getHost()) && inTable.getPort().equals(outputTable.getPort())
                            && inTable.getDatabase().equals(outputTable.getDatabase()) && inTable.getTable().equals(outputTable.getTable())) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(nodes)) {
                throw new AtlasBaseException(AtlasErrorCode.INVALID_STRUCT_VALUE, "表结构数据异常，血缘出现死循环");
            }
            List<TableInfoVo> tableInfos = new ArrayList<>();
            tableInfos.add(simpleTaskNode.getInputTable());
            int flag = 1;
            while (true) {
                List<TableInfoVo> tableCache = new ArrayList<>();
                for (TableInfoVo tableInfo : tableInfos) {
                    tableCache.addAll(taskNodeCache.stream().filter(n -> {
                        TableInfoVo outputTable = n.getOutputTable();
                        return outputTable.getHost().equals(tableInfo.getHost()) && outputTable.getPort().equals(tableInfo.getPort())
                                && outputTable.getDatabase().equals(tableInfo.getDatabase()) && outputTable.getTable().equals(tableInfo.getTable());
                    }).map(SimpleTaskNode::getInputTable).collect(Collectors.toList()));
                }
                if (CollectionUtils.isEmpty(tableCache)) {
                    return;
                }
                List<TableInfoVo> info = tableCache.stream().filter(s -> {
                    return s.getHost().equals(simpleTaskNode.getOutputTable().getHost()) && s.getPort().equals(simpleTaskNode.getOutputTable().getPort())
                            && s.getDatabase().equals(simpleTaskNode.getOutputTable().getDatabase()) && s.getPort().equals(simpleTaskNode.getOutputTable().getDatabase());
                }).collect(Collectors.toList());
                // 存在则死循环
                if (CollectionUtils.isNotEmpty(info)) {
                    throw new AtlasBaseException(AtlasErrorCode.INVALID_STRUCT_VALUE, "表结构数据异常，血缘出现死循环");
                }
                tableInfos = tableCache.stream().filter(MetaDataService.distinctByKey(TableInfoVo::getGuid)).collect(Collectors.toList());
                // 是否死循环
                ++flag;
                if (flag > 100) {
                    throw new AtlasBaseException(AtlasErrorCode.INVALID_STRUCT_VALUE, "表结构数据异常，血缘出现死循环");
                }
            }
        }
    }

    /**
     * 判断当前节点是否出现重复输入输出表
     *
     * @param taskNodeCache  缓存出现过的节点
     * @param simpleTaskNode 当前节点
     */
    public static boolean judgeAlike(List<SimpleTaskNode> taskNodeCache, SimpleTaskNode simpleTaskNode) {
        if (CollectionUtils.isNotEmpty(taskNodeCache)) {
            List<SimpleTaskNode> nodes = taskNodeCache.stream().filter(t -> {
                TableInfoVo outTable = t.getOutputTable();
                TableInfoVo inTable = t.getInputTable();
                TableInfoVo inputTable = simpleTaskNode.getInputTable();
                TableInfoVo outputTable = simpleTaskNode.getOutputTable();
                if (outTable != null && inTable != null && outputTable != null && inputTable != null) {
                    if (inTable.getHost().equals(inputTable.getHost()) && inTable.getPort().equals(inputTable.getPort())
                            && inTable.getDatabase().equals(inputTable.getDatabase()) && inTable.getTable().equals(inputTable.getTable())
                            && outTable.getHost().equals(outputTable.getHost()) && outTable.getPort().equals(outputTable.getPort())
                            && outTable.getDatabase().equals(outputTable.getDatabase()) && outTable.getTable().equals(outputTable.getTable())) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(nodes)) {
                return true;
            }
        }
        return false;
    }
}
