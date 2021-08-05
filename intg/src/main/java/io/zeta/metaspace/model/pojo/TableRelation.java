package io.zeta.metaspace.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class TableRelation {
    private String relationshipGuid;
    private String categoryGuid;
    private String tableGuid;
    private String generateTime;
    private String tenantId;
    private String categoryName;
    private Date createDate;
}
