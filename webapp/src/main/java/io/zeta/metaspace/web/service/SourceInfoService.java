package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveParas;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.enums.SubmitType;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeansUtil;
import io.zeta.metaspace.web.util.ParamUtil;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SourceInfoService implements ApproveService {
    @Autowired
    DatabaseDAO databaseDAO;

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    DatabaseInfoDAO databaseInfoDAO;




    public Result addDatabaseInfo(String tenantId, DatabaseInfo databaseInfo,SubmitType submitType) throws Exception {
        Result checkResult = this.checkParam(databaseInfo,tenantId,submitType);
        if (ReturnUtil.isSuccess(checkResult)){
            return checkResult;
        }
        return null;




    }

    private void registerDatabaseInfo(String tenantId, DatabaseInfo databaseInfo){
        databaseInfoDAO.insertDatabaseInfo(this.convertToPO(tenantId,databaseInfo));
    }
    public void insert(DatabaseInfoPO databaseInfoPO){
        databaseInfoDAO.insertDatabaseInfo(databaseInfoPO);
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

    private Result checkParam(DatabaseInfo databaseInfo,String tenantId,SubmitType submitType) throws Exception {
        //校验数据合法性
        if (databaseInfo == null){
            return ReturnUtil.error(AtlasErrorCode.INVALID_PARAMETERS.getErrorCode(),
                    AtlasErrorCode.INVALID_PARAMETERS.getFormattedErrorMessage());        }
        //校验数据库是否存在
        if (ParamUtil.isNull(databaseInfo.getDatabaseId())){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据库id"));
        }
        if (databaseDAO.getDatabaseById(databaseInfo.getDatabaseId()) == 0){
            return ReturnUtil.error(AtlasErrorCode.INVALID_OBJECT_ID.getErrorCode(),
                    AtlasErrorCode.INVALID_OBJECT_ID.getFormattedErrorMessage(databaseInfo.getDatabaseId()));
        }
        //校验中文名
        if (ParamUtil.isNull(databaseInfo.getDatabaseAlias())) {
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("数据库中文名"));
        }
        //校验目录
        if (ParamUtil.isNull(databaseInfo.getCategoryId())){
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
        if (ParamUtil.isNull(databaseInfo.getBoDepartmentName(),databaseInfo.getBoName(),databaseInfo.getBoEmail(),databaseInfo.getBoTel(),
                            databaseInfo.getToDepartmentName(),databaseInfo.getToName(),databaseInfo.getToEmail(),databaseInfo.getToTel())){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("对接人信息"));
        }

        if (ParamUtil.isNull(databaseInfo.getTechnicalLeader())){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("技术负责人"));
        }
        if (ParamUtil.isNull(databaseInfo.getBusinessLeader())){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("业务负责人"));
        }

        if(ParamUtil.isNull(submitType)){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("提交类型"));
        }
        if(SubmitType.SUBMIT_AND_PUBLISH.equals(submitType)&&ParamUtil.isNull(databaseInfo.getApproveGroupId())){
            return ReturnUtil.error(AtlasErrorCode.EMPTY_PARAMS.getErrorCode(),
                    AtlasErrorCode.EMPTY_PARAMS.getFormattedErrorMessage("审核组"));
        }
        return ReturnUtil.success();
    }

    @Override
    public PageResult<ApproveItem> search(ApproveParas paras, String tenant_id) {
        return null;
    }

    @Override
    public void deal(ApproveParas paras, String tenant_id) throws IllegalAccessException, ClassNotFoundException, InstantiationException {

    }

    @Override
    public void addApproveItem(ApproveItem Item) {

    }

    @Override
    public Object ApproveObjectDetail(String tenantId, String objectId, String objectType, int version, String moduleId) throws Exception {
        return null;
    }
}
