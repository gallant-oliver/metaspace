package io.zeta.metaspace.web.model;

import lombok.Data;

@Data
public class TableSchema {
    /**
     * 数据源id
     */
    String instance;
    String database;
    String table;
}
