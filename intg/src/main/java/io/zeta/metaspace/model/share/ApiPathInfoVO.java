package io.zeta.metaspace.model.share;

import lombok.Data;

/**
 * @author huangrongwen
 * @Description: api详情返回，带完整路径
 * @date 2022/9/111:28
 */
@Data
public class ApiPathInfoVO extends ApiInfoV2{
    /**
     * 访问路径
     */
    private String accessPath;
}
