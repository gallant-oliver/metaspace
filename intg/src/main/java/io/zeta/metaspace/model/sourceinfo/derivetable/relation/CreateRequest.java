package io.zeta.metaspace.model.sourceinfo.derivetable.relation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.zeta.metaspace.model.enums.PrivilegeType;
import lombok.Data;

import java.util.List;

@Data
@ApiModel
public class CreateRequest {

    @ApiModelProperty(value = "授权用户组名称")
    private String userGroupId;

    @ApiModelProperty(value = "授权类型")
    private PrivilegeType privilegeType;

    @ApiModelProperty(value = "被授权表列表")
    private List<String> tableIdList;
}
