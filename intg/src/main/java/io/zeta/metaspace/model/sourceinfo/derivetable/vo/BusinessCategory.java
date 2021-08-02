package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "BusinessCategory", description = "业务目录实体")
@Data
public class BusinessCategory {

    @ApiModelProperty(value = "主键id")
    private String guid;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "父级id")
    private String parentCategoryGuid;

    @ApiModelProperty(value = "同级上一层id")
    private String upBrotherCategoryGuid;

    @ApiModelProperty(value = "同级下一层id")
    private String downBrotherCategoryGuid;

    @ApiModelProperty(value = "层级")
    private int level;


}
