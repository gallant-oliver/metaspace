package io.zeta.metaspace.web.service;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiAudit;
import io.zeta.metaspace.model.share.ApiHead;
import io.zeta.metaspace.model.share.ApiInfoV2;
import io.zeta.metaspace.model.share.ApiLog;
import io.zeta.metaspace.model.share.ApiLogEnum;
import io.zeta.metaspace.model.share.ApiStatusEnum;
import io.zeta.metaspace.model.share.AuditStatusEnum;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.ApiAuditDAO;
import io.zeta.metaspace.web.dao.ApiGroupDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);


    @Autowired
    private ApiAuditDAO apiAuditDAO;
    @Autowired
    private DataShareDAO dataShareDAO;
    @Autowired
    private ApiGroupDAO apiGroupDAO;


    public ApiAudit getApiAuditById(String id, String tenantId) {
        return apiAuditDAO.getApiAuditById(id, tenantId);
    }

    public int insertApiAudit(String tenantId, String apiGuid, String apiVersion) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            ApiAudit apiAudit = new ApiAudit();
            apiAudit.setId(UUIDUtils.uuid());
            apiAudit.setApiGuid(apiGuid);
            apiAudit.setApiVersion(apiVersion);
            apiAudit.setApplicant(user.getAccount());
            apiAudit.setApplicantName(user.getUsername());
            apiAudit.setStatus(AuditStatusEnum.NEW);
            return apiAuditDAO.insertApiAudit(apiAudit, tenantId);
        } catch (Exception e) {
            LOG.error("创建审核记录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "插入审核记录失败");
        }
    }

    public int cancelApiAudit(String tenantId, String auditId) throws AtlasBaseException {
        try {
            ApiAudit apiAudit = apiAuditDAO.getApiAuditById(auditId, tenantId);
            if (Objects.isNull(apiAudit)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到审核记录");
            }

            if (!AuditStatusEnum.NEW.equals(apiAudit.getStatus())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核已经处理");
            }

            ApiInfoV2 apiInfoV2 = dataShareDAO.getApiInfo(apiAudit.getApiGuid(), apiAudit.getApiVersion());
            if (apiInfoV2 != null && ApiStatusEnum.AUDIT.getName().equals(apiInfoV2.getStatus())) {
                dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiInfoV2.getVersion(), ApiStatusEnum.DRAFT.getName(), DateUtils.currentTimestamp());
            }

            ApiLog apiLog = new ApiLog();
            apiLog.setApiId(apiInfoV2.getGuid());
            apiLog.setType(ApiLogEnum.UNSUBMIT.getName());
            apiLog.setCreator( AdminUtils.getUserData().getUserId());
            apiLog.setDate(DateUtils.currentTimestamp());
            dataShareDAO.addApiLog(apiLog);

            return apiAuditDAO.cancelApiAuditById(AdminUtils.getUserData().getAccount(), apiAudit.getId(), tenantId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("取消审核失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取消审核失败");
        }
    }

    public int cancelApiAudit(String tenantId, String apiGuid, String apiVersion) throws AtlasBaseException {
        try {
            ApiInfoV2 apiInfoV2 = dataShareDAO.getApiInfo(apiGuid, apiVersion);
            if (apiInfoV2 == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核 Api 不存在");
            }

            if (!ApiStatusEnum.AUDIT.getName().equals(apiInfoV2.getStatus())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核 Api 不在是审核状态");
            }
            Timestamp updateTime = DateUtils.currentTimestamp();
            dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiVersion, ApiStatusEnum.DRAFT.getName(), updateTime);
            return apiAuditDAO.cancelApiAudit(AdminUtils.getUserData().getAccount(), apiGuid, apiVersion, tenantId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("取消审核失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取消审核失败");
        }
    }

    public void updateApiAudit(String tenantId, String auditId, AuditStatusEnum status, String reason) throws AtlasBaseException {
        try {
            ApiAudit apiAudit = apiAuditDAO.getApiAuditById(auditId, tenantId);
            if (Objects.isNull(apiAudit)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到审核记录");
            }

            if (!AuditStatusEnum.NEW.equals(apiAudit.getStatus())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不能重复审核");
            }

            if (!Arrays.asList(AuditStatusEnum.AGREE, AuditStatusEnum.DISAGREE).contains(status)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核状态参数错误");
            }

            ApiInfoV2 apiInfoV2 = dataShareDAO.getApiInfo(apiAudit.getApiGuid(), apiAudit.getApiVersion());
            if (apiInfoV2 == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核 Api 不存在");
            }

            if (!ApiStatusEnum.AUDIT.getName().equals(apiInfoV2.getStatus())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核 Api 不在是审核状态");
            }

            apiAudit.setStatus(status);
            apiAudit.setReason(reason);
            apiAudit.setUpdater(AdminUtils.getUserData().getAccount());

            apiAuditDAO.updateApiAudit(apiAudit, tenantId);

            Timestamp updateTime = DateUtils.currentTimestamp();
            if (AuditStatusEnum.AGREE.equals(status)) {
                ApiHead apiHead = dataShareDAO.getSubmitApiHeadById(apiInfoV2.getGuid());
                if (apiHead != null) {
                    dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiAudit.getApiVersion(), apiHead.getStatus(), updateTime);
                } else {
                    dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiAudit.getApiVersion(), ApiStatusEnum.DOWN.getName(), updateTime);
                }
                apiGroupDAO.updateApiRelationByApi(apiInfoV2.getGuid(), updateTime);
            } else {
                dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiAudit.getApiVersion(), ApiStatusEnum.DRAFT.getName(), updateTime);

            }

        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新审核记录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新审核记录失败");
        }
    }

    public PageResult<ApiAudit> getApiAuditList(Parameters parameters, String tenantId, List<AuditStatusEnum> statuses,  List<AuditStatusEnum> nonStatuses,String applicant) throws AtlasBaseException {
        try {
            if (Objects.nonNull(parameters.getQuery())) {
                parameters.setQuery(parameters.getQuery().replaceAll("%", "/%").replaceAll("_", "/_"));
            }

            if(nonStatuses != null && !nonStatuses.isEmpty()){
                statuses = (statuses !=null && !statuses.isEmpty() ? statuses.stream() : Arrays.stream(AuditStatusEnum.values()))
                        .filter(s->!nonStatuses.contains(s))
                        .collect(Collectors.toList());
            }

            List<ApiAudit> apiAuditList = apiAuditDAO.getApiAuditList(tenantId, parameters, statuses, applicant);

            PageResult<ApiAudit> pageResult = new PageResult<>();
            pageResult.setTotalSize(apiAuditList.isEmpty() ? 0 : apiAuditList.get(0).getTotal());
            pageResult.setCurrentSize(apiAuditList.size());
            pageResult.setLists(apiAuditList);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询审核记录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询审核记录失败");
        }
    }
}
