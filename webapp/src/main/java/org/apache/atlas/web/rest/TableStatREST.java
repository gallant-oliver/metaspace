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
import org.apache.atlas.model.DateType;
import org.apache.atlas.model.PageList;
import org.apache.atlas.model.table.Table;
import org.apache.atlas.model.table.TableSourceCount;
import org.apache.atlas.model.table.TableStat;
import org.apache.atlas.model.table.TableStatRequest;
import org.apache.atlas.repository.tablestat.TableStatService;
import org.apache.atlas.utils.DateUtils;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.atlas.utils.BytesUtils.byteCountByUnit;

@Path("table/stat")
@Singleton
@Service
public class TableStatREST {

    private static final Logger log = LoggerFactory.getLogger(TableStatREST.class);

    @Inject
    private TableStatService tableStatService;

    @Inject
    private AtlasScheduler atlasScheduler;

    @Autowired
    private DiscoveryREST discoveryREST;

    @GET
    @Path("/schedule")
    public void schedule() throws Exception {
        atlasScheduler.insertTableMetadataStat();
    }

    @GET
    @Path("/schedule/{date}")
    public void scheduleDay(@PathParam("date") String date) throws Exception {
        atlasScheduler.insertTableMetadataStat(date);
    }

    @GET
    @Path("/schedule/{date}/{tableName}")
    public void schedule(@PathParam("date") String date, @PathParam("tableName") String tableName) throws Exception {
        List<List<Object>> hiveTables = discoveryREST.searchUsingDSL("name like '*" + tableName + "*' where __state = 'ACTIVE' select __guid orderby __timestamp", "hive_table", "", 1000, 0).getAttributes().getValues();

        if (hiveTables.isEmpty()) {
            log.info("没有找到表{}", tableName);
        }
        for (List<Object> table : hiveTables) {
            String tableId = table.get(0).toString();
            atlasScheduler.insertTableMetadataStat(date, tableId);
        }
    }

    @GET
    @Path("/today/{tableId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableStat today(@PathParam("tableId") String tableId) throws Exception {
        TableStatRequest request = new TableStatRequest(tableId, DateType.DAY.getLiteral(), DateUtils.yesterday(), DateUtils.yesterday(), 0, 10);
        List<TableStat> statList = tableStatService.query(request).getRight();
        TableStat tableStat = new TableStat();
        if (statList != null && !statList.isEmpty()) {
            tableStat = statList.get(0);
        }
        return tableStat;
    }


    @POST
    @Path("/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageList<TableStat> history(TableStatRequest request) throws Exception {
        Pair<Integer, List<TableStat>> pair = tableStatService.query(request);
        List<TableStat> statList = tableStatService.query(request).getRight();
        if (!statList.isEmpty()) {
            addDataNumAndUnit(statList);
        }
        return new PageList<>(request.getOffset(), pair.getLeft(), statList);
    }

    /**
     * 前端折线图需要，统一单位
     *
     * @param statList
     */
    private void addDataNumAndUnit(List<TableStat> statList) {

        statList.sort(new Comparator<TableStat>() {
            @Override
            public int compare(TableStat o1, TableStat o2) {
                return o1.getDataVolumeBytes() > o2.getDataVolumeBytes() ? -1 : 1;
            }
        });
        String unit = statList.get(0).getDataVolume().split(" ")[1];
        statList.forEach(tableStat -> {
            Double dataVolumeBytes = byteCountByUnit(tableStat.getDataVolumeBytes(), unit);
            tableStat.setDataVolumeNum(dataVolumeBytes);
            tableStat.setDataVolumeNumUnit(unit);
        });

        //dataIncrement
        statList.sort(new Comparator<TableStat>() {
            @Override
            public int compare(TableStat o1, TableStat o2) {
                return o1.getDataIncrementBytes() > o2.getDataIncrementBytes() ? -1 : 1;
            }
        });
        String unit2 = statList.get(0).getDataIncrement().split(" ")[1];
        statList.forEach(tableStat -> {
            Double dataIncrementBytes = byteCountByUnit(tableStat.getDataIncrementBytes(), unit2);
            tableStat.setDataIncrementNum(dataIncrementBytes);
            tableStat.setDataIncrementNumUnit(unit2);
        });

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

