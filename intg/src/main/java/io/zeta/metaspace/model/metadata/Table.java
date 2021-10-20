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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.table.Tag;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Table implements Serializable {
    private String tableId;
    private String tableName;
    private String owner;
    private List<DataOwnerHeader> dataOwner;
    private String updateTime;
    private String databaseId;
    private String databaseName;
    private Boolean importancePrivilege;
    private Boolean securityPrivilege;
    private String partitionKey;
    private Boolean partitionTable;
    private String format;
    private String location;
    private String description;
    private String type;
    private Boolean virtualTable;
    private String status;
    private List<Tag> tags;
    private boolean edit;
    private boolean subscribeTo;

    private String subordinateSystem;
    private String subordinateDatabase;
    private String systemAdmin;
    private String createTime;

    private String dataWarehouseAdmin;
    private String dataWarehouseDescription;
    @JsonIgnore
    private int total;

    private String sourceId;
    private String sourceName;
    private boolean hasDerivetable;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    private String databaseStatus;

    private List<String> relations;
    private String catalogAdmin;
    private String relationTime;

    private List<BusinessObject> businessObjects;

    private TablePermission tablePermission;

    private String displayName;

    private List<Column> columns;

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getSubordinateSystem() {
        return subordinateSystem;
    }

    public void setSubordinateSystem(String subordinateSystem) {
        this.subordinateSystem = subordinateSystem;
    }

    public String getSubordinateDatabase() {
        return subordinateDatabase;
    }

    public void setSubordinateDatabase(String subordinateDatabase) {
        this.subordinateDatabase = subordinateDatabase;
    }

    public String getDataWarehouseAdmin() {
        return dataWarehouseAdmin;
    }

    public void setDataWarehouseAdmin(String dataWarehouseAdmin) {
        this.dataWarehouseAdmin = dataWarehouseAdmin;
    }

    public String getDataWarehouseDescription() {
        return dataWarehouseDescription;
    }

    public void setDataWarehouseDescription(String dataWarehouseDescription) {
        this.dataWarehouseDescription = dataWarehouseDescription;
    }

    public String getRelationTime() {
        return relationTime;
    }

    public void setRelationTime(String relationTime) {
        this.relationTime = relationTime;
    }

    public List<BusinessObject> getBusinessObjects() {
        return businessObjects;
    }

    public void setBusinessObjects(List<BusinessObject> businessObjects) {
        this.businessObjects = businessObjects;
    }


    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<String> getRelations() {
        return relations;
    }

    public void setRelations(List<String> relations) {
        this.relations = relations;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

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


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
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


    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public Boolean getPartitionTable() {
        return partitionTable;
    }

    public void setPartitionTable(Boolean partitionTable) {
        this.partitionTable = partitionTable;
    }


    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public TablePermission getTablePermission() {
        return tablePermission;
    }

    public void setTablePermission(TablePermission tablePermission) {
        this.tablePermission = tablePermission;
    }

    public Boolean getVirtualTable() {
        return virtualTable;
    }

    public void setVirtualTable(Boolean virtualTable) {
        this.virtualTable = virtualTable;
    }

    public List<DataOwnerHeader> getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(List<DataOwnerHeader> dataOwner) {
        this.dataOwner = dataOwner;
    }

    public String getSystemAdmin() {
        return systemAdmin;
    }

    public void setSystemAdmin(String systemAdmin) {
        this.systemAdmin = systemAdmin;
    }

    public String getCatalogAdmin() {
        return catalogAdmin;
    }

    public void setCatalogAdmin(String catalogAdmin) {
        this.catalogAdmin = catalogAdmin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSubscribeTo() {
        return subscribeTo;
    }

    public void setSubscribeTo(boolean subscribeTo) {
        this.subscribeTo = subscribeTo;
    }

    @Data
    public static class BusinessObject {
        private String businessId;
        private String businessObject;
        private String department;
        private String businessLeader;
    }
}