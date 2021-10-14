package io.zeta.metaspace.model.sourceinfo.derivetable.relation;

import lombok.Data;

@Data
public class GroupDeriveTableRelation {

    private String groupTableRelationId;

    private String userGroupId;

    private String deriveTableId;

    private Boolean importancePrivilege;

    private Boolean securityPrivilege;

    private String tenantId;
}
