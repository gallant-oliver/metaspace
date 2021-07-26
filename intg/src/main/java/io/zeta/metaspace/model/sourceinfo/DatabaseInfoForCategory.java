package io.zeta.metaspace.model.sourceinfo;

import lombok.Data;

@Data
public class DatabaseInfoForCategory {

    private String id;

    private String parentCategoryId;

    private String name;
}
