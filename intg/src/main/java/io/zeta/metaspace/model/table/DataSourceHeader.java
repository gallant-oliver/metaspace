package io.zeta.metaspace.model.table;

import lombok.Data;

@Data
public class DataSourceHeader {
    private String sourceId;
    private String sourceName;
    private String sourceStatus;
    private int check;
    private int total;
}
