package io.zeta.metaspace.model.datasource;

import org.apache.atlas.exception.AtlasBaseException;

import java.util.Arrays;

public enum DataSourceType {
    MYSQL("MYSQL",3306),
    POSTGRESQL("POSTGRESQL",5432),
    HIVE("HIVE",10000),
    IMPALA("IMPALA",21050),
    ORACLE("ORACLE",1521),
    DB2("DB2",50000),
    SQLSERVER("SQLSERVER",1433),
    HBASE("HBASE",2181);

    private final String name;
    private final int defaultPort;

    DataSourceType(String name,int defaultPort) {
        this.name = name;
        this.defaultPort=defaultPort;
    }

    public String getName() {
        return name;
    }

    public int getDefaultPort(){
        return defaultPort;
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
        return HIVE.equals(this)||IMPALA.equals(this);
    }
    /**
     * 区分hbase和kafka数据源
     */
    public boolean isAdapter(){
        return !HBASE.equals(this);
    }
}
