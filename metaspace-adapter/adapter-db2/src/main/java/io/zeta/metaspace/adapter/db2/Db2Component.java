package io.zeta.metaspace.adapter.db2;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterComponent;
import io.zeta.metaspace.adapter.AdapterExtensionPoint;
import org.pf4j.Extension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

public class Db2Component extends AdapterComponent {
    public Db2Component(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class Db2Extension implements AdapterExtensionPoint {
        @Override
        public Adapter build(PluginDescriptor descriptor) {
            return new Db2Adapter(descriptor);
        }
    }
}
