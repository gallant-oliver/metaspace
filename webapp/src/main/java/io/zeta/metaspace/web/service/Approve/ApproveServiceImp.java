package io.zeta.metaspace.web.service.Approve;


import com.google.common.collect.ImmutableMap;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveOperate;
import io.zeta.metaspace.model.approve.ApproveParas;
import io.zeta.metaspace.model.approve.ApproveStatus;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.web.dao.ApproveDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ApproveServiceImp implements ApproveService{

    private static final Logger LOG = LoggerFactory.getLogger(ApproveServiceImp.class);


    private static  Map<ModuleEnum, String> moduleServiceClass = ImmutableMap.<ModuleEnum, String>builder()  //key:模块单例，value:service name
            .put(ModuleEnum.NORMDESIGN, "indexService")
            .build();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ApproveDAO approveDao;


    @Override
    public List<ApproveItem> search(ApproveParas paras, String tenantId) {
        String userId = AdminUtils.getUserData().getUserId();
        List<String> groups = null;
        if("1".equals(paras.getReferer())){  //我的申请
            paras.setUserId(userId);
        }else if("2".equals(paras.getReferer())){   //待审批
            groups = approveDao.selectApproveGroupoByUserId(userId, tenantId);
            List<String> status = new LinkedList<>();
            status.add(ApproveStatus.WAITING.getCode());
            paras.setApproveStatus(status);
        }else{  //已审批
            groups = approveDao.selectApproveGroupoByUserId(userId, tenantId);
            List<String> status = new LinkedList<>();
            status.add(ApproveStatus.FINISH.getCode());
            paras.setApproveStatus(status);
        }
        return approveDao.getApproveItems(tenantId,paras,groups);
    }

    @Override
    public void deal(ApproveParas paras, String tenant_id) {
        ApproveOperate result = null;
        if(ApproveOperate.APPROVE.equals(ApproveOperate.getOprateByCode(paras.getResult()))){  //审批通过
            result = ApproveOperate.APPROVE;
            ApproveItem item = new ApproveItem();
            item.setId(paras.getId());
            item.setTenantId(tenant_id);
            item.setStatus(ApproveStatus.FINISH.code);  //更新为已通过状态
            item.setApprover(AdminUtils.getUserData().getUserId()); //写入审批人
            approveDao.updateStatus(item);
        }else if(ApproveOperate.REJECTED.equals(ApproveOperate.getOprateByCode(paras.getResult()))){ //驳回
            result = ApproveOperate.REJECTED;
            ApproveItem item = new ApproveItem();
            item.setId(paras.getId());
            item.setTenantId(tenant_id);
            item.setReason(paras.getDesc());  //驳回需要原因
            item.setStatus(ApproveStatus.REJECTED.code);  //更新为驳回状态
            item.setApprover(AdminUtils.getUserData().getUserId()); //写入审批人
            approveDao.updateStatus(item);
        }
        //获取审批模块所对应的服务层对象名称
        String serviceName = moduleServiceClass.get(ModuleEnum.getModuleById(Integer.parseInt(paras.getModuleId())));
        try {
            ApproveItem approveItemById = approveDao.getApproveItemById(paras.getId(), tenant_id);
            //从容器中获取实现审批业务接口的服务对象
            Approvable obj = (Approvable)applicationContext.getBean(serviceName);
            //调用接口方法，继续状态变更业务
            obj.changeObjectStatus(approveItemById.getId(),paras.getBusinessType(),paras.getVersion(),result.code,tenant_id,paras.getApproveType());
        } catch (Exception e) {
            LOG.error("审批失败", e);
            throw e;
        }
    }

    /**
     * 添加审批条目
     * @param item
     */
    @Override
    public void addApproveItem(ApproveItem item) {
        approveDao.addApproveItem(item);
    }


    /**
     * 审批业务对象详情
     * @param
     */
    @Override
    public Object ApproveObjectDetail(String tenantId,String objectId,String objectType,int version,String moduleId) {
        String serviceName = moduleServiceClass.get(ModuleEnum.getModuleById(Integer.parseInt(moduleId)));
        Object objectDetail;
        try {
            Approvable obj = (Approvable)applicationContext.getBean(serviceName);
            objectDetail = obj.getObjectDetail(objectId, objectType, version,tenantId);
        } catch (Exception e) {
            LOG.error("查询审批对象详情失败", e);
            throw e;
        }
        return objectDetail;
    }

}
