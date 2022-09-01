package io.zeta.metaspace.web.service;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.enums.MessagePush;
import io.zeta.metaspace.model.enums.ProcessEnum;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.moebius.MoebiusApi;
import io.zeta.metaspace.model.moebius.MoebiusApiData;
import io.zeta.metaspace.model.moebius.MoebiusApiParam;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.*;
import io.zeta.metaspace.model.share.apisix.ApiSixCreateInfo;
import io.zeta.metaspace.model.share.apisix.ApiSixResultVO;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DataServiceUtil;
import io.zeta.metaspace.web.util.JsonUtils;
import io.zeta.metaspace.web.util.RedisUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.repository.Constants;
import org.apache.commons.lang.StringUtils;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.zeta.metaspace.model.enums.MessagePush.DATA_SERVICE_AUDIT_FINISH;

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
    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    private ApiPolyDao apiPolyDao;

    @Autowired
    ApproveGroupDAO approveGroupDAO;

    @Autowired
    MessageCenterService messageCenterService;

    @Autowired
    UserDAO userDAO;


    public ApiAudit getApiAuditById(String id, String tenantId) {
        return apiAuditDAO.getApiAuditById(id, tenantId);
    }

    public int insertApiAudit(String tenantId, String apiGuid, String apiVersion, int apiVersionNum) throws AtlasBaseException {
        return insertApiAudit(tenantId, apiGuid, apiVersion, apiVersionNum, null);
    }

    public int insertApiAudit(String tenantId, String apiGuid, String apiVersion, int apiVersionNum, String apiPolyId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            ApiAudit apiAudit = new ApiAudit();
            apiAudit.setId(UUIDUtils.uuid());
            apiAudit.setApiGuid(apiGuid);
            apiAudit.setApiVersion(apiVersion);
            apiAudit.setApiVersionNum(apiVersionNum);
            apiAudit.setApplicant(user.getAccount());
            apiAudit.setApplicantName(user.getUsername());
            apiAudit.setStatus(AuditStatusEnum.NEW);
            apiAudit.setApiPolyId(apiPolyId);
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

            if (apiAudit.getApiPolyId() == null) {
                ApiInfoV2 apiInfoV2 = dataShareDAO.getApiInfoByVersion(apiAudit.getApiGuid(), apiAudit.getApiVersion());
                if (apiInfoV2 != null && ApiStatusEnum.AUDIT.getName().equals(apiInfoV2.getStatus())) {
                    dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiInfoV2.getVersion(), ApiStatusEnum.DRAFT.getName(), DateUtils.currentTimestamp());
                }

                ApiLog apiLog = new ApiLog();
                apiLog.setApiId(apiInfoV2 == null ? "" : apiInfoV2.getGuid());
                apiLog.setType(ApiLogEnum.UNSUBMIT.getName());
                apiLog.setCreator(AdminUtils.getUserData().getUserId());
                apiLog.setDate(DateUtils.currentTimestamp());
                dataShareDAO.addApiLog(apiLog);
            } else {
                apiPolyDao.updateStatus(apiAudit.getApiPolyId(), AuditStatusEnum.CANCEL);
            }

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
            ApiInfoV2 apiInfoV2 = dataShareDAO.getApiInfoByVersion(apiGuid, apiVersion);
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

    @Transactional(rollbackFor = Exception.class)
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

            ApiInfoV2 apiInfoV2 = dataShareDAO.getApiInfoByVersion(apiAudit.getApiGuid(), apiAudit.getApiVersion());
            if (apiInfoV2 == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核 Api 不存在");
            }

            HttpRequestContext.get().auditLog(ModuleEnum.AUDIT.getAlias(), MessageFormat.format("审核 Api : {0} {1}", apiInfoV2.getName(), apiInfoV2.getVersion()));

            if (apiAudit.getApiPolyId() == null) {
                if (!ApiStatusEnum.AUDIT.getName().equals(apiInfoV2.getStatus())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核 Api 不在是审核状态");
                }

                Timestamp updateTime = DateUtils.currentTimestamp();
                if (AuditStatusEnum.AGREE.equals(status)) {
                    ApiHead apiHead = dataShareDAO.getSubmitApiHeadById(apiInfoV2.getGuid());
                    if (apiHead != null) {
                        apiInfoV2.setStatus(apiHead.getStatus());
                        dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiAudit.getApiVersion(), apiHead.getStatus(), updateTime);
                    } else {
                        apiInfoV2.setStatus(ApiStatusEnum.DOWN.getName());
                        dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiAudit.getApiVersion(), ApiStatusEnum.DOWN.getName(), updateTime);
                    }
                    apiInfoV2.setApiKey(UUID.randomUUID().toString());
                    apiGroupDAO.updateApiRelationByApi(apiInfoV2.getGuid(), updateTime);
                    apiExternalCreate(apiInfoV2);
                } else {
                    dataShareDAO.updateApiVersionStatus(apiInfoV2.getGuid(), apiAudit.getApiVersion(), ApiStatusEnum.DRAFT.getName(), updateTime);
                }
            } else {
                ApiPoly apiPoly = apiPolyDao.getApiPoly(apiAudit.getApiPolyId());
                if (apiPoly == null) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审核的API策略不存在");
                }
                apiPolyDao.updateStatus(apiPoly.getId(), status);
            }
            apiAudit.setStatus(status);
            apiAudit.setReason(reason);
            apiAudit.setUpdater(AdminUtils.getUserData().getAccount());

            apiAuditDAO.updateApiAudit(apiAudit, tenantId);

            // 审核消息推送审核人
            MessageEntity message = null;
            if (AuditStatusEnum.AGREE.equals(status)) {
                message = new MessageEntity(DATA_SERVICE_AUDIT_FINISH.type, MessagePush.getFormattedMessageName(DATA_SERVICE_AUDIT_FINISH.name, apiInfoV2.getName(), MessagePush.PASS), DATA_SERVICE_AUDIT_FINISH.module, ProcessEnum.PROCESS_APPROVED.code);
            } else if (AuditStatusEnum.DISAGREE.equals(status)) {
                message = new MessageEntity(DATA_SERVICE_AUDIT_FINISH.type, MessagePush.getFormattedMessageName(DATA_SERVICE_AUDIT_FINISH.name, apiInfoV2.getName(), MessagePush.REJECT), DATA_SERVICE_AUDIT_FINISH.module, ProcessEnum.PROCESS_APPROVED.code);
            }
            message.setCreateUser(apiAudit.getApplicant());
            messageCenterService.addMessage(message, tenantId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("更新审核记录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新审核记录失败");
        }
    }

    private void apiExternalCreate(ApiInfoV2 apiInfoV2) {
        if (Constants.DATA_SHARE_DOCKING_TYPE == CommonConstant.MOBIUS_TYPE) {
            mobiusCreateApi(apiInfoV2);
        }
        if (Constants.DATA_SHARE_DOCKING_TYPE == CommonConstant.API_SIX_TYPE) {
            apiSixCreate(apiInfoV2);
        }
    }

    public PageResult<ApiAudit> getApiAuditList(Parameters parameters, String tenantId, List<AuditStatusEnum> statuses, List<AuditStatusEnum> nonStatuses, String applicant) throws AtlasBaseException {
        try {
            if (Objects.nonNull(parameters.getQuery())) {
                parameters.setQuery(parameters.getQuery().replaceAll("%", "/%").replaceAll("_", "/_"));
            }

            if (nonStatuses != null && !nonStatuses.isEmpty()) {
                statuses = (statuses != null && !statuses.isEmpty() ? statuses.stream() : Arrays.stream(AuditStatusEnum.values()))
                        .filter(s -> !nonStatuses.contains(s))
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

    private void mobiusCreateApi(ApiInfoV2 apiInfoV2) {
        MoebiusApi api = new MoebiusApi();
        MoebiusApiData data = new MoebiusApiData(apiInfoV2);
        api.setPath(data.getPath());
        setMoebiusApiParam(apiInfoV2, api);
        data.setUpstream(apiInfoV2.getProtocol().toLowerCase() + "://" + getMetaspaceHost());
        api.setMeta_data(data);
        if (ApiStatusEnum.DOWN.getName().equalsIgnoreCase(apiInfoV2.getStatus())) {
            api.setStatus("draft");
        } else {
            api.setStatus("release");
        }
        api.setToken(apiInfoV2.getApiKey());
        Gson gson = new Gson();
        String jsonStr = gson.toJson(api, MoebiusApi.class);
        int retries = 3;
        int retryCount = 0;
        String mobiusURL = DataServiceUtil.mobiusUrl + "/v3/open/metaspace/api";
        String errorId = null;
        String errorReason = null;
        String proper = "0.0";
        String apiId = null;
        Map<String, Object> headerMap = Maps.newHashMap();
        headerMap.put(Constants.TICKET_KEY, AdminUtils.getSSOTicket());
        while (retryCount < retries) {
            String res = OKHttpClient.doPost(mobiusURL, jsonStr, headerMap);
            LOG.info(res);
            if (Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    apiId = String.valueOf(response.get("id"));
                    break;
                } else {
                    retryCount++;
                }
            } else {
                retryCount++;
            }
        }
        if (!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(), AtlasErrorCode.BAD_REQUEST, "审核api失败，云平台无法创建该api");
        }
        dataShareDAO.updateApiMobiusId(apiInfoV2.getGuid(), apiInfoV2.getVersion(), apiInfoV2.getApiKey(), apiId);
    }

    private void apiSixCreate(ApiInfoV2 apiInfoV2) {
        ApiSixCreateInfo apiSixCreateInfo = new ApiSixCreateInfo(apiInfoV2);
        String apiSixUrl = DataServiceUtil.apiSixUrl + CommonConstant.API_SIX_CREATE_PATH;
        String requestBody = JsonUtils.toJson(apiSixCreateInfo);
        Map<String, Object> headerMap = Maps.newHashMap();
        headerMap.put("X-API-KEY", Constants.X_API_KEY);
        int retryCount = 0;
        int retries = 3;
        ApiSixResultVO apiSixCreateVO = new ApiSixResultVO();
        boolean createResult = true;
        while (retryCount < retries) {
            String res = OKHttpClient.doPost(apiSixUrl, requestBody, headerMap);
            LOG.info(res);
            apiSixCreateVO = analResult(res);
            if (StringUtils.isEmpty(apiSixCreateVO.getError_msg())) {
                createResult = false;
                break;
            }
            retryCount++;
        }
        if (createResult) {
            throw new AtlasBaseException(apiSixCreateVO.getError_msg(), AtlasErrorCode.BAD_REQUEST, "审核api失败，apiSix创建api失败");
        }
        String apiSixKey = getApiSixKey(apiSixCreateVO.getNode().getKey());
        dataShareDAO.apiNewPath(apiInfoV2.getGuid(), apiInfoV2.getVersion(), apiSixKey);
    }

    //将云平台部署的api迁移到apiSix平台
    public Result MigrateApi() {
        //查询所有上线或下线状态的api
        List<ApiInfoV2> allApiInfo = dataShareDAO.getAllApiInfo();
        for (ApiInfoV2 apiInfoV2 : allApiInfo) {
            apiSixCreate(apiInfoV2);
        }
        return new Result("200", "迁移成功");
    }

    ApiSixResultVO analResult(String res) {
        if (StringUtils.isEmpty(res)) {
            return null;
        }
        return JsonUtils.fromJson(res, ApiSixResultVO.class);
    }

    private String getApiSixKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String[] split = key.split("/");
        return split[(split.length - 1)];
    }


    public static String getMetaspaceHost() {
        String url = MetaspaceConfig.getMetaspaceUrl();
        return url.substring(url.indexOf("://") + 3, url.length());
    }

    public Map convertMobiusResponse(String message) {
        Gson gson = new Gson();
        Map response = gson.fromJson(message, Map.class);
        return response;
    }

    private MoebiusApi setMoebiusApiParam(ApiInfoV2 apiInfoV2, MoebiusApi api) {
        getParam(apiInfoV2.getGuid(), apiInfoV2, apiInfoV2.getVersion());
        List<ApiInfoV2.FieldV2> params = apiInfoV2.getParam();
        List<MoebiusApiParam.HeaderParam> headerParamList = new ArrayList<>();
        Map<String, MoebiusApiParam.BodyParam> returnParamMap = new HashMap<>();
        List<MoebiusApiParam.QueryParam> queryParamList = new ArrayList<>();
        params.forEach(param -> {
            String place = param.getPlace();
            if ("HEADER".equalsIgnoreCase(place)) {
                MoebiusApiParam.HeaderParam headerParam = new MoebiusApiParam.HeaderParam();
                headerParam.setHeaderName(param.getName());
                headerParam.setDescription(param.getDescription());
                headerParam.setHeaderType(param.getType());
                headerParam.setHeaderValue(param.getExample());
                headerParam.setIsrequired(param.isFill() ? 1 : 0);
                headerParamList.add(headerParam);
            } else if ("QUERY".equalsIgnoreCase(place)) {
                MoebiusApiParam.QueryParam queryParam = new MoebiusApiParam.QueryParam();
                queryParam.setQueryName(param.getName());
                queryParam.setDescription(param.getDescription());
                queryParam.setQueryType(param.getType());
                queryParam.setQueryExample(param.getExample());
                queryParam.setIsrequired(param.isFill() ? 1 : 0);
                queryParamList.add(queryParam);
            }
        });
        apiInfoV2.getReturnParam().forEach(param -> {
            MoebiusApiParam.BodyParam bodyParam = new MoebiusApiParam.BodyParam();
//            bodyParam.setType(param.getColumnType());
            bodyParam.setType("");
            bodyParam.setDescription(param.getDescription());
            returnParamMap.put(param.getName(), bodyParam);
        });

        Gson gson = new Gson();
        MoebiusApiParam.QueryParam numParam = new MoebiusApiParam.QueryParam();
        numParam.setQueryName("page_num");
        numParam.setQueryType("number");
        numParam.setQueryExample("0");
        numParam.setIsrequired(1);
        queryParamList.add(numParam);
        MoebiusApiParam.QueryParam sizeParam = new MoebiusApiParam.QueryParam();
        sizeParam.setQueryName("page_size");
        sizeParam.setQueryType("number");
        sizeParam.setQueryExample("10");
        sizeParam.setIsrequired(1);
        queryParamList.add(sizeParam);
        String queryString = gson.toJson(queryParamList);
        String headerString = gson.toJson(headerParamList);
        Map<String, Object> map = new HashMap<>();
        map.put("type", "object");
        map.put("title", "empty object");
        map.put("properties", returnParamMap);
        String returnString = gson.toJson(map);
        api.setResBody(returnString);
        api.setParamQuery(queryString);
        api.setParamHeaders(headerString);
        return api;
    }

    public ApiInfoV2 getParam(String guid, ApiInfoV2 apiInfo, String version) throws AtlasBaseException {
        try {
            Gson gson = new Gson();
            Object param = shareDAO.getParamByGuid(guid, version);
            Object returnParam = shareDAO.getReturnParamByGuid(guid, version);
            Object sortParam = shareDAO.getSortParamByGuid(guid, version);

            PGobject paramPg = (PGobject) param;
            PGobject returnParamPg = (PGobject) returnParam;
            PGobject sortParamPg = (PGobject) sortParam;
            String paramValue = paramPg.getValue();
            String returnParamValue = returnParamPg.getValue();
            String sortParamValue = sortParamPg.getValue();
            Type type = new TypeToken<List<ApiInfoV2.FieldV2>>() {
            }.getType();
            List<ApiInfoV2.FieldV2> params = gson.fromJson(paramValue, type);
            List<ApiInfoV2.FieldV2> returnParams = gson.fromJson(returnParamValue, type);
            List<ApiInfoV2.FieldV2> sortParams = gson.fromJson(sortParamValue, type);
            apiInfo.setParam(params);
            apiInfo.setReturnParam(returnParams);
            apiInfo.setSortParam(sortParams);
            return apiInfo;
        } catch (Exception e) {
            LOG.error("获取数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }
}
