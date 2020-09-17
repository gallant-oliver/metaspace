package io.zeta.metaspace.adapter.hive;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterComponent;
import io.zeta.metaspace.adapter.AdapterExtensionPoint;
import org.pf4j.Extension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

public class HiveComponent extends AdapterComponent {

    public HiveComponent(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class HiveExtension implements AdapterExtensionPoint {
        @Override
        public Adapter build(PluginDescriptor descriptor) {
            return new HiveAdapter(descriptor);
        }
    }
}
