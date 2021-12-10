package io.zeta.metaspace.model.dto;

import lombok.Data;

@Data
public class Page {
    private String query;

    private int offset;

    private int limit;

}
