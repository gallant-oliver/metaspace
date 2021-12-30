package io.zeta.metaspace.web.service.Approve;


import com.google.common.collect.ImmutableMap;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveOperate;
import io.zeta.metaspace.model.approve.ApproveParas;
import io.zeta.metaspace.model.approve.ApproveStatus;
import io.zeta.metaspace.model.enums.BusinessType;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.ApproveDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.FilterUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ApproveServiceImp implements ApproveService{

    private static final Logger LOG = LoggerFactory.getLogger(ApproveServiceImp.class);


    private static  Map<ModuleEnum, String> moduleServiceClass = ImmutableMap.<ModuleEnum, String>builder()  //key:模块单例，value:service name
            .put(ModuleEnum.NORMDESIGN, "indexService")
            .put(ModuleEnum.DATABASEREGISTER, "sourceInfoDatabaseService")
            .put(ModuleEnum.BUSINESSCATALOGUE, "businessCatalogueService")
            .put(ModuleEnum.INDEX, "businessCatalogueService")
            .put(ModuleEnum.BUSINESS, "businessService")
            .build();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ApproveDAO approveDao;


    @Override
    public PageResult<ApproveItem> search(ApproveParas paras, String tenantId) {
        String userId = AdminUtils.getUserData().getUserId();
        List<String> groups = null;
        PageResult<ApproveItem> result = new PageResult<>();
        if("1".equals(paras.getReferer())){  //我的申请
            paras.setUserId(userId);
        }else if("2".equals(paras.getReferer())){   //待审批
            groups = approveDao.selectApproveGroupByUserId(userId, tenantId);
            //用户不属于任何审批组，无匹配审批项目
            if(CollectionUtils.isEmpty(groups)){
                return result;
            }
            List<String> status = new LinkedList<>();
            status.add(ApproveStatus.WAITING.getCode());
            paras.setApproveStatus(status);
        }else{  //已审批
            groups = approveDao.selectApproveGroupByUserId(userId, tenantId);
            //用户不属于任何审批组，无匹配审批项目
            if(CollectionUtils.isEmpty(groups)){
                return result;
            }
            List<String> status = new LinkedList<>();
            status.add(ApproveStatus.FINISH.getCode()); //审批通过
            status.add(ApproveStatus.REJECTED.getCode()); //驳回
            paras.setApproveStatus(status);
        }
        if(StringUtils.isBlank(paras.getOrder()) || !Arrays.asList("ASC","DESC").contains(paras.getOrder().toUpperCase())){
            paras.setOrder("ASC");
        }
        String sortByField = FilterUtils.filterSqlKeys(paras.getSortBy());
        if(sortByField.split(" ").length > 1){
            sortByField = sortByField.split(" ")[0];
            paras.setSortBy(sortByField);
        }
        paras.setStartTimeParam(paras.getStartTime());
        paras.setEndTimeParam(paras.getEndTime());
        List<ApproveItem> approveItems = approveDao.getApproveItems(tenantId, paras, groups);
       for(ApproveItem item:approveItems){
           item.setBusinessTypeText(BusinessType.getTextByCode(item.getBusinessType()));
           item.setSourceSystem(BusinessType.getSystem(item.getBusinessType()));
       }
        if (CollectionUtils.isEmpty(approveItems)) {
            return result;
        }
        result.setLists(approveItems);
        result.setCurrentSize(approveItems.size());
        result.setTotalSize(approveItems.get(0).getTotalSize());
        return result;
    }

    @Override
    @Transactional
    public void deal(ApproveParas paras, String tenant_id) throws Exception {
        ApproveOperate result = null;
        Map<String,List<ApproveItem>> moduleItemMap = new HashMap<>(); //key:moduleId value: list<approveItem>
        if (CollectionUtils.isEmpty(paras.getApproveList())) {
            throw new AtlasBaseException("审批列表不能为空");
        }
        if(ApproveOperate.APPROVE.equals(ApproveOperate.getOprateByCode(paras.getResult()))){  //审批通过
            result = ApproveOperate.APPROVE;
            List<ApproveItem> approveList = paras.getApproveList(); //批量审批列表
            for(ApproveItem item : approveList) {
                item.setId(item.getId());
                item.setTenantId(tenant_id);
                item.setApproveStatus(ApproveStatus.FINISH.code);  //更新为已通过状态
                item.setApprover(AdminUtils.getUserData().getUserId()); //写入审批人
                approveDao.updateStatus(item);
                addToMapByClass(moduleItemMap,item);
            }
        }else if(ApproveOperate.REJECTED.equals(ApproveOperate.getOprateByCode(paras.getResult())) || ApproveOperate.CANCEL.equals(ApproveOperate.getOprateByCode(paras.getResult()))){ //驳回或者取回
            List<ApproveItem> approveList = paras.getApproveList(); //批量审批列表
            for(ApproveItem item : approveList) {
                item.setId(item.getId());
                item.setTenantId(tenant_id);
                item.setReason(paras.getDesc());  //驳回需要原因
                item.setApproveStatus(ApproveStatus.REJECTED.code); //更新为驳回状态
                item.setApprover(AdminUtils.getUserData().getUserId()); //写入审批人
                if(ApproveOperate.REJECTED.equals(ApproveOperate.getOprateByCode(paras.getResult()))){
                    result = ApproveOperate.REJECTED;
                    approveDao.updateStatus(item);  //todo 批量优化
                }else{
                    result = ApproveOperate.CANCEL;
                    approveDao.deleteItemById(item); //取回与驳回对业务模块操作一致
                }
                addToMapByClass(moduleItemMap,item);
            }
        }
        //获取审批模块所对应的服务层对象名称
        for(Map.Entry<String,List<ApproveItem>> en: moduleItemMap.entrySet()){  //对不同模块审批的回调
            String serviceName = moduleServiceClass.get(ModuleEnum.getModuleById(Integer.parseInt(en.getKey())));
            try {
                //从容器中获取实现审批业务接口的服务对象
                Approvable obj = (Approvable)applicationContext.getBean(serviceName);
                //调用接口方法，继续状态变更业务
                obj.changeObjectStatus(result.code,tenant_id,en.getValue());
            } catch (Exception e) {
                LOG.error("审批失败", e);
                throw e;
            }
        }

    }


    public void addToMapByClass(Map<String,List<ApproveItem>> map,ApproveItem item){
        if(map.containsKey(item.getModuleId())){
            map.get(item.getModuleId()).add(item);
        }else{
            List<ApproveItem> dataList = new ArrayList<>();
            dataList.add(item);
            map.put(item.getModuleId(),dataList);
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
            if(objectDetail == null){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"获取详情失败,对应审批对象已删除");
            }
        } catch (Exception e) {
            LOG.error("查询审批对象详情失败", e);
            throw e;
        }
        return objectDetail;
    }

}
