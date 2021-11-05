package io.zeta.metaspace.model.dto;

import io.zeta.metaspace.model.BaseListParamVO;
import lombok.Data;

@Data
public class AlertRequest extends BaseListParamVO {
    private String startTime;
    private String endTime;
    // 目前仅支持数据质量告警
    private String alertType;
    private String keyword;
    // 目前仅支持2级
    private String alertLevel;
}
