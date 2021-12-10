package io.zeta.metaspace.model.po.requirements;

import io.zeta.metaspace.model.dto.requirements.RequirementIssuedDTO;
import lombok.Data;

/**
 * {@link RequirementIssuedDTO}
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-09
 */
@Data
public class RequirementIssuedPO {
    /**
     * 下发租户ID
     */
    private String tenantId;
    /**
     * 下发租户: 需求关联的数据表所属的租户
     */
    private String tenantName;
    /**
     * 所属技术目录ID
     */
    private String categoryId;
    /**
     * 所属数据库：关联数据表所属的数据库
     */
    private String database;
    /**
     * 业务责任人：数据库登记的业务负责人信息
     */
    private String businessOwner;
}
