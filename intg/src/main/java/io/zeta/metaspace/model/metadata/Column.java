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

package io.zeta.metaspace.model.metadata;

import java.io.Serializable;

public class Column implements Serializable {
    private String columnId;
    private String columnName;
    private String tableId;
    private String tableName;
    private String databaseId;
    private String databaseName;
    private String type;
    private String description;
    private Boolean isPartitionKey;
    private String status;
    private Integer columnPrivilegeGuid;
    private String columnPrivilege;
    private String displayName;
    private String displayNameUpdateTime;

    private String displayNameOperator;

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Boolean getPartitionKey() {
        return isPartitionKey;
    }

    public void setPartitionKey(Boolean partitionKey) {
        isPartitionKey = partitionKey;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getColumnPrivilegeGuid() {
        return columnPrivilegeGuid;
    }

    public void setColumnPrivilegeGuid(Integer columnPrivilegeGuid) {
        this.columnPrivilegeGuid = columnPrivilegeGuid;
    }

    public String getColumnPrivilege() {
        return columnPrivilege;
    }

    public void setColumnPrivilege(String columnPrivilege) {
        this.columnPrivilege = columnPrivilege;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayNameUpdateTime() {
        return displayNameUpdateTime;
    }

    public void setDisplayNameUpdateTime(String displayNameUpdateTime) {
        this.displayNameUpdateTime = displayNameUpdateTime;
    }

    public String getDisplayNameOperator() {
        return displayNameOperator;
    }

    public void setDisplayNameOperator(String displayNameOperator) {
        this.displayNameOperator = displayNameOperator;
    }

}
