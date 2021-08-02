package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName TechnicalCategory
 * @Descroption TODO
 * @Author Lvmengliang
 * @Date 2021/7/15 17:28
 * @Version 1.0
 */
@ApiModel(value = "TechnicalCategory", description = "技术目录实体")
@Data
public class TechnicalCategory extends BusinessCategory {

    @ApiModelProperty(value = "是否是数据库")
    private boolean dataBase = false;

    @ApiModelProperty(value = "数据库类型")
    private String dbType;

    @ApiModelProperty(value = "数据库Id")
    private String dbId;

    @ApiModelProperty(value = "数据源Id")
    private String sourceId;

}
