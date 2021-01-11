package io.zeta.metaspace.model.measure;

import lombok.Data;

@Data
public class MeasureDataSource {
    public String name;
    public MeasureConnector connector;
}
