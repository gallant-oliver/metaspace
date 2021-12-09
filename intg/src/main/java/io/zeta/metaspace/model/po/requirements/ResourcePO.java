package io.zeta.metaspace.model.po.requirements;

import io.zeta.metaspace.model.dto.requirements.ResourceDTO;
import io.zeta.metaspace.model.enums.ResourceState;
import io.zeta.metaspace.model.enums.ResourceType;
import lombok.Data;

/**
 * {@link ResourceDTO} 对应的PO
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-08
 */
@Data
public class ResourcePO {
    /**
     * 资源所属的需求ID
     */
    private String requirementId;
    /**
     * 资源名称
     */
    private String name;
    /**
     * 资源类型 {@link ResourceType#getCode()}
     */
    private Integer type;
    /**
     * 数据表名
     */
    private String dataTableName;
    /**
     * 版本
     */
    private String version;
    /**
     * 状态 {@link ResourceState#getCode()}
     */
    private Integer state;
    /**
     * 创建人账号名称
     */
    private String creator;
}
