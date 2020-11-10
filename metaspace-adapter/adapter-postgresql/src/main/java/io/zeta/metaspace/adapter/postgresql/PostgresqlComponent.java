package io.zeta.metaspace.adapter.postgresql;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterComponent;
import io.zeta.metaspace.adapter.AdapterExtensionPoint;
import org.pf4j.Extension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

public class PostgresqlComponent extends AdapterComponent {
    public PostgresqlComponent(PluginWrapper wrapper) {
        super(wrapper);
    }


    @Extension
    public static class PostgresExtension implements AdapterExtensionPoint {
        @Override
        public Adapter build(PluginDescriptor descriptor) {
            return new PostgresqlAdapter(descriptor);
        }
    }
}
