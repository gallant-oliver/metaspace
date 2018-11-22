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

package org.apache.atlas;

import org.apache.atlas.discovery.AtlasDiscoveryService;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.DateType;
import org.apache.atlas.model.discovery.AtlasSearchResult;
import org.apache.atlas.model.discovery.SearchParameters;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.table.Table;
import org.apache.atlas.model.table.TableMetadata;
import org.apache.atlas.model.table.TableStat;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.tablestat.TableStatService;
import org.apache.atlas.utils.BytesUtils;
import org.apache.atlas.utils.DateUtils;
import org.apache.atlas.web.util.AdminUtils;
import org.apache.atlas.web.util.HiveJdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.inject.Inject;

@EnableScheduling
@Component
public class AtlasScheduler {

    private static Logger log = LoggerFactory.getLogger(AtlasScheduler.class);

    @Inject
    private AtlasDiscoveryService atlasDiscoveryService;

    @Inject
    private AtlasEntityStore entitiesStore;

    @Inject
    private AtlasLineageService atlasLineageService;

    @Inject
    private TableStatService tableStatService;


    public AtlasScheduler() {
    }

    @Scheduled(cron = "0 0 23 * * ?")   //每天晚上11点
    public void insertTableMetadataStat() throws Exception {
        log.info("scheduler start");
        List<AtlasEntityHeader> tables = allTable();
        String date = DateUtils.today();
        List<TableStat> tableStatList = buildTableStatList(tables, date);
        tableStatService.add(tableStatList);
        log.info("scheduler end");
    }

    /**
     * 指定日期所有表
     *
     * @param date
     * @throws Exception
     */
    public void insertTableMetadataStat(String date) throws Exception {
        log.info("scheduler start");
        List<AtlasEntityHeader> tables = allTable();
        List<TableStat> tableStatList = buildTableStatList(tables, date);
        tableStatService.add(tableStatList);
        log.info("scheduler end");
    }

    /**
     * 指定日期、指定表的元数据
     *
     * @param date
     * @param tableId
     * @throws Exception
     */
    public void insertTableMetadataStat(String date, String tableId) throws Exception {
        log.info("scheduler start");
        List<TableStat> tableStatList = buildTableStat(tableId, date);
        tableStatService.add(tableStatList);
        log.info("scheduler end");
    }


