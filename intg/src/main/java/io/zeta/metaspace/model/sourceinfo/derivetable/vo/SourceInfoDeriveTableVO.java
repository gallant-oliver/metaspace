package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@ApiModel(value = "SourceInfoDeriveTableVO", description = "衍生表列表实体")
public class SourceInfoDeriveTableVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private String id;

    @ApiModelProperty(value = "表guid")
    private String tableGuid;

    @ApiModelProperty(value = "表英文名")
    private String tableNameEn;

    @ApiModelProperty(value = "表中文名")
    private String tableNameZh;

    @ApiModelProperty(value = "目标层级库")
    private String category;

    @ApiModelProperty(value = "关联主题")
    private String business;

    @ApiModelProperty(value = "修改人")
    private String updater;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "衍生表状态")
    private String state;

    @ApiModelProperty(value = "DDL语句")
    private String ddl;

    @ApiModelProperty(value = "DML语句")
    private String dml;

    @ApiModelProperty(value = "支持语句查询：true，false")
    private boolean queryDDL;

    @JsonIgnore
    private int total;

}
