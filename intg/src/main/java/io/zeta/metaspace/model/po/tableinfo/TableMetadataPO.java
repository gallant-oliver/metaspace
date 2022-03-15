package io.zeta.metaspace.model.po.tableinfo;

import lombok.Data;

@Data
public class TableMetadataPO {
    /**
     * 数据源ip
     */
    private String ip;

    /**
     * 端口
     */
    private String port;

    /**
     * 数据库id
     */
    private String databaseGuid;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 数据表名称
     */
    private String tableName;

    /**
     * 数据表id
     */
    private String tableGuid;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 数据源id
     */
    private String sourceId;

    /**
     * 数据源类型
     */
    private String sourceType;
}
