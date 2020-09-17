package io.zeta.metaspace.adapter.impala;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterComponent;
import io.zeta.metaspace.adapter.AdapterExtensionPoint;
import org.pf4j.Extension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

public class ImpalaComponent extends AdapterComponent {
    public ImpalaComponent(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class ImpalaExtension implements AdapterExtensionPoint {
        @Override
        public Adapter build(PluginDescriptor descriptor) {
            return new ImpalaAdapter(descriptor);
        }
    }
}
