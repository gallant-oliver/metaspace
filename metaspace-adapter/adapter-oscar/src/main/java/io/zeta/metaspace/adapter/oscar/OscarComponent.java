package io.zeta.metaspace.adapter.oscar;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterComponent;
import io.zeta.metaspace.adapter.AdapterExtensionPoint;
import org.pf4j.Extension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

public class OscarComponent extends AdapterComponent {
    public OscarComponent(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class OscarExtension implements AdapterExtensionPoint {
        @Override
        public Adapter build(PluginDescriptor descriptor) {
            return new OscarAdapter(descriptor);
        }
    }
}
