package io.zeta.metaspace.model.metadata;

import lombok.Data;

@Data
public class TableInfoId {
    /**
     * 数据源ID
     */
    private String sourceId;

    /**
     * 数据库ID
     */
    private String databaseGuid;

    /**
     * 数据表ID
     */
    private String tableGuid;

    /**
     * 列ID
     */
    private String columnGuid;
}
