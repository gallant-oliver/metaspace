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

import org.apache.atlas.KerberosConfig;
import org.apache.atlas.MetaspaceConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HbaseUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HbaseUtils.class);
    private static String hbaseConf;
    private static Configuration configuration = HBaseConfiguration.create();

    static {
        try {

            hbaseConf = MetaspaceConfig.getHbaseConf();
            configuration.addResource(new Path(hbaseConf, "hbase-site.xml"));
            if (KerberosConfig.isKerberosEnable()) {
                configuration.set("hadoop.security.authentication", "Kerberos");
                UserGroupInformation.setConfiguration(configuration);
                UserGroupInformation.loginUserFromKeytab(KerberosConfig.getMetaspaceAdmin(), KerberosConfig.getMetaspaceKeytab());
            } else {
                configuration.set("HADOOP_USER_NAME", "metaspace");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConn() throws IOException {
        Connection conn;
        if (KerberosConfig.isKerberosEnable()) {
            //自动续约
            if (UserGroupInformation.isLoginKeytabBased()) {
                UserGroupInformation.getLoginUser().reloginFromKeytab();
            } else if (UserGroupInformation.isLoginTicketBased()) {
                UserGroupInformation.getLoginUser().reloginFromTicketCache();
            }
            conn = ConnectionFactory.createConnection(configuration);
        } else {
            conn = ConnectionFactory.createConnection(configuration);
        }
        return conn;
    }

    public static Configuration getConf() throws IOException {
        return configuration;
    }


}
