package io.zeta.metaspace.model.pojo;

import lombok.Data;

@Data
public class TableRelation {
    private String relationshipGuid;
    private String categoryGuid;
    private String tableGuid;
    private String generateTime;
    private String tenantId;

}
