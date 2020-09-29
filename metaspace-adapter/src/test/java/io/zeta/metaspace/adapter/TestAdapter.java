package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 测试插件 zip 包获取数据源连接
 */
@Slf4j
public class TestAdapter extends AdapterTestConfig {

    @Test
    public void testOracle() {
        UnitTestUtils.checkConnection("src/test/resources/dataSourceInfo/oracle.json");
    }

    @Test
    public void testOracleServiceName() {
        UnitTestUtils.checkConnection("src/test/resources/dataSourceInfo/oracle_service_name.json");
    }

    @Test
    public void testMysql() {
        UnitTestUtils.checkConnection("src/test/resources/dataSourceInfo/mysql.json");
    }

    @Test
    public void testSqlserver() {
        UnitTestUtils.checkConnection("src/test/resources/dataSourceInfo/sqlserver.json");
    }

    /**
     * 使用 UGI 方式过 kerberos 认证
     */
    @Test
    public void testHive() throws SQLException {
        UnitTestUtils.login("metaspace@PANEL.COM", "src/test/resources/metaspace.keytab");
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
    @Test
    public void testImpala() throws SQLException {
        try (Connection connection = AdapterUtils.getImpalaAdapterSource().getConnection()) {
            Assert.assertNotNull(connection);
        }
    }
}
