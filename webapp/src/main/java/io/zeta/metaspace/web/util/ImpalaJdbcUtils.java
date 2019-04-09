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
import io.zeta.metaspace.model.table.TableMetadata;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
    private static final Logger LOG = LoggerFactory.getLogger(HiveJdbcUtils.class);
    private static String hivedriverClassName = "org.apache.hive.jdbc.HiveDriver";
    private static String impalaUrl = "";
    private static String impalaPrincipal = "";


    static {
        try {
            Class.forName(hivedriverClassName);
            impalaUrl = MetaspaceConfig.getImpalaConf();
            impalaPrincipal = ";principal=" + KerberosConfig.getHivePrincipal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection(String db) throws SQLException, IOException, AtlasBaseException {

        Connection connection = null;
        String user = AdminUtils.getUserName();
        String jdbcUrl;
        if (KerberosConfig.isKerberosEnable()) {
            jdbcUrl = impalaUrl + "/" + db + impalaPrincipal + ";hive.server2.proxy.user=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        } else {
            jdbcUrl = impalaUrl + "/" + db + ";hive.server2.proxy.user=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        }
        return connection;
    }

    /**
     * 系统调度
     */
    static Connection connection;
    private static Connection getSystemConnection(String db) throws SQLException, IOException {
        String user = "hive";

        String jdbcUrl;
        if (KerberosConfig.isKerberosEnable()) {
            jdbcUrl = impalaUrl + "/" + db + impalaPrincipal + ";hive.server2.proxy.user=" + user;
            connection = DriverManager.getConnection(jdbcUrl);
        } else {
            jdbcUrl = impalaUrl + "/" + db + ";hive.server2.proxy.user=" + user;
            connection = DriverManager.getConnection(jdbcUrl, user, "");
        }
        return connection;
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
            try (FileSystem fs = HdfsUtils.getSystemFs("hdfs")){
                ContentSummary contentSummary = fs.getContentSummary(new Path(location));
                long numFiles = contentSummary.getFileCount();
                long totalSize = contentSummary.getLength();
                return new TableMetadata(numFiles, Long.valueOf(totalSize));
            }catch (Exception e){
                LOG.warn(e.getMessage(),e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "hdfs服务异常");
            }
        } else {//view
            return new TableMetadata();
        }
    }

    private static String location(String db, String tableName) {
        try(Connection conn = getSystemConnection(db);
            ResultSet rs = conn.createStatement().executeQuery("SHOW CREATE TABLE " + tableName)) {
            while (rs.next()) {
                String text = rs.getString(1);
                if (text.contains("hdfs://")) {

                    String s = text.replaceAll("'", "").replaceAll("hdfs://\\w+", "").replaceAll(":\\d+","").replaceAll(" ", "");
                    LOG.info(db + "." + tableName + " location:" + s);
                    return s;
                }
            }
        } catch (Exception e) {
            LOG.warn(db + "." + tableName + " location is not found, may be it's view.");
        }
        return null;
    }

    public static ResultSet selectBySQLWithSystemCon(String sql, String db) throws AtlasBaseException, IOException {
        try {
            Connection conn = getSystemConnection(db);
            ResultSet resultSet = conn.createStatement().executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            if(e.getMessage().contains("Permission denied")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限访问");
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }
}
