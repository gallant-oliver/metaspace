package io.zeta.metaspace.model.dto.api;

import io.zeta.metaspace.model.share.ApiInfoV2;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import java.util.List;

/**
 * @author huangrongwen
 * @Description: api测试接口VO
 * @date 2022/3/217:00
 */
@Data
public class ApiTestInfoVO {
    List<ApiInfoV2.FieldV2> param;
    @NotNull
    String apiId;
    @NotNull
    String version;
    @DefaultValue("1")
    Long pageNum;
    @DefaultValue("10")
    Long pageSize;
}
