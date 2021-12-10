package io.zeta.metaspace.model.dto.requirements;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 需求管理 - 数据表的列
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementColumnDTO {
    private String columnId;
    private String columnName;
}
