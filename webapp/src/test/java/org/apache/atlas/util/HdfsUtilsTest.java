package org.apache.atlas.util;

import io.zeta.metaspace.utils.UnitTestUtils;
import io.zeta.metaspace.web.util.HdfsUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.stream.Collectors;


@Slf4j
public class HdfsUtilsTest {

    @Test(enabled = false)
    public void  testReadFile() throws IOException {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("java.security.krb5.conf", "src/test/resources/krb5.conf");
        System.setProperty("atlas.conf", "src/test/resources");

        UnitTestUtils.login("metaspace@PANEL.COM", "src/test/resources/metaspace.keytab");

        HdfsUtils hdfsUtils = new HdfsUtils("user");
        String tmp = "/apps/studio/project/abcd/griffin/t2/1604636195894/_METRICS";

       log.info("文件是否存在："+ hdfsUtils.exists(tmp));
       log.info("内容："+ String.join("\n", hdfsUtils.catFile(tmp, -1)));

    }

    @Test
    public void testOracleDriver(){
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
