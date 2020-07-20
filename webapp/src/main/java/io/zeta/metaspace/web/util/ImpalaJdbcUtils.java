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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    private static String impalaResourcePool = "metaspace";

    static {
        try {
            Class.forName(driverClassName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection(String db) throws SQLException, IOException, AtlasBaseException {
        impalaResourcePool = MetaspaceConfig.getImpalaResourcePool();
        String user = AdminUtils.getUserName();
        return getConnection(db,user,impalaResourcePool);
    }

    public static Connection getConnection(String db,String user,String pool) throws SQLException, IOException, AtlasBaseException {

        Connection connection = null;
        String jdbcUrl;
        impalaUrl = MetaspaceConfig.getImpalaConf();
        Properties properties = new Properties();
        //单引号防止特殊字符
        if (pool==null||pool.length()==0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "资源池不能为空");
        pool="'"+pool+"'";
        properties.setProperty("REQUEST_POOL",pool);
        if (KerberosConfig.isKerberosEnable()) {
            krbStr = KerberosConfig.getImpalaJdbc();
            jdbcUrl = impalaUrl + "/" + db +  ";" + krbStr + ";DelegationUID=" + user;
            connection = DriverManager.getConnection(jdbcUrl,properties);
        } else {
            jdbcUrl = impalaUrl + "/" + db + ";DelegationUID=" + user;
            connection = DriverManager.getConnection(jdbcUrl,properties);
        }
        return connection;
    }

    /**
     * 系统调度
     */
    static Connection connection;
    public static Connection getSystemConnection(String db) throws SQLException, AtlasBaseException {
        impalaResourcePool = MetaspaceConfig.getImpalaResourcePool();
        return getSystemConnection(db,impalaResourcePool);
    }

    public static Connection getSystemConnection(String db,String pool) throws SQLException, AtlasBaseException {
        impalaUrl = MetaspaceConfig.getImpalaConf();
        String user = MetaspaceConfig.getHiveAdmin();
        String jdbcUrl;
        Properties properties = new Properties();
        if (pool==null||pool.length()==0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "资源池不能为空");
        //单引号防止特殊字符
        pool="'"+pool+"'";
        properties.setProperty("REQUEST_POOL",pool);
        if (KerberosConfig.isKerberosEnable()) {
            krbStr = KerberosConfig.getImpalaJdbc();
            jdbcUrl = impalaUrl + "/" + db +  ";" + krbStr + ";DelegationUID=" + user;
            LOG.info("Impala Jdbc:" + jdbcUrl);
            connection = DriverManager.getConnection(jdbcUrl,properties);
        } else {
            jdbcUrl = impalaUrl + "/" + db + ";DelegationUID=" + user;
            connection = DriverManager.getConnection(jdbcUrl,properties);
        }
        return connection;
    }

    public static ResultSet selectBySQLWithSystemCon(Connection conn, String sql) throws Exception {
        try {
            ResultSet resultSet = conn.createStatement().executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            LOG.info(e.getMessage(),e);
            String noPermissionMessage = "Permission denied";
            if(e.getMessage().contains(noPermissionMessage)) {
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

    public static float getTableSize(String db, String tableName,String pool) throws Exception {
        try(Connection conn = getSystemConnection(db,pool)) {
            float totalSize = 0;
            String querySQL = "show table stats " + tableName;
            ResultSet resultSet = conn.createStatement().executeQuery(querySQL);
            while (resultSet.next()) {
                String str = resultSet.getString("size");
                if (str!=null&&str.length()!=0){
                    totalSize=ByteFormat.parse(str);
                    break;
                }
            }
            return totalSize;
        } catch (Exception e) {
            throw e;
        }
    }

}
