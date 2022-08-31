package io.zeta.metaspace.model.share.apisix;

import lombok.Data;

/**
 * @author huangrongwen
 * @Description: apisix返回实体
 * @date 2022/8/2615:31
 */
@Data
public class ApiValue {
    private String[] uris;
    private String desc;
    private int priority;
    private String[] methods;
    private String upstream_id;
}
