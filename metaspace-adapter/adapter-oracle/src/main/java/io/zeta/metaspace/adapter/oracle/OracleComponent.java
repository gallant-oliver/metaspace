package io.zeta.metaspace.adapter.oracle;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterComponent;
import io.zeta.metaspace.adapter.AdapterExtensionPoint;
import org.pf4j.Extension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

public class OracleComponent extends AdapterComponent {
    public OracleComponent(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class OracleExtension implements AdapterExtensionPoint {
        @Override
        public Adapter build(PluginDescriptor descriptor) {
            return new OracleAdapter(descriptor);
        }
    }
}
