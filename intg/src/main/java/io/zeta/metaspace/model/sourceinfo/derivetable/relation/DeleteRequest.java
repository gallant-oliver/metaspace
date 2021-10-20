package io.zeta.metaspace.model.sourceinfo.derivetable.relation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.zeta.metaspace.model.enums.PrivilegeType;
import lombok.Data;

import java.util.List;

@Data
@ApiModel
public class DeleteRequest {

    @ApiModelProperty(value = "被删除关系id列表")
    private List<String> groupTableRelationIds;
}
