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

package io.zeta.metaspace;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

/**
 * @author lixiang03
 * @Data 2020/5/27 18:39
 */
public enum SqlEnum {
    MYSQL("MYSQL","com.mysql.jdbc.Driver","jdbc:mysql://%s:%s/%s"),
    ORACLE_SERVICE_NAME("ORACLE SERVICE_NAME","oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:@%s:%s:%s"),
    ORACLE_SID("ORACLE SID","oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:@%s:%s:%s"),
    SQL_SERVER("SQL SERVER","com.microsoft.sqlserver.jdbc.SQLServerDriver","jdbc:sqlserver://%s:%s;DatabaseName=%s"),
    HIVE("HIVE","org.apache.hive.jdbc.HiveDriver","jdbc:hive2://%s:%s/%s"),
    POSTGRESQL("POSTGRESQL","org.postgresql.Driver","jdbc:postgresql://%s:%s/%s");


    private String name;
    private String driver;
    private String jdbcUrl;

    SqlEnum(String name, String driver, String jdbcUrl) {
        this.name = name;
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public static String getDriverByName(String name,String serviceType) throws AtlasBaseException {
        if (ORACLE_SID.getName().startsWith(name.toUpperCase())){
            name=name+" "+serviceType;
        }
        for (SqlEnum sql : SqlEnum.values()) {
            if(sql.name.equalsIgnoreCase(name))
                return sql.getDriver();
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持数据源类型");
    }
    public static String getUrlByName(String name,String ip,String serviceType,String port,String schema) throws AtlasBaseException {
        if (ORACLE_SID.getName().startsWith(name.toUpperCase())){
            name=name+" "+serviceType;
        }
        for (SqlEnum sql : SqlEnum.values()) {
            if(sql.name.equalsIgnoreCase(name)) {
                String jdbcUrl = String.format(sql.getJdbcUrl(), ip, port, schema);
                return jdbcUrl;
            }
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持数据源类型");
    }
}
