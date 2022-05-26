package io.zeta.metaspace.adapter.oscar;

import io.zeta.metaspace.adapter.AbstractAdapter;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;
import schemacrawler.schemacrawler.*;

public class OscarAdapter extends AbstractAdapter {

    public OscarAdapter(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        return new OscarAdapterSource(this, dataSourceInfo, dataSourcePool);
    }

    @Override
    public AdapterTransformer getAdapterTransformer() {
        return new OscarAdapterTransformer(this);
    }

    @Override
    public InclusionRule getSchemaRegularExpressionRule() {
        return new RegularExpressionExclusionRule("(?:INFO_SCHEM|PUBLIC|REPLICATION|SYS_GLOBAL_TEMP)");
    }
}
