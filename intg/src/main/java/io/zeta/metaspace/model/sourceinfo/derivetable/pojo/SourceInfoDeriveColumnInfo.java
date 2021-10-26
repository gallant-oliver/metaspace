package io.zeta.metaspace.model.sourceinfo.derivetable.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * 衍生表对应的字段
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SourceInfoDeriveColumnInfo", description = "衍生表对应的字段")
public class SourceInfoDeriveColumnInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "字段的guid")
    private String columnGuid;

    @ApiModelProperty(value = "字段英文名", required = true)
    private String columnNameEn;

    @ApiModelProperty(value = "字段中文名（描述信息）", required = true)
    private String columnNameZh;

    @ApiModelProperty(value = "数据类型", required = true)
    private String dataType;

    @ApiModelProperty(value = "来源字段的guid（column_info）", required = true)
    private String sourceColumnGuid;

    @ApiModelProperty(value = "来源字段的名称")
    private String sourceColumnNameEn;

    @ApiModelProperty(value = "是否是主键")
    private boolean primaryKey;

    @ApiModelProperty(value = "是否脱敏")
    private boolean removeSensitive;

    @ApiModelProperty(value = "映射规则")
    private String mappingRule;

    @ApiModelProperty(value = "映射说明")
    private String mappingDescribe;

    @ApiModelProperty(value = "是否是分组字段")
    private boolean groupField;

    @ApiModelProperty(value = "是否保密")
    private boolean secret;

    @ApiModelProperty(value = "保密期限")
    private String secretPeriod;

    @ApiModelProperty(value = "是否重要")
    private boolean important;

    @ApiModelProperty(value = "是否是权限字段")
    private boolean permissionField;

    @ApiModelProperty(value = "备注")
    private String remark;

    @JsonIgnore
    private String tenantId;

    @JsonIgnore
    private String tableGuid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceInfoDeriveColumnInfo that = (SourceInfoDeriveColumnInfo) o;
        return Objects.equals(columnNameEn, that.columnNameEn) &&
                Objects.equals(columnNameZh, that.columnNameZh) &&
                Objects.equals(dataType, that.dataType) &&
                Objects.equals(sourceColumnGuid, that.sourceColumnGuid) &&
                Objects.equals(primaryKey, that.primaryKey) &&
                Objects.equals(removeSensitive, that.removeSensitive) &&
                Objects.equals(mappingRule, that.mappingRule) &&
                Objects.equals(mappingDescribe, that.mappingDescribe) &&
                Objects.equals(groupField, that.groupField) &&
                Objects.equals(secret, that.secret) &&
                Objects.equals(secretPeriod, that.secretPeriod) &&
                Objects.equals(important, that.important) &&
                Objects.equals(permissionField, that.permissionField) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(tableGuid, that.tableGuid);
    }

}
