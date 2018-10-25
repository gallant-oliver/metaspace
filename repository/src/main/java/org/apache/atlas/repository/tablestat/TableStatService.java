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

package org.apache.atlas.repository.tablestat;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.model.DateType;
import org.apache.atlas.model.table.TableStat;
import org.apache.atlas.model.table.TableStatRequest;
import org.apache.atlas.utils.DateUtils;
import org.apache.atlas.utils.PageUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.BiConsumer;

@AtlasService
public class TableStatService {

    private static final Logger LOG = LoggerFactory.getLogger(TableStatService.class);
    private static final String TABLE_STAT = "table_stat";

    private static Configuration conf = HBaseConfiguration.create();

    static {
        try {
            String hbaseUrl = ApplicationProperties.get().getString("atlas.graph.storage.hostname");
            conf.set("hbase.zookeeper.quorum", hbaseUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConn() throws IOException {
        Connection conn = ConnectionFactory.createConnection(conf);
        return conn;
    }

    public void add(List<TableStat> tableStatList) throws Exception {
        Gson gson = new Gson();
        Table table = getConn().getTable(TableName.valueOf("table_stat"));
        tableStatList.forEach(stat -> {
            try {
                String rowKey = (stat.getTableId() + stat.getDateType() + stat.getDate()).replace("-", "");

                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("tableName"), Bytes.toBytes(stat.getTableName()));

                put.add("info".getBytes(), "tableId".getBytes(), stat.getTableId().getBytes());
                put.add("info".getBytes(), "tableName".getBytes(), stat.getTableName().getBytes());
                put.add("info".getBytes(), "date".getBytes(), stat.getDate().getBytes());
                put.add("info".getBytes(), "dateType".getBytes(), stat.getDateType().getBytes());
                put.add("info".getBytes(), "fieldNum".getBytes(), stat.getFieldNum().toString().getBytes());
                put.add("info".getBytes(), "fileNum".getBytes(), stat.getFileNum().toString().getBytes());
                put.add("info".getBytes(), "dataVolume".getBytes(), stat.getDataVolume().getBytes());
                put.add("info".getBytes(), "dataVolumeBytes".getBytes(), stat.getDataVolumeBytes().toString().getBytes());
                put.add("info".getBytes(), "dataIncrement".getBytes(), stat.getDataIncrement().getBytes());
                String sourceTable = gson.toJson(stat.getSourceTable());
                put.add("info".getBytes(), "sourceTable".getBytes(), sourceTable.getBytes());
                table.put(put);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        table.close();
    }

    /**
     * 昨天，上月，去年的数据量
     *
     * @param tableId
     * @return
     */
    public Map<String, Long> lastDataVolumn(String tableId) throws Exception {
        TableStatRequest dayRequest = new TableStatRequest(tableId, DateType.DAY.getLiteral(), DateUtils.yesterday(), DateUtils.yesterday(), 0, 1);
        TableStatRequest monthRequest = new TableStatRequest(tableId, DateType.MONTH.getLiteral(), DateUtils.lastMonth(), DateUtils.lastMonth(), 0, 1);
        TableStatRequest yearRequest = new TableStatRequest(tableId, DateType.YEAR.getLiteral(), DateUtils.lastYear(), DateUtils.lastYear(), 0, 1);

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

        HTable table = new HTable(conf, TABLE_STAT);
        Scan scan = new Scan();
        scan.addFamily("info".getBytes());

        String startRowKey = (request.getTableId() + request.getDateType() + request.getFromDate()).replace("-", "");
        String endRowKey = (request.getTableId() + request.getDateType() + request.getEndDate()).replace("-", "");
        scan.setStartRow(startRowKey.getBytes());
        scan.setStopRow(endRowKey.getBytes());
        ResultScanner scanner = table.getScanner(scan);

        Gson gson = new Gson();
        List<TableStat> tableStatList = new ArrayList<>();
        for (Result result : scanner) {
            NavigableMap<byte[], byte[]> row = result.getFamilyMap("info".getBytes());
            String tableId = row.get("tableId".getBytes()) == null ? "" : new String(row.get("tableId".getBytes()));
            String tableName = row.get("tableName".getBytes()) == null ? "" : new String(row.get("tableName".getBytes()));
            String date = row.get("date".getBytes()) == null ? "" : new String(row.get("date".getBytes()));
            String dateType = row.get("dateType".getBytes()) == null ? "" : new String(row.get("dateType".getBytes()));
            int fieldNum = row.get("fieldNum".getBytes()) == null ? 0 : Integer.valueOf(new String(row.get("fieldNum".getBytes())));
            int fileNum = row.get("fileNum".getBytes()) == null ? 0 : Integer.valueOf(new String(row.get("fileNum".getBytes())));
            long recordNum = row.get("recordNum".getBytes()) == null ? 0 : Long.valueOf(new String(row.get("recordNum".getBytes())));
            String dataVolume = row.get("dataVolume".getBytes()) == null ? "" : new String(row.get("dataVolume".getBytes()));
            long dataVolumeBytes = row.get("dataVolumeBytes".getBytes()) == null ? 0L : Long.valueOf(new String(row.get("dataVolumeBytes".getBytes())));
            String dataIncrement = row.get("dataIncrement".getBytes()) == null ? "" : new String(row.get("dataIncrement".getBytes()));
            String sourceTable = row.get("sourceTable".getBytes()) == null ? "" : new String(row.get("sourceTable".getBytes()));
            List<org.apache.atlas.model.table.Table> sourceTableList = gson.fromJson(sourceTable, new TypeToken<List<org.apache.atlas.model.table.Table>>() {
            }.getType());
            TableStat stat = new TableStat(tableId, tableName, date, dateType, fieldNum, fileNum, recordNum, dataVolume, dataVolumeBytes, dataIncrement, sourceTableList);
            tableStatList.add(stat);
        }
        table.close();
        List<TableStat> pageList = PageUtils.pageList(tableStatList.iterator(), request.getOffset(), request.getLimit());

        return Pair.of(tableStatList.size(), pageList);
    }

}