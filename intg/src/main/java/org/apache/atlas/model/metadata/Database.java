package org.apache.atlas.model.metadata;

import java.util.List;

public class Database {
    private String databaseId;
    private String databaseName;
    private List tableList;

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

    public List getTableList() {
        return tableList;
    }

    public void setTableList(List tableList) {
        this.tableList = tableList;
    }
}
