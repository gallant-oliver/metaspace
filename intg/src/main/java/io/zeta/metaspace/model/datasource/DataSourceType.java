package io.zeta.metaspace.model.datasource;

import org.apache.atlas.exception.AtlasBaseException;

import java.util.Arrays;

public enum DataSourceType {
    MYSQL("MYSQL"),
    POSTGRESQL("POSTGRESQL"),
    HIVE("HIVE"),
    IMPALA("IMPALA"),
    ORACLE("ORACLE"),
    SQLSERVER("SQLSERVER");

    private final String name;

    DataSourceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean equals(String typeName) {
        return this.name.equalsIgnoreCase(typeName);
    }

    public static DataSourceType getType(String typeName) {
        return Arrays.stream(values()).filter(v -> v.equals(typeName)).findAny().orElseThrow(() -> new AtlasBaseException("数据源类型错误"));
    }

    /**
     * 区分 hive 和其他数据源
     */
    public boolean isBuildIn() {
        return HIVE.equals(this);
    }
}
