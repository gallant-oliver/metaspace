package io.zeta.metaspace.model.share.apisix;

import lombok.Data;

/**
 * @author huangrongwen
 * @Description: apisix返回实体
 * @date 2022/8/2615:32
 */
@Data
public class ApiNode {
    private ApiValue value;
    private String modifiedIndex;
    private String key;
    private String createdIndex;
}
