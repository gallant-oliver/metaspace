package io.zeta.metaspace.model.dataassets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.table.Tag;
import lombok.Data;

import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/11/15 14:20
 * @Description 数据表信息
 */
@Data
public class TableInfo {
    /**
    * 表id
    */
    private String tableId;

    /**
     * 表中文名
     */
    private String tableNameZh;

    /**
     * 表英文名
     */
    private String tableNameEn;

    /**
     * 是否保密
     */
    private Boolean secret;

    /**
     * 保密期限
     */
    private String period;

    /**
     * 是否重要
     */
    private Boolean important;

    /**
     * 设计人（衍生表登记）
     */
    private String creator;

    /**
     * 目标层级（技术目录）
     */
    private String category;

    /**
     * 更新频率
     */
    private String updateFrequency;

    /**
     * 表标签
     */
    private List<Tag> tags;

    /**
     * 所属租户id
     */
    private String tenantId;

    /**
     * 数据总数，分页使用
     */
    @JsonIgnore
    private int total;
}
