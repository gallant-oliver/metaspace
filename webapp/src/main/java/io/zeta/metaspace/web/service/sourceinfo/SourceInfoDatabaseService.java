package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.bo.DatabaseInfoBO;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveOperate;
import io.zeta.metaspace.model.approve.ApproveParas;
import io.zeta.metaspace.model.approve.ApproveType;
import io.zeta.metaspace.model.dto.indices.ApprovalGroupMember;
import io.zeta.metaspace.model.dto.sourceinfo.DatabaseInfoDTO;
import io.zeta.metaspace.model.enums.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.ApproveItemForReset;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForCategory;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForList;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.web.dao.ApproveDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.service.Approve.Approvable;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SourceInfoDatabaseService implements Approvable {

    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.TechnicalREST");
    private static final int CATEGORY_TYPE = 0;
    private static final int DELETE_CATEGORY = 1;
    @Autowired
    DatabaseDAO databaseDAO;

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    DatabaseInfoDAO databaseInfoDAO;

    @Autowired
    SourceInfoParamCheckService checkService;

    @Autowired
    ApproveService approveServiceImp;

    @Autowired
    ApproveDAO approveDAO;

    @Autowired
    UserGroupDAO userGroupDAO;

    @Autowired
    DataManageService dataManageService;


    /**
     * 新增源信息数据库登记
     * @param tenantId 租户id
     * @param databaseInfo 数据库信息对象
     * @param approveGroupId 审核组id
     * @param submitType 提交类型
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addDatabaseInfo(String tenantId, DatabaseInfo databaseInfo,String approveGroupId,SubmitType submitType){
        // 发布时做检测；只保存不发布时不做检测
        Result checkResult = checkService.checkCreateParam(databaseInfo, tenantId, approveGroupId, submitType);
        if (Boolean.FALSE.equals((ReturnUtil.isSuccess(checkResult))) && SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)) {
            return checkResult;
        }
        //暂存时新增校验逻辑
        Result checkResult1 = checkService.saveCheckCreateParam(databaseInfo, tenantId, approveGroupId, submitType);
        if (Boolean.FALSE.equals((ReturnUtil.isSuccess(checkResult1))) && SubmitType.SUBMIT_ONLY.equals(submitType)) {

            return checkResult1;
        }
        DatabaseInfoPO dp = this.convertToPO(tenantId,databaseInfo);
        List<DatabaseInfoPO> dpList = new ArrayList<>();
        dpList.add(dp);
        this.registerDatabaseInfo(dpList);

        if (SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)){
            List<DatabaseInfo> databaseInfoList = new ArrayList<>();
            databaseInfoList.add(databaseInfo);
            List<String> ids = new ArrayList<>();
            ids.add(dp.getId());
            databaseInfoDAO.updateStatusByIds(ids,Status.AUDITING.getIntValue()+"");
            this.approveItems(tenantId,databaseInfoList,approveGroupId);
        }
        return checkResult;
    }

    /**
     * 批量新增新增源信息数据库登记
     * @param tenantId 租户id
     * @param databaseInfos 数据库信息对象
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addDatabaseInfoList(String tenantId, List<DatabaseInfo> databaseInfos){
        Result checkResult = checkService.checkCreateListParam(databaseInfos,tenantId);
        if (Boolean.FALSE.equals((ReturnUtil.isSuccess(checkResult)))){
            return checkResult;
        }
        List<DatabaseInfoPO> dps = this.convertToPOs(tenantId,databaseInfos);
        this.registerDatabaseInfo(dps);
        return ReturnUtil.success();
    }
    /**
     * 查询源信息数据库登记详情
     * @param id 信息id
     * @param tenantId 租户id
     * @param version 版本
     * @return result
     */
    public Result getDatabaseInfoById(String id,String tenantId,int version){
        return ReturnUtil.success(this.getDatabaseInfoBOById(id,tenantId,version));
    }


    /**
     * 发布源信息数据库登记
     * @param idList 发布的信息id的集合
     * @param approveGroupId 审核组id
     * @param tenantId 租户id
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result publish(List<String> idList,String approveGroupId,String tenantId){
        if (Boolean.TRUE.equals(ParamUtil.isNull(idList))){
            return ReturnUtil.success();
        }
        Result checkResult = checkService.checkSourceInfoStatus(idList,SourceInfoOperation.PUBLISH);
        if (Boolean.FALSE.equals(ReturnUtil.isSuccess(checkResult))){
            return checkResult;
        }
        if (Boolean.TRUE.equals(ParamUtil.isNull(approveGroupId))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("审核组"));
        }
        List<DatabaseInfo> databaseInfoList = databaseInfoDAO.getDatabaseInfosByIds(idList);

        // 发布时做检测
        Result checkParamResult = checkService.checkCreateListParam(databaseInfoList, tenantId);
        if (Boolean.FALSE.equals((ReturnUtil.isSuccess(checkParamResult)))) {
            return checkParamResult;
        }

        databaseInfoDAO.updateStatusByIds(idList,Status.AUDITING.getIntValue()+"");
        this.approveItems(tenantId,databaseInfoList,approveGroupId);

        return ReturnUtil.success();
    }

    private String convertStringFromList(List<String> strList){
        StringBuilder sb = new StringBuilder();
        strList.forEach(str->{
            sb.append(str);
            sb.append("，");
        });
        return sb.substring(0,sb.length()-1);
    }

    /**
     * 撤回发布
     * @param id 被撤回的信息id
     * @param tenantId 租户id
     * @return 处理结果
     * @throws Exception 处理异常
     */
    @Transactional(rollbackFor = Exception.class)
    public Result revoke(String id,String tenantId) throws Exception {
        List<String> idList = new ArrayList<>();
        idList.add(id);
        Result checkResult = checkService.checkSourceInfoStatus(idList, SourceInfoOperation.REVOKE);
        if (Boolean.FALSE.equals(ReturnUtil.isSuccess(checkResult))){
            return checkResult;
        }

        approveServiceImp.deal(this.buildApproveParas(id,tenantId,ApproveOperate.CANCEL),tenantId);
        databaseInfoDAO.updateStatusByIds(idList,Status.FOUNDED.getIntValue()+"");
        return ReturnUtil.success();
    }

    /**
     * 重名验证
     * @param tenantId 租户id
     * @param name 中文名
     * @param categoryId 挂载的目录id
     * @param id 源信息id
     * @return 处理结果
     */
    public Result validate(String tenantId,String name,String categoryId,String id) {
        if (databaseInfoDAO.getDatabaseDuplicateName(tenantId,name, id)) {
            return ReturnUtil.error(AtlasErrorCode.DUPLICATE_ALIAS_NAME.getErrorCode(),
                    AtlasErrorCode.DUPLICATE_ALIAS_NAME.getFormattedErrorMessage(name));
        }
        int count = categoryDAO.getCategoryCountByParentIdAndName(tenantId,categoryId,name);
        if (count>0){
            return ReturnUtil.error(AtlasErrorCode.DUPLICATE_ALIAS_NAME.getErrorCode(),
                    AtlasErrorCode.DUPLICATE_ALIAS_NAME.getFormattedErrorMessage(name));
        }
        return ReturnUtil.success();
    }
    /**
     * 源信息数据库登记信息列表
     * @param tenantId 租户id
     * @param status 信息状态
     * @param name 数据库中文名或者数据库名
     * @param offset 下标
     * @param limit 一页包含的信息条数
     * @return result
     */

    public Result getDatabaseInfoList(String tenantId, Status status, String name, int offset, int limit){
        List<DatabaseInfoForList> diLists = databaseInfoDAO.getDatabaseInfoList(tenantId,status==null?null:status.getIntValue()+"",null,name,offset,limit);
        int totalSize = databaseInfoDAO.getDatabaseInfoListCount(tenantId,status==null?null:status.getIntValue()+"",name);
        PageResult<DatabaseInfoForList> pageResult=new PageResult<>(diLists);
       if (Boolean.FALSE.equals(ParamUtil.isNull(diLists))){
           for (DatabaseInfoForList di:diLists){
               String statusValue = Status.getStatusByValue(di.getStatus());
               di.setCategoryName(Boolean.FALSE.equals(ParamUtil.isNull(di.getCategoryId()))?
                       this.getActiveInfoAllPath(di.getCategoryId(),tenantId):this.getAllPath(di.getId(),tenantId));
               di.setStatus(statusValue);
           }
       }
        pageResult.setCurrentSize(diLists.size());
        pageResult.setTotalSize(totalSize);
        return ReturnUtil.success(pageResult);
    }

    /**
     *
     * @param tenantId 租户id
     * @param idList 被删除的idList
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result delete(String tenantId, List<String> idList, int deleteType) {
        Result checkResult = checkService.checkSourceInfoStatus(idList,SourceInfoOperation.DELETE);
        if (Boolean.FALSE.equals(ReturnUtil.isSuccess(checkResult))){
            return checkResult;
        }
        List<DatabaseInfoForList> list=new ArrayList<>();
        if (Boolean.FALSE.equals(ParamUtil.isNull(idList))){
            list=databaseInfoDAO.getDatabaseInfoList(tenantId,null,idList,null,0,Integer.MAX_VALUE);
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATABASEREGISTER.getAlias(),this.convertStringFromList(databaseInfoDAO.getDatabaseIdAndAliasByIds(idList).stream().map(DatabaseInfo::getDatabaseAlias).collect(Collectors.toList())));

        list.forEach(l->{
            try {
                if (Status.ACTIVE.name().equals(Status.getStatusByValue(l.getStatus()))|| Status.REJECT.name().equals(Status.getStatusByValue(l.getStatus()))){
                    approveServiceImp.deal(this.buildApproveParas(l.getId(),tenantId,ApproveOperate.CANCEL),tenantId);
                }
                if (DELETE_CATEGORY!=deleteType&&Boolean.FALSE.equals(ParamUtil.isNull(l.getCategoryId()))) {
                    dataManageService.deleteCategory(l.getCategoryId(), tenantId, CATEGORY_TYPE);
                }
            } catch (Exception e) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录已关联源信息登记，无法删除");
            }
        });
        databaseInfoDAO.deleteSourceInfoAndParentCategoryRelation(idList);
        databaseInfoDAO.deleteSourceInfoForVersion(idList,0);
        return ReturnUtil.success();
    }

    /**
     *  更新数据登记信息
     * @param databaseInfo 更新对象
     * @param tenantId 租户id
     * @param approveGroupId 审核组
     * @param submitType 提交类型
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result updateSourceInfo(DatabaseInfo databaseInfo,String tenantId,String approveGroupId,SubmitType submitType){
        Result checkResult = checkService.checkUpdateParam(databaseInfo, tenantId, approveGroupId, submitType);
        if (Boolean.FALSE.equals((ReturnUtil.isSuccess(checkResult))) && SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)) {
            return checkResult;
        }

        databaseInfoDAO.updateSourceInfo(databaseInfo,AdminUtils.getUserData().getUserId());
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfoDAO.getParentCategoryIdById(databaseInfo.getId())))) {
            databaseInfoDAO.insertParentRelation(databaseInfo);
        }
        List<String> ids = new ArrayList<>();
        ids.add(databaseInfo.getId());
        List<DatabaseInfo> databaseInfoList = new ArrayList<>();
        databaseInfoList.add(databaseInfo);
        if (SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)){
            this.approveItems(tenantId,databaseInfoList,approveGroupId);
            databaseInfoDAO.updateStatusByIds(ids,Status.AUDITING.getIntValue()+"");
        }else{
            databaseInfoDAO.updateStatusByIds(ids,Status.FOUNDED.getIntValue()+"");
        }
        return checkResult;
    }

    /**
     *  构建送审用的入参对象
     * @param id 源信息对象id
     * @param tenantId 租户id
     * @param operate 具体操作
     * @return 送审入参对象
     */
    private ApproveParas buildApproveParas(String id,String tenantId,ApproveOperate operate){
        ApproveParas approveParas = new ApproveParas();
        approveParas.setResult(operate.getCode());
        DatabaseInfoBO databaseInfoBO = databaseInfoDAO.getDatabaseInfoById(id,tenantId,0);
        List<ApproveItem> approveItemList = new ArrayList<>();
        approveItemList.add(this.buildApproveItem(databaseInfoBO,databaseInfoBO.getApproveGroupId(),tenantId));
        approveParas.setApproveList(approveItemList);
        return approveParas;
    }

     /**
     * 执行源信息保存
     * @param databaseInfos 源信息对象
     */
    private void registerDatabaseInfo(List<DatabaseInfoPO> databaseInfos){
        List<DatabaseInfoForCategory> parentCategoryIds = new ArrayList<>();
        databaseInfos.forEach(di->{
            DatabaseInfoForCategory dif = new DatabaseInfoForCategory();
            String parentCategoryId = di.getCategoryId();
            dif.setId(di.getId());
            dif.setParentCategoryId(parentCategoryId);
            di.setCategoryId(null);
            parentCategoryIds.add(dif);
        });
        databaseInfoDAO.insertDatabaseInfo(databaseInfos);
        databaseInfoDAO.insertDatabaseInfoRelationParentCategory(parentCategoryIds);
    }

    /**
     * 将审批对象送审
     * @param tenantId 租户id
     * @param databaseInfos 要被送审的源信息对象
     * @param approveGroupId 审核组id
     */
    private void approveItems(String tenantId, List<DatabaseInfo> databaseInfos,String approveGroupId){
        for (DatabaseInfo databaseInfo:databaseInfos) {
            ApproveItem approveItem = this.buildApproveItem(databaseInfo,approveGroupId,tenantId);
            databaseInfoDAO.updateApproveIdAndApproveGroupIdById(databaseInfo.getId(),approveItem.getId(),approveGroupId);
            databaseInfoDAO.insertHistoryVersion(approveItem.getObjectId());
            approveServiceImp.addApproveItem(approveItem);
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATABASEREGISTER.getAlias(),this.convertStringFromList(databaseInfos.stream().map(DatabaseInfo::getDatabaseAlias).collect(Collectors.toList())));

    }

    /**
     * 构建审批对象
     * @param databaseInfo 源信息对象
     * @param approveGroupId 审核组id
     * @param tenantId 租户id
     * @return 审批对象
     */
    private ApproveItem buildApproveItem(Object databaseInfo,String approveGroupId,String tenantId){

        ApproveItem approveItem = new ApproveItem();

        String approveId = UUID.randomUUID().toString();
        approveItem.setId(approveId);
        if (databaseInfo instanceof DatabaseInfo){
            approveItem.setObjectId(((DatabaseInfo) databaseInfo).getId());
            approveItem.setObjectName(((DatabaseInfo) databaseInfo).getDatabaseAlias());
        }
        if (databaseInfo instanceof DatabaseInfoBO){
            approveItem.setObjectId(((DatabaseInfoBO) databaseInfo).getId());
            approveItem.setObjectName(((DatabaseInfoBO) databaseInfo).getDatabaseAlias());
        }
        if (databaseInfo instanceof DatabaseInfoDTO){
            approveItem.setObjectId(((DatabaseInfoDTO) databaseInfo).getId());
            approveItem.setObjectName(((DatabaseInfoDTO) databaseInfo).getDatabaseAlias());
        }
        if (databaseInfo instanceof DatabaseInfoForList){
            approveItem.setObjectId(((DatabaseInfoForList) databaseInfo).getId());
            approveItem.setObjectName(((DatabaseInfoForList) databaseInfo).getDatabaseAlias());
        }
        int maxVersion = databaseInfoDAO.getMaxVersionById(approveItem.getObjectId());
        approveItem.setApproveType(ApproveType.PUBLISH.getCode());
        approveItem.setApproveGroup(approveGroupId);
        approveItem.setBusinessType(BusinessType.DATABASE_INFO_REGISTER.getTypeCode());
        approveItem.setBusinessTypeText(BusinessType.DATABASE_INFO_REGISTER.getTypeText());
        approveItem.setSubmitter(AdminUtils.getUserData().getUserId());
        approveItem.setCommitTime(Timestamp.valueOf(LocalDateTime.now()));
        approveItem.setModuleId(ModuleEnum.DATABASEREGISTER.getId() + "");
        approveItem.setVersion(maxVersion+1);
        approveItem.setTenantId(tenantId);

        return approveItem;
    }

    /**
     * 构建PO对象
     * @param tenantId 租户id
     * @param databaseInfo 源信息对象
     * @return 源信息PO
     */
    private DatabaseInfoPO convertToPO(String tenantId, DatabaseInfo databaseInfo){
        DatabaseInfoPO databaseInfoPO = new DatabaseInfoPO();

        BeansUtil.copyPropertiesIgnoreNull(databaseInfo,databaseInfoPO);
        String uuid = UUID.randomUUID().toString();
        databaseInfo.setId(uuid);
        databaseInfoPO.setId(uuid);
        databaseInfoPO.setStatus(Status.FOUNDED.getIntValue()+"");
        databaseInfoPO.setCreator(AdminUtils.getUserData().getUserId());
        databaseInfoPO.setUpdater(AdminUtils.getUserData().getUserId());
        databaseInfoPO.setTenantId(tenantId);

        return databaseInfoPO;
    }

    public List<DatabaseInfo> getAliasByIdList(List<String> ids){
        return databaseInfoDAO.getDatabaseIdAndAliasByIds(ids);
    }
    /**
     * 批量构建PO对象
     * @param tenantId 租户id
     * @param databaseInfos 源信息对象
     * @return 源信息PO
     */
    private List<DatabaseInfoPO> convertToPOs(String tenantId, List<DatabaseInfo> databaseInfos){
        List<DatabaseInfoPO> databaseInfoPOs = new ArrayList<>();
        databaseInfos.forEach(databaseInfo->{
            DatabaseInfoPO databaseInfoPO = new DatabaseInfoPO();

            BeansUtil.copyPropertiesIgnoreNull(databaseInfo,databaseInfoPO);
            String uuid = UUID.randomUUID().toString();
            databaseInfo.setId(uuid);
            databaseInfoPO.setId(uuid);
            databaseInfoPO.setStatus(Status.FOUNDED.getIntValue()+"");
            databaseInfoPO.setCreator(AdminUtils.getUserData().getUserId());
            databaseInfoPO.setUpdater(AdminUtils.getUserData().getUserId());
            databaseInfoPO.setTenantId(tenantId);
            databaseInfoPOs.add(databaseInfoPO);
                }
        );
        return databaseInfoPOs;
    }
    /**
     * 获取被审批的对象详情接口实现
     * @param objectId  对象ID
     * @param type 业务对象类型
     * @param version 查看版本
     * @param tenantId 租户id
     * @return 被审批对象
     */
    @Override
    public Object getObjectDetail(String objectId, String type, int version, String tenantId) {
        DatabaseInfoBO databaseInfoBO = this.getDatabaseInfoBOById(objectId,tenantId,version);
        DatabaseInfoDTO databaseInfoDTO = new DatabaseInfoDTO();
        BeansUtil.copyPropertiesIgnoreNull(databaseInfoBO,databaseInfoDTO);
        databaseInfoDTO.setCategoryName(this.getActiveInfoAllPath(databaseInfoBO.getCategoryId(),tenantId));
        databaseInfoDTO.setPublisherName(databaseInfoBO.getUpdaterName());
        databaseInfoDTO.setPublishTime(databaseInfoBO.getUpdateTime());
        List<User> users = approveDAO.getApproveUsers(databaseInfoBO.getApproveGroupId());
        if (!CollectionUtils.isEmpty(users)) {
            List<ApprovalGroupMember> approvalGroupMembers = users.stream().map(x -> BeanMapper.map(x, ApprovalGroupMember.class)).collect(Collectors.toList());
            databaseInfoDTO.setApproveGroupMembers(approvalGroupMembers);
        } else {
            databaseInfoDTO.setApproveGroupMembers(new ArrayList<>());
        }
        return databaseInfoDTO;
    }

    /**
     * 根据id查看详情
     * @param id 源信息id
     * @param tenantId 租户id
     * @param version 版本
     * @return 源信息对象
     */

    private DatabaseInfoBO getDatabaseInfoBOById(String id,String tenantId,int version){
        DatabaseInfoBO databaseInfoBO=databaseInfoDAO.getDatabaseInfoById(id,tenantId,version);

        if (Boolean.FALSE.equals(ParamUtil.isNull(databaseInfoBO))&&Boolean.TRUE.equals(ParamUtil.isNull(databaseInfoBO.getCategoryId()))){
            databaseInfoBO.setCategoryId(databaseInfoDAO.getParentCategoryIdById(databaseInfoBO.getId()));
        }
        if ("hive".equals(databaseInfoBO.getDataSourceId())){
            databaseInfoBO.setDataSourceName("hive");
        }
        databaseInfoBO.setCategoryName(databaseInfoBO.getStatus().equals(Status.ACTIVE.getIntValue()+"")?
                this.getActiveInfoAllPath(databaseInfoBO.getCategoryId(),tenantId):this.getAllPath(id,tenantId));
        return databaseInfoBO;
    }

    /**
     *  修改被审批对象的审批状态的接口实现
     * @param approveResult 审核结果
     * @param tenantId 租户id
     * @param items 被审核对象
     * @throws Exception
     */

    @Override
    public void changeObjectStatus(String approveResult, String tenantId, List<ApproveItem> items) throws Exception {
        List<ApproveItemForReset> itemList = new ArrayList<>();
        items.forEach(item->{
            ApproveItemForReset aif = new ApproveItemForReset();

            aif.setObjectId(item.getObjectId());
            aif.setVersion(item.getVersion());

            itemList.add(aif);
        });
                List<String> idList = items.stream().map(ApproveItem::getObjectId).collect(Collectors.toList());
        if (ApproveOperate.APPROVE.getCode().equals(approveResult)) {
            databaseInfoDAO.updateStatusByIds(idList,Status.ACTIVE.getIntValue()+"");
            List<DatabaseInfoForCategory> databaseInfoList = databaseInfoDAO.getDatabaseInfoByIds(idList);
            for (DatabaseInfoForCategory databaseInfo:databaseInfoList){
                List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(databaseInfo.getCreator(), tenantId).stream().map(UserGroup::getId).collect(Collectors.toList());
                if (Boolean.FALSE.equals(ParamUtil.isNull(databaseInfo.getCategoryId()))){
                    categoryDAO.updateCategoryName(databaseInfo.getName(),databaseInfo.getCategoryId(),databaseInfo.isImportance()? CategoryPrivateStatus.PRIVATE.name():CategoryPrivateStatus.PUBLIC.name());
                    userGroupDAO.insertGroupRelations(userGroupIds,databaseInfo.getCategoryId(), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
                }else{
                    this.createCategoryInfo(databaseInfo,tenantId);
                }
            }
            databaseInfoDAO.deleteSourceInfoAndParentCategoryRelation(idList);
        } else if (ApproveOperate.REJECTED.getCode().equals(approveResult)) {
            if (items.stream().anyMatch(item->Boolean.FALSE.equals(ParamUtil.isNull(item.getApprover())))){
                databaseInfoDAO.updateStatusByIds(idList,Status.REJECT.getIntValue()+"");
            }else {
                databaseInfoDAO.updateStatusByIds(idList,Status.FOUNDED.getIntValue()+"");
            }
        } else if (ApproveOperate.CANCEL.getCode().equals(approveResult)) {
            databaseInfoDAO.updateStatusByIds(idList,Status.FOUNDED.getIntValue()+"");
            itemList.forEach(itemForReset->databaseInfoDAO.removeHistoryVersion(itemForReset.getObjectId(),itemForReset.getVersion()));
        }
    }

    /**
     * 调用目录service创建目录
     * @param databaseInfoForCategory 创建目录需要的信息
     * @param tenantId 租户id
     * @throws Exception
     */

    private void createCategoryInfo(DatabaseInfoForCategory databaseInfoForCategory,String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.createMetadataCategory()");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.DATABASEREGISTER.getAlias(), databaseInfoForCategory.getName());
            CategoryPrivilege categoryPrivilege= dataManageService.createCategory(this.buildCategoryInfo(databaseInfoForCategory), CATEGORY_TYPE, tenantId);
            databaseInfoDAO.updateRealCategoryRelation(databaseInfoForCategory.getId(),categoryPrivilege.getGuid(),0);
            databaseDAO.insertDbCategoryRelation(tenantId,UUID.randomUUID().toString(),databaseInfoForCategory.getDatabaseId(),categoryPrivilege.getGuid());
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    public String getActiveInfoAllPath(String categoryId,String tenantId){
        String parentCategoryId = categoryId;
        StringBuilder sb = new StringBuilder("/");
        while (Boolean.FALSE.equals(ParamUtil.isNull(parentCategoryId))){
            String name=categoryDAO.getCategoryNameById(parentCategoryId,tenantId);
            StringBuilder sbInner = new StringBuilder("/");
            sbInner.append(name);
            sbInner.append(sb);
            sb = sbInner;
            parentCategoryId = categoryDAO.getParentIdByGuid(parentCategoryId,tenantId);
        }
        return sb.toString();
    }
    public String getAllPath(String sourceInfoId,String tenantId){
        String parentCategoryId = databaseInfoDAO.getParentCategoryIdById(sourceInfoId);
        StringBuilder sb = new StringBuilder("/");
        while (Boolean.FALSE.equals(ParamUtil.isNull(parentCategoryId))){
            String name=categoryDAO.getCategoryNameById(parentCategoryId,tenantId);
            if (Boolean.FALSE.equals(ParamUtil.isNull(name))){
                StringBuilder sbInner = new StringBuilder("/");
                sbInner.append(name);
                sbInner.append(sb);
                sb = sbInner;
            }
            parentCategoryId = categoryDAO.getParentIdByGuid(parentCategoryId,tenantId);
        }
        return sb.toString();
    }

    /**
     * 构建目录对象
     * @param databaseInfoBO 源信息对象
     * @return 目录对象
     */
    private CategoryInfoV2 buildCategoryInfo(DatabaseInfoForCategory databaseInfoBO){
        CategoryInfoV2 categoryInfoV2 = new CategoryInfoV2();

        categoryInfoV2.setAuthorized(Boolean.FALSE);
        categoryInfoV2.setCreator(databaseInfoBO.getCreator());
        categoryInfoV2.setPrivateStatus(databaseInfoBO.isImportance()?CategoryPrivateStatus.PRIVATE:CategoryPrivateStatus.PUBLIC);
        categoryInfoV2.setName(databaseInfoBO.getName());
        categoryInfoV2.setGuid(databaseInfoBO.getParentCategoryId());
        categoryInfoV2.setParentCategoryGuid(databaseInfoBO.getParentCategoryId());

        return categoryInfoV2;
    }


}
