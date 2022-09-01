package io.zeta.metaspace.model.share.apisix;

import lombok.Data;

/**
 * @author huangrongwen
 * @Description: apisix接口返回的实体
 * @date 2022/8/2613:42
 */
@Data
public class ApiSixResultVO {
    //api节点
    private ApiNode node;
    //操作（删除，新增...）
    private String action;
    //返回的报错信息
    private String error_msg;

    public ApiNode getNode() {
        return node;
    }
}

