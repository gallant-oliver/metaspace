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
 * @date 2019/9/19 9:33
 */
package io.zeta.metaspace.model.metadata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Timestamp;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/19 9:33
 */
public class TableMetadata extends BasicMetadata {
    //private String guid;
    private String databaseName;
    /*private String name;
    private String creator;
    private String updater;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;*/
    private String tableType;
    private Boolean partitionTable;
    private String tableFormat;
    private String storeLocation;
    /*private String description;
    private Integer version;
    private String status;
    @JsonIgnore
    private Integer total;*/

    public TableMetadata() { }

    public TableMetadata(String guid, String databaseName, String name, String creator, String updater, Timestamp createTime, Timestamp updateTime, String tableType, Boolean partitionTable, String tableFormat, String storeLocation, String description, String status) {
        this.guid = guid;
        this.databaseName = databaseName;
        this.name = name;
        this.creator = creator;
        this.updater = updater;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.tableType = tableType;
        this.partitionTable = partitionTable;
        this.tableFormat = tableFormat;
        this.storeLocation = storeLocation;
        this.description = description;
        this.status = status;
    }

    /*public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }*/

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /*public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }*/

    /*public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }*/

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public Boolean getPartitionTable() {
        return partitionTable;
    }

    public void setPartitionTable(Boolean partitionTable) {
        this.partitionTable = partitionTable;
    }

    public String getTableFormat() {
        return tableFormat;
    }

    public void setTableFormat(String tableFormat) {
        this.tableFormat = tableFormat;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    /*public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }*/
}
