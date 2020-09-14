package io.zeta.metaspace.adapter.oracle;

import io.zeta.metaspace.adapter.AbstractAdapter;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.RegularExpressionExclusionRule;
import schemacrawler.schemacrawler.RegularExpressionInclusionRule;

public class OracleAdapter extends AbstractAdapter {

    public OracleAdapter(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        return new OracleAdapterSource(this, dataSourceInfo, dataSourcePool);
    }

    @Override
    public InclusionRule getSchemaRegularExpressionRule() {
        return new RegularExpressionExclusionRule("SYS");
    }
}
