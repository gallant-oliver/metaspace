package io.zeta.metaspace.web.model;

import lombok.Data;

import java.util.List;

@Data
public class TableSchema {
    /**
     * 数据源id
     */
    String instance;
    String database;
    List<String> databases;
    String table;
    boolean allDatabase;
}
