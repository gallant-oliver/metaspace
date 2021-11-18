package io.zeta.metaspace.adapter.postgresql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.zeta.metaspace.adapter.AbstractAdapterSource;
import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterBaseException;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class PostgresqlAdapterSource extends AbstractAdapterSource {

    private static final String JDBC_PREFIX = "jdbc:postgresql://";
    private static final String defaultJdbcParameter = "";


    public PostgresqlAdapterSource(Adapter adapter, DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        super(adapter, dataSourceInfo, dataSourcePool);
    }

    @Override
    public String getDriverClass() {
        return "org.postgresql.Driver";
    }


    @Override
    public DataSource initDataSource() {
        try {
            Class.forName(getDriverClass());
            HikariConfig jdbcConfig = new HikariConfig();
            jdbcConfig.setJdbcUrl(getJdbcUrl());
            jdbcConfig.setUsername(getDataSourceInfo().getUserName());
            jdbcConfig.setPassword(getDataSourceInfo().getPassword());
            jdbcConfig.setDriverClassName(getDriverClass());

            jdbcConfig.addDataSourceProperty("cachePrepStmts", dataSourcePool != null ? dataSourcePool.getCachePrepStmts() : "true");
            jdbcConfig.addDataSourceProperty("prepStmtCacheSize", dataSourcePool != null ? dataSourcePool.getPrepStmtCacheSize() : "250");
            jdbcConfig.addDataSourceProperty("prepStmtCacheSqlLimit", dataSourcePool != null ? dataSourcePool.getPrepStmtCacheSqlLimit() : "2048");

            jdbcConfig.setMaxLifetime(dataSourcePool != null ? dataSourcePool.getMaxLifeTime() : 60000);
            jdbcConfig.setMaximumPoolSize(dataSourcePool != null ? dataSourcePool.getMaximumPoolSize() : 60);
            jdbcConfig.setIdleTimeout(dataSourcePool != null ? dataSourcePool.getIdleTimeout() : 60000);
            jdbcConfig.setConnectionTimeout(dataSourcePool != null ? dataSourcePool.getConnectionTimeout() : 60000);
            jdbcConfig.setValidationTimeout(dataSourcePool != null ? dataSourcePool.getValidationTimeout() : 3000);
            jdbcConfig.setMinimumIdle(dataSourcePool != null ? dataSourcePool.getMinIdleSize() : 10);
            return new HikariDataSource(jdbcConfig);
        } catch (Exception e) {
            throw new AdapterBaseException(e);
        }
    }

    @Override
    public String getJdbcUrl(String proxyUser, String schema) {
        StringBuilder jdbcUrlBuilder = new StringBuilder()
                .append(JDBC_PREFIX)
                .append(getDataSourceInfo().getIp());

        if (StringUtils.isNotEmpty(getDataSourceInfo().getPort())) {
            jdbcUrlBuilder.append(":").append(getDataSourceInfo().getPort());
        }

        if (StringUtils.isNotEmpty(getDataSourceInfo().getDatabase())) {
            jdbcUrlBuilder.append("/").append(getDataSourceInfo().getDatabase());
        }


        if (StringUtils.isNotEmpty(defaultJdbcParameter)){
            jdbcUrlBuilder.append("?").append(defaultJdbcParameter);
        }
        String jdbcParameter = getDataSourceInfo().getJdbcParameter();
        if (StringUtils.isNotEmpty(jdbcParameter)) {
            if (StringUtils.isNotEmpty(defaultJdbcParameter)){
                jdbcUrlBuilder.append("&");
            }else{
                jdbcUrlBuilder.append("?");
            }
            if (jdbcParameter.startsWith("?") || jdbcParameter.startsWith("&")) {
                jdbcUrlBuilder.append(jdbcParameter.substring(1));
            } else {
                jdbcUrlBuilder.append(jdbcParameter);
            }
        }

        return jdbcUrlBuilder.toString();
    }

    @Override
    public void closeDataSource() {
        DataSource dataSource = getDataSource();
        CompletableFuture.runAsync(() -> {
            LocalDateTime start = LocalDateTime.now();
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            hikariDataSource.getHikariConfigMXBean().setMinimumIdle(0);
            HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
            hikariPoolMXBean.softEvictConnections();
            while (true) {
                int total = hikariPoolMXBean.getTotalConnections();
                LocalDateTime now = LocalDateTime.now();
                log.info("等待关闭连接池，开始时间：" + start + "当前时间 :" + now + "连接数 :" + total);
                if (total <= 0 || Duration.between(start, now).getSeconds() >= 600) {
                    break;
                }

                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                }
            }
            hikariDataSource.close();
        }).exceptionally(e -> {
            log.error("关闭数据源连接池异常", e);
            return null;
        });
    }

    /**
     * 获取数据库连接
     * 首先从连接池中获取获取连接，异常则重新初始化连接池，使用直接使用驱动获取连接
     */
    @Override
    public Connection getConnection() {
        try {
            Connection conn=null;
            try {
                conn=dataSource.getConnection();
                conn.setSchema("public");
                return conn;
            } catch (SQLException e) {
                log.error("getConnection for DataSource fail :" + e.getMessage(), e);
                try {
                    closeDataSource();
                    this.dataSource = initDataSource();
                } catch (Exception e1) {
                    log.error("Refresh the connection pool fail : " + e1.getMessage(), e);
                }
            }
            conn=getConnectionForDriver();
            conn.setSchema("public");
            return conn;
        } catch (Exception e) {
            log.info("getConnection for " + getJdbcUrl() + "," + getDataSourceInfo().getUserName() + "," + getDataSourceInfo().getPassword() + " error:" + e.getMessage(), e);
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取连接失败，请求检查数据源配置信息");
        }
    }

    @Override
    public Connection getConnection(String proxyUser, String schema, String pool) {
        try {
            Connection conn=null;
            try {
                conn=dataSource.getConnection();
                if(StringUtils.isNotEmpty(schema)){
                    conn.setSchema(schema);
                }else{
                    conn.setSchema("public");
                }
                return conn;
            } catch (SQLException e) {
                log.error("getConnection for DataSource fail :" + e.getMessage(), e);
                try {
                    closeDataSource();
                    this.dataSource = initDataSource();
                } catch (Exception e1) {
                    log.error("Refresh the connection pool fail : " + e1.getMessage(), e);
                }
            }
            conn=getConnectionForDriver();
            if(StringUtils.isNotEmpty(schema)){
                conn.setSchema(schema);
            }else{
                conn.setSchema("public");
            }
            return conn;
        } catch (Exception e) {
            log.info("getConnection for " + getJdbcUrl() + "," + getDataSourceInfo().getUserName() + "," + getDataSourceInfo().getPassword() + " error:" + e.getMessage(), e);
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取连接失败，请求检查数据源配置信息");
        }
    }

    @Override
    public Connection getConnectionForDriver() {
        try {
            return DriverManager.getConnection(getJdbcUrl(), getDataSourceInfo().getUserName(), getDataSourceInfo().getPassword());
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
    }

    @Override
    public AdapterExecutor getNewAdapterExecutor() {
        return new PostgresqlAdapterExecutor(this);
    }
}
