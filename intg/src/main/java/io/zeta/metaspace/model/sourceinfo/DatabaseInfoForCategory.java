package io.zeta.metaspace.model.sourceinfo;

import lombok.Data;

@Data
public class DatabaseInfoForCategory {

    private String id;

    private String parentCategoryId;

    private boolean importance;

    private String creator;

    private String databaseId;

    private String categoryId;

    private String name;
}
