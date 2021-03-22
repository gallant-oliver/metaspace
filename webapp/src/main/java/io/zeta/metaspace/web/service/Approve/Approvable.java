package io.zeta.metaspace.web.service.Approve;


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
     * @param objectId  对象ID
     * @param type 业务对象类型
     * @param version 查看版本
     * @param approveResult 审批结果，通过或驳回
     * @param tenantId 租户id
     * @return
     */
    void changeObjectStatus(String objectId,String type,int version,String approveResult,String tenantId,String approveType);

}
