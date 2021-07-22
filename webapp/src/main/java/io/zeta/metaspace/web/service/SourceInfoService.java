package io.zeta.metaspace.web.service;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.bo.DatabaseInfoBO;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveOperate;
import io.zeta.metaspace.model.approve.ApproveParas;
import io.zeta.metaspace.model.approve.ApproveType;
import io.zeta.metaspace.model.dto.indices.ApprovalGroupMember;
import io.zeta.metaspace.model.dto.sourceinfo.DatabaseInfoDTO;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.enums.SubmitType;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
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
    ApproveService approveServiceImp;

    @Autowired
    ApproveDAO approveDAO;

    @Autowired
    DataManageService dataManageService;



    @Transactional(rollbackFor = Exception.class)
    public Result addDatabaseInfo(String tenantId, DatabaseInfo databaseInfo,String approveGroupId,SubmitType submitType){
        Result checkResult = this.checkCreateParam(databaseInfo,tenantId,approveGroupId,submitType);
        if (Boolean.TRUE.equals((ReturnUtil.isSuccess(checkResult)))){
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

    private void registerDatabaseInfo(DatabaseInfoPO databaseInfo){
        String parentCategoryId = databaseInfo.getCategoryId();
        databaseInfo.setCategoryId(null);
        databaseInfoDAO.insertDatabaseInfo(databaseInfo);
        databaseInfoDAO.insertDatabaseInfoRelationParentCategory(databaseInfo.getId(),parentCategoryId);
    }

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

    private Result checkCreateParam(DatabaseInfo databaseInfo,String tenantId,String approveGroupId,SubmitType submitType){
        //校验数据合法性
        if (databaseInfo == null){
            return ReturnUtil.error(AtlasErrorCode.INVALID_PARAMETERS.getErrorCode(),
                    AtlasErrorCode.INVALID_PARAMETERS.getFormattedErrorMessage());        }
        //校验数据库是否存在
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDatabaseId()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据库id"));
        }
        if (databaseDAO.getDatabaseById(databaseInfo.getDatabaseId()) == 0){
            return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                    AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getDatabaseId()));
        }
        //校验中文名
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDatabaseAlias()))) {
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据库中文名"));
        }
        //校验目录
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getCategoryId()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("所属层级目录id"));
        }
        if (categoryDAO.getCategoryCountById(databaseInfo.getCategoryId(),tenantId) == 0){
            return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                    AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getCategoryId()));
        }
        //保密期限校验
        if (databaseInfo.getSecurity() && ParamUtil.isNull(databaseInfo.getSecurityCycle())){
                    return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                            AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("保密期限"));
                }
        //对接人信息校验
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getBoDepartmentName(),databaseInfo.getBoName(),databaseInfo.getBoEmail(),databaseInfo.getBoTel(),
                            databaseInfo.getToDepartmentName(),databaseInfo.getToName(),databaseInfo.getToEmail(),databaseInfo.getToTel()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("对接人信息"));
        }

        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getTechnicalLeader()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("技术负责人"));
        }
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getBusinessLeader()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("业务负责人"));
        }

        if(Boolean.TRUE.equals(ParamUtil.isNull(submitType))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("提交类型"));
        }
        if(SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)&&Boolean.TRUE.equals(ParamUtil.isNull(approveGroupId))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("审核组"));
        }
        return ReturnUtil.success();
    }

    @Override
    public Object getObjectDetail(String objectId, String type, int version, String tenantId) {
        DatabaseInfoBO databaseInfoBO = this.getDatabaseInfoBOById(objectId,tenantId,version+"");
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

    public Result getDatabaseInfoById(String id,String tenantId,String version){
        return ReturnUtil.success(this.getDatabaseInfoBOById(id,tenantId,version));
    }

    public Result getDatabaseInfoListByIds(String tenantId, Status status, String name, int offset, int limit){
        List<DatabaseInfoForList> diLists = databaseInfoDAO.getDatabaseInfoList(tenantId,status.getIntValue()+"",name,offset,limit);
        int totalSize = databaseInfoDAO.getDatabaseInfoListCount(tenantId,status.getIntValue()+"",name);
        PageResult<DatabaseInfoForList> pageResult=new PageResult<>(diLists);
        pageResult.setCurrentSize(diLists.size());
        pageResult.setTotalSize(totalSize);
        return ReturnUtil.success(pageResult);
    }

    private DatabaseInfoBO getDatabaseInfoBOById(String id,String tenantId,String version){
        return databaseInfoDAO.getDatabaseInfoById(id,tenantId,version);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result revoke(String id,String tenantId) throws Exception {
        ApproveParas approveParas = new ApproveParas();
        approveParas.setResult(ApproveOperate.CANCEL.getCode());

        List<ApproveItem> approveItemList = new ArrayList<>();
        DatabaseInfoBO databaseInfoBO = databaseInfoDAO.getDatabaseInfoById(id,tenantId,"0");
        approveItemList.add(this.buildApproveItem(databaseInfoBO,databaseInfoBO.getApproveGroupId(),tenantId));
        approveParas.setApproveList(approveItemList);
        approveServiceImp.deal(approveParas,tenantId);
        return ReturnUtil.success();
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
            //TODO 插入历史版本记录
            //TODO 移除目录关联表父子关系
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
            dataManageService.createCategory(this.buildCategoryInfo(databaseInfoForCategory), CATEGORY_TYPE, tenantId);
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
