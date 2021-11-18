package io.zeta.metaspace.model.dataassets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.table.Tag;
import lombok.Data;

import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/11/10 16:34
 * @Description 数据资产搜索列表（业务对象、业务对象挂载表等）
 */

@Data
public class DataAssets {
    /**
     * id
     */
    private String id;

    /**
    * 名称
    */
    private String name;

    /**
     * 资产类型（0业务对象；1数据表）
     */
    private int type;

    /**
     * 目录路径
     */
    private String businessPath;

    /**
     * 技术路径（数据表需要）
     */
    private String technicalPath;

    /**
     * 所属业务对象id（数据表信息展示）
     */
    private String businessId;

    /**
     * 所属业务对象名称（数据表信息展示）
     */
    private String businessName;

    /**
     * 所属租户id
     */
    private String tenantId;

    /**
     * 描述
     */
    private String description;

    /**
     * 表标签（数据表信息展示）
     */
    private List<Tag> tags;

    /**
     * 数据总数，分页使用
     */
    @JsonIgnore
    private int total;
}
