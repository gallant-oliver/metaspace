package io.zeta.metaspace.model.sourceinfo;

import lombok.Data;

@Data
public class DatabaseInfoForDb {
    private String databaseId;
    private String databaseName;
    private String databaseAlias;
    private String dbType;
    private String tenantId;
    private String categoryId;
}
