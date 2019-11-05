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

package io.zeta.metaspace.repository.util;

import io.zeta.metaspace.MetaspaceConfig;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HbaseUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HbaseUtils.class);
    private static String hbaseConf;
    private static Configuration configuration = HBaseConfiguration.create();
    private static final String TABLE_NAME = "table_stat";
    private static final String COLUMN_FAMILIES_NAME = "info";

    static {
        try {
            hbaseConf = MetaspaceConfig.getHbaseConf();
            configuration.addResource(new Path(hbaseConf, "hbase-site.xml"));
            configuration.set("hbase.client.pause", "3000");
            configuration.set("hbase.client.retries.number", "3");
            configuration.set("hbase.rpc.timeout", "3000");
            configuration.set("hbase.client.operation.timeout", "30000");
            configuration.set("hbase.client.scanner.timeout.period", "50000");
            configuration.set("zookeeper.recovery.retry", "3");
            configuration.set("zookeeper.recovery.retry.intervalmill", "200");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConn() throws IOException {
        Connection conn;
        conn = ConnectionFactory.createConnection(configuration);
        return conn;
    }

    public static Configuration getConf() throws IOException {
        return configuration;
    }

    public static void createTableStat() throws AtlasBaseException {
        Connection conn = null;
        Admin admin =null;
        try{
            conn = getConn();
            TableName table = TableName.valueOf(TABLE_NAME);
            admin = conn.getAdmin();
            if (!admin.tableExists(table)){
                LOG.info("Create Table {}", TABLE_NAME);
                HTableDescriptor tableDesc = new HTableDescriptor(table);
                HColumnDescriptor columnDesc = new HColumnDescriptor(COLUMN_FAMILIES_NAME);
                tableDesc.addFamily(columnDesc);
                admin.createTable(tableDesc);
            }else{
                LOG.info("Table {} exists", TABLE_NAME);
            }
        } catch (IOException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "hbase连接异常:"+e.getMessage());
        }finally {
            try {
                if (admin!=null) admin.close();
                if (conn!=null) conn.close();
            }catch (IOException e) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "hbase连接异常:"+e.getMessage());
            }
        }
    }


}
