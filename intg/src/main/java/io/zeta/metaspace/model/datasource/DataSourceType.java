package io.zeta.metaspace.model.datasource;

public enum DataSourceType {
    MYSQL("mysql"),
    POSTGRESQL("postgresql"),
    HIVE("hive"),
    IMPALA("impala"),
    ORACLE("oracle"),
    SQLSERVER("sqlserver");

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
}
