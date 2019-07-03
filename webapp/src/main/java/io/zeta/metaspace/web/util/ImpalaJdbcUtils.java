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
 * @date 2019/4/8 16:54
 */
package io.zeta.metaspace.web.util;

import io.zeta.metaspace.KerberosConfig;
import io.zeta.metaspace.MetaspaceConfig;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/8 16:54
 */
public class ImpalaJdbcUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ImpalaJdbcUtils.class);
    private static String driverClassName = "com.cloudera.impala.jdbc41.Driver";
    private static String impalaUrl = "";
    private static String krbStr = "";

    static {
        try {
            Class.forName(driverClassName);
            impalaUrl = MetaspaceConfig.getImpalaConf();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection(String db) throws SQLException, IOException, AtlasBaseException {

        Connection connection = null;
        String user = AdminUtils.getUserName();
        String jdbcUrl;
        if (KerberosConfig.isKerberosEnable()) {
            krbStr = KerberosConfig.getImpalaJdbc();
            jdbcUrl = impalaUrl + "/" + db +  ";" + krbStr + ";DelegationUID=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        } else {
            jdbcUrl = impalaUrl + "/" + db + ";DelegationUID=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        }
        return connection;
    }

    /**
     * 系统调度
     */
    static Connection connection;
    public static Connection getSystemConnection(String db) throws SQLException {
        String user = "hive";
        String jdbcUrl;
        if (KerberosConfig.isKerberosEnable()) {
            krbStr = KerberosConfig.getImpalaJdbc();
            jdbcUrl = impalaUrl + "/" + db +  ";" + krbStr + ";DelegationUID=" + user;
            LOG.info("Impala Jdbc:" + jdbcUrl);
            connection = DriverManager.getConnection(jdbcUrl);
        } else {
            jdbcUrl = impalaUrl + "/" + db + ";DelegationUID=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        }
        return connection;
    }

    public static ResultSet selectBySQLWithSystemCon(Connection conn, String sql) throws Exception {
        try {
            ResultSet resultSet = conn.createStatement().executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            LOG.info(e.getMessage(),e);
            if(e.getMessage().contains("Permission denied")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限访问");
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        } catch (Exception e) {
            LOG.error(e.toString());
            throw e;
        }
    }

    public static void execute(String sql, String db) throws AtlasBaseException {
        try (Connection conn = getConnection(db)) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }

}
