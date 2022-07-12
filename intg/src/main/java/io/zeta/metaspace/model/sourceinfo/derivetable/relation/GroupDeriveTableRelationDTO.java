package io.zeta.metaspace.model.sourceinfo.derivetable.relation;

import lombok.Data;

/**
 * @author huangrongwen
 * @Description: 消息中心衍生表权限信息
 * @date 2022/7/1117:34
 */
@Data
public class GroupDeriveTableRelationDTO {

    private String userGroupId;

    private String userGroupName;

    private String tableName;

    private Boolean importancePrivilege;

    private Boolean securityPrivilege;

    private String tenantId;
}
