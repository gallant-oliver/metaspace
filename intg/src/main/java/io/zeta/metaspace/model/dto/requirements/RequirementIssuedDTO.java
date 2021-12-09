package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * 需求管理 - 需求下发信息
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-08
 */
@Data
public class RequirementIssuedDTO {
    /**
     * 下发租户: 需求关联的数据表所属的租户
     */
    private String tenantName;
    /**
     * 所属技术目录：需求关联的数据表 源信息登记的技术目录
     */
    private String technicalCatalog;
    /**
     * 所属数据库：关联数据表所属的数据库
     */
    private String database;
    /**
     * 业务责任人：数据库登记的业务负责人信息
     */
    private String businessOwner;
    /**
     * 业务责任人是否具有下发租户的权限
     * <p>
     * ture - 有权限
     * <p>
     * false - 无权限
     */
    private Boolean tenantPermission;
}
