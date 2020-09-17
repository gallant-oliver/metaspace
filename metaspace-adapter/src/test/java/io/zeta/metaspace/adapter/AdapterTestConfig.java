package io.zeta.metaspace.adapter;

import io.zeta.metaspace.utils.AdapterUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeClass;

import java.util.Collection;

@Slf4j
public class AdapterTestConfig {
    public static final boolean ENABLE_TEST = false;

    @BeforeClass
    public void init() {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("java.security.auth.login.config", "src/test/resources/atlas_jaas.conf");
        System.setProperty("java.security.krb5.conf", "src/test/resources/krb5.conf");
        System.setProperty("atlas.conf", "src/test/resources");
        System.setProperty("metaspace.adapter.dir", "target/pluginZip");

        Collection<Adapter> adapters = AdapterUtils.findDatabaseAdapters();
        log.info("已经加载成功插件：" + adapters.size());
    }
}
