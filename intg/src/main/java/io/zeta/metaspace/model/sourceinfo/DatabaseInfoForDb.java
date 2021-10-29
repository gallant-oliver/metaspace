package io.zeta.metaspace.model.sourceinfo;

import lombok.Data;

@Data
public class DatabaseInfoForDb {
    private String databaseId;
    private String databaseName;
    private String databaseAlias;
    private String dbType;
    private String tenantId;
    private String categoryId;

    /**
     * 数据源ID
     */
    private String sourceId;

    /**
     * 数据库实例
     */
    private String instanceGuid;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 数据库实例名
     */
    private String database;
}
