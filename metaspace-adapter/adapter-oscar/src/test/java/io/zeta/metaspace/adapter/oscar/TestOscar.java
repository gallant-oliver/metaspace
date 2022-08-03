package io.zeta.metaspace.adapter.oscar;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;

@Slf4j
public class TestOscar {
    @BeforeClass
    public void init() {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("atlas.conf", "../src/test/resources");
        System.setProperty("pf4j.mode", "development");
        System.setProperty("metaspace.adapter.dir", ".");

        //UnitTestUtils.skipTest();

        Collection<Adapter> adapters = AdapterUtils.findDatabaseAdapters(true);
        log.info("已经加载成功插件：" + adapters.size());
    }

    @Test
    public void testMysql() {
        UnitTestUtils.checkConnection("../src/test/resources/dataSourceInfo/oscar.json");
    }

    /**
     * 测试获取元数据和查询创建时间
     */
    @Test
    public void testOscarMetaDataInfo() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("../src/test/resources/dataSourceInfo/oscar.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        TableSchema tableSchema = new TableSchema();
        tableSchema.setAll(true);
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
        metaDataInfo.getTables().stream().findAny().ifPresent(table -> {
            LocalDateTime dateTime = adapterExecutor.getTableCreateTime(table.getSchema().getName(), table.getName());
            log.info(table.getFullName() + " " + dateTime);
        });
    }


    /**
     * 测试获取元数据和建表语句
     */
    @Test
    public void testOscarGetCreateTableSql() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("../src/test/resources/dataSourceInfo/oscar.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        TableSchema tableSchema = new TableSchema();
        tableSchema.setAll(true);
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
        metaDataInfo.getTables().stream().findAny().ifPresent(table -> {
            String createTableSql = adapterExecutor.getCreateTableSql(table.getSchema().getName(), table.getName());
            log.info(createTableSql.toString());
        });
    }

    /**
     * 测试获取元数据和建表语句
     */
    @Test
    public void testOscarGetTableSize() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("../src/test/resources/dataSourceInfo/oscar.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        TableSchema tableSchema = new TableSchema();
        tableSchema.setAll(true);
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
        metaDataInfo.getTables().stream().findAny().ifPresent(table -> {
            float tableSize = adapterExecutor.getTableSize(table.getSchema().getName(), table.getName(), "root.default");
            log.info("tableSieze is ：{}", tableSize);
        });
    }

    /**
     * 测试获取元数据和建表语句
     */
    @Test
    public void testOscarGetUserObject() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("../src/test/resources/dataSourceInfo/oscar.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        TableSchema tableSchema = new TableSchema();
        tableSchema.setAll(true);
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
        metaDataInfo.getTables().stream().findAny().ifPresent(table -> {
            float tableSize = adapterExecutor.getTableSize(table.getSchema().getName(), table.getName(), "root.default");
            log.info("tableSieze is ：{}", tableSize);
        });
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
