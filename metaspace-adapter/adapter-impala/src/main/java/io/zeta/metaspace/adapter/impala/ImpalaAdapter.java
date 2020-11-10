package io.zeta.metaspace.adapter.impala;

import io.zeta.metaspace.adapter.AbstractAdapter;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;

public class ImpalaAdapter extends AbstractAdapter {
    public ImpalaAdapter(PluginDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        return new ImpalaAdapterSource(this, dataSourceInfo, dataSourcePool);
    }

    @Override
    public AdapterTransformer getAdapterTransformer() {
        return new ImpalaAdapterTransformer(this);
    }

    @Override
    public boolean isSupportMetaDataSync() {
        return false;
    }
}
