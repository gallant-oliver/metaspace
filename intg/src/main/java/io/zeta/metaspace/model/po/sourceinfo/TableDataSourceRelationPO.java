package io.zeta.metaspace.model.po.sourceinfo;

import lombok.Data;

@Data
public class TableDataSourceRelationPO {
    private String id;

    private String categoryId;

    private String tableId;

    private String dataSourceId;

    private String createTime;

    private String tenantId;

    private String updateTime;
}
