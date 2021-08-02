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
 * @date 2018/11/21 13:48
 */
package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/21 13:48
 */
@ApiModel(value = "SourceTableEntity",description = "源表实体")
@Data
public class SourceTableEntity {

    @ApiModelProperty(value = "技术目录guid")
    private String categoryGuid;

    @ApiModelProperty(value = "表名称")
    private String tableName;

    @ApiModelProperty(value = "表guid")
    private String tableGuid;

}
