//// ======================================================================
////
////      Copyright (C) 北京国双科技有限公司
////                    http://www.gridsum.com
////
////      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
////      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
////      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
////      散播此文件。
////
////
//// ======================================================================
//
//package org.apache.atlas;
//
//import static javax.swing.text.html.CSS.getAttribute;
//
//import org.apache.atlas.discovery.AtlasDiscoveryService;
//import org.apache.atlas.discovery.AtlasLineageService;
//import org.apache.atlas.exception.AtlasBaseException;
//import org.apache.atlas.model.discovery.AtlasSearchResult;
//import org.apache.atlas.model.discovery.SearchParameters;
//import org.apache.atlas.model.instance.AtlasEntity;
//import org.apache.atlas.model.lineage.AtlasLineageInfo;
//import org.apache.atlas.model.table.Table;
//import org.apache.atlas.repository.store.graph.AtlasEntityStore;
//import org.apache.atlas.web.util.HiveJdbcUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.inject.Inject;
//
//@Component
//public class AtlasScheduler {
//
//    private static Logger log = LoggerFactory.getLogger(AtlasScheduler.class);
//
//    @Inject
//    private AtlasDiscoveryService atlasDiscoveryService;
//
//    @Inject
//    private AtlasEntityStore entitiesStore;
//
//    @Inject
//    private AtlasLineageService atlasLineageService;
//
//    public AtlasScheduler() {
//        log.info("AtlasScheduler");
//    }
//
//    @Scheduled(cron = "0/5 * *  * * ? ")   //每5秒执行一次
//    public void myTest() {
//        try {
//            System.out.println("进入测试");
//            log.info("进入测试atlasScheduler");
//
//            SearchParameters parameters = new SearchParameters();
//            parameters.setTypeName("hive_table");
//            parameters.setOffset(0);
//            parameters.setLimit(10000);
//
//            AtlasSearchResult atlasSearchResult = atlasDiscoveryService.searchWithParameters(parameters);
//            atlasSearchResult.getEntities().stream().map(entity -> {
//                String guid = entity.getGuid();
//                return guid;
//            }).forEach(guid -> {
//                try {
//                    AtlasEntity.AtlasEntityWithExtInfo table = entitiesStore.getById(guid);
//                    AtlasEntity entity = table.getEntity();
//                    Object columns = entity.getAttribute("columns");
//                    String qualifiedName = entity.getAttribute("qualifiedName").toString();
//                    String tableName = qualifiedName.substring(0, qualifiedName.indexOf("@"));
//                    log.info("columns=" + columns);
//
//                    String tableSize = HiveJdbcUtils.tableSize(tableName);
//
//                    List<Table> sourceTableList = new ArrayList<>();
//                    AtlasLineageInfo lineage = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.INPUT, 3);
//                    Map<String, String> toFromGuidMap = new HashMap<>();
//                    lineage.getRelations().stream().forEach(relation -> {
//                        toFromGuidMap.put(relation.getToEntityId(), relation.getFromEntityId());
//                    });
//                    String fromProcessGuid = toFromGuidMap.get(guid);
//                    String sourceTableGuid = toFromGuidMap.get(fromProcessGuid);
//
//                    String sourceTableQualifiedName = lineage.getGuidEntityMap().get(sourceTableGuid).getAttribute("qualifiedName").toString();
//                    String sourceTableName = qualifiedName.substring(0, qualifiedName.indexOf("@"));
//                    String[] split = sourceTableName.split("\\.");
//                    sourceTableList.add(new Table(split[0], split[1]));
//                } catch (AtlasBaseException e) {
//                    log.error(e.getMessage(), e);
//                }
//            });
//
//        } catch (AtlasBaseException e) {
//            log.error(e.getMessage(), e);
//        }
//    }
//
//}
