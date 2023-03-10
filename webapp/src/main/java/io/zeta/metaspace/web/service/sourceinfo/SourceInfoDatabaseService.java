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
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.enums.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.*;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.service.Approve.Approvable;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.service.MessageCenterService;
import io.zeta.metaspace.web.service.fileinfo.FileInfoService;
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
import org.apache.commons.collections4.CollectionUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import io.zeta.metaspace.web.service.DataManageService;

import static io.zeta.metaspace.model.enums.MessagePush.RESOURCE_AUDIT_INFO_DATABASE;

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

    @Autowired
    ApproveGroupDAO approveGroupDAO;

    @Autowired
    MessageCenterService messageCenterService;

    @Autowired
    UserDAO userDAO;

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * ??????????????????????????????
     * @param tenantId ??????id
     * @param databaseInfo ?????????????????????
     * @param approveGroupId ?????????id
     * @param submitType ????????????
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addDatabaseInfo(String tenantId, DatabaseInfo databaseInfo,String approveGroupId,SubmitType submitType){
        // ??????????????????????????????????????????????????????
        Result checkResult = checkService.checkCreateParam(databaseInfo, tenantId, approveGroupId, submitType);
        if (Boolean.FALSE.equals((ReturnUtil.isSuccess(checkResult))) && SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)) {
            return checkResult;
        }
        //???????????????????????????
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
        fileInfoService.createFileuploadRecord(databaseInfo.getAnnexId(),FileInfoPath.DATABASE_REGISTRATION_FILE);
        return checkResult;
    }

    /**
     * ??????????????????????????????????????????
     * @param tenantId ??????id
     * @param databaseInfos ?????????????????????
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
     * ????????????????????????????????????
     * @param id ??????id
     * @param tenantId ??????id
     * @param version ??????
     * @return result
     */
    public Result getDatabaseInfoById(String id,String tenantId,int version){
        return ReturnUtil.success(this.getDatabaseInfoBOById(id,tenantId,version));
    }


    /**
     * ??????????????????????????????
     * @param idList ???????????????id?????????
     * @param approveGroupId ?????????id
     * @param tenantId ??????id
     * @return ????????????
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
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("?????????"));
        }
        List<DatabaseInfo> databaseInfoList = databaseInfoDAO.getDatabaseInfosByIds(idList);

        // ??????????????????
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
            sb.append("???");
        });
        return sb.substring(0,sb.length()-1);
    }

    /**
     * ????????????
     * @param id ??????????????????id
     * @param tenantId ??????id
     * @return ????????????
     * @throws Exception ????????????
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
     * ????????????
     * @param tenantId ??????id
     * @param name ?????????
     * @param categoryId ???????????????id
     * @param id ?????????id
     * @return ????????????
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
     * ????????????????????????????????????
     * @param tenantId ??????id
     * @param status ????????????
     * @param name ????????????????????????????????????
     * @param offset ??????
     * @param limit ???????????????????????????
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
     * @param tenantId ??????id
     * @param idList ????????????idList
     * @return ????????????
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
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????????????????????????????");
            }
        });
        databaseInfoDAO.deleteSourceInfoAndParentCategoryRelation(idList);
        databaseInfoDAO.deleteSourceInfoForVersion(idList,0);
        return ReturnUtil.success();
    }

    /**
     *  ????????????????????????
     * @param databaseInfo ????????????
     * @param tenantId ??????id
     * @param approveGroupId ?????????
     * @param submitType ????????????
     * @return ????????????
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
        fileInfoService.createFileuploadRecord(databaseInfo.getAnnexId(),FileInfoPath.DATABASE_REGISTRATION_FILE);
        return checkResult;
    }

    /**
     *  ??????????????????????????????
     * @param id ???????????????id
     * @param tenantId ??????id
     * @param operate ????????????
     * @return ??????????????????
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
     * ?????????????????????
     * @param databaseInfos ???????????????
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
     * ?????????????????????
     * @param tenantId ??????id
     * @param databaseInfos ??????????????????????????????
     * @param approveGroupId ?????????id
     */
    private void approveItems(String tenantId, List<DatabaseInfo> databaseInfos,String approveGroupId){
        for (DatabaseInfo databaseInfo:databaseInfos) {
            ApproveItem approveItem = this.buildApproveItem(databaseInfo,approveGroupId,tenantId);
            databaseInfoDAO.updateApproveIdAndApproveGroupIdById(databaseInfo.getId(),approveItem.getId(),approveGroupId);
            databaseInfoDAO.insertHistoryVersion(approveItem.getObjectId());
            approveServiceImp.addApproveItem(approveItem);

            // ???????????????????????????
            List<String> userIdList = approveGroupDAO.getUserIdByApproveGroup(approveGroupId);
            List<String> userEmailList = (CollectionUtils.isNotEmpty(userIdList) ? userDAO.getUsersEmailByIds(userIdList) : null);
            MessageEntity message = new MessageEntity(RESOURCE_AUDIT_INFO_DATABASE.type, MessagePush.getFormattedMessageName(RESOURCE_AUDIT_INFO_DATABASE.name, databaseInfo.getDatabaseAlias()), RESOURCE_AUDIT_INFO_DATABASE.module, ProcessEnum.PROCESS_APPROVED_NOT_APPROVED.code);
            if (CollectionUtils.isNotEmpty(userEmailList)){
                for (String userEmail : userEmailList){
                    message.setCreateUser(userEmail);
                    messageCenterService.addMessage(message, tenantId);
                }
            }
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATABASEREGISTER.getAlias(),this.convertStringFromList(databaseInfos.stream().map(DatabaseInfo::getDatabaseAlias).collect(Collectors.toList())));

    }

    /**
     * ??????????????????
     * @param databaseInfo ???????????????
     * @param approveGroupId ?????????id
     * @param tenantId ??????id
     * @return ????????????
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
     * ??????PO??????
     * @param tenantId ??????id
     * @param databaseInfo ???????????????
     * @return ?????????PO
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
     * ????????????PO??????
     * @param tenantId ??????id
     * @param databaseInfos ???????????????
     * @return ?????????PO
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
     * ??????????????????????????????????????????
     * @param objectId  ??????ID
     * @param type ??????????????????
     * @param version ????????????
     * @param tenantId ??????id
     * @return ???????????????
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
     * ??????id????????????
     * @param id ?????????id
     * @param tenantId ??????id
     * @param version ??????
     * @return ???????????????
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
     *  ???????????????????????????????????????????????????
     * @param approveResult ????????????
     * @param tenantId ??????id
     * @param items ???????????????
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
     * ????????????service????????????
     * @param databaseInfoForCategory ???????????????????????????
     * @param tenantId ??????id
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
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
     * ??????????????????
     * @param databaseInfoBO ???????????????
     * @return ????????????
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