    /**
     * 构建指定日期所有元数据表
     * @param tables
     * @param date
     * @return
     */
    private List<TableStat> buildTableStatList(List<AtlasEntityHeader> tables, String date) {
        List<TableStat> tableStatList = new ArrayList<>();
        if (tables != null && !tables.isEmpty()) {
            log.info("table amount {}", tables.size());
            for (int i = 0; i < tables.size(); i++) {
                int totalCnt = tables.size();
                String tableId = tables.get(i).getGuid();
                String tableName = tables.get(i).getDisplayText();
                try {
                    log.info("add tableStat start {} ,index at {}/{}", tableName, i, totalCnt);
                    tableStatList.addAll(buildTableStat(tableId, date));
                    log.info("add tableStat done {} ,index at {}/{}", tableName, i, totalCnt);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return tableStatList;
    }

    /**
     * 所有元数据表
     * @return
     * @throws AtlasBaseException
     */
    private List<AtlasEntityHeader> allTable() throws AtlasBaseException {
        SearchParameters parameters = new SearchParameters();
        parameters.setTypeName("hive_table");
        parameters.setOffset(0);
        parameters.setLimit(10000);
        AtlasSearchResult atlasSearchResult = atlasDiscoveryService.searchWithParameters(parameters);
        return atlasSearchResult.getEntities();
    }


    /**
     * @param tableId guid
     * @param date    yyyy-MM-dd
     * @return
     * @throws Exception
     */
    private List<TableStat> buildTableStat(String tableId, String date) throws Exception {
        List<TableStat> tableStatList = new ArrayList<>();
        TableStat tableStat = new TableStat();
        tableStat.setTableId(tableId);
        AtlasEntity.AtlasEntityWithExtInfo table = entitiesStore.getById(tableId);
        AtlasEntity entity = table.getEntity();
        //字段数量
        Object columns = entity.getAttribute("columns");
        if (columns != null) {
            Integer fieldNum = ((ArrayList) columns).size();
            tableStat.setFieldNum(fieldNum);
        }
        //表名
        String qualifiedName = entity.getAttribute("qualifiedName").toString();
        String displayName = entity.getAttribute("name").toString();
        String tableName = qualifiedName.substring(0, qualifiedName.indexOf("@"));

        tableStat.setTableName(displayName);

        TableMetadata metadata = HiveJdbcUtils.metadata(tableName);
        //表数据量
        long totalSize = metadata.getTotalSize();
        tableStat.setDataVolume(BytesUtils.humanReadableByteCount(Long.valueOf(totalSize)));
        tableStat.setDataVolumeBytes(totalSize);
        //文件个数
        int fieldNum = metadata.getNumFiles();
        tableStat.setFileNum(fieldNum);
        //数据行数
        long recordNum = metadata.getNumRows();
        tableStat.setRecordNum(recordNum);

        //数据来源表
        AtlasLineageInfo lineage = atlasLineageService.getAtlasLineageInfo(tableId, AtlasLineageInfo.LineageDirection.INPUT, 3);
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
        List<String> fromProcessGuids = toFromGuidMap.get(tableId);
        if (fromProcessGuids != null) {
            List<Table> sourceTableList = new ArrayList<>();
            fromProcessGuids.forEach(fromProcessGuid -> {
                String sourceTableGuid = toFromGuidMap.get(fromProcessGuid).get(0);
                AtlasEntityHeader entityHeader = lineage.getGuidEntityMap().get(sourceTableGuid);
                String sourceTableQualifiedName = entityHeader.getAttribute("qualifiedName").toString();
                String sourceTableTypeName = entityHeader.getTypeName();
                String sourceDisplayName = entityHeader.getAttribute("name").toString();
                String sourceTableName = sourceTableQualifiedName.substring(0, sourceTableQualifiedName.indexOf("@"));
                if ("hdfs_path".equals(sourceTableTypeName)) {
                    sourceTableList.add(new Table(sourceTableGuid, "", sourceTableName));
                } else if ("hive_table".equals(sourceTableTypeName)) {
                    String[] split = sourceTableName.split("\\.");
                    sourceTableList.add(new Table(sourceTableGuid, split[0], sourceDisplayName));
                }
            });
            tableStat.setSourceTable(sourceTableList);
        }

        //数据增量
        Map<String, Long> lastDataVolumn = tableStatService.lastDataVolumn(tableId, DateUtils.yesterday());
        tableStat.setDateType(DateType.DAY.getLiteral());
        long dayIncrement = tableStat.getDataVolumeBytes() - lastDataVolumn.get("day");
        tableStat.setDataIncrementBytes(dayIncrement);
        tableStat.setDataIncrement(BytesUtils.humanReadableByteCount(dayIncrement));
        tableStat.setDate(date);
        tableStatList.add(tableStat);

        TableStat tableStatMonth = (TableStat) tableStat.clone();
        tableStatMonth.setDateType(DateType.MONTH.getLiteral());
        long monthIncrement = tableStat.getDataVolumeBytes() - lastDataVolumn.get("month");
        tableStatMonth.setDataIncrementBytes(monthIncrement);
        tableStatMonth.setDataIncrement(BytesUtils.humanReadableByteCount(monthIncrement));
        tableStatMonth.setDate(DateUtils.month(date));
        tableStatList.add(tableStatMonth);

        TableStat tableStatYear = (TableStat) tableStat.clone();
        tableStatYear.setDateType(DateType.YEAR.getLiteral());
        long yearIncrement = tableStat.getDataVolumeBytes() - lastDataVolumn.get("year");
        tableStatYear.setDataIncrementBytes(yearIncrement);
        tableStatYear.setDataIncrement(BytesUtils.humanReadableByteCount(yearIncrement));
        tableStatYear.setDate(DateUtils.year(date));
        tableStatList.add(tableStatYear);
        return tableStatList;
    }

}
