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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

public class DataSourceHead {

    private String sourceId;
    private String sourceName;
    private String sourceType;
    private String description;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;
    private String updateUserName;
    private boolean rely;
    private boolean permission;
    @JsonIgnore
    private int totalSize;
    private String manager;
    private boolean editManager;
    private boolean isSchema;
    private String oracleDb;
    private String serviceType;
    /**
     * 数据库名
     */
    private String database;
    private String bizTreeId;

    public String getBizTreeId() {
        return bizTreeId;
    }

    public void setBizTreeId(String bizTreeId) {
        this.bizTreeId = bizTreeId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getOracleDb() {
        return oracleDb;
    }

    public void setOracleDb(String oracleDb) {
        this.oracleDb = oracleDb;
    }

    public boolean isSchema() {
        return isSchema;
    }

    public void setSchema(boolean schema) {
        isSchema = schema;
    }

    public boolean isEditManager() {
        return editManager;
    }

    public void setEditManager(boolean editManager) {
        this.editManager = editManager;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }
    private boolean synchronize=false;

    public boolean isSynchronize() {
        return synchronize;
    }

    public void setSynchronize(boolean synchronize) {
        this.synchronize = synchronize;
    }

    public boolean isRely() {
        return rely;
    }

    public void setRely(boolean rely) {
        this.rely = rely;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }


    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
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

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
