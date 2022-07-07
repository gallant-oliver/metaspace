package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 元数据采集衍生表差异对比详情
 *
 * @author w
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DeriveTableColumnContrastVO", description = "元数据采集衍生表差异对比详情")
public class DeriveTableColumnContrastVO {
    /**
     * 字段名
     */
    @ApiModelProperty(value = "字段名")
    private String columnName;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    private String type;
    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述")
    private String description;
    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段是否变更")
    private Boolean hasChange;
    /**
     * 元数据采集的差异（字段变更原因 ADD(字段新增)  DELETE (字段删除) TYPECHANGE (字段类型变更)）
     */
    @ApiModelProperty(value = "字段变更原因 ADD(字段新增)  DELETE (字段删除) TYPECHANGE (字段类型变更)")
    private String contrast;
}
