package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.LineageTrace;
import io.zeta.metaspace.model.metadata.SimpleTaskNode;
import io.zeta.metaspace.model.metadata.TableInfoVo;
import io.zeta.metaspace.model.metadata.TableLineageInfo;
import io.zeta.metaspace.model.po.tableinfo.TableSourceDataBasePO;
import io.zeta.metaspace.web.model.CommonConstant;
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
     * 将任务调度获取的数据库名数据处理为元数据采集的数据库名
     *
     * @param simpleTaskNodes
     */
    public static void unifyDataBase(List<SimpleTaskNode> simpleTaskNodes) {
        if (CollectionUtils.isNotEmpty(simpleTaskNodes)) {
            simpleTaskNodes.forEach(p -> {
                if ("POSTGRESQL".equals(p.getInputTable().getType())) {
                    String database = p.getInputTable().getDatabase();
                    p.getInputTable().setDatabase(
                            database.substring(database.lastIndexOf('.') + 1)
                    );
                }
                if ("POSTGRESQL".equals(p.getOutputTable().getType())) {
                    String database = p.getOutputTable().getDatabase();
                    p.getOutputTable().setDatabase(
                            database.substring(database.lastIndexOf('.') + 1)
                    );
                }
            });
        }
    }

    /**
     * 任务调度映射元数据信息（获取对应的guid）
     *
     * @param simpleTaskNodes 任务调度节点
     * @param basePOS         元数据信息
     */
    public static void updateExistTableGuid(List<SimpleTaskNode> simpleTaskNodes, List<TableSourceDataBasePO> basePOS) {
        if (CollectionUtils.isNotEmpty(simpleTaskNodes)) {
            if (CollectionUtils.isNotEmpty(basePOS)) {
                // k,v存储元数据采集过的表，值为对应的表Guid
                Map<String, String> mapGuid = basePOS.stream().collect(Collectors.toMap(t -> t.getHost() + ";" + t.getPort() + ";"
                        + t.getDatabase() + ";" + t.getTable(), TableSourceDataBasePO::getGuid, (key1, key2) -> key1));

                simpleTaskNodes.forEach(f -> {
                    String inputKey = f.getInputTable().getHost() + ";" + f.getInputTable().getPort() + ";"
                            + f.getInputTable().getDatabase() + ";" + f.getInputTable().getTable();
                    String outputKey = f.getOutputTable().getHost() + ";" + f.getOutputTable().getPort() + ";"
                            + f.getOutputTable().getDatabase() + ";" + f.getOutputTable().getTable();
                    if (mapGuid.containsKey(inputKey)) {
                        f.getInputTable().setGuid(mapGuid.get(inputKey));
                    }
                    if (mapGuid.containsKey(outputKey)) {
                        f.getOutputTable().setGuid(mapGuid.get(outputKey));
                    }
                });
            }
        }
    }

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
     * @param map                    记录遍历的节点层级数
     */
    public static void upTaskNode(List<SimpleTaskNode> simpleTaskNodes, List<TableInfoVo> tableInfoVo, int depth, Set<LineageTrace> lineageTraceSet,
                                  List<TableLineageInfo.LineageEntity> lineageEntities, List<TableInfoVo> inputTableInfoVosCache,
                                  List<SimpleTaskNode> taskNodeCache, Map<String, Integer> map) {
        if (depth < 1) {
            return;
        }
        List<TableInfoVo> outputTableInfoVos = new ArrayList<>();

        for (TableInfoVo infoVo : tableInfoVo) {
            // 遍历上级描述节点区别与下级（下级总的放在对应实体类中）
            List<String> descGuids = new ArrayList<>();
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
                // 一个节点对应一个详细描述
                String descGuid;

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
                            if (StringUtils.isEmpty(r.getInputTable().getGuid())) {
                                r.getInputTable().setGuid(tableGuid);
                            }
                            inputTableInfoVosCache.add(r.getInputTable());
                        }
                    } else {
                        if (StringUtils.isEmpty(r.getInputTable().getGuid())) {
                            r.getInputTable().setGuid(tableGuid);
                        }
                        inputTableInfoVosCache.add(r.getInputTable());
                    }
                    // 判断描述节点存在相同的值指向输出节点
                    descGuid = judgeDesc(r, descGuids, lineageEntities);
                    // 记录遍历节点层级
                    judgeLayers(r, map, CommonConstant.INPUT_DIRECTION);
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
        upTaskNode(simpleTaskNodes, outputTableInfoVos, --depth, lineageTraceSet, lineageEntities, inputTableInfoVosCache, taskNodeCache, map);
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
                                    List<TableLineageInfo.LineageEntity> lineageEntities, List<TableInfoVo> outputTableInfoVosCache, List<SimpleTaskNode> taskNodeCache,
                                    Map<String, Integer> map) {
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
                            // 判断描述节点存在相同的值指向输出节点
                            descGuid = judgeDesc(r, outputCache.get(0).getDownDescGuids(), lineageEntities);
                        } else {
                            r.getOutputTable().getDownDescGuids().add(descGuid);
                            if (StringUtils.isEmpty(r.getOutputTable().getGuid())) {
                                r.getOutputTable().setGuid(tableGuid);
                            }
                            outputTableInfoVosCache.add(r.getOutputTable());
                        }
                    } else {
                        r.getOutputTable().getDownDescGuids().add(descGuid);
                        if (StringUtils.isEmpty(r.getOutputTable().getGuid())) {
                            r.getOutputTable().setGuid(tableGuid);
                        }
                        outputTableInfoVosCache.add(r.getOutputTable());
                    }
                    // 记录遍历节点层级
                    judgeLayers(r, map, CommonConstant.OUTPUT_DIRECTION);
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
        downTaskNode(simpleTaskNodes, inputTableInfoVos, --depth, lineageTraceSet, lineageEntities, outputTableInfoVosCache, taskNodeCache, map);
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

    /**
     * 判断描述
     * 指向同一个节点的描述是否相同
     * 相同者合并，负责新加一个描述节点
     *
     * @param node            当前节点
     * @param descGuids       指向的当前节点
     * @param lineageEntities 已遍历的所有节点
     * @return 返回对应的描述节点guid
     */
    public static String judgeDesc(SimpleTaskNode node, List<String> descGuids, List<TableLineageInfo.LineageEntity> lineageEntities) {
        String descGuid = UUID.randomUUID().toString();
        String desc;
        if (!StringUtils.isEmpty(node.getSql())) {
            desc = node.getSql();
        } else if (StringUtils.isEmpty(node.getSql()) && !StringUtils.isEmpty(node.getDesc())) {
            desc = node.getDesc();
        } else {
            desc = "";
        }
        List<String> descGuidList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(descGuids)) {
            for (String guid : descGuids) {
                List<String> collect = lineageEntities.stream().filter(r -> guid.equals(r.getGuid()) && desc.equals(r.getTableName()))
                        .map(TableLineageInfo.LineageEntity::getGuid).collect(Collectors.toList());
                descGuidList.addAll(collect);
            }
        }
        if (CollectionUtils.isNotEmpty(descGuidList)) {
            descGuid = descGuidList.get(0);
        } else {
            descGuids.add(descGuid);
        }
        return descGuid;
    }

    /**
     * 判断并遍历添加层级
     *
     * @param s         当前任务调度节点
     * @param map       map对应的key为guid与value
     * @param direction 遍历的方向
     */
    public static void judgeLayers(SimpleTaskNode s, Map<String, Integer> map, String direction) {
        // 向上判断
        if (CommonConstant.INPUT_DIRECTION.equals(direction)) {
            // 如果输出表已存在
            Integer out = map.get(s.getOutputTable().getGuid());
            int sum = out + 1;
            if (!map.containsKey(s.getInputTable().getGuid())) {
                map.put(s.getInputTable().getGuid(), sum);
            } else {
                Integer in = map.get(s.getInputTable().getGuid());
                if (sum > in) {
                    map.put(s.getInputTable().getGuid(), sum);
                }
            }
        }
        // 向下判断
        if (CommonConstant.OUTPUT_DIRECTION.equals(direction)) {
            // 如果输入表已存在
            Integer in = map.get(s.getInputTable().getGuid());
            int sum = in + 1;
            if (map.containsKey(s.getOutputTable().getGuid())) {
                Integer out = map.get(s.getOutputTable().getGuid());
                if (sum > out) {
                    map.put(s.getOutputTable().getGuid(), sum);
                }
            } else {
                map.put(s.getOutputTable().getGuid(), ++in);
            }
        }
    }

    /**
     * 删除多余层级
     *
     * @param tableLineageInfo 所有遍历过的（关联会超过指定层级）
     * @param map              map对应的key为guid与value
     * @param tableInfo        开始节点
     * @param direction        遍历的层级
     */
    public static void removeExtraLayers(TableLineageInfo tableLineageInfo, Map<String, Integer> map, TableInfoVo tableInfo, String direction) {
        Iterator it = map.entrySet().iterator();
        List<String> beginList = new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (value.equals(tableInfo.getDepth())) {
                beginList.add(key);
            }
        }
        Set<LineageTrace> relations = tableLineageInfo.getRelations();
        // 获取超过当前层级的guid
        if (CollectionUtils.isNotEmpty(beginList)) {
            while (CollectionUtils.isNotEmpty(beginList)) {
                // 遍历删除描述前部分
                beginList = fristExtraLayer(beginList, relations, direction);
                if (CollectionUtils.isEmpty(beginList)) {
                    break;
                }
                // 遍历删除描述后部分
                List<LineageTrace> all = lineageTraceList(beginList, relations, direction);
                if (CollectionUtils.isEmpty(all)) {
                    break;
                }
                List<String> entity = null;
                if (CommonConstant.INPUT_DIRECTION.equals(direction)) {
                    entity = all.stream().map(LineageTrace::getFromEntityId).collect(Collectors.toList());
                    tableLineageInfo.getRelations().removeAll(all);
                }
                if (CommonConstant.OUTPUT_DIRECTION.equals(direction)) {
                    entity = all.stream().map(LineageTrace::getToEntityId).collect(Collectors.toList());
                    tableLineageInfo.getRelations().removeAll(all);
                }
                beginList = entity;
            }
        }
    }

    /**
     * 获取描述节点前要删除的关系
     *
     * @param beginList 开始
     * @param relations 对应关联数组
     * @param direction 对应的遍历方向
     * @return 描述节点集合
     */
    public static List<String> fristExtraLayer(List<String> beginList, Set<LineageTrace> relations, String direction) {
        List<LineageTrace> fristAll = lineageTraceList(beginList, relations, direction);
        List<LineageTrace> oneAll = new ArrayList<>();
        List<String> oneList = null;
        if (CommonConstant.INPUT_DIRECTION.equals(direction)) {
            if (CollectionUtils.isNotEmpty(fristAll)) {
                oneList = fristAll.stream().map(LineageTrace::getFromEntityId)
                        .filter(MetaDataService.distinctByKey(String::toString)).collect(Collectors.toList());
                for (String guid : oneList) {
                    List<LineageTrace> collect = relations.stream().filter(r -> r.getFromEntityId().equals(guid)).collect(Collectors.toList());
                    oneAll.addAll(collect);
                }
                relations.removeAll(oneAll);
            }

        }
        if (CommonConstant.OUTPUT_DIRECTION.equals(direction)) {
            if (CollectionUtils.isNotEmpty(fristAll)) {
                oneList = fristAll.stream().map(LineageTrace::getToEntityId)
                        .filter(MetaDataService.distinctByKey(String::toString)).collect(Collectors.toList());
                for (String guid : oneList) {
                    List<LineageTrace> collect = relations.stream().filter(r -> r.getToEntityId().equals(guid)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect)) {
                        oneAll.addAll(collect);
                    }
                }
                relations.removeAll(oneAll);
            }
        }
        return oneList;
    }

    /**
     * 遍历关联数组
     *
     * @param beginList 包含的guid数组
     * @param relations 总的关联数组
     * @param direction 对应的遍历方向
     * @return 对应遍历方向的下一层所有节点段
     */
    public static List<LineageTrace> lineageTraceList(List<String> beginList, Set<LineageTrace> relations, String direction) {
        List<LineageTrace> all = new ArrayList<>();
        if (CommonConstant.INPUT_DIRECTION.equals(direction)) {
            for (String guid : beginList) {
                List<LineageTrace> collect = relations.stream().filter(r -> r.getToEntityId().equals(guid)).collect(Collectors.toList());
                all.addAll(collect);
            }
        }
        if (CommonConstant.OUTPUT_DIRECTION.equals(direction)) {
            for (String guid : beginList) {
                List<LineageTrace> collect = relations.stream().filter(r -> r.getFromEntityId().equals(guid)).collect(Collectors.toList());
                all.addAll(collect);
            }
        }
        return all;
    }

}
