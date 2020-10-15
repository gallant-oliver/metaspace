package io.zeta.metaspace.adapter;


import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;
import schemacrawler.schemacrawler.IncludeAll;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;

import java.util.Arrays;

public interface Adapter {
    PluginDescriptor getDescriptor();

    /**
     * 获取插件名称，也是数据源类型
     */
    default String getName() {
        return getDescriptor().getPluginId();
    }

    /**
     *  用数据源信息和连接池配置创建新的 AdapterSource
     */
    AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool);

    AdapterTransformer getAdapterTransformer();

    /**
     * 获取元数据时排除的 schema 正则
     */
    default InclusionRule getSchemaRegularExpressionRule() {
        return null;
    }

    /**
     * 获取元数据时排除的 Table 正则
     */
    default InclusionRule getTableRegularExpressionRule() {
        return null;
    }

    /**
     * 根据TableSchema获取过滤器
     * @param tableSchema
     * @return
     */
    default SchemaCrawlerOptions getSchemaCrawlerOptions(TableSchema tableSchema) {
        SchemaCrawlerOptions options;
        if (tableSchema.isAll()) {
            options = SchemaCrawlerOptionsBuilder.builder()
                    .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard).setRetrieveRoutines(false).toOptions())
                    .includeSchemas(getSchemaRegularExpressionRule())
                    .includeTables(getTableRegularExpressionRule())
                    .toOptions();
        } else if (tableSchema.isAllDatabase()) {
            options = SchemaCrawlerOptionsBuilder.builder()
                    .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard).setRetrieveRoutines(false).toOptions())
                    .includeSchemas(getSchemaRegularExpressionRule())
                    .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.minimum).setRetrieveRoutines(false).setRetrieveTables(false).toOptions())
                    .toOptions();
        } else {
            options = SchemaCrawlerOptionsBuilder.builder()
                    .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard).setRetrieveRoutines(false).toOptions())
                    .includeSchemas(s -> tableSchema.getDatabases().contains(s))
                    .toOptions();
        }

        return options;
    }
    /**
     * 是否支持元数据同步
     */
    default boolean isSupportMetaDataSync() {
        return true;
    }
}
