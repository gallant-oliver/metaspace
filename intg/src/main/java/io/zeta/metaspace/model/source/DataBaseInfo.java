package io.zeta.metaspace.model.source;

import lombok.Data;

@Data
public class DataBaseInfo {

    /**
     * 数据库id
     */
    private String databaseId;

    /**
     * 数据库名
     */
    private String databaseName;
}
