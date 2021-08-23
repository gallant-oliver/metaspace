package io.zeta.metaspace.model.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class TableEntity {
    private String sourceId;
    private String sourceName;
    private String databaseId;
    private String dbName;
    private String id;
    private String name;
    private String status;
    private String description;
    private boolean virtualTable;
    private String tableSize;
    private String sql;
    private boolean isHiveTable;
    private String tableType;
    @JsonIgnore
    private int total;
}
