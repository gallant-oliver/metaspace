package io.zeta.metaspace.adapter.hive;

import io.zeta.metaspace.adapter.AbstractAdapter;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;

public class HiveAdapter extends AbstractAdapter {
    public HiveAdapter(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        return new HiveAdapterSource(this, dataSourceInfo, dataSourcePool);
    }

    @Override
    public AdapterTransformer getAdapterTransformer() {
        return new HiveAdapterTransformer(this);
    }

}
