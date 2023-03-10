package io.zeta.metaspace.adapter;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.security.UserGroupInformation;
import org.testng.annotations.Test;

import java.io.IOException;

@Slf4j
public class TestExecutor extends AdapterTestConfig {
    private static final String TestUser = "user";
    private static final String TestDb = "kylin";
    private static final String TestTable = "kylin_country";

    @Test
    public void testHiveExecutor() {
        UnitTestUtils.login("metaspace@PANEL.COM","src/test/resources/metaspace.keytab");
        AdapterSource adapterSource = AdapterUtils.getHiveAdapterSource();
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        boolean isExist = adapterExecutor.tableExists(TestUser, TestDb, TestTable);
        if (isExist) {
            log.info("表大小 :" + adapterExecutor.getTableSize(TestDb, TestTable, MetaspaceConfig.getHiveJobQueueName()));
        }
    }

    @Test
    public void testImpalaExecutor() {
        try {
            UserGroupInformation.getLoginUser().logoutUserFromKeytab();
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        System.setProperty("java.security.auth.login.config", "src/test/resources/atlas_jaas.conf");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        AdapterSource adapterSource = AdapterUtils.getImpalaAdapterSource();
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        log.info("表大小 :" + adapterExecutor.getTableSize(TestDb, TestTable, MetaspaceConfig.getImpalaResourcePool()));
    }

}
