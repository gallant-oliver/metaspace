// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package org.apache.atlas.web.rest;

import org.apache.atlas.discovery.AtlasDiscoveryService;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.discovery.AtlasSearchResult;
import org.apache.atlas.model.discovery.SearchParameters;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.table.Table;
import org.apache.atlas.model.table.TableMetadata;
import org.apache.atlas.model.table.TableStat;
import org.apache.atlas.model.table.TableStatRequest;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.web.util.HiveJdbcUtils;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("v2/table/stat")
@Singleton
@Service
public class TableStatREST {

    private static final Logger log = LoggerFactory.getLogger(TableStatREST.class);


    @Inject
    private AtlasDiscoveryService atlasDiscoveryService;

    @Inject
    private AtlasEntityStore entitiesStore;

    @Inject
    private AtlasLineageService atlasLineageService;


    @GET
    @Path("/test")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TableStat> test() throws Exception {

        List<TableStat> ret = new ArrayList<>();
        SearchParameters parameters = new SearchParameters();
        parameters.setTypeName("hive_table");
        parameters.setOffset(0);
        parameters.setLimit(10000);

        //1.遍历所有表
        AtlasSearchResult atlasSearchResult = atlasDiscoveryService.searchWithParameters(parameters);
        List<AtlasEntityHeader> tables = atlasSearchResult.getEntities();
        if (tables != null && !tables.isEmpty()) {
            tables.stream().map(entity -> {
                String guid = entity.getGuid();
                return guid;
            }).forEach(guid -> {
                try {
                    TableStat tableStat = new TableStat();
                    tableStat.setGuid(guid);
                    AtlasEntity.AtlasEntityWithExtInfo table = entitiesStore.getById(guid);

                    AtlasEntity entity = table.getEntity();
                    //字段数量
                    Object columns = entity.getAttribute("columns");
                    if (columns != null) {
                        int fieldNum = ((ArrayList) columns).size();
                        tableStat.setFieldNum(fieldNum);
                    }
                    String qualifiedName = entity.getAttribute("qualifiedName").toString();
                    String tableName = qualifiedName.substring(0, qualifiedName.indexOf("@"));
                    tableStat.setTableName(tableName);

                    TableMetadata metadata = HiveJdbcUtils.metadata(tableName);

                    if (metadata != null) {
                        //表数据量/文件个数
                        String totalSize = metadata.getTotalSize();
                        if (StringUtils.isNotBlank(totalSize)) {
                            tableStat.setDataVolume(totalSize);
                        }
                        String fieldNum = metadata.getNumFiles();
                        if (StringUtils.isNotBlank(fieldNum)) {
                            tableStat.setFileNum(Integer.valueOf(fieldNum));
                        }
                        String recordNum = metadata.getNumRows();
                        if (StringUtils.isNotBlank(recordNum)) {
                            tableStat.setRecordNum(Integer.valueOf(recordNum));
                        }

                        //数据来源表
                        AtlasLineageInfo lineage = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.INPUT, 3);
                        Map<String, List<String>> toFromGuidMap = new HashMap<>();
                        lineage.getRelations().stream().forEach(relation -> {
                            String key = relation.getToEntityId();
                            String value = relation.getFromEntityId();
                            toFromGuidMap.compute(key, new BiFunction<String, List<String>, List<String>>() {
                                @Override
                                public List<String> apply(String oldValue, List<String> list) {
                                    if (list == null) {
                                        list = new ArrayList<>();
                                    }
                                    list.add(value);
                                    return list;
                                }
                            });
                        });
                        List<String> fromProcessGuids = toFromGuidMap.get(guid);
                        if (fromProcessGuids != null) {
                            List<Table> sourceTableList = new ArrayList<>();
                            fromProcessGuids.forEach(fromProcessGuid -> {
                                String sourceTableGuid = toFromGuidMap.get(fromProcessGuid).get(0);
                                String sourceTableQualifiedName = lineage.getGuidEntityMap().get(sourceTableGuid).getAttribute("qualifiedName").toString();
                                String sourceTableName = qualifiedName.substring(0, qualifiedName.indexOf("@"));
                                String[] split = sourceTableName.split("\\.");
                                sourceTableList.add(new Table(split[0], split[1]));
                            });
                            tableStat.setSourceTable(sourceTableList);
                        }

                    } else {
                        log.warn(tableName + " not found metadata");
                    }
                    ret.add(tableStat);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
        return ret;
    }

    @GET
    @Path("/today/{tableName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String today(@PathParam("tableName") String tableName) throws Exception {

        return "{'success':'true'}";
    }

    @POST
    @Path("/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String today(TableStatRequest request) throws Exception {
        return "{'success':'true'}";
    }

}

