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

package io.zeta.metaspace.model.datasource;

import io.zeta.metaspace.SqlEnum;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

public class DataSourceConnection {
    private String sourceType;
    private String ip;
    private String port;
    private String userName;
    private String password;
    private String database;
    private String jdbcParameter;
    private String driver;
    private String url;
    private String aesPassword;
    private String serviceType;

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAesPassword() {
        return aesPassword;
    }

    public void setAesPassword(String aesPassword) {
        this.aesPassword = aesPassword;
    }



    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getJdbcParameter() {
        return jdbcParameter;
    }

    public void setJdbcParameter(String jdbcParameter) {
        this.jdbcParameter = jdbcParameter;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver() throws AtlasBaseException {
        this.driver=SqlEnum.getDriverByName(sourceType,serviceType);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl() throws AtlasBaseException {
        this.url=SqlEnum.getUrlByName(sourceType,serviceType,ip,port,database);
    }
}