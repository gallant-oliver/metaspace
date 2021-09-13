package io.zeta.metaspace.model.sourceinfo.derivetable.pojo;

import lombok.Data;

@Data
public class MetadataDeriveTableInfo {
    private String id;
    private String tableGuid;
    private String tableNameEn;
    private String tableNameZh;
    private String procedure;
    private String categoryId;
    private String category;

    private String dbType;

    private String dbId;

    private String sourceId;

    private String businessId;
    private String businessHeader;
    private String businessHeaderId;
    private String business;

    private String updateFrequency;

    private String etlPolicy;

    private String increStandard;

    private String cleanRule;

    private String filter;

    private String tenantId;

    private String remark;

    private Integer version;

    private String sourceTableGuid;

    private String creator;

    private String creatorName;
}
