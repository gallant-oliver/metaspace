package io.zeta.metaspace.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("告警租户VO")
public class AlertTenantDTO {

    @ApiModelProperty("租户名称")
    private String tenant;

    @ApiModelProperty("告警编号")
    private String id;

    @ApiModelProperty("告警内容")
    private String content;

    @ApiModelProperty("通知人")
    private String receivers;

    @ApiModelProperty("告警时间")
    private String createTime;

    @ApiModelProperty("告警名称")
    private String title;

    @ApiModelProperty("告警类型")
    private String alertType = "数据质量告警";

    @ApiModelProperty("告警来源")
    private String source = "数据质量";

    @ApiModelProperty("告警渠道")
    private String channel = "EMAIL";

    @ApiModelProperty("告警级别")
    private String alertLevel = "2级";

    @JsonIgnore
    private Integer total;
}
