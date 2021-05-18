package io.zeta.metaspace.model.metadata;

import lombok.Data;

@Data
public class TableInfoId {
    /**
     * 数据源ID
     */
    private String sourceId;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 数据库ID
     */
    private String databaseGuid;

    /**
     * 数据库名称
     */
    private String dbname;

    /**
     * 数据表ID
     */
    private String tableGuid;

    /**
     * 数据表名称
     */
    private String tableName;

    /**
     * 列ID
     */
    private String columnGuid;

    /**
     * 列名称
     */
    private String columnName;
}
