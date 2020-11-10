package io.zeta.metaspace.adapter.Impala;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

@Slf4j
public class TestImpala {
    @BeforeClass
    public void init() {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("java.security.auth.login.config", "src/test/resources/atlas_jaas.conf");
        System.setProperty("java.security.krb5.conf", "../src/test/resources/krb5.conf");
        System.setProperty("atlas.conf", "../src/test/resources");
        System.setProperty("pf4j.mode", "development");
        System.setProperty("metaspace.adapter.dir", ".");

        UnitTestUtils.skipTest();

        Collection<Adapter> adapters = AdapterUtils.findDatabaseAdapters(true);
        log.info("已经加载成功插件：" + adapters.size());
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
