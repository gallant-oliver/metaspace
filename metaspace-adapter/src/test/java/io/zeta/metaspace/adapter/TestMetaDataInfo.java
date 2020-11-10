package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.UnitTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * 元数据获取测试
 */
@Slf4j
@Test(enabled = false)
public class TestMetaDataInfo extends AdapterTestConfig {

    /**
     * 测试 oracle 获取元数据，并手动获取列信息等
     */
    public void testOracleMetaDataInfo() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("src/test/resources/dataSourceInfo/oracle.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        TableSchema tableSchema = new TableSchema();
        tableSchema.setAll(true);
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
        log.info("表总数：" + metaDataInfo.getIncompleteTables().size());
        metaDataInfo.getIncompleteTables().forEach(table -> {
            log.info(table.getFullName());
            adapterExecutor.getTable(table);
            adapterExecutor.getTableCreateTime(table.getSchema().getName(), table.getName());
        });
    }

    public void testMysqlMetaDataInfo() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("src/test/resources/dataSourceInfo/mysql.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        TableSchema tableSchema = new TableSchema();
        tableSchema.setAll(true);
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
        metaDataInfo.getTables().stream().findAny().ifPresent(table -> {
            adapterExecutor.getTableCreateTime(table.getSchema().getName(), table.getName());
        });
    }

    public void testSqlServerMetaDataInfo() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("src/test/resources/dataSourceInfo/sqlserver.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        TableSchema tableSchema = new TableSchema();
        tableSchema.setAll(true);
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
        metaDataInfo.getTables().stream().findAny().ifPresent(table -> {
            adapterExecutor.getTableCreateTime(table.getSchema().getName(), table.getName());
        });
    }

    /**
     * 测试 postgresql 手动同步元数据
     */
    public void testPostgresqlMetaDataInfo() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("src/test/resources/dataSourceInfo/postgresql.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        TableSchema tableSchema = new TableSchema();
        tableSchema.setAll(true);
        MetaDataInfo metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
        metaDataInfo.getTables().stream().findAny().ifPresent(table -> {
            adapterExecutor.getTableCreateTime(table.getSchema().getName(), table.getName());
        });
    }

    /**
     * 测试 oracle 获取 schema table  column 等用于创建 Api 的接口
     */
    public void testOraclePageResultInfo() {
        AdapterSource adapterSource = AdapterUtils.getAdapterSource(UnitTestUtils.readDataSourceInfoJson("src/test/resources/dataSourceInfo/oracle.json"));
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        int limit = -1;
        int offset = 0;
        Parameters parameters = new Parameters();
        parameters.setLimit(limit);
        parameters.setOffset(offset);
        PageResult<LinkedHashMap<String, Object>> schemePageResult = adapterExecutor.getSchemaPage(parameters);
        log.info("schema : " + schemePageResult.getTotalSize() + "  " + schemePageResult.getLists().toString());

        schemePageResult.getLists().stream().parallel().map(m -> m.get("schemaName").toString()).forEach(schemaName -> {
            PageResult<LinkedHashMap<String, Object>> tablePageResult = adapterExecutor.getTablePage(schemaName, parameters);
            log.info("table : " + tablePageResult.getTotalSize() + "  " + tablePageResult.getLists().toString());

            tablePageResult.getLists().stream().parallel().map(m -> m.get("tableName").toString()).forEach(tableName -> {
                try {
                    PageResult<LinkedHashMap<String, Object>> columnPageResult = adapterExecutor.getColumnPage(schemaName, tableName, parameters);
                    log.info("column : " + columnPageResult.getTotalSize() + "  " + columnPageResult.getLists().toString());
                } catch (Exception e) {
                    log.info(e.getMessage() + " " + tableName);
                    throw e;
                }
            });
        });
    }
}
