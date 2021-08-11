package io.zeta.metaspace.model.result;

import lombok.Data;

@Data
public class AddRelationTable {
    private String tableId;
    private String tableName;
    private String databaseName;
    private String databaseId;
    private String createTime;
    private String status;
    private String path;
    private int check;
    private String sourceId;
    private String sourceName;
}
