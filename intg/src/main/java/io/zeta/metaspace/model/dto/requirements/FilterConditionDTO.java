package io.zeta.metaspace.model.dto.requirements;

import io.zeta.metaspace.model.enums.FilterOperation;
import lombok.Data;

/**
 * 需求管理 - 过滤条件
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-07
 */
@Data
public class FilterConditionDTO {
    /**
     * 字段ID
     */
    private String columnId;
    /**
     * 操作符 {@link FilterOperation#getDesc()}
     */
    private FilterOperation operation;
    /**
     * 示例数据
     */
    private String sampleData;
    /**
     * 备注
     */
    private String description;
}
