package io.zeta.metaspace.adapter.oracle;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collection;

@Slf4j
public class TestOracle {
    @BeforeClass
    public void init() {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("atlas.conf", "../src/test/resources");
        System.setProperty("pf4j.mode", "development");
        System.setProperty("metaspace.adapter.dir", ".");

        UnitTestUtils.skipTest();

        Collection<Adapter> adapters = AdapterUtils.findDatabaseAdapters(true);
        log.info("已经加载成功插件：" + adapters.size());
    }

    @Test
    public void testOracle() {
        UnitTestUtils.checkConnection("../src/test/resources/dataSourceInfo/oracle.json");
    }

    @Test
    public void testOracleServiceName() {
        UnitTestUtils.checkConnection("../src/test/resources/dataSourceInfo/oracle_service_name.json");
    }
}
