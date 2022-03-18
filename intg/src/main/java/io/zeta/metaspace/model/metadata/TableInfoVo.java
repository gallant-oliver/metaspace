package io.zeta.metaspace.model.metadata;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @author w
 * 关系型数据血缘VO实体
 */
@Data
public class TableInfoVo {
    /**
     * 数据表ID
     */
    @NotBlank
    private String guid;
    /**
     * 查询的数据库ip
     */
    @NotBlank
    private String host;
    /**
     * 查询的数据库端口
     */
    @NotNull
    private Integer port;
    /**
     * 查询的数据库名称
     */
    @NotBlank
    private String database;
    /**
     * 查询的数据表
     */
    @NotBlank
    private String table;
    /**
     * 查询的数据源
     */
    private Long datasource;
    /**
     * 查询的数据库类型（Mysql、Oracle）
     */
    private String type;
    /**
     * 血缘深度
     */
    private int depth;
    /**
     * 血缘方向（INPUT, OUTPUT, BOTH）
     */
    private String direction;
    /**
     * 指向当前guid的描述id
     */
    private String descGuid;
}
