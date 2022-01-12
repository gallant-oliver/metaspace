package io.zeta.metaspace.model.po.tableinfo;

import lombok.Data;

@Data
public class TableInfoDerivePO {
    /**
     * 数据库id
     */
    private String databaseGuid;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 数据表id
     */
    private String tableGuid;

    /**
     * 数据表名称
     */
    private String tableName;

    /**
     * 字段id
     */
    private String columnGuid;

    /**
     * 字段名称
     */
    private String columnName;
}
