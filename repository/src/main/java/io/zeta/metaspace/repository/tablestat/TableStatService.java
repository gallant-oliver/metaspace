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

package io.zeta.metaspace.repository.tablestat;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import io.zeta.metaspace.model.DateType;
import io.zeta.metaspace.model.table.TableStat;
import io.zeta.metaspace.model.table.TableStatRequest;
import io.zeta.metaspace.repository.util.HbaseUtils;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.utils.PageUtils;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@AtlasService
@Slf4j
public class TableStatService {

    private static final Logger LOG = LoggerFactory.getLogger(TableStatService.class);
    private static final String TABLE_STAT = "table_stat";

    public void add(List<TableStat> tableStatList) throws Exception {
        Gson gson = new Gson();

        try (Connection conn = HbaseUtils.getConn();
             Table table =conn.getTable(TableName.valueOf(TABLE_STAT))) {
            tableStatList.forEach(stat -> {
                try {
                    String rowKey = (stat.getTableId() + stat.getDateType() + stat.getDate()).replace("-", "");

                    Put put = new Put(Bytes.toBytes(rowKey));
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("tableName"), Bytes.toBytes(stat.getTableName()));

                    put.addColumn("info".getBytes(), "tableId".getBytes(), stat.getTableId().getBytes());
                    put.addColumn("info".getBytes(), "tableName".getBytes(), stat.getTableName().getBytes());
                    put.addColumn("info".getBytes(), "date".getBytes(), stat.getDate().getBytes());
                    put.addColumn("info".getBytes(), "dateType".getBytes(), stat.getDateType().getBytes());
                    put.addColumn("info".getBytes(), "fieldNum".getBytes(), stat.getFieldNum().toString().getBytes());
                    put.addColumn("info".getBytes(), "fileNum".getBytes(), stat.getFileNum().toString().getBytes());
                    put.addColumn("info".getBytes(), "dataVolume".getBytes(), stat.getDataVolume().getBytes());
                    put.addColumn("info".getBytes(), "dataVolumeBytes".getBytes(), stat.getDataVolumeBytes().toString().getBytes());
                    put.addColumn("info".getBytes(), "dataIncrement".getBytes(), stat.getDataIncrement().getBytes());
                    put.addColumn("info".getBytes(), "dataIncrementBytes".getBytes(), stat.getDataIncrementBytes().toString().getBytes());
                    String sourceTable = gson.toJson(stat.getSourceTable());
                    put.addColumn("info".getBytes(), "sourceTable".getBytes(), sourceTable.getBytes());
                    table.put(put);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        }
    }

    /**
     * 日期昨天，上月，去年的数据量
     *
     * @param tableId
     * @param date
     * @return
     */
    public Map<String, Long> lastDataVolumn(String tableId, String date) throws Exception {
        TableStatRequest dayRequest = new TableStatRequest(tableId, DateType.DAY.getLiteral(), DateUtils.yesterday(date), DateUtils.yesterday(date), 0, 1);
        TableStatRequest monthRequest = new TableStatRequest(tableId, DateType.MONTH.getLiteral(), DateUtils.lastMonth(date), DateUtils.lastMonth(date), 0, 1);
        TableStatRequest yearRequest = new TableStatRequest(tableId, DateType.YEAR.getLiteral(), DateUtils.lastYear(date), DateUtils.lastYear(date), 0, 1);

        Map<String, Long> ret = new HashMap<>();

        List<TableStat> day = query(dayRequest).getRight();
        if (!day.isEmpty()) {
            ret.put("day", Long.valueOf(day.get(0).getDataVolumeBytes()));
        } else {
            ret.put("day", 0L);
        }

        List<TableStat> month = query(monthRequest).getRight();
        if (!month.isEmpty()) {
            ret.put("month", Long.valueOf(month.get(0).getDataVolumeBytes()));
        } else {
            ret.put("month", 0L);
        }

        List<TableStat> year = query(yearRequest).getRight();
        if (!year.isEmpty()) {
            ret.put("year", Long.valueOf(year.get(0).getDataVolumeBytes()));
        } else {
            ret.put("year", 0L);
        }
        return ret;
    }

    public Pair<Integer, List<TableStat>> query(TableStatRequest request) throws Exception {
        Scan scan = new Scan();
        scan.addFamily("info".getBytes());
        String startRowKey = (request.getTableId() + request.getDateType() + request.getFromDate()).replace("-", "");
        //因为hbase查询不包含endKey,所以日期往后顺推
        String convertedEndDate = convertedEndDate(request.getDateType(), request.getEndDate());
        String endRowKey = (request.getTableId() + request.getDateType() + convertedEndDate).replace("-", "");
        scan.setStartRow(startRowKey.getBytes());
        scan.setStopRow(endRowKey.getBytes());
        try (Connection conn = HbaseUtils.getConn();
             Table table = conn.getTable(TableName.valueOf(TABLE_STAT));
             ResultScanner scanner = table.getScanner(scan);
        ) {
            Gson gson = new Gson();
            List<TableStat> tableStatList = new ArrayList<>();
            for (Result result : scanner) {
                NavigableMap<byte[], byte[]> row = result.getFamilyMap("info".getBytes());
                String tableId = row.get("tableId".getBytes()) == null ? "" : new String(row.get("tableId".getBytes()));
                String tableName = row.get("tableName".getBytes()) == null ? "" : new String(row.get("tableName".getBytes()));
                String date = row.get("date".getBytes()) == null ? "" : new String(row.get("date".getBytes()));
                String dateType = row.get("dateType".getBytes()) == null ? "" : new String(row.get("dateType".getBytes()));
                int fieldNum = row.get("fieldNum".getBytes()) == null ? 0 : Integer.valueOf(new String(row.get("fieldNum".getBytes())));
                long fileNum = row.get("fileNum".getBytes()) == null ? 0 : Long.valueOf(new String(row.get("fileNum".getBytes())));
                long recordNum = row.get("recordNum".getBytes()) == null ? 0 : Long.valueOf(new String(row.get("recordNum".getBytes())));
                String dataVolume = row.get("dataVolume".getBytes()) == null ? "" : new String(row.get("dataVolume".getBytes()));
                long dataVolumeBytes = row.get("dataVolumeBytes".getBytes()) == null ? 0L : Long.valueOf(new String(row.get("dataVolumeBytes".getBytes())));
                String dataIncrement = row.get("dataIncrement".getBytes()) == null ? "" : new String(row.get("dataIncrement".getBytes()));
                long dataIncrementBytes = row.get("dataIncrementBytes".getBytes()) == null ? 0L : Long.valueOf(new String(row.get("dataIncrementBytes".getBytes())));
                String sourceTable = row.get("sourceTable".getBytes()) == null ? "" : new String(row.get("sourceTable".getBytes()));
                List<io.zeta.metaspace.model.table.Table> sourceTableList = gson.fromJson(sourceTable, new TypeToken<List<io.zeta.metaspace.model.table.Table>>() {
                }.getType());
                TableStat stat = new TableStat(tableId, tableName, date, dateType, fieldNum, fileNum, recordNum, dataVolume, dataVolumeBytes, dataIncrement, dataIncrementBytes, sourceTableList);
                tableStatList.add(stat);
            }
            tableStatList.sort(new Comparator<TableStat>() {
                @Override
                public int compare(TableStat o1, TableStat o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            table.close();
            List<TableStat> pageList = PageUtils.pageList(tableStatList.iterator(), request.getOffset(), request.getLimit());

            return Pair.of(tableStatList.size(), pageList);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "HBase 异常");
        }
    }

    private String convertedEndDate(String dateType, String endDate) {
        if (DateType.DAY.getLiteral().equals(dateType)) {
            return DateUtils.nextDay(endDate);
        } else if (DateType.MONTH.getLiteral().equals(dateType)) {
            return DateUtils.nextMonth(endDate);
        } else if (DateType.YEAR.getLiteral().equals(dateType)) {
            return DateUtils.nextYear(endDate);
        } else {
            throw new RuntimeException("无效的dateType: " + dateType);
        }
    }

}
