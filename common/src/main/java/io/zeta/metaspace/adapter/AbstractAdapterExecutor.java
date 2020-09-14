package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.metadata.MetaDataInfo;
import lombok.Getter;
import org.apache.atlas.exception.AtlasBaseException;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.sql.Connection;

@Getter
public class AbstractAdapterExecutor implements AdapterExecutor {
    private final AdapterSource adapterSource;
    private final Adapter adapter;

    public AbstractAdapterExecutor(AdapterSource adapterSource) {
        this.adapterSource = adapterSource;
        this.adapter = adapterSource.getAdapter();
    }

    /**
     * 使用 SchemaCrawler 获取元数据信息
     */
    @Override
    public MetaDataInfo getMeteDataInfo() {
        MetaDataInfo metaDataInfo = new MetaDataInfo();
        SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard).setRetrieveRoutines(false).toOptions())
                .includeSchemas(getAdapter().getSchemaRegularExpressionRule())
                .includeTables(getAdapter().getTableRegularExpressionRule())
                .toOptions();
        try (Connection connection = getAdapterSource().getConnection()) {
            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);
            metaDataInfo.setJdbcUrl(catalog.getJdbcDriverInfo().getConnectionUrl());
            metaDataInfo.setSchemas(catalog.getSchemas());
            metaDataInfo.setTables(catalog.getTables());
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
        return metaDataInfo;
    }
}
