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
import org.apache.commons.lang.StringUtils;
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
import java.util.*;


public class HiveJdbcUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HiveJdbcUtils.class);
    private static String hivedriverClassName = "org.apache.hive.jdbc.HiveDriver";
    private static String hivePrincipal = "";
    private static String hiveUrlPrefix = "jdbc:hive2://";
    private static String hiveJobQueuName;
    private static Queue<String> hiveUrlQueue = new LinkedList<>();


    private static final String QUERY = "SELECT %s,count(*) over() as count FROM `%s`";
    private static final String WHERE = " WHERE ";
    private static final String ORDER = " ORDER BY 1 ";
    private static final String LIMIT = " LIMIT %d OFFSET %d ";

    static {
        try {
            Class.forName(hivedriverClassName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection(String db, String user) throws AtlasBaseException {
        hiveJobQueuName=MetaspaceConfig.getHiveJobQueueName();
        return getConnection(db,user,hiveJobQueuName);
    }

    public static Connection getConnection(String db, String user,String pool) throws AtlasBaseException {
        Connection connection = null;
        String jdbcUrl;
        hiveUrlQueue=MetaspaceConfig.getHiveUrlQueue();
        if (hiveUrlQueue != null && hiveUrlQueue.size() > 0) {
            for (int i = 0; i < hiveUrlQueue.size(); i++) {
                try {
                    String hiveUrl = hiveUrlQueue.peek();
                    if (KerberosConfig.isKerberosEnable()) {
                        hivePrincipal = ";principal=" + KerberosConfig.getHivePrincipal();
                        jdbcUrl = hiveUrlPrefix + hiveUrl + "/" + db + hivePrincipal + ";hive.server2.proxy.user=" + user;
                    } else {
                        jdbcUrl = hiveUrlPrefix + hiveUrl + "/" + db + ";hive.server2.proxy.user=" + user;
                    }
                    LOG.info("hive jdbc url:" + jdbcUrl);
                    Properties properties = new Properties();
                    if (pool==null||pool.length()==0)
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "资源池不能为空");
                    properties.setProperty("hiveconf:tez.queue.name",pool);
                    properties.setProperty("hiveconf:mapreduce.job.queuename",pool);
                    connection = DriverManager.getConnection(jdbcUrl,properties);
                    return connection;
                } catch (Exception e) {
                    String badUrl = hiveUrlQueue.remove();
                    hiveUrlQueue.add(badUrl);
                    LOG.error("获取hive连接失败", e);
                }
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取hive连接失败");
        } else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "hive的URL配置为空");
        }

    }

    /**
     * 系统调度
     */
    public static Connection getSystemConnection(String db) throws AtlasBaseException {
        String user = MetaspaceConfig.getHiveAdmin();
        return getConnection(db, user);
    }
    public static Connection getSystemConnection(String db,String pool) throws AtlasBaseException {
        String user = MetaspaceConfig.getHiveAdmin();
        return getConnection(db, user,pool);
    }

    public static void execute(List<String> sqlList) throws Exception {
        Connection conn = null;
        try {
            conn = getSystemConnection("");
            Statement stmt = conn.createStatement();
            for(int i=0; i<sqlList.size(); i++) {
                stmt.execute(sqlList.get(i));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if(conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void execute(String sql) throws AtlasBaseException {
        String user = AdminUtils.getUserName();
        try (Connection conn = getConnection("", user)) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            String message = "Permission denied: user=" + user + ", access=WRITE";
            if (stackTrace.contains(message))
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "新建离线表失败," + user + "用户没有权限在此路径新建离线表");
            else
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "新建离线表失败,请检查表单信息和hive服务");
        }
    }


    public static List<String> databases() throws AtlasBaseException {
        String user = AdminUtils.getUserName();
        try (Connection conn = getConnection("", user)) {
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
        Connection conn = null;
        try {
            String[] split = dbAndtableName.split("\\.");
            String db = split[0];
            String tableName = split[1];
            String sql = "describe formatted " + dbAndtableName;
            LOG.debug("Running: " + sql);
            conn = getSystemConnection(db);
            ResultSet resultSet = selectBySQLWithSystemCon(conn, sql);
            while (resultSet.next()) {
                String string = resultSet.getString(2);
                if (string!=null&&string.contains("numFiles"))
                    tableMetadata.setNumFiles(Long.parseLong(resultSet.getString(3).replaceAll(" ","")));
                if (string!=null&&string.contains("totalSize"))
                    tableMetadata.setTotalSize(Long.parseLong(resultSet.getString(3).replaceAll(" ","")));
            }
        }catch (Exception e) {
            LOG.error("从hive获取表统计异常",e);
        } finally {
            if(Objects.nonNull(conn)) {
                conn.close();
            }
        }
        return tableMetadata;

    }

    public static float getTableSize(String db, String tableName,String pool) throws Exception {
        try(Connection conn = getSystemConnection(db,pool)) {
            float totalSize = 0;
            String querySQL = "show tblproperties " + tableName;
            ResultSet resultSet = conn.createStatement().executeQuery(querySQL);
            while (resultSet.next()) {
                String str = resultSet.getString(1);
                if ("totalSize".equals(str)) {
                    totalSize = resultSet.getLong(2);
                    break;
                }
                System.out.println(str);
            }
            return totalSize;
        } catch (Exception e) {
            throw e;
        }
    }

    public static ResultSet selectBySQLWithSystemCon(Connection conn, String sql) throws AtlasBaseException, IOException {
        try {
            ResultSet resultSet = conn.createStatement().executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            LOG.info(e.getMessage());
            String noPermissionMessage = "Permission denied";
            if(e.getMessage().contains(noPermissionMessage)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限访问");
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }

    public static void execute(String sql, String db) throws AtlasBaseException {
        String user = AdminUtils.getUserName();
        try (Connection conn = getConnection(db, user)) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }

    public static boolean tableExists(String db, String tableName) throws AtlasBaseException {
        String user = AdminUtils.getUserName();
        try(Connection conn = getConnection(db, user);
            ResultSet resultSet = conn.createStatement().executeQuery("show tables in " + db + " like '" + tableName + "'")){
            boolean next = resultSet.next();
            resultSet.close();
            return next;
        }catch (Exception e) {
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }


    public static String getQuerySql(String tableName, String queryFields, String filterFields, long limit, long offset) throws AtlasBaseException {
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            String queryStr = String.format(QUERY, queryFields, tableName);
            sqlBuilder.append(queryStr);
            if (StringUtils.isNotEmpty(filterFields)) {
                sqlBuilder.append(WHERE);
                sqlBuilder.append(filterFields);
            }
            String sql = buildSql(sqlBuilder.toString(), limit, offset);
            return sql;
        } catch (Exception e) {
            throw e;
        }
    }

    public static String buildSql(String sqlTemplate, long limit, long offset, String... param) {
        StringBuilder sqlBuilder = new StringBuilder();
        String querySql = String.format(sqlTemplate, param);
        sqlBuilder.append(querySql);
        if(limit == -1) {
            return sqlBuilder.toString();
        }
        sqlBuilder.append(ORDER);
        sqlBuilder.append(String.format(LIMIT, limit, offset));
        return sqlBuilder.toString();
    }
}
