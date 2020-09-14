package io.zeta.metaspace.adapter.mysql;

import io.zeta.metaspace.adapter.AbstractAdapter;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.RegularExpressionExclusionRule;

public class MysqlAdapter extends AbstractAdapter {

    public MysqlAdapter(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        return new MysqlAdapterSource(this, dataSourceInfo, dataSourcePool);
    }

    @Override
    public InclusionRule getSchemaRegularExpressionRule() {
        return new RegularExpressionExclusionRule("sys");
    }
}
