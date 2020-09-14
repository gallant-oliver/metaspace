package io.zeta.metaspace.adapter;

import io.zeta.metaspace.adapter.utils.JsonFileUtil;
import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.utils.AdapterUtil;
import org.apache.atlas.exception.AtlasBaseException;
import org.testng.annotations.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.sql.Connection;

/**
 * 元数据获取测试
 */
public class TestMetaDataInfo {
    static {
        System.setProperty("metaspace.adapter.dir", "target/pluginZip");
        AdapterUtil.findDatabaseAdapters();
    }

    /**
     * 测试 oracle 获取元数据，并手动获取列信息等
     */
    @Test
    public void testOracleMetaDataInfo() {
        AdapterSource adapterSource = AdapterUtil.getAdapterSource(JsonFileUtil.readDataSourceInfoJson("src/test/resources/dataSourceInfo/oracle.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo();
        System.out.println(metaDataInfo.getIncompleteTables().size());
        metaDataInfo.getIncompleteTables().forEach(table -> {
            System.out.println(table.getFullName());
            adapterExecutor.getColumns(table.getSchema().getName(), table.getName());
            adapterExecutor.getIndexes(table.getSchema().getName(), table.getName());
            adapterExecutor.getForeignKey(table.getSchema().getName(), table.getName());
            adapterExecutor.getTableCreateTime(table.getSchema().getName(), table.getName());
        });
    }

    @Test
    public void testMysqlMetaDataInfo() {
        AdapterSource adapterSource = AdapterUtil.getAdapterSource(JsonFileUtil.readDataSourceInfoJson("src/test/resources/dataSourceInfo/mysql.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo();
    }

    @Test
    public void testSqlServerMetaDataInfo() {
        AdapterSource adapterSource = AdapterUtil.getAdapterSource(JsonFileUtil.readDataSourceInfoJson("src/test/resources/dataSourceInfo/sqlserver.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo();
    }

    /**
     * 完全手动查询 6m
     */
    @Test
    public void testOracleMetaDataInfo2() throws SchemaCrawlerException {
        long a = System.currentTimeMillis();
        AdapterSource adapterSource = AdapterUtil.getAdapterSource(JsonFileUtil.readDataSourceInfoJson("src/test/resources/dataSourceInfo/oracle.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        Connection con = adapterSource.getConnection();
        SchemaCrawlerOptionsBuilder optionsBuilder = SchemaCrawlerOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.minimum())
                .includeSchemas(new RegularExpressionInclusionRule("XIANGLI"))
                .includeRoutines(new ExcludeAll())
                .includeColumns(new ExcludeAll());
        Catalog catalog = SchemaCrawlerUtility.getCatalog(con, optionsBuilder.toOptions());
        long b = System.currentTimeMillis();
        System.out.println("表加载:" + (b - a) + "  数目：" + catalog.getTables().size());
        catalog.getTables().forEach(table -> {
            System.out.println(adapterExecutor.getColumns(table.getSchema().getName(), table.getName()).size());
        });
        long c = System.currentTimeMillis();
        System.out.println("列加载加载:" + (c - b));

        catalog.getTables().forEach(table -> {
            System.out.println(adapterExecutor.getIndexes(table.getSchema().getName(), table.getName()).size());
        });
        long d = System.currentTimeMillis();
        System.out.println("索引加载:" + (d - c));
        catalog.getTables().forEach(table -> {
            System.out.println(adapterExecutor.getForeignKey(table.getSchema().getName(), table.getName()).size());
        });
        long e = System.currentTimeMillis();
        System.out.println("外键加载:" + (e - d));
        System.out.println("总时间:" + (e - a));
    }

    /**
     * 单表循环使用 SchemaCrawler 查询 1h 16 m 53 s
     * @throws SchemaCrawlerException
     */
    @Test
    public void testOracleMetaDataInfo3() throws SchemaCrawlerException {
        long a = System.currentTimeMillis();
        AdapterSource adapterSource = AdapterUtil.getAdapterSource(JsonFileUtil.readDataSourceInfoJson("src/test/resources/dataSourceInfo/oracle.json"));
        MetaDataInfo metaDataInfo = new MetaDataInfo();
        SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.minimum).setRetrieveRoutines(false).toOptions())
                .includeSchemas(new RegularExpressionInclusionRule("XIANGLI"))
                .toOptions();
        try (Connection connection = adapterSource.getConnection()) {
            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);
            metaDataInfo.getIncompleteTables().addAll(catalog.getTables());
            metaDataInfo.getTables().addAll(catalog.getTables());
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        System.out.println("开始" + metaDataInfo.getTables().size());
        for (Table table : metaDataInfo.getTables()) {
            adapterExecutor.getTable(table);
        }
        long e = System.currentTimeMillis();
        System.out.println("总时间:" + (e - a));
    }
}
