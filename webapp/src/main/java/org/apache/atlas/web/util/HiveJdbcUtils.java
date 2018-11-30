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

package org.apache.atlas.web.util;


import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.table.TableMetadata;
import org.apache.atlas.KerberosConfig;
import org.apache.atlas.MetaspaceConfig;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
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
            if (KerberosConfig.isKerberosEnable()) {
                Configuration configuration = new Configuration();
                configuration.set("hadoop.security.authentication", "Kerberos");
                UserGroupInformation.setConfiguration(configuration);
                UserGroupInformation.loginUserFromKeytab(KerberosConfig.getMetaspaceAdmin(), KerberosConfig.getMetaspaceKeytab());
                hivePrincipal = ";principal=" + KerberosConfig.getHivePrincipal();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection(String db) throws SQLException, IOException, AtlasBaseException {

        Connection connection = null;
        String user = AdminUtils.getUserName();
        String jdbcUrl;
        if (KerberosConfig.isKerberosEnable()) {
            //自动续约
            if (UserGroupInformation.isLoginKeytabBased()) {
                UserGroupInformation.getLoginUser().reloginFromKeytab();
            } else if (UserGroupInformation.isLoginTicketBased()) {
                UserGroupInformation.getLoginUser().reloginFromTicketCache();
            }
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
            //自动续约
            if (UserGroupInformation.isLoginKeytabBased()) {
                UserGroupInformation.getLoginUser().reloginFromKeytab();
            } else if (UserGroupInformation.isLoginTicketBased()) {
                UserGroupInformation.getLoginUser().reloginFromTicketCache();
            }
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

    public static TableMetadata metadata(String dbAndtableName) {
        String[] split = dbAndtableName.split("\\.");
        return metadata(split[0], split[1]);
    }

    /**
     * 表数据量大小，单位bytes
     *
     * @param
     * @return
     */
    public static TableMetadata metadata(String db, String tableName) {
        TableMetadata ret = new TableMetadata();
        try (Connection conn = getConnection(db)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("show tblproperties " + tableName);
            while (rs.next()) {
                String key = rs.getString("prpt_name");
                String value = rs.getString("prpt_value");
                if ("totalSize".equals(key)) {
                    ret.setTotalSize(Long.valueOf(value));
                }
                if ("numFiles".equals(key)) {
                    ret.setNumFiles(Integer.valueOf(value));
                }
                if ("numRows".equals(key)) {
                    ret.setNumRows(Long.valueOf(value));
                }
            }
            return ret;
        } catch (SQLException e) {
            LOG.debug(e.getMessage(), e);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static TableMetadata systemMetadata(String dbAndtableName) {
        String[] split = dbAndtableName.split("\\.");
        String db = split[0];
        String tableName = split[1];
        TableMetadata ret = new TableMetadata();
        try (Connection conn = getSystemConnection(db)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("show tblproperties " + tableName);
            while (rs.next()) {
                String key = rs.getString("prpt_name");
                String value = rs.getString("prpt_value");
                if ("totalSize".equals(key)) {
                    ret.setTotalSize(Long.valueOf(value));
                }
                if ("numFiles".equals(key)) {
                    ret.setNumFiles(Integer.valueOf(value));
                }
                if ("numRows".equals(key)) {
                    ret.setNumRows(Long.valueOf(value));
                }
            }
            return ret;
        } catch (SQLException e) {
            LOG.debug(e.getMessage(), e);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
