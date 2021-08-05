package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName DeriveTableVersion
 * @Descroption TODO
 * @Author Lvmengliang
 * @Date 2021/7/16 10:46
 * @Version 1.0
 */
@ApiModel(value = "DeriveTableVersion",description = "衍生表版本实体")
@Data
public class DeriveTableVersion {

    @ApiModelProperty(value = "版本号", example = "1")
    private Integer version;

    @ApiModelProperty(value = "更新人")
    private String updater;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @JsonIgnore
    private int total;

}
