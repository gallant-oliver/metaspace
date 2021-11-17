package io.zeta.metaspace.model.dataassets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/11/15 10:49
 */
@Data
public class BussinessObject {
    //业务对象编号
    private String businessId;
    //业务对象名称
    private String name;
    //所有者
    private String owner;
    //管理者
    private String manager;
    //维护者
    private String maintainer;
    //更新时间
    private String businesslastupdate;
    //更新人
    private String businessoperator;
    //有效状态
    private String status;//0-有效 1-无效
    //租户id
    private String tenantId;
    @JsonIgnore
    private int total; //业务对象数量

}
