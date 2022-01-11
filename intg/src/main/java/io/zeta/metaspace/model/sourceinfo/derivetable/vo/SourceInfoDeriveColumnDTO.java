package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author huangrongwen
 * @Description: 衍生表导出dto
 * @date 2022/1/1015:49
 */
@Data
public class SourceInfoDeriveColumnDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "字段的guid")
    private String columnGuid;

    @ApiModelProperty(value = "字段英文名")
    private String columnNameEn;

    @ApiModelProperty(value = "字段中文名（描述信息）")
    private String columnNameZh;

    @ApiModelProperty(value = "数据类型")
    private String dataType;

    @ApiModelProperty(value = "源库系统名")
    private String dataBaseName;

    @ApiModelProperty(value = "源表guid")
    private String sourceTableGuid;

    @ApiModelProperty(value = "源表英文名称")
    private String sourceTableNameEn;

    @ApiModelProperty(value = "源表中文名称")
    private String sourceTableNameZh;

    @ApiModelProperty(value = "来源字段的guid")
    private String sourceColumnGuid;

    @ApiModelProperty(value = "源字段英文名称")
    private String sourceColumnNameEn;

    @ApiModelProperty(value = "源字段中文名称")
    private String sourceColumnNameZh;

    @ApiModelProperty(value = "源字段类型")
    private String sourceColumnType;

    @ApiModelProperty(value = "是否是主键")
    private String primaryKey;

    @ApiModelProperty(value = "是否脱敏")
    private String removeSensitive;

    @ApiModelProperty(value = "映射规则")
    private String mappingRule;

    @ApiModelProperty(value = "映射说明")
    private String mappingDescribe;

    @ApiModelProperty(value = "是否是分组字段")
    private String groupField;

    @ApiModelProperty(value = "是否保密")
    private String secret;

    @ApiModelProperty(value = "保密期限")
    private String secretPeriod;

    @ApiModelProperty(value = "是否重要")
    private String important;

    @ApiModelProperty(value = "是否是权限字段")
    private String permissionField;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "关联标签数组")
    private String tags;

    @ApiModelProperty(value = "目标字段脱敏规则")
    private String desensitizationRules;

}
