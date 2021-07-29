package io.zeta.metaspace.model.pojo;

import lombok.Data;

import java.util.List;

@Data
public class TableInfo {
    private String tableGuid;
    private String tableName;
    private String dbName;
    private String status;
    private String createTime;
    private List<String> dataOwner;
    private String databaseGuid;
    private String databaseStatus = "ACTIVE";
    private String subordinateSystem;
    private String subordinateDatabase;
    private String systemAdmin;
    private String dataWarehouseAdmin;
    private String dataWarehouseDescription;
    private String catalogAdmin;
    private String displayName;
    private String description;
    private String sourceId;
    private String owner;
    private String type = "table";
}
