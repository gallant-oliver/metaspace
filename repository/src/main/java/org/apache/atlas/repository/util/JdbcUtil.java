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
/**
 * @author sunhaoning@gridsum.com
 * @date 2018/11/19 17:59
 */
package org.apache.atlas.repository.util;

import org.apache.atlas.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.zeta.metaspace.repository.util.HbaseUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/19 17:59
 */
public class JdbcUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HbaseUtils.class);
    private static  String driver;
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            org.apache.commons.configuration.Configuration conf = ApplicationProperties.get();
            driver = conf.getString("metaspace.postgresql.driverClassName");
            url = conf.getString("metaspace.postgresql.url");
            user = conf.getString("metaspace.postgresql.username");
            password = conf.getString("metaspace.postgresql.password");
            Class.forName(driver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConn() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
}
