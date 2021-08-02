package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
@ApiModel(value = "SourceInfoDeriveTableColumnVO", description = "衍生表详情实体")
public class SourceInfoDeriveTableColumnVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "衍生表的guid")
    private String tableGuid;

    @ApiModelProperty(value = "表英文名")
    private String tableNameEn;

    @ApiModelProperty(value = "表中文名")
    private String tableNameZh;

    @ApiModelProperty(value = "存储过程")
    private String procedure;

    @ApiModelProperty(value = "目标层级库")
    private String category;

    @ApiModelProperty(value = "目标层级库Id")
    private String categoryId;

    @ApiModelProperty(value = "目标数据库id")
    private String dbId;

    @ApiModelProperty(value = "目标数据库名称")
    private String dbName;

    @ApiModelProperty(value = "目标数据源Id")
    private String sourceId;

    @ApiModelProperty(value = "目标数据源名称")
    private String sourceName;

    @ApiModelProperty(value = "源数据层/库")
    private String sourceDb;

    @ApiModelProperty(value = "源数据层/库Id")
    private String sourceDbGuid;

    @ApiModelProperty(value = "源表名称")
    private String sourceTable;

    @ApiModelProperty(value = "源表Id")
    private String sourceTableGuid;

    @ApiModelProperty(value = "业务目录")
    private String businessHeader;

    @ApiModelProperty(value = "业务目录Id")
    private String businessHeaderId;

    @ApiModelProperty(value = "主题对象")
    private String business;

    @ApiModelProperty(value = "主题对象Id")
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

    @ApiModelProperty(value = "设计人")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "修改人")
    private String updater;

    @ApiModelProperty(value = "修改时间")
    private String updateTime;

    @ApiModelProperty(value = "1:已提交，0：未提交", example = "1")
    private Integer state;

    @ApiModelProperty(value = "DDL语句")
    private String ddl;

    @ApiModelProperty(value = "DML语句")
    private String dml;

    @ApiModelProperty(value = "支持语句查询：true，false")
    private boolean queryDDL;

    @ApiModelProperty(value = "数据库类型")
    private String dbType;

    @ApiModelProperty(value = "字段列表")
    List<SourceInfoDeriveColumnVO> sourceInfoDeriveColumnVOS;

}
