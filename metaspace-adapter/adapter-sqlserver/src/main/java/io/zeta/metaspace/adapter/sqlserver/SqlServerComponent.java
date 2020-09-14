package io.zeta.metaspace.adapter.sqlserver;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterComponent;
import io.zeta.metaspace.adapter.AdapterExtensionPoint;
import org.pf4j.Extension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

public class SqlServerComponent extends AdapterComponent {
    public SqlServerComponent(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class SqlServerExtension implements AdapterExtensionPoint {
        @Override
        public Adapter build(PluginDescriptor descriptor) {
            return new SqlServerAdapter(descriptor);
        }
    }
}
