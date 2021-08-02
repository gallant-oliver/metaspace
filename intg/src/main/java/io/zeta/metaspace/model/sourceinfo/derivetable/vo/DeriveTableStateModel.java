package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName DeriveTableStateModel
 * @Descroption TODO
 * @Author Lvmengliang
 * @Date 2021/7/20 9:56
 * @Version 1.0
 */
@Data
@ApiModel(value = "DeriveTableStateModel", description = "衍生表状态模型")
@Accessors(chain = true)
public class DeriveTableStateModel {

    @ApiModelProperty(value = "状态名称")
    private String name;

    @ApiModelProperty(value = "状态编码", example = "1")
    private int state;
}
