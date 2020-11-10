package io.zeta.metaspace.adapter.mysql;

import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterComponent;
import io.zeta.metaspace.adapter.AdapterExtensionPoint;
import org.pf4j.Extension;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

public class MysqlComponent extends AdapterComponent {
    public MysqlComponent(PluginWrapper wrapper) {
        super(wrapper);
    }


    @Extension
    public static class MysqlExtension implements AdapterExtensionPoint {
        @Override
        public Adapter build(PluginDescriptor descriptor) {
            return new MysqlAdapter(descriptor);
        }
    }
}
