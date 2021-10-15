package io.zeta.metaspace.model.sourceinfo.derivetable.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 衍生表信息表
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DeriveTableInfo对象", description = "衍生表信息表")
public class SourceInfoDeriveTableInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "衍生表的guid")
    private String tableGuid;

    @ApiModelProperty(value = "表英文名")
    private String tableNameEn;

    @ApiModelProperty(value = "是否重要")
    private Boolean importance;

    @ApiModelProperty(value = "是否保密")
    private Boolean security;

    @ApiModelProperty(value = "表中文名")
    private String tableNameZh;

    @ApiModelProperty(value = "存储过程")
    private String procedure;

    @ApiModelProperty(value = "技术目录对应id")
    private String categoryId;

    @ApiModelProperty(value = "目标层级/库类型")
    private String dbType;

    @ApiModelProperty(value = "目标数据库Id")
    private String dbId;

    @ApiModelProperty(value = "目标数据源Id")
    private String sourceId;

    @ApiModelProperty(value = "业务目录对应id")
    private String businessId;

    @ApiModelProperty(value = "更新频率")
    private String updateFrequency;

    @ApiModelProperty(value = "etl策略")
    private String etlPolicy;

    @ApiModelProperty(value = "增量抽取标准")
    private String increStandard;

    @ApiModelProperty(value = "目标清洗规则")
    private String cleanRule;

    @ApiModelProperty(value = "过滤条件")
    private String filter;

    @ApiModelProperty(value = "租户id")
    private String tenantId;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "版本号", example = "1")
    private Integer version;

    @ApiModelProperty(value = "源表的guid")
    private String sourceTableGuid;

    @ApiModelProperty(value = "设计人")
    private String creator;

    @ApiModelProperty(value = "设计人名称")
    private String creatorName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String createTimeStr;

    @ApiModelProperty(value = "修改人")
    private String updater;

    @ApiModelProperty(value = "修改人名称")
    private String updaterName;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private String updateTimeStr;

    @ApiModelProperty(value = "1:已提交，0：未提交", example = "1")
    private Integer state;

    @ApiModelProperty(value = "DDL语句")
    private String ddl;

    @ApiModelProperty(value = "DML语句")
    private String dml;

    private int total;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceInfoDeriveTableInfo that = (SourceInfoDeriveTableInfo) o;
        return total == that.total &&
                Objects.equals(tableGuid, that.tableGuid) &&
                Objects.equals(tableNameEn, that.tableNameEn) &&
                Objects.equals(tableNameZh, that.tableNameZh) &&
                Objects.equals(procedure, that.procedure) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(dbType, that.dbType) &&
                Objects.equals(dbId, that.dbId) &&
                Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(businessId, that.businessId) &&
                Objects.equals(updateFrequency, that.updateFrequency) &&
                Objects.equals(etlPolicy, that.etlPolicy) &&
                Objects.equals(increStandard, that.increStandard) &&
                Objects.equals(cleanRule, that.cleanRule) &&
                Objects.equals(filter, that.filter) &&
                Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(sourceTableGuid, that.sourceTableGuid);
    }

}
