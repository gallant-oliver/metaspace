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

import org.apache.atlas.AtlasScheduler;
import org.apache.atlas.discovery.AtlasDiscoveryService;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.DateType;
import org.apache.atlas.model.PageList;
import org.apache.atlas.model.discovery.AtlasSearchResult;
import org.apache.atlas.model.discovery.SearchParameters;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.table.Table;
import org.apache.atlas.model.table.TableMetadata;
import org.apache.atlas.model.table.TableSourceCount;
import org.apache.atlas.model.table.TableStat;
import org.apache.atlas.model.table.TableStatRequest;
import org.apache.atlas.model.typedef.AtlasBaseTypeDef;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.tablestat.TableStatService;
import org.apache.atlas.utils.BytesUtils;
import org.apache.atlas.utils.DateUtils;
import org.apache.atlas.web.util.HiveJdbcUtils;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("table/stat")
@Singleton
@Service
public class TableStatREST {

    private static final Logger log = LoggerFactory.getLogger(TableStatREST.class);

    @Inject
    private TableStatService tableStatService;

    @Inject
    private AtlasScheduler atlasScheduler;

    @GET
    @Path("/schedule")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void schedule() throws Exception {
        atlasScheduler.insertTableMetadataStat();
    }

    @GET
    @Path("/today/{tableId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TableStat> today(@PathParam("tableId") String tableId) throws Exception {
        TableStatRequest request = new TableStatRequest(tableId, DateType.DAY.getLiteral(), DateUtils.yesterday(), DateUtils.yesterday(), 0, 10);
        List<TableStat> statList = tableStatService.query(request).getRight();
        return statList;
    }


    @POST
    @Path("/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageList<TableStat> history(TableStatRequest request) throws Exception {
        Pair<Integer, List<TableStat>> pair = tableStatService.query(request);
        List<TableStat> statList = tableStatService.query(request).getRight();
        return new PageList<>(request.getOffset(), pair.getLeft(), statList);
    }

    @POST
    @Path("/sourceTableCount")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TableSourceCount> sourceTableCount(TableStatRequest request) throws Exception {

        List<TableSourceCount> ret = new ArrayList<>();
        Pair<Integer, List<TableStat>> pair = tableStatService.query(request);
        List<TableStat> statList = tableStatService.query(request).getRight();
        Map<String, Long> counted = statList.stream().flatMap(stat -> {
            List<Table> sourceTableList = stat.getSourceTable();
            return sourceTableList.stream().map(sourceTable -> sourceTable.getDatabase() + "." + sourceTable.getTableName());
        }).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        if (counted != null) {
            counted.forEach(new BiConsumer<String, Long>() {
                @Override
                public void accept(String key, Long value) {
                    ret.add(new TableSourceCount(key, value));
                }
            });
        }
        return ret;
    }


}

