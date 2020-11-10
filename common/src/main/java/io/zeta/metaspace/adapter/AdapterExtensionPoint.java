package io.zeta.metaspace.adapter;


import org.pf4j.ExtensionPoint;
import org.pf4j.PluginDescriptor;

public interface AdapterExtensionPoint extends ExtensionPoint {

    Adapter build(PluginDescriptor descriptor);

}
