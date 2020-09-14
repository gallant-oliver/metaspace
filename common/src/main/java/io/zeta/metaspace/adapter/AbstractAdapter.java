package io.zeta.metaspace.adapter;

import lombok.Getter;
import org.pf4j.PluginDescriptor;

@Getter
public abstract class AbstractAdapter implements Adapter {
    /**
     * 插件原始信息
     */
    protected PluginDescriptor descriptor;

    public AbstractAdapter(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
