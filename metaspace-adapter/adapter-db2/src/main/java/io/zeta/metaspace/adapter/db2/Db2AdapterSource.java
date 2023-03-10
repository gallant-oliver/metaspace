package io.zeta.metaspace.adapter.db2;

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
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Db2AdapterSource extends AbstractAdapterSource {

    private static final String JDBC_PREFIX = "jdbc:db2://";
    private static final String defaultJdbcParameter = "";

    public Db2AdapterSource(Adapter adapter, DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        super(adapter, dataSourceInfo, dataSourcePool);
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
            jdbcConfig.setMaximumPoolSize(dataSourcePool != null ? dataSourcePool.getMaximumPoolSize() : 10);
            jdbcConfig.setIdleTimeout(dataSourcePool != null ? dataSourcePool.getIdleTimeout() : 60000);
            jdbcConfig.setConnectionTimeout(dataSourcePool != null ? dataSourcePool.getConnectionTimeout() : 60000);
            jdbcConfig.setValidationTimeout(dataSourcePool != null ? dataSourcePool.getValidationTimeout() : 3000);
            jdbcConfig.setMinimumIdle(dataSourcePool != null ? dataSourcePool.getMinIdleSize() : 10);

            jdbcConfig.addDataSourceProperty("remarksReporting", true);

            String jdbcParameter = getDataSourceInfo().getJdbcParameter();
            if (StringUtils.isNotEmpty(jdbcParameter)) {
                for (String parameter : jdbcParameter.split("&")) {
                    String[] array = parameter.split("=");
                    if (array.length == 2) {
                        jdbcConfig.addDataSourceProperty(array[0], array[1]);
                    }
                }
            }

            return new HikariDataSource(jdbcConfig);
        } catch (Exception e) {
            throw new AdapterBaseException(e);
        }
    }

    @Override
    public String getJdbcUrl(String proxyUser, String schema) {
        StringBuilder jdbcUrlBuilder = new StringBuilder(JDBC_PREFIX);

        jdbcUrlBuilder.append(getDataSourceInfo().getIp());

        if (StringUtils.isNotEmpty(getDataSourceInfo().getPort())) {
            jdbcUrlBuilder.append(":").append(getDataSourceInfo().getPort());
        }

        if (StringUtils.isNotEmpty(getDataSourceInfo().getDatabase())) {
            jdbcUrlBuilder.append("/").append(getDataSourceInfo().getDatabase());
        }

        boolean isExistDefault = StringUtils.isNotEmpty(defaultJdbcParameter);
        if (isExistDefault) {
            jdbcUrlBuilder.append("?").append(defaultJdbcParameter);
        }

        String jdbcParameter = getDataSourceInfo().getJdbcParameter();
        if (StringUtils.isNotEmpty(jdbcParameter)) {
            if (isExistDefault) {
                jdbcUrlBuilder.append("&");
            } else {
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
    public String getDriverClass() {
        return "com.ibm.db2.jcc.DB2Driver";
    }

    @Override
    public Connection getConnectionForDriver() {
        try {
            return DriverManager.getConnection(getJdbcUrl(), getDataSourceInfo().getUserName(), getDataSourceInfo().getPassword());
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
    }

    @Override
    public AdapterExecutor getNewAdapterExecutor() {
        return new Db2AdapterExecutor(this);
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
                log.info("???????????????????????????????????????" + start + "???????????? :" + now + "????????? :" + total);
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
            log.error("??????????????????????????????", e);
            return null;
        });
    }
}
