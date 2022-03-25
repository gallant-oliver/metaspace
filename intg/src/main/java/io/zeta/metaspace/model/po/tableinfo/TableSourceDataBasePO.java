package io.zeta.metaspace.model.po.tableinfo;

import lombok.Data;

/**
 * 源数据配置信息
 * @author w
 */
@Data
public class TableSourceDataBasePO {
    /**
     * 数据表ID
     */
    private String guid;
    /**
     * 查询的数据库ip
     */
    private String host;
    /**
     * 查询的数据库端口
     */
    private String port;
    /**
     * 查询的数据库名称
     */
    private String database;
    /**
     * 查询的数据表
     */
    private String table;
    /**
     * 查询的数据库类型（Mysql、Oracle）
     */
    private String type;
}
