package io.zeta.metaspace.model.table.column.tag;

import lombok.Data;

@Data
public class ColumnTag {

    private String columnId;

    private String id;

    private String name;

    private String tenantId;

    private String createTime;

    private String updateTime;
}
