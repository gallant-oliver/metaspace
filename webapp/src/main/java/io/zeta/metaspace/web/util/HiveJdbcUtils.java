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

package io.zeta.metaspace.web.util;


import org.apache.atlas.AtlasErrorCode;
import io.zeta.metaspace.KerberosConfig;
import io.zeta.metaspace.MetaspaceConfig;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.model.table.TableMetadata;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class HiveJdbcUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HiveJdbcUtils.class);

    private static String hivedriverClassName = "org.apache.hive.jdbc.HiveDriver";
    private static String hiveUrl = "";
    private static String hivePrincipal = "";


    static {
        try {
            Class.forName(hivedriverClassName);
            hiveUrl = MetaspaceConfig.getHiveUrl();
            hivePrincipal = ";principal=" + KerberosConfig.getHivePrincipal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection(String db) throws SQLException, IOException, AtlasBaseException {

        Connection connection = null;
        String user = AdminUtils.getUserName();
        String jdbcUrl;
        if (KerberosConfig.isKerberosEnable()) {
            jdbcUrl = hiveUrl + "/" + db + hivePrincipal + ";hive.server2.proxy.user=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        } else {
            jdbcUrl = hiveUrl + "/" + db;
            connection = DriverManager.getConnection(jdbcUrl, user, "");
        }
        return connection;
    }

    /**
     * 系统调度
     */
    private static Connection getSystemConnection(String db) throws SQLException, IOException {
        String user = "hive";
        Connection connection;
        String jdbcUrl;
        if (KerberosConfig.isKerberosEnable()) {
            jdbcUrl = hiveUrl + "/" + db + hivePrincipal + ";hive.server2.proxy.user=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        } else {
            jdbcUrl = hiveUrl + "/" + db;
            connection = DriverManager.getConnection(jdbcUrl, user, "");
        }
        return connection;
    }

    public static void execute(String sql) throws AtlasBaseException {

        try (Connection conn = getConnection("")) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, ExceptionUtils.getStackTrace(e));
        }
    }


    public static List<String> databases() throws AtlasBaseException {
        try (Connection conn = getConnection("")) {
            List<String> ret = new ArrayList<>();
            ResultSet resultSet = conn.createStatement().executeQuery("show databases;");
            while (resultSet.next()) {
                ret.add(resultSet.getString(1));
            }
            return ret;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 表数据量大小，单位bytes
     * 文件个数
     *
     * @param
     * @return
     */
    public static TableMetadata systemMetadata(String dbAndtableName) throws Exception {
        String[] split = dbAndtableName.split("\\.");
        String db = split[0];
        String tableName = split[1];
        String location = location(db, tableName);
        if (location != null) {
            ResultSet rs = systemSelectBySQL("dfs -count " + location, db);
            rs.next();
            String text = rs.getString(1);
            String[] s = text.replaceAll("\\s+", "-").split("-");
            String numFiles = s[2];
            String totalSize = s[3];
            return new TableMetadata(Integer.valueOf(numFiles), Long.valueOf(totalSize));
        } else {//view
            return new TableMetadata();
        }
    }

    private static String location(String db, String tableName) throws AtlasBaseException, SQLException {
        ResultSet rs = systemSelectBySQL("SHOW CREATE TABLE " + tableName, db);
        while (rs.next()) {
            String text = rs.getString(1);
            if (text.contains("hdfs://")) {
                return text.replaceAll("'", "");
            }
        }
        LOG.warn(db + "." + tableName + " location is not found, may be it's view.");
//        throw new RuntimeException("没有获取到表的location: " + db + "." + tableName);
        return null;
    }

    public static ResultSet systemSelectBySQL(String sql, String db) throws AtlasBaseException {
        try {
            Connection conn = getSystemConnection(db);
            ResultSet resultSet = conn.createStatement().executeQuery(sql);
            return resultSet;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public static ResultSet selectBySQL(String sql, String db) throws AtlasBaseException, IOException, SQLException {

        Connection conn = getConnection(db);
        ResultSet resultSet = conn.createStatement().executeQuery(sql);
        return resultSet;

    }

    public static void execute(String sql, String db) throws AtlasBaseException {
        try (Connection conn = getConnection(db)) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public static boolean tableExists(String db, String tableName) throws AtlasBaseException, SQLException, IOException {
        ResultSet resultSet = selectBySQL("show tables in " + db + " like '" + tableName + "'", db);
        return resultSet.next();
    }


}
