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

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/19 9:33
 */
public class TableMetadata extends BasicMetadata {
    private String databaseName;
    private String tableType;
    private Boolean partitionTable;
    private String tableFormat;
    private String storeLocation;

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


    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

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

    public Boolean compareTable(TableMetadata tableMetadata) {
        if (StringUtils.isNotBlank(this.guid) && !this.guid.equals(tableMetadata.getGuid())) {
            return false;
        }
        if (StringUtils.isNotBlank(this.name) && !this.name.equals(tableMetadata.getName())) {
            return false;
        }
        if (StringUtils.isNotBlank(this.tableType) && !this.tableType.equals(tableMetadata.getTableType())) {
            return false;
        }
        if (StringUtils.isNotBlank(this.tableFormat) && !this.tableFormat.equals(tableMetadata.getTableFormat())) {
            return false;
        }
        if (StringUtils.isNotBlank(this.storeLocation) && !this.storeLocation.equals(tableMetadata.getStoreLocation())) {
            return false;
        }
        if (StringUtils.isNotBlank(this.status) && !this.status.equals(tableMetadata.getStatus())) {
            return false;
        }
        return true;
    }
}
