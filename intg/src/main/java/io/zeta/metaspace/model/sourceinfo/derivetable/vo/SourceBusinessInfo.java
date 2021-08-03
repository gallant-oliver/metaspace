// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/2/21 11:43
 */
package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "BusinessInfoHeader", description = "业务对象实体")
@Data
public class SourceBusinessInfo {

    @ApiModelProperty(value = "业务对象id")
    private String businessId;

    @ApiModelProperty(value = "对象名称")
    private String name;

    @ApiModelProperty(value = "业务目录")
    private String path;

    @ApiModelProperty(value = "业务目录id")
    private String categoryGuid;

}
