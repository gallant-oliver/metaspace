package io.zeta.metaspace.model.pojo;

import java.util.List;

public class TableInfo {
    private String tableGuid;
    private String tableName;
    private String dbName;
    private String status;
    private String createTime;
    private List<String> dataOwner;
    private String databaseGuid;
    private String databaseStatus;

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public String getDatabaseGuid() {
        return databaseGuid;
    }

    public void setDatabaseGuid(String databaseGuid) {
        this.databaseGuid = databaseGuid;
    }

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<String> getDataOwner() {
        /*List<Map> list = new ArrayList<>();
        if (dataOwner != null) {
            Gson gson = new Gson();
            list = gson.fromJson(dataOwner.getValue(), List.class);
        }*/
        return dataOwner;
    }

    public void setDataOwner(List<String> dataOwner) {
        this.dataOwner = dataOwner;
    }
}
