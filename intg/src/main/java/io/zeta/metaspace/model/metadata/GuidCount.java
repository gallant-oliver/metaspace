package io.zeta.metaspace.model.metadata;

import lombok.Data;

@Data
public class GuidCount {
    private String guid;
    private int count;
    private String sourceId;

}
