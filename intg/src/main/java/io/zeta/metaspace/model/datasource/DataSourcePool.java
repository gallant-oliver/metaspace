package io.zeta.metaspace.model.datasource;

import lombok.Data;

@Data
public class DataSourcePool {
    private int maximumPoolSize;
    private long idleTimeout;
    private long connectionTimeout;
    private long validationTimeout;
    private long maxLifeTime;
    private String cachePrepStmts;
    private String prepStmtCacheSize;
    private String prepStmtCacheSqlLimit;
    private String keyUser;
    private String keyTabPath;
}
