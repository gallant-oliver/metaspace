package io.zeta.metaspace.utils;

import com.google.gson.Gson;
import io.zeta.metaspace.adapter.AdapterBaseException;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.testng.SkipException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
public class UnitTestUtils {
    private final static Gson gson = new Gson();

    /**
     * Kerberos 认证
     */
    public static UserGroupInformation login(String user, String keyTabPath) {
        try {
            Configuration conf = new Configuration();
            conf.set("hadoop.security.authentication", "Kerberos");
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab(user, keyTabPath);
            return UserGroupInformation.getLoginUser();
        } catch (Exception e) {
            throw new AdapterBaseException(e);
        }
    }

    /**
     * 读取数据配置信息
     */
    public static DataSourceInfo readDataSourceInfoJson(String path) {
        String json = null;
        try {
            json = Files.lines(Paths.get(path), StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gson.fromJson(json, DataSourceInfo.class);
    }


    /**
     * 测试数据源连接
     */
    public static void checkConnection(DataSourceInfo dataSourceInfo) {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
        try (Connection con = adapterSource.getConnection()) {
            log.info("get connection success");
        } catch (SQLException e) {
            throw new AdapterBaseException(e);
        }
    }

    public static void checkConnection(String path) {
        checkConnection(UnitTestUtils.readDataSourceInfoJson(path));
    }

    /**
     * 是否跳过插件的测试用例
     */
    public static void skipTest() {
        if (Boolean.TRUE != Boolean.valueOf(System.getProperty("metaspace.test.adapter"))) {
            throw new SkipException("跳过测试");
        }
    }

}
