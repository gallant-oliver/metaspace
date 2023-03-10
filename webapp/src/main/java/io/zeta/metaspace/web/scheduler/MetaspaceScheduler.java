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

package io.zeta.metaspace.web.scheduler;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.DateType;
import io.zeta.metaspace.model.table.Table;
import io.zeta.metaspace.model.table.TableMetadata;
import io.zeta.metaspace.model.table.TableStat;
import io.zeta.metaspace.repository.tablestat.TableStatService;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.BytesUtils;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.util.BaseHiveEvent;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.discovery.AtlasDiscoveryService;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.discovery.AtlasSearchResult;
import org.apache.atlas.model.discovery.SearchParameters;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@EnableScheduling
@Component
public class MetaspaceScheduler {

    private static Logger log = LoggerFactory.getLogger(MetaspaceScheduler.class);

    @Inject
    private AtlasDiscoveryService atlasDiscoveryService;

    @Inject
    private AtlasEntityStore entitiesStore;

    @Inject
    private AtlasLineageService atlasLineageService;

    @Inject
    private TableStatService tableStatService;

    @Inject
    MetaspaceGremlinQueryService metaspaceEntityService;


    public MetaspaceScheduler() {
    }

    //每天晚上12点
    public void insertTableMetadataStat() throws Exception {
        log.info("scheduler start");
        //List<AtlasEntityHeader> tables = allActiveTable();
        List<AtlasEntityHeader> tables = allActiveTableV2();
        String date = DateUtils.yesterday();
        insertTableStatList(tables, date);
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
        List<AtlasEntityHeader> tables = allActiveTable();
        insertTableStatList(tables, date);
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
        log.info("scheduler start:{},{}", date, tableId);
        List<TableStat> tableStatList = buildTableStat(tableId, date);
        tableStatService.add(tableStatList);
        log.info("scheduler end:{},{}", date, tableId);
    }


    /**
     * 构建指定日期所有元数据表
     *
     * @param tables
     * @param date
     * @return
     */
    private void insertTableStatList(List<AtlasEntityHeader> tables, String date) {
        log.info("start insert into table_stat");
        if (tables != null && !tables.isEmpty()) {
            log.info("table amount {}", tables.size());
            int totalCnt = tables.size();
            for (int i = 0; i < totalCnt; i++) {
                String tableId = tables.get(i).getGuid();
                String tableName = tables.get(i).getDisplayText();
                try {
                    log.info("add tableStat start {} ,index at {}/{}", tableName, i, totalCnt);
                    List<TableStat> statList = buildTableStat(tableId, date);
                    tableStatService.add(statList);
                    log.info("add tableStat done {} ,index at {}/{}", tableName, i, totalCnt);
                } catch (Exception e) {
                    log.info("add tableStat failed {} ,index at {}/{}", tableName, i, totalCnt);
                    log.warn(e.getMessage(), e);
                }
            }
        }else {
            log.warn("tables is null");
        }
    }

    /**
     * 所有元数据表
     *
     * @return
     * @throws AtlasBaseException
     */
    private List<AtlasEntityHeader> allActiveTable() throws AtlasBaseException {
        SearchParameters parameters = new SearchParameters();
        parameters.setTypeName("hive_table");
        parameters.setOffset(0);
        parameters.setLimit(10000);
        AtlasSearchResult atlasSearchResult = atlasDiscoveryService.searchWithParameters(parameters);
        List<AtlasEntityHeader> ret = atlasSearchResult.getEntities().stream().filter(new Predicate<AtlasEntityHeader>() {
            @Override
            public boolean test(AtlasEntityHeader entity) {
                return entity.getStatus() == AtlasEntity.Status.ACTIVE;
            }
        }).collect(Collectors.toList());
        return ret;
    }

    private List<AtlasEntityHeader> allActiveTableV2() throws AtlasBaseException {
        List<AtlasEntityHeader> allTables = metaspaceEntityService.getAllTables();
        List<AtlasEntityHeader> ret = allTables.stream().filter(table -> table.getStatus() == AtlasEntity.Status.ACTIVE).collect(Collectors.toList());
        log.info("found {} tables,filter surplus {} tables ", allTables.size(),ret.size());
        return ret;
    }


