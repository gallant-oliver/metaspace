package io.zeta.metaspace.adapter.oracle;

import io.zeta.metaspace.adapter.AbstractAdapter;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.schemacrawler.RegularExpressionExclusionRule;
import schemacrawler.schemacrawler.RegularExpressionInclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;

public class OracleAdapter extends AbstractAdapter {

    public OracleAdapter(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        return new OracleAdapterSource(this, dataSourceInfo, dataSourcePool);
    }

    @Override
    public AdapterTransformer getAdapterTransformer() {
        return new OracleAdapterTransformer(this);
    }

    @Override
    public InclusionRule getSchemaRegularExpressionRule() {
        return new RegularExpressionExclusionRule("SYS");
    }

    @Override
    public SchemaCrawlerOptions getSchemaCrawlerOptions(TableSchema tableSchema) {
        SchemaCrawlerOptions options;
        if (tableSchema.isAll()){
            options = SchemaCrawlerOptionsBuilder.builder()
                    .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.minimum).setRetrieveRoutines(false).toOptions())
                    .toOptions();
        } else if (tableSchema.isAllDatabase()){
            options = SchemaCrawlerOptionsBuilder.builder()
                    .includeSchemas(getSchemaRegularExpressionRule())
                    .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.minimum).setRetrieveRoutines(false).setRetrieveTables(false).toOptions())
                    .toOptions();
        }else{
            options = SchemaCrawlerOptionsBuilder.builder()
                    .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard).setRetrieveRoutines(false).toOptions())
                    .includeSchemas(s -> tableSchema.getDatabases().contains(s))
                    .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.minimum).setRetrieveRoutines(false).toOptions())
                    .toOptions();
        }

        return options;
    }
}
