package io.zeta.metaspace.model.sourceinfo.derivetable.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 衍生表和字段的关联关系
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DeriveTableColumnRelation对象", description = "衍生表和字段的关联关系")
public class SourceInfoDeriveTableColumnRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "衍生表的主键id")
    private String tableId;

    @ApiModelProperty(value = "衍生表字段的guid")
    private String columnGuid;

    @ApiModelProperty(value = "衍生表的guid")
    private String tableGuid;

}
