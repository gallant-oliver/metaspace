package io.zeta.metaspace.model.po.sourceinfo;

import lombok.Data;

@Data
public class TableDataSourceRelationPO {
    private String id;

    private String categoryId;

    private String tableId;

    private String dataSourceId;

    private String databaseId;

    private String createTime;

    private String tenantId;

    private String updateTime;

    /**
     * 业务负责人id
     */
    private String businessLeader;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 账号
     */
    private String account;
}
