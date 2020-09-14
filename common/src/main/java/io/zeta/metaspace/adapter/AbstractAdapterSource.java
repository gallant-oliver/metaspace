package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Getter
public abstract class AbstractAdapterSource implements AdapterSource {
    protected Adapter adapter;
    /**
     * 连接池
     */
    protected DataSource dataSource;
    /**
     * 连接池配置
     */
    protected DataSourcePool dataSourcePool;
    /**
     * 数据源信息
     */
    protected DataSourceInfo dataSourceInfo;

    public AbstractAdapterSource(Adapter adapter, DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        this.adapter = adapter;
        this.dataSourcePool = dataSourcePool;
        this.dataSourceInfo = dataSourceInfo;
        this.dataSource = initDataSource();
    }

    /**
     * 创建对象的时候初始化连接池
     */
    public abstract DataSource initDataSource();

    public abstract String getJdbcUrl(String proxyUser);

    public String getJdbcUrl() {
        return getJdbcUrl(null);
    }

    /**
     * 获取数据库连接
     * 首先从连接池中获取获取连接，异常则重新初始化连接池，使用直接使用驱动获取连接
     */
    @Override
    public Connection getConnection() {
        try {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                log.error("getConnection for DataSource fail :" + e.getMessage(), e);
                try {
                    closeDataSource();
                    initDataSource();
                } catch (Exception e1) {
                    log.error("Refresh the connection pool fail : " + e1.getMessage(), e);
                }
            }
            return getConnectionForDriver();
        } catch (Exception e) {
            log.info("getConnection for " + getJdbcUrl(null) + "," + getDataSourceInfo().getUserName() + "," + getDataSourceInfo().getPassword() + " error:" + e.getMessage(), e);
            throw new AtlasBaseException(e);
        }
    }
}
