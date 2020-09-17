package io.zeta.metaspace.adapter;

import lombok.Getter;

@Getter
public class AbstractAdapterTransformer implements AdapterTransformer {
    protected Adapter adapter;

    public AbstractAdapterTransformer(Adapter adapter) {
        this.adapter = adapter;
    }

}
