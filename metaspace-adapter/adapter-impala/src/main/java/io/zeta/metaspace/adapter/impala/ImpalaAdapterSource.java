package io.zeta.metaspace.adapter.impala;

import io.zeta.metaspace.adapter.AbstractAdapterSource;
import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterBaseException;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ImpalaAdapterSource extends AbstractAdapterSource {


    public ImpalaAdapterSource(Adapter adapter, DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        super(adapter, dataSourceInfo, dataSourcePool);
        try {
            Class.forName(getDriverClass());
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
    }

    @Override
    public DataSource initDataSource() {
        return null;
    }

    /**
     * 获取 impala 的 jdbcUrl ，注意因为兼容旧配置，所以传入的地址需要存在前缀 jdbc:impala://
     */
    @Override
    public String getJdbcUrl(String proxyUser, String schema) {
        String jdbcUrl = getDataSourceInfo().getIp();
        StringBuilder url = new StringBuilder(jdbcUrl).append("/");
        if (StringUtils.isNotEmpty(schema)) {
            url.append(schema);
        }
        url.append(getDataSourceInfo().getJdbcParameter());
        if (StringUtils.isNotEmpty(proxyUser)) {
            url.append(";DelegationUID=").append(proxyUser);
        }
        return url.toString();
    }

    @Override
    public Connection getConnection(String proxyUser, String schema, String pool) {
        Properties properties = new Properties();
        if (pool != null && pool.length() != 0) {
            properties.setProperty("REQUEST_POOL", "'" + pool + "'");
        }

        try {
            return DriverManager.getConnection(getJdbcUrl(proxyUser, schema), properties);
        } catch (Exception e) {
            throw new AdapterBaseException("获取 IMPALA 连接失败:" + e.getMessage(), e);
        }
    }

    @Override
    public String getDriverClass() {
        return "com.cloudera.impala.jdbc41.Driver";
    }


    @Override
    public Connection getConnectionForDriver() {
        return null;
    }

    @Override
    public AdapterExecutor getNewAdapterExecutor() {
        return new ImpalaAdapterExecutor(this);
    }

    @Override
    public void closeDataSource() {

    }

    @Override
    public Connection getConnection() {
        return getConnection(null, "", getDataSourceInfo().getPool());
    }
}
