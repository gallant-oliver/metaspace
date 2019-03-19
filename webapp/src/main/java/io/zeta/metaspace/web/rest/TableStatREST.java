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

package io.zeta.metaspace.web.rest;

import com.google.common.collect.Lists;
import io.zeta.metaspace.web.scheduler.MetaspaceScheduler;
import io.zeta.metaspace.model.DateType;
import io.zeta.metaspace.model.PageList;
import io.zeta.metaspace.model.table.Table;
import io.zeta.metaspace.model.table.TableSourceCount;
import io.zeta.metaspace.model.table.TableStat;
import io.zeta.metaspace.model.table.TableStatRequest;
import io.zeta.metaspace.repository.tablestat.TableStatService;
import io.zeta.metaspace.utils.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.rest.DiscoveryREST;
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

import static io.zeta.metaspace.utils.BytesUtils.byteCountByUnit;

@Path("table/stat")
@Singleton
@Service
public class TableStatREST {

    private static final Logger log = LoggerFactory.getLogger(TableStatREST.class);

    @Inject
    private TableStatService tableStatService;

    @Inject
    private MetaspaceScheduler metaspaceScheduler;

    @Autowired
    private DiscoveryREST discoveryREST;

    @GET
    @Path("/schedule")
    public void schedule() throws Exception {
        metaspaceScheduler.insertTableMetadataStat();
    }
    @GET
    @Path("/schedule/{date}")
    public void scheduleDay(@PathParam("date") String date) throws Exception {
        metaspaceScheduler.insertTableMetadataStat(date);
    }
    @GET
    @Path("/schedule/today/{tableId}")
    public String scheduleToday(@PathParam("tableId") String tableId) throws Exception {
        try {
            metaspaceScheduler.insertTableMetadataStat(DateUtils.yesterday(), tableId);
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"生成统计信息失败");
        }
        return "success";
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
        List<TableStat> statList = pair.getRight();
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

        String unit = dataVolumeMaxUnit(statList);
        statList.forEach(tableStat -> {
            Double dataVolumeBytes = byteCountByUnit(tableStat.getDataVolumeBytes(), unit);
            tableStat.setDataVolumeNum(dataVolumeBytes);
            tableStat.setDataVolumeNumUnit(unit);
        });

        //dataIncrement
        String dataIncrementUnit = dataVolumeMaxUnit(statList);
        statList.forEach(tableStat -> {
            Double dataIncrementBytes = byteCountByUnit(tableStat.getDataIncrementBytes(), dataIncrementUnit);
            tableStat.setDataIncrementNum(dataIncrementBytes);
            tableStat.setDataIncrementNumUnit(dataIncrementUnit);
        });

    }

    private String dataIncrementMaxUnit(List<TableStat> statList) {
        ArrayList<TableStat> copy = Lists.newArrayList(statList);

        copy.sort(new Comparator<TableStat>() {
            @Override
            public int compare(TableStat o1, TableStat o2) {
                return o1.getDataIncrementBytes() > o2.getDataIncrementBytes() ? -1 : 1;
            }
        });
        return copy.get(0).getDataIncrement().split(" ")[1];
    }

    private String dataVolumeMaxUnit(List<TableStat> statList) {
        ArrayList<TableStat> copy = Lists.newArrayList(statList);

        copy.sort(new Comparator<TableStat>() {
            @Override
            public int compare(TableStat o1, TableStat o2) {
                return o1.getDataVolumeBytes() > o2.getDataVolumeBytes() ? -1 : 1;
            }
        });
        return copy.get(0).getDataVolume().split(" ")[1];
    }

    @POST
    @Path("/sourceTableCount")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TableSourceCount> sourceTableCount(TableStatRequest request) throws Exception {

        List<TableSourceCount> ret = new ArrayList<>();
        Pair<Integer, List<TableStat>> pair = tableStatService.query(request);
        List<TableStat> statList = pair.getRight();
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

