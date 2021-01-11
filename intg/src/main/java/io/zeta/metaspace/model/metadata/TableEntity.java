package io.zeta.metaspace.model.metadata;

import lombok.Data;

@Data
public class TableEntity {
    private String sourceId;
    private String databaseId;
    private String id;
    private String name;
    private String status;
    private String description;
    private boolean virtualTable;
    private String tableSize;
    private String sql;
    private boolean isHiveTable;
    private String tableType;
}
