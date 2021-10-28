package io.zeta.metaspace.model.sourceinfo.derivetable.relation;

import lombok.Data;

@Data
public class GroupDeriveTableInfo {
    private Integer total;

    private String groupTableRelationId;

    private String tableId;

    private String dbname;

    private String businessCategoryName;

    private String businessObjectName;

    private Boolean importancePrivilege;

    private Boolean securityPrivilege;

    private String tableNameEn;

    private String tableNameZn;
}
