package org.apache.atlas.model.metadata;

public class Table {
    private String tableId;
    private String tableName;
    private String business;
    private String relations;
    private String owner;
    private String createTime;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String category;
    private String databaseId;
    private String databaseName;
    private String tableLife;
    private String partitionKey;
    private Boolean partitionTable;
    private String partitionLife;
    private String format;
    private String location;
    private String description;
    private String topic;
    private TablePermission tablePermission;

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

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getRelations() {
        return relations;
    }

    public void setRelations(String relations) {
        this.relations = relations;
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

    public String getTableLife() {
        return tableLife;
    }

    public void setTableLife(String tableLife) {
        this.tableLife = tableLife;
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

    public String getPartitionLife() {
        return partitionLife;
    }

    public void setPartitionLife(String partitionLife) {
        this.partitionLife = partitionLife;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public TablePermission getTablePermission() {
        return tablePermission;
    }

    public void setTablePermission(TablePermission tablePermission) {
        this.tablePermission = tablePermission;
    }
}