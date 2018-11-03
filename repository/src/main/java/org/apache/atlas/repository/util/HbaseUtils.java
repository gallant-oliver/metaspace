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

package org.apache.atlas.repository.util;

import org.apache.atlas.ApplicationProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;

public class HbaseUtils {


    private static Configuration conf = HBaseConfiguration.create();

    static {
        try {
            String hbaseUrl = ApplicationProperties.get().getString("atlas.graph.storage.hostname");
            conf.set("hbase.zookeeper.quorum", hbaseUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConn() throws IOException {
        Connection conn = ConnectionFactory.createConnection(conf);
        return conn;
    }

    public static Configuration getConf() throws IOException {
        return conf;
    }


}
