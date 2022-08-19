package io.zeta.metaspace.adapter.hive;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

@Slf4j
public class TestHive {
    @BeforeClass
    public void init() {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("java.security.krb5.conf", "../src/test/resources/krb5.conf");
        System.setProperty("atlas.conf", "../src/test/resources");
        System.setProperty("pf4j.mode", "development");
        System.setProperty("metaspace.adapter.dir", ".");

        UnitTestUtils.skipTest();

        Collection<Adapter> adapters = AdapterUtils.findDatabaseAdapters(true);
        log.info("已经加载成功插件：" + adapters.size());
    }

    /**
     * 使用 UGI 方式过 kerberos 认证
     */
    @Test
    public void testHive() throws SQLException {
        UnitTestUtils.login("metaspace@PANEL.COM","../src/test/resources/metaspace.keytab");
        try (Connection connection = AdapterUtils.getHiveAdapterSource().getConnection()) {
            Assert.assertNotNull(connection);
        }

        DataSourceInfo hiveInfo = AdapterUtils.getBuildInHive();
        hiveInfo.setIp("panel-1:2181,panel-2:2181,panel-3:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2");
        try (Connection connection = AdapterUtils.getAdapter("HIVE").getNewAdapterSource(hiveInfo, null).getConnection()) {
            Assert.assertNotNull(connection);
        }
    }

    @Test
    public void testGetTblRemarkCountByDb() {
        DataSourceInfo dataSourceInfo = UnitTestUtils.readDataSourceInfoJson("../src/test/resources/dataSourceInfo/oscar.json");
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        String db = dataSourceInfo.getUserName();
        String user = dataSourceInfo.getUserName();
        String pool = "root.default";
        float result = adapterExecutor.getTblRemarkCountByDb(adapterSource, user, db, pool, new HashMap<>());
        Assert.assertTrue(result > 0);

        float resultNull = adapterExecutor.getTblRemarkCountByDb(adapterSource, null, null, pool, new HashMap<>());
        Assert.assertTrue(0.0 == resultNull);
    }
}
