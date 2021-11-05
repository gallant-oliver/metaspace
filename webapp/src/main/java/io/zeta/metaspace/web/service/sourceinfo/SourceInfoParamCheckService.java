package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.enums.SourceInfoOperation;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.enums.SubmitType;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.util.ParamUtil;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SourceInfoParamCheckService {
    @Autowired
    DatabaseDAO databaseDAO;

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    DatabaseInfoDAO databaseInfoDAO;

    public Result checkCreateParam(DatabaseInfo databaseInfo, String tenantId, String approveGroupId, SubmitType submitType){
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
        if(databaseInfoDAO.getDatabaseByDbId(databaseInfo.getDatabaseId(),tenantId)){
            return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                    AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getDatabaseId()+"已经登记，无法重复登记"));
        }
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDataSourceId()))) {
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据源id"));
        }
        //校验中文名
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDatabaseAlias()))) {
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据库中文名"));
        }
        //校验中文名重名
        if (databaseInfoDAO.getDatabaseDuplicateName(tenantId,databaseInfo.getDatabaseAlias(),null)) {
            return ReturnUtil.error(AtlasErrorCode.DUPLICATE_ALIAS_NAME.getErrorCode(),
                    AtlasErrorCode.DUPLICATE_ALIAS_NAME.getFormattedErrorMessage(databaseInfo.getDatabaseAlias()));
        }
        int count = categoryDAO.getCategoryCountByParentIdAndName(tenantId,databaseInfo.getCategoryId(),databaseInfo.getDatabaseAlias());
        if (count>0){
            return ReturnUtil.error(AtlasErrorCode.DUPLICATE_ALIAS_NAME.getErrorCode(),
                    AtlasErrorCode.DUPLICATE_ALIAS_NAME.getFormattedErrorMessage(databaseInfo.getDatabaseAlias()));
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
        /*//对接人信息校验
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getBoDepartmentName(),databaseInfo.getBoName(),databaseInfo.getBoEmail(),databaseInfo.getBoTel(),
                databaseInfo.getToDepartmentName(),databaseInfo.getToName(),databaseInfo.getToEmail(),databaseInfo.getToTel()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("对接人信息"));
        }*/

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

    //暂存校验
    public Result saveCheckCreateParam(DatabaseInfo databaseInfo, String tenantId, String approveGroupId, SubmitType submitType){
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
        if(databaseInfoDAO.getDatabaseByDbId(databaseInfo.getDatabaseId(),tenantId)){
            return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                    AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getDatabaseId()+"已经登记，无法重复登记"));
        }
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDataSourceId()))) {
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据源id"));
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
        return ReturnUtil.success();
    }

    public Result checkCreateListParam(List<DatabaseInfo> databaseInfos, String tenantId){
        for (DatabaseInfo databaseInfo:databaseInfos) {
            //校验数据合法性
            if (databaseInfo == null) {
                return ReturnUtil.error(AtlasErrorCode.INVALID_PARAMETERS.getErrorCode(),
                        AtlasErrorCode.INVALID_PARAMETERS.getFormattedErrorMessage());
            }
            //校验数据库是否存在
            if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDatabaseId()))) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据库id"));
            }
            if (databaseDAO.getDatabaseById(databaseInfo.getDatabaseId()) == 0) {
                return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                        AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getDatabaseId()));
            }
            if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDataSourceId()))) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据源id"));
            }
            if(databaseInfoDAO.getDatabaseByDbId(databaseInfo.getDatabaseId(),tenantId)){
                return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                        AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getDatabaseId()+"已经登记，无法重复登记"));
            }
            //校验中文名
            if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDatabaseAlias()))) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据库中文名"));
            }
            //校验中文名重名
            if (databaseInfoDAO.getDatabaseDuplicateName(tenantId, databaseInfo.getDatabaseAlias(), databaseInfo.getId())) {
                return ReturnUtil.error(AtlasErrorCode.DUPLICATE_ALIAS_NAME.getErrorCode(),
                        AtlasErrorCode.DUPLICATE_ALIAS_NAME.getFormattedErrorMessage(databaseInfo.getDatabaseAlias()));
            }
            int count = categoryDAO.getCategoryCountByParentIdAndName(tenantId, databaseInfo.getCategoryId(), databaseInfo.getDatabaseAlias());
            if (count > 0) {
                return ReturnUtil.error(AtlasErrorCode.DUPLICATE_ALIAS_NAME.getErrorCode(),
                        AtlasErrorCode.DUPLICATE_ALIAS_NAME.getFormattedErrorMessage(databaseInfo.getDatabaseAlias()));
            }
            //校验目录
            if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getCategoryId()))) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("所属层级目录id"));
            }
            if (categoryDAO.getCategoryCountById(databaseInfo.getCategoryId(), tenantId) == 0) {
                return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                        AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getCategoryId()));
            }
            //保密期限校验
            if (databaseInfo.getSecurity() && ParamUtil.isNull(databaseInfo.getSecurityCycle())) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("保密期限"));
            }
            /*//对接人信息校验
            if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getBoDepartmentName(), databaseInfo.getBoName(), databaseInfo.getBoEmail(), databaseInfo.getBoTel(),
                    databaseInfo.getToDepartmentName(), databaseInfo.getToName(), databaseInfo.getToEmail(), databaseInfo.getToTel()))) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("对接人信息"));
            }*/

            if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getTechnicalLeader()))) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("技术负责人"));
            }
            if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getBusinessLeader()))) {
                return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                        AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("业务负责人"));
            }
        }
        return ReturnUtil.success();
    }

    public Result checkUpdateParam(DatabaseInfo databaseInfo, String tenantId,String approveGroupId, SubmitType submitType){

        if(Boolean.TRUE.equals(ParamUtil.isNull(submitType))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("提交类型"));
        }
        if ((Status.AUDITING.getIntValue()+"").equals(databaseInfoDAO.getDatabaseInfoById(databaseInfo.getId(),tenantId,0).getStatus())){
            return ReturnUtil.error(AtlasErrorCode.INVALID_PARAMS.getErrorCode(),
                    AtlasErrorCode.INVALID_PARAMS.getFormattedErrorMessage("待审核状态下无法修改"));
        }
        if(SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)&&Boolean.TRUE.equals(ParamUtil.isNull(approveGroupId))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("审核组"));
        }

        // 查询旧的登记状态，是否为‘发布审核通过’或者‘待审核’
        boolean isApprove = databaseInfoDAO.auditStatusIsApprove(databaseInfo.getId());

        if(!isApprove && databaseInfoDAO.getDatabaseByDbId(databaseInfo.getDatabaseId(),tenantId)){
            return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                    AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getDatabaseId()+"已经登记，无法重复登记"));
        }
        //校验中文名
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getDatabaseAlias()))) {
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据库中文名"));
        }
        //校验中文名重名
        if (databaseInfoDAO.getDatabaseDuplicateName(tenantId,databaseInfo.getDatabaseAlias(),databaseInfo.getId())) {
            return ReturnUtil.error(AtlasErrorCode.DUPLICATE_ALIAS_NAME.getErrorCode(),
                    AtlasErrorCode.DUPLICATE_ALIAS_NAME.getFormattedErrorMessage(databaseInfo.getDatabaseAlias()));
        }
        int count = categoryDAO.getCategoryCountByParentIdAndName(tenantId,databaseInfo.getCategoryId(),databaseInfo.getDatabaseAlias());
        if (count>0){
            return ReturnUtil.error(AtlasErrorCode.DUPLICATE_ALIAS_NAME.getErrorCode(),
                    AtlasErrorCode.DUPLICATE_ALIAS_NAME.getFormattedErrorMessage(databaseInfo.getDatabaseAlias()));
        }
        //保密期限校验
        if (databaseInfo.getSecurity() && ParamUtil.isNull(databaseInfo.getSecurityCycle())){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("保密期限"));
        }
        /*//对接人信息校验
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getBoDepartmentName(),databaseInfo.getBoName(),databaseInfo.getBoEmail(),databaseInfo.getBoTel(),
                databaseInfo.getToDepartmentName(),databaseInfo.getToName(),databaseInfo.getToEmail(),databaseInfo.getToTel()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("对接人信息"));
        }*/

        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getTechnicalLeader()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("技术负责人"));
        }
        if (Boolean.TRUE.equals(ParamUtil.isNull(databaseInfo.getBusinessLeader()))){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("业务负责人"));
        }


        return ReturnUtil.success();
    }

    public Result checkSourceInfoStatus(List<String> idList, SourceInfoOperation operation){
        boolean checkResult;
        List<String> statusList = databaseInfoDAO.getStatusByIdList(idList);
        if (SourceInfoOperation.PUBLISH.equals(operation)){
            checkResult = statusList.stream().anyMatch(status->(Status.AUDITING.getIntValue()+"").equals(status)||(Status.ACTIVE.getIntValue()+"").equals(status));
            if (checkResult){
                return ReturnUtil.error(AtlasErrorCode.INVALID_PARAMS.getErrorCode(),
                        AtlasErrorCode.INVALID_PARAMS.getFormattedErrorMessage("审批中状态和已发布状态不可发起审批"));
            }
        }
        if (SourceInfoOperation.DELETE.equals(operation)){
            checkResult = statusList.stream().anyMatch((Status.AUDITING.getIntValue()+"")::equals);
            if (checkResult){
                return ReturnUtil.error(AtlasErrorCode.INVALID_PARAMS.getErrorCode(),
                        AtlasErrorCode.INVALID_PARAMS.getFormattedErrorMessage("待审核状态不可删除"));
            }
        }
        if (SourceInfoOperation.REVOKE.equals(operation)){
            checkResult = statusList.stream().noneMatch((Status.AUDITING.getIntValue()+"")::equals);
            if (checkResult){
                return ReturnUtil.error(AtlasErrorCode.INVALID_PARAMS.getErrorCode(),
                        AtlasErrorCode.INVALID_PARAMS.getFormattedErrorMessage("非审核中状态不可撤回"));
            }
        }
        return ReturnUtil.success();
    }

}
