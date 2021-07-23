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
import io.zeta.metaspace.model.enums.SourceInfoOperation;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.enums.SubmitType;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForCategory;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForList;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.ApproveDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
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
public class SourceInfoService implements Approvable {

    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.TechnicalREST");
    private static int CATEGORY_TYPE = 0;
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
        Result checkResult = checkService.checkCreateParam(databaseInfo,tenantId,approveGroupId,submitType);
        if (Boolean.FALSE.equals((ReturnUtil.isSuccess(checkResult)))){
            return checkResult;
        }
        this.registerDatabaseInfo(this.convertToPO(tenantId,databaseInfo));

        List<DatabaseInfo> databaseInfoList = new ArrayList<>();
        databaseInfoList.add(databaseInfo);
        if (SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)){
            this.approveItems(tenantId,databaseInfoList,approveGroupId);
        }
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
        if (Boolean.TRUE.equals(ParamUtil.isNull(approveGroupId))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("审核组"));
        }
        List<DatabaseInfo> databaseInfoList = databaseInfoDAO.getDatabaseIdAndAliasByIds(idList);
        databaseInfoDAO.updateStatusByIds(idList,Status.AUDITING.getIntValue()+"");
        this.approveItems(tenantId,databaseInfoList,approveGroupId);

        return ReturnUtil.success();
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
        ApproveParas approveParas = new ApproveParas();
        approveParas.setResult(ApproveOperate.CANCEL.getCode());

        List<ApproveItem> approveItemList = new ArrayList<>();
        DatabaseInfoBO databaseInfoBO = databaseInfoDAO.getDatabaseInfoById(id,tenantId,0);
        approveItemList.add(this.buildApproveItem(databaseInfoBO,databaseInfoBO.getApproveGroupId(),tenantId));
        approveParas.setApproveList(approveItemList);
        approveServiceImp.deal(approveParas,tenantId);
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
        List<DatabaseInfoForList> diLists = databaseInfoDAO.getDatabaseInfoList(tenantId,status.getIntValue()+"",name,offset,limit);
        int totalSize = databaseInfoDAO.getDatabaseInfoListCount(tenantId,status.getIntValue()+"",name);
        PageResult<DatabaseInfoForList> pageResult=new PageResult<>(diLists);
        pageResult.setCurrentSize(diLists.size());
        pageResult.setTotalSize(totalSize);
        return ReturnUtil.success(pageResult);
    }

    public Result updateSourceInfo(DatabaseInfo databaseInfo,String tenantId,String approveGroupId,SubmitType submitType){
        Result checkResult = checkService.checkUpdateParam(databaseInfo,approveGroupId,submitType);
        if (Boolean.FALSE.equals((ReturnUtil.isSuccess(checkResult)))){
            return checkResult;
        }
        databaseInfoDAO.updateSourceInfo(databaseInfo);
        return ReturnUtil.success();
    }

    private void registerDatabaseInfo(DatabaseInfoPO databaseInfo){
        String parentCategoryId = databaseInfo.getCategoryId();
        databaseInfo.setCategoryId(null);
        databaseInfoDAO.insertDatabaseInfo(databaseInfo);
        databaseInfoDAO.insertDatabaseInfoRelationParentCategory(databaseInfo.getId(),parentCategoryId);
    }

    private void approveItems(String tenantId, List<DatabaseInfo> databaseInfos,String approveGroupId){
        for (DatabaseInfo databaseInfo:databaseInfos) {
            ApproveItem approveItem = this.buildApproveItem(databaseInfo,approveGroupId,tenantId);
            approveServiceImp.addApproveItem(approveItem);
            databaseInfoDAO.updateApproveIdAndApproveGroupIdById(databaseInfo.getId(),approveItem.getId(),approveGroupId);
        }
    }

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
        approveItem.setApproveType(ApproveType.PUBLISH.getCode());
        approveItem.setApproveGroup(approveGroupId);
        approveItem.setSubmitter(AdminUtils.getUserData().getUserId());
        approveItem.setCommitTime(Timestamp.valueOf(LocalDateTime.now()));
        approveItem.setModuleId(ModuleEnum.SOURCEINFO.getId() + "");
        approveItem.setVersion(0);
        approveItem.setTenantId(tenantId);

        return approveItem;
    }

    private DatabaseInfoPO convertToPO(String tenantId, DatabaseInfo databaseInfo){
        DatabaseInfoPO databaseInfoPO = new DatabaseInfoPO();

        BeansUtil.copyPropertiesIgnoreNull(databaseInfo,databaseInfoPO);
        databaseInfoPO.setId(UUID.randomUUID().toString());
        databaseInfoPO.setStatus(Status.FOUNDED.getIntValue()+"");
        databaseInfoPO.setCreator(AdminUtils.getUserData().getUserId());
        databaseInfoPO.setUpdater(AdminUtils.getUserData().getUserId());
        databaseInfoPO.setTenantId(tenantId);

        return databaseInfoPO;
    }


    @Override
    public Object getObjectDetail(String objectId, String type, int version, String tenantId) {
        DatabaseInfoBO databaseInfoBO = this.getDatabaseInfoBOById(objectId,tenantId,version);
        DatabaseInfoDTO databaseInfoDTO = new DatabaseInfoDTO();
        BeansUtil.copyPropertiesIgnoreNull(databaseInfoBO,databaseInfoDTO);
        List<User> users = approveDAO.getApproveUsers(databaseInfoBO.getApproveGroupId());
        if (!CollectionUtils.isEmpty(users)) {
            List<ApprovalGroupMember> approvalGroupMembers = users.stream().map(x -> BeanMapper.map(x, ApprovalGroupMember.class)).collect(Collectors.toList());
            databaseInfoDTO.setApproveGroupMembers(approvalGroupMembers);
        } else {
            databaseInfoDTO.setApproveGroupMembers(new ArrayList<>());
        }
        return databaseInfoDTO;
    }

    private DatabaseInfoBO getDatabaseInfoBOById(String id,String tenantId,int version){
        return databaseInfoDAO.getDatabaseInfoById(id,tenantId,version);
    }

    @Override
    public void changeObjectStatus(String approveResult, String tenantId, List<ApproveItem> items) throws Exception {
        List<String> idList = items.stream().map(ApproveItem::getObjectId).collect(Collectors.toList());
        if (ApproveOperate.APPROVE.getCode().equals(approveResult)) {
            databaseInfoDAO.updateStatusByIds(idList,Status.ACTIVE.getIntValue()+"");
            List<DatabaseInfoForCategory> databaseInfoList = databaseInfoDAO.getDatabaseInfoByIds(idList);
            for (DatabaseInfoForCategory databaseInfo:databaseInfoList){
                this.createCategoryInfo(databaseInfo,tenantId);
            }
            databaseInfoDAO.insertHistoryVersion(idList);
            databaseInfoDAO.deleteSourceInfoAndParentCategoryRelation(idList);
        } else if (ApproveOperate.REJECTED.getCode().equals(approveResult)) {
            databaseInfoDAO.updateStatusByIds(idList,Status.REJECT.getIntValue()+"");
        } else if (ApproveOperate.CANCEL.getCode().equals(approveResult)) {
            databaseInfoDAO.updateStatusByIds(idList,Status.FOUNDED.getIntValue()+"");
        }
    }

    private void createCategoryInfo(DatabaseInfoForCategory databaseInfoForCategory,String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.createMetadataCategory()");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.SOURCEINFO.getAlias(), databaseInfoForCategory.getName());
            CategoryPrivilege categoryPrivilege= dataManageService.createCategory(this.buildCategoryInfo(databaseInfoForCategory), CATEGORY_TYPE, tenantId);
            databaseInfoDAO.updateRealCategoryRelation(databaseInfoForCategory.getId(),categoryPrivilege.getGuid());
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    private CategoryInfoV2 buildCategoryInfo(DatabaseInfoForCategory databaseInfoBO){
        CategoryInfoV2 categoryInfoV2 = new CategoryInfoV2();

        categoryInfoV2.setAuthorized(Boolean.FALSE);
        categoryInfoV2.setName(databaseInfoBO.getName());
        categoryInfoV2.setParentCategoryGuid(databaseInfoBO.getParentCategoryId());

        return categoryInfoV2;
    }
}
