package io.zeta.metaspace.model.dataassets;

import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import lombok.Data;

import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/11/15 14:55
 * @Description
 */

@Data
public class ColumnInfo {
    /**
     * 字段id
     */
    private String columnId;

    /**
     * 字段中文名
     */
    private String columnNameZh;

    /**
     * 字段英文名
     */
    private String columnNameEn;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 是否为主键
     */
    private boolean isPrimaryKey;

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
     * 字段标签
     */
    private List<ColumnTag> tags;

    /**
     * 备注
     */
    private String remark;
}
