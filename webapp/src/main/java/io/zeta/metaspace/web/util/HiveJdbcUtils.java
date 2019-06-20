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
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileChecksum;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
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

    public static Connection getConnection(String db) throws SQLException, IOException, AtlasBaseException {

        Connection connection = null;
        String user = AdminUtils.getUserName();
        String jdbcUrl;
        if (KerberosConfig.isKerberosEnable()) {
            jdbcUrl = hiveUrl + "/" + db + hivePrincipal + ";hive.server2.proxy.user=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        } else {
            jdbcUrl = hiveUrl + "/" + db + ";hive.server2.proxy.user=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        }
        return connection;
    }

    /**
     * 系统调度
     */
    static Connection connection;
    public static Connection getSystemConnection(String db) throws SQLException, IOException {
            String user = "hive";

            String jdbcUrl;
            if (KerberosConfig.isKerberosEnable()) {
                jdbcUrl = hiveUrl + "/" + db + hivePrincipal + ";hive.server2.proxy.user=" + user;
                jdbcUrl += "?tez.am.resource.memory.mb=256";
                connection = DriverManager.getConnection(jdbcUrl);
            } else {
                jdbcUrl = hiveUrl + "/" + db + ";hive.server2.proxy.user=" + user;
                connection = DriverManager.getConnection(jdbcUrl, user, "");
                jdbcUrl += "?tez.am.resource.memory.mb=256";
            }
        return connection;
    }

    public static void execute(String sql) throws AtlasBaseException {

        try (Connection conn = getConnection("default")) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            if (stackTrace.contains("Permission denied: user=hive, access=WRITE"))
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "新建离线表失败,hive没有权限在此路径新建离线表");
                else
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "新建离线表失败,请检查表单信息和hive服务");
        }
    }


    public static List<String> databases() throws AtlasBaseException {
        try (Connection conn = getConnection("default")) {
            List<String> ret = new ArrayList<>();
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("show databases");
            while (resultSet.next()) {
                ret.add(resultSet.getString(1));
            }
            return ret;
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据库列表失败");
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
        TableMetadata tableMetadata = new TableMetadata();
        try {
            String[] split = dbAndtableName.split("\\.");
            String db = split[0];
            String tableName = split[1];
            String sql = "describe formatted " + dbAndtableName;
            LOG.debug("Running: " + sql);
            ResultSet resultSet = selectBySQLWithSystemCon(sql, db);
            while (resultSet.next()) {
                String string = resultSet.getString(2);
                if (string!=null&&string.contains("numFiles"))
                    tableMetadata.setNumFiles(Long.parseLong(resultSet.getString(3).replaceAll(" ","")));
                if (string!=null&&string.contains("totalSize"))
                    tableMetadata.setTotalSize(Long.parseLong(resultSet.getString(3).replaceAll(" ","")));
            }
        }catch (Exception e){
            LOG.error("从hive获取表统计异常",e);
        }
        return tableMetadata;

    }

    public static long getTableSize(Connection conn, String tableName) throws Exception {
        long totalSize = 0;
        String querySQL = "show tblproperties " + tableName;
        ResultSet resultSet = conn.createStatement().executeQuery(querySQL);
        while(resultSet.next()) {
            String str = resultSet.getString(1);
            if("totalSize".equals(str)) {
                totalSize = resultSet.getLong(2);
                break;
            }
            System.out.println(str);
        }
        return totalSize;
    }

    public static ResultSet selectBySQLWithSystemCon(String sql, String db) throws AtlasBaseException, IOException {
        try {
            Connection conn = getSystemConnection(db);
            ResultSet resultSet = conn.createStatement().executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            LOG.info(e.getMessage());
            if(e.getMessage().contains("Permission denied")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限访问");
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }

    public static ResultSet selectBySQLWithSystemCon(Connection conn, String sql, String db) throws AtlasBaseException, IOException {
        try {
            ResultSet resultSet = conn.createStatement().executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            if(e.getMessage().contains("Permission denied")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限访问");
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }

    public static void execute(String sql, String db) throws AtlasBaseException {
        try (Connection conn = getConnection(db)) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }

    public static boolean tableExists(String db, String tableName) throws AtlasBaseException {
        try(Connection conn = getConnection(db);
            ResultSet resultSet = conn.createStatement().executeQuery("show tables in " + db + " like '" + tableName + "'")){
            boolean next = resultSet.next();
            resultSet.close();
            return next;
        }catch (Exception e){
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }
}
