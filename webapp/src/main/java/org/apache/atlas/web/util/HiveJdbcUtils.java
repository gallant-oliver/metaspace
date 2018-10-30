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

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.table.TableMetadata;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.ResourceBundle;

public class HiveJdbcUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HiveJdbcUtils.class);

    private static String hivedriverClassName = "org.apache.hive.jdbc.HiveDriver";
    private static String hiveUrl="";
    private static String hivePrincipal="";
    private static String kerberosAdmin="";
    private static String kerberosKeytab="";


    static {
        try {
            Class.forName(hivedriverClassName);
            Configuration conf = ApplicationProperties.get();
            hiveUrl = conf.getString("metaspace.hive.url");
            if(conf.getString("metaspace.kerberos.enable").equals("true")) {
                if(
                        conf.getString("metaspace.kerberos.admin")  ==null|
                        conf.getString("metaspace.kerberos.keytab")  ==null|
                        conf.getString("metaspace.hive.principal")  ==null|
                        conf.getString("metaspace.kerberos.admin")  .equals("")|
                        conf.getString("metaspace.kerberos.keytab")  .equals("")|
                        conf.getString("metaspace.hive.principal")  .equals("")
                ){
                    LOG.error("kerberos info incomplete");
                }else {

                    org.apache.hadoop.conf.Configuration configuration = new
                            org.apache.hadoop.conf.Configuration();
                    configuration.set("hadoop.security.authentication", "Kerberos");
                    UserGroupInformation.setConfiguration(configuration);
                    UserGroupInformation.loginUserFromKeytab(conf.getString("metaspace.kerberos.admin"), conf.getString("metaspace.kerberos.keytab"));
                    hivePrincipal = ";principal="+conf.getString("metaspace.hive.principal");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void execute(String sql) throws AtlasBaseException {
        try (Connection conn = DriverManager.getConnection((hiveUrl+";").replace(";",hivePrincipal))) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 表数据量大小，单位bytes
     *
     * @param
     * @return
     */
    public static TableMetadata metadata(String tableName) {
        TableMetadata ret = new TableMetadata();
        try (Connection conn = DriverManager.getConnection((hiveUrl+";").replace(";",hivePrincipal))) {
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

    public static ResultSet selectBySQL(String sql, String db) throws AtlasBaseException {
        try {
            Connection conn = DriverManager.getConnection((hiveUrl+"/"+db+";").replace(";",hivePrincipal));
            ResultSet resultSet = conn.createStatement().executeQuery(sql);
            return resultSet;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public static void execute(String sql, String db) throws AtlasBaseException {
        try (Connection conn = DriverManager.getConnection((hiveUrl+"/"+db+";").replace(";",hivePrincipal))) {
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }
}
