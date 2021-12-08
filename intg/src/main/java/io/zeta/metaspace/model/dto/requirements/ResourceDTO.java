package io.zeta.metaspace.model.dto.requirements;

import io.zeta.metaspace.model.enums.ResourceState;
import io.zeta.metaspace.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 需求管理 - 资源
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {
    /**
     * 资源所属的需求ID
     */
    private String requirementId;
    /**
     * 资源名称
     */
    private String name;
    /**
     * 资源类型 {@link ResourceType#getDesc()}
     */
    private ResourceType type;
    /**
     * 数据表名
     */
    private String dataTableName;
    /**
     * 版本
     */
    private String version;
    /**
     * 状态 {@link ResourceState#getDesc()}
     */
    private ResourceState state;
    /**
     * 创建人账号名称
     */
    private String creator;
    
}