    /**
     * @param tableId guid
     * @param date    yyyy-MM-dd
     * @return
     * @throws Exception
     */
    private List<TableStat> buildTableStat(String tableId, String date) throws Exception {
        try {
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
            String qualified = "qualifiedName";
            if (null == entity.getAttribute(qualified)) {
                return tableStatList;
            }
            //表名
            String qualifiedName = entity.getAttribute(qualified).toString();
            String displayName = entity.getAttribute("name").toString();
            String dbAndtableName = qualifiedName.substring(0, qualifiedName.indexOf("@"));

            tableStat.setTableName(displayName);


            Object parameters = entity.getAttribute(BaseHiveEvent.ATTRIBUTE_PARAMETERS);
            Object totalSizeObj = null;
            Object numFilesObj = null;
            if (parameters != null) {
                Map params = (Map) parameters;
                //表数据量
                totalSizeObj = params.get("totalSize");
                long totalSize = totalSizeObj != null ? Long.parseLong(totalSizeObj.toString()) : 0;
                tableStat.setDataVolume(BytesUtils.humanReadableByteCount(totalSize));
                tableStat.setDataVolumeBytes(totalSize);
                //文件个数
                numFilesObj = params.get("numFiles");
                long numFiles = numFilesObj != null ? Long.parseLong(numFilesObj.toString()) : 0;
                tableStat.setFileNum(numFiles);
            }
            if (null == totalSizeObj || null == numFilesObj) {
                TableMetadata metadata = systemMetadata(dbAndtableName);
                //表数据量
                long totalSize = metadata.getTotalSize();
                tableStat.setDataVolume(BytesUtils.humanReadableByteCount(totalSize));
                tableStat.setDataVolumeBytes(totalSize);
                //文件个数
                long fieldNum = metadata.getNumFiles();
                tableStat.setFileNum(fieldNum);
            }

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
                    String hdfsPath = "hdfs_path";
                    if (hdfsPath.equals(sourceTableTypeName)) {
                        sourceTableList.add(new Table(sourceTableGuid, "", sourceTableName));
                    } else {
                        String hiveTable = "hive_table";
                        if (hiveTable.equals(sourceTableTypeName)) {
                            String[] split = sourceTableName.split("\\.");
                            sourceTableList.add(new Table(sourceTableGuid, split[0], sourceDisplayName));
                        }
                    }
                });
                tableStat.setSourceTable(sourceTableList);
            }

            //数据增量
            Map<String, Long> lastDataVolumn = tableStatService.lastDataVolumn(tableId, date);
            tableStat.setDateType(DateType.DAY.getLiteral());
            long dayIncrement = tableStat.getDataVolumeBytes() - lastDataVolumn.get("day");
            tableStat.setDataIncrementBytes(dayIncrement);
            tableStat.setDataIncrement(BytesUtils.humanReadableByteCount(dayIncrement));
            tableStat.setDate(date);
            log.info("day tableStat={}", tableStat);
            tableStatList.add(tableStat);

            TableStat tableStatMonth = (TableStat) tableStat.clone();
            tableStatMonth.setDateType(DateType.MONTH.getLiteral());
            long monthIncrement = tableStat.getDataVolumeBytes() - lastDataVolumn.get("month");
            tableStatMonth.setDataIncrementBytes(monthIncrement);
            tableStatMonth.setDataIncrement(BytesUtils.humanReadableByteCount(monthIncrement));
            tableStatMonth.setDate(DateUtils.month(date));
            tableStatList.add(tableStatMonth);
            log.info("month tableStat={}", tableStat);

            TableStat tableStatYear = (TableStat) tableStat.clone();
            tableStatYear.setDateType(DateType.YEAR.getLiteral());
            long yearIncrement = tableStat.getDataVolumeBytes() - lastDataVolumn.get("year");
            tableStatYear.setDataIncrementBytes(yearIncrement);
            tableStatYear.setDataIncrement(BytesUtils.humanReadableByteCount(yearIncrement));
            tableStatYear.setDate(DateUtils.year(date));
            tableStatList.add(tableStatYear);
            log.info("year tableStat={}", tableStat);
            return tableStatList;
        } catch (Exception e) {
            log.error("手动生成统计信息失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public TableMetadata systemMetadata(String dbAndtableName) {
        TableMetadata metadata = null;
        try {
            String[] split = dbAndtableName.split("\\.");
            String db = split[0];
            String tableName = split[1];
            String sql = "describe formatted " + dbAndtableName;
            log.debug("Running: " + sql);
            AdapterSource adapterSource = AdapterUtils.getHiveAdapterSource();
            Connection connection = adapterSource.getConnection(MetaspaceConfig.getHiveAdmin(), db, MetaspaceConfig.getHiveJobQueueName());
            metadata = adapterSource.getNewAdapterExecutor().queryResult(connection, sql, resultSet -> {
                try {
                    TableMetadata tableMetadata = new TableMetadata();
                    while (resultSet.next()) {
                        String string = resultSet.getString(2);
                        if (string != null && string.contains("numFiles"))
                            tableMetadata.setNumFiles(Long.parseLong(resultSet.getString(3).replaceAll(" ", "")));
                        if (string != null && string.contains("totalSize"))
                            tableMetadata.setTotalSize(Long.parseLong(resultSet.getString(3).replaceAll(" ", "")));
                    }
                    return tableMetadata;
                } catch (Exception e) {
                    throw new AtlasBaseException(e);
                }
            });
        } catch (Exception e) {
            log.error("从hive获取表统计异常", e);
        }
        return metadata;

    }

}
