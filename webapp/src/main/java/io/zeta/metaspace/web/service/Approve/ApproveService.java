package io.zeta.metaspace.web.service.Approve;

import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveParas;
import io.zeta.metaspace.model.result.PageResult;

public interface ApproveService {
    /**
     * 审核项搜索列表，支撑多模块
     * @param paras
     * @param tenant_id
     * @return
     */
    PageResult<ApproveItem> search(ApproveParas paras,String tenant_id);


    /**
     * 审批动作，包含审批通过与驳回
     * @param paras
     * @param tenant_id
     * @return
     */
    void deal(ApproveParas paras,String tenant_id) throws IllegalAccessException, ClassNotFoundException, InstantiationException;

    void addApproveItem(ApproveItem Item);

    Object ApproveObjectDetail(String tenantId, String objectId, String objectType,  int version, String moduleId) throws Exception;

    }
