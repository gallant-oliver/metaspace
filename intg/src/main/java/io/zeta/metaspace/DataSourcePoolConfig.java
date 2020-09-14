package io.zeta.metaspace;

import io.zeta.metaspace.model.datasource.DataSourcePool;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.ApplicationProperties;
import org.apache.commons.configuration.Configuration;

@Slf4j
public class DataSourcePoolConfig {
    private static Configuration conf;
    private static DataSourcePool defaultDataSourcePool;

    static {
        try {
            conf = ApplicationProperties.get();
            DataSourcePool dataSourcePool = new DataSourcePool();
            dataSourcePool.setMaximumPoolSize(conf.getInt("dataSourcePool.maximumPoolSize", 10));
            dataSourcePool.setIdleTimeout(conf.getInt("dataSourcePool.idleTimeout", 60000));
            dataSourcePool.setConnectionTimeout(conf.getInt("dataSourcePool.connectionTimeout", 60000));
            dataSourcePool.setValidationTimeout(conf.getInt("dataSourcePool.validationTimeout", 3000));
            dataSourcePool.setMaxLifeTime(conf.getInt("dataSourcePool.maxLifeTime", 60000));
            dataSourcePool.setCachePrepStmts(conf.getString("dataSourcePool.cachePrepStmts", "true"));
            dataSourcePool.setPrepStmtCacheSize(conf.getString("dataSourcePool.prepStmtCacheSize", "250"));
            dataSourcePool.setPrepStmtCacheSqlLimit(conf.getString("dataSourcePool.prepStmtCacheSqlLimit", "2048"));
            defaultDataSourcePool = dataSourcePool;
        } catch (Exception e) {
            log.info("加载连接池配置失败:" + e.getMessage());
            defaultDataSourcePool = null;
        }
    }

    public static DataSourcePool getDefaultDataSourcePool() {
        return defaultDataSourcePool;
    }
}
