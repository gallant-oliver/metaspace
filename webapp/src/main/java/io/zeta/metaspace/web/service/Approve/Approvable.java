package io.zeta.metaspace.web.service.Approve;


import io.zeta.metaspace.model.approve.ApproveItem;

import java.util.List;

/**
 * 各业务模块的可审批能力，用于回调业务对象详情，和审批后的对象状态变更
 */
public interface Approvable {

    /**
     *
     * @param objectId  对象ID
     * @param type 业务对象类型
     * @param version 查看版本
     * @param tenantId 租户id
     * @return
     */
    Object getObjectDetail(String objectId,String type,int version,String tenantId);


    /**
     *
     * @param approveResult
     * @param tenantId
     * @param items
     */
    void changeObjectStatus(String approveResult, String tenantId, List<ApproveItem> items) throws Exception;

}
