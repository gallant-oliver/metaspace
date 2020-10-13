package io.zeta.metaspace.adapter.db2;

import io.zeta.metaspace.adapter.AbstractAdapter;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;

public class Db2Adapter extends AbstractAdapter {

    public Db2Adapter(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        return new Db2AdapterSource(this, dataSourceInfo, dataSourcePool);
    }

    @Override
    public AdapterTransformer getAdapterTransformer() {
        return new Db2AdapterTransformer(this);
    }
}
