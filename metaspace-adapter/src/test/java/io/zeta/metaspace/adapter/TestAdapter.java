package io.zeta.metaspace.adapter;

import io.zeta.metaspace.adapter.utils.JsonFileUtil;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.utils.AdapterUtil;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 测试插件获取数据源连接
 */
@Slf4j
public class TestAdapter {
    public static final Collection<Adapter> adapters;


    static {
        System.setProperty("metaspace.adapter.dir", "target/pluginZip");
        adapters = AdapterUtil.findDatabaseAdapters();
        System.out.println("已经加载成功插件：" + adapters.size());
    }

    public void checkConnection(String path) {
        checkConnection(JsonFileUtil.readDataSourceInfoJson(path));
    }

    public void checkConnection(DataSourceInfo dataSourceInfo) {
        AdapterSource adapterSource = AdapterUtil.getAdapterSource(dataSourceInfo);
        Connection con = adapterSource.getConnection();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("close connection fail at DataSourceService::checkConnection()", e);
            }
        }
    }

    @Test
    public void testOracle() {
        checkConnection("src/test/resources/dataSourceInfo/oracle.json");
    }

    @Test
    public void testOracleServiceName() {
        checkConnection("src/test/resources/dataSourceInfo/oracle_service_name.json");
    }

    @Test
    public void testMysql() {
        checkConnection("src/test/resources/dataSourceInfo/mysql.json");
    }

    @Test
    public void testSqlserver() {
        checkConnection("src/test/resources/dataSourceInfo/sqlserver.json");
    }
}
