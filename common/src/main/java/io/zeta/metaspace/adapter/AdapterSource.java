package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.datasource.DataSourceInfo;

import java.sql.Connection;

public interface AdapterSource {

    Adapter getAdapter();

    String getDriverClass();

    Connection getConnection();

    Connection getConnectionForDriver();

    default Connection getConnectionForDriver(String proxyUser, String schema) {
        return getConnectionForDriver(proxyUser, schema);
    }

    default Connection getConnection(String proxyUser, String schema, String pool) {
        return getConnection();
    }

    AdapterExecutor getNewAdapterExecutor();

    DataSourceInfo getDataSourceInfo();

    void closeDataSource();

    String getJdbcUrl();

}
