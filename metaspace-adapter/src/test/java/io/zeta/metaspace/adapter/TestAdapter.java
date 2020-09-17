package io.zeta.metaspace.adapter;

import io.zeta.metaspace.adapter.utils.JsonFileUtil;
import io.zeta.metaspace.adapter.utils.KerberosTestUtils;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.utils.AdapterUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 测试插件获取数据源连接
 */
@Slf4j
public class TestAdapter extends AdapterTestConfig {

    public void checkConnection(String path) {
        checkConnection(JsonFileUtil.readDataSourceInfoJson(path));
    }

    public void checkConnection(DataSourceInfo dataSourceInfo) {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
        Connection con = adapterSource.getConnection();
        Assert.assertNotNull(con);
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("close connection fail at DataSourceService::checkConnection()", e);
            }
        }
    }

    @Test(enabled = ENABLE_TEST)
    public void testOracle() {
        checkConnection("src/test/resources/dataSourceInfo/oracle.json");
    }

    @Test(enabled = ENABLE_TEST)
    public void testOracleServiceName() {
        checkConnection("src/test/resources/dataSourceInfo/oracle_service_name.json");
    }

    @Test(enabled = ENABLE_TEST)
    public void testMysql() {
        checkConnection("src/test/resources/dataSourceInfo/mysql.json");
    }

    @Test(enabled = ENABLE_TEST)
    public void testSqlserver() {
        checkConnection("src/test/resources/dataSourceInfo/sqlserver.json");
    }

    /**
     * 使用 UGI 方式过 kerberos 认证
     */
    @Test(enabled = ENABLE_TEST)
    public void testHive() throws SQLException {
        KerberosTestUtils.login();
        try (Connection connection = AdapterUtils.getHiveAdapterSource().getConnection()) {
            Assert.assertNotNull(connection);
        }

        DataSourceInfo hiveInfo = AdapterUtils.getBuildInHive();
        hiveInfo.setIp("panel-1:2181,panel-2:2181,panel-3:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2");
        try (Connection connection = AdapterUtils.getAdapter("HIVE").getNewAdapterSource(hiveInfo, null).getConnection()) {
            Assert.assertNotNull(connection);
        }
    }

    /**
     * 使用 jaas 方式过 kerberos 认证
     */
    @Test(enabled = ENABLE_TEST)
    public void testImpala() throws SQLException {
        try (Connection connection = AdapterUtils.getImpalaAdapterSource().getConnection()) {
            Assert.assertNotNull(connection);
        }
    }
}
