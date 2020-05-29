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

public class DataSource {
    private String sourceId;
    private String sourceName;
    private String sourceType;
    private String description;
    private String createTime;
    private String updateTime;
    private String updateUserid;
    private String ip;
    private String port;
    private String userName;
    private String password;
    private String database;
    private String jdbcParameter;

    public DataSource(){}

    public DataSource(String sourceId, String sourceName, String sourceType, String description, String createTime, String updateTime, String updateUserid, String ip, String port, String userName, String password, String database, String jdbcParameter) {
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.description = description;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.updateUserid = updateUserid;
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.database = database;
        this.jdbcParameter = jdbcParameter;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceid(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUserid() {
        return updateUserid;
    }

    public void setUpdateUserid(String updateUserid) {
        this.updateUserid = updateUserid;
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

    @Override
    public String toString() {
        return "DataSource{" +
               "sourceId='" + sourceId + '\'' +
               ", sourceName='" + sourceName + '\'' +
               ", sourceType='" + sourceType + '\'' +
               ", description='" + description + '\'' +
               ", createTime='" + createTime + '\'' +
               ", updateTime='" + updateTime + '\'' +
               ", updateUserid='" + updateUserid + '\'' +
               ", ip='" + ip + '\'' +
               ", port='" + port + '\'' +
               ", userName='" + userName + '\'' +
               ", password='" + password + '\'' +
               ", database='" + database + '\'' +
               ", jdbcParameter='" + jdbcParameter + '\'' +
               '}';
    }
}
