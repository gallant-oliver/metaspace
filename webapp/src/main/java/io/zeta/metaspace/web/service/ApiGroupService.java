// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.zeta.metaspace.model.apigroup.ApiCategory;
import io.zeta.metaspace.model.apigroup.ApiGroupInfo;
import io.zeta.metaspace.model.apigroup.ApiGroupLog;
import io.zeta.metaspace.model.apigroup.ApiGroupLogEnum;
import io.zeta.metaspace.model.apigroup.ApiGroupRelation;
import io.zeta.metaspace.model.apigroup.ApiGroupStatusApi;
import io.zeta.metaspace.model.apigroup.ApiGroupV2;
import io.zeta.metaspace.model.apigroup.ApiVersion;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.moebius.MoebiusApiGroup;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.share.APIContent;
import io.zeta.metaspace.model.share.ApiInfoV2;
import io.zeta.metaspace.model.share.ApiLog;
import io.zeta.metaspace.model.share.ApiLogEnum;
import io.zeta.metaspace.model.share.ApiStatusEnum;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.ApiGroupDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DataServiceUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author lixiang03
 * @Data 2020/8/10 18:52
 */
@Service
public class ApiGroupService {
    private static final Logger LOG = LoggerFactory.getLogger(ApiGroupService.class);
    @Autowired
    ApiGroupDAO apiGroupDAO;
    @Autowired
    UserDAO userDAO;
    @Autowired
    DataShareDAO dataShareDAO;
    @Autowired
    TenantService tenantService;

    @Transactional(rollbackFor=Exception.class)
    public void insertApiGroup(ApiGroupV2 group,String tenantId) throws AtlasBaseException {
        int sameNum = apiGroupDAO.sameName(group.getId(), group.getName(), tenantId);
        if (sameNum!=0){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同的api分组名称");
        }
        String id = UUID.randomUUID().toString();
        group.setId(id);
        User user = AdminUtils.getUserData();
        group.setCreator(user.getUserId());
        group.setUpdater(user.getUserId());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        group.setCreateTime(timestamp);
        group.setUpdateTime(timestamp);
        group.setPublish(false);
        apiGroupDAO.insertApiGroup(group,tenantId);
        List<String> apis = group.getApis();
        if (apis!=null&& apis.size()!=0){
            List<ApiInfoV2> apiInfoByIds = dataShareDAO.getNoDraftApiInfoByIds(apis);
            apiGroupDAO.insertApiRelation(apiInfoByIds,group.getId());
        }
        addApiGroupLogs(ApiGroupLogEnum.INSERT, Lists.newArrayList(id),AdminUtils.getUserData().getUserId());
        List<String> apiMobiusId = apiGroupDAO.getGroupRelationMobiusId(group.getId());
        String groupMobiusIds = createGroupMobius(group,apiMobiusId);
        apiGroupDAO.updateApiMobiusId(id,groupMobiusIds);
    }

    public String createGroupMobius(ApiGroupV2 group,List<String> apiIds){
        MoebiusApiGroup moebiusApiGroup = new MoebiusApiGroup();
        List<String> usersEmailByIds = userDAO.getUsersEmailByIds(group.getApprove());
        moebiusApiGroup.setName(group.getName());
        moebiusApiGroup.setAuditor(usersEmailByIds);
        moebiusApiGroup.setDesc(group.getDescription());
        moebiusApiGroup.setApi_ids(apiIds);
        moebiusApiGroup.setOrigin("metaspace");
        int retries = 3;
        int retryCount = 0;
        String mobiusURL= DataServiceUtil.mobiusUrl + "/v3/open/capacity";
        String errorId = null;
        String errorReason = null;
        String proper = "0.0";
        Gson gson = new Gson();
        String jsonStr = gson.toJson(moebiusApiGroup, MoebiusApiGroup.class);
        String groupId=null;
        while(retryCount < retries) {
            String res = OKHttpClient.doPost(mobiusURL, jsonStr);
            LOG.info(res);
            if(Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    groupId=String.valueOf(response.get("id"));
                    break;
                } else {
                    retryCount++;
                }
            } else {
                retryCount++;
            }
        }
        if(!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(),AtlasErrorCode.BAD_REQUEST, "云平台创建能力失败");
        }
        return groupId;
    }

    public Map convertMobiusResponse(String message) {
        Gson gson = new Gson();
        Map response = gson.fromJson(message, Map.class);
        return response;
    }

    public void updateApiGroup(ApiGroupV2 group,String tenantId) throws AtlasBaseException {
        int sameNum = apiGroupDAO.sameName(group.getId(), group.getName(), tenantId);
        if (sameNum!=0){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同的api分组名称");
        }
        User user = AdminUtils.getUserData();
        group.setUpdater(user.getUserId());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        group.setUpdateTime(timestamp);
        group.setPublish(false);
        apiGroupDAO.updateApiGroup(group);
        List<String> aPiIdByGroup = apiGroupDAO.getAPiIdByGroup(group.getId());
        List<String> insertIds = new ArrayList<>();
        List<String> apis = group.getApis();
        if (apis==null||apis.size()==0){
            apiGroupDAO.deleteRelationByGroupIds(Lists.newArrayList(group.getId()));
            addApiGroupLogs(ApiGroupLogEnum.UPDATE, Lists.newArrayList(group.getId()),AdminUtils.getUserData().getUserId());
            return;
        }
        for (String id:apis){
            if (!aPiIdByGroup.contains(id)){
                insertIds.add(id);
            }
        }
        if (insertIds.size()!=0){
            List<ApiInfoV2> apiInfoByIds = dataShareDAO.getNoDraftApiInfoByIds(insertIds);
            apiGroupDAO.insertApiRelation(apiInfoByIds,group.getId());
        }
        apiGroupDAO.updateRelation(apis,group.getId());
        addApiGroupLogs(ApiGroupLogEnum.UPDATE, Lists.newArrayList(group.getId()),AdminUtils.getUserData().getUserId());
        List<String> apiMobiusId = apiGroupDAO.getGroupRelationMobiusId(group.getId());
        updateGroupMobius(group,apiMobiusId);
    }

    public void updateGroupMobius(ApiGroupV2 group,List<String> apiMobiusIds){
        MoebiusApiGroup moebiusApiGroup = new MoebiusApiGroup();
        List<String> usersEmailByIds = userDAO.getUsersEmailByIds(group.getApprove());
        moebiusApiGroup.setName(group.getName());
        moebiusApiGroup.setAuditor(usersEmailByIds);
        moebiusApiGroup.setApi_ids(apiMobiusIds);
        String apiMobiusId = apiGroupDAO.getApiGroupMobiusId(group.getId());
        moebiusApiGroup.setId(apiMobiusId);
        moebiusApiGroup.setDesc(group.getDescription());
        int retries = 3;
        int retryCount = 0;
        String mobiusURL= DataServiceUtil.mobiusUrl + "/v3/open/capacity";
        String errorId = null;
        String errorReason = null;
        String proper = "0.0";
        Gson gson = new Gson();
        String jsonStr = gson.toJson(moebiusApiGroup, MoebiusApiGroup.class);
        while(retryCount < retries) {
            String res = OKHttpClient.doPut(mobiusURL, jsonStr);
            LOG.info(res);
            if(Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    break;
                } else {
                    retryCount++;
                }
            } else {
                retryCount++;
            }
        }
        if(!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(),AtlasErrorCode.BAD_REQUEST, "云平台更新能力失败");
        }
    }

    public void updateMobiusApiRelation(String mobiusGroupId,List<String> moebiusApiIds){
        Map<String,Object> map = new HashMap<>();
        map.put("capacity_id",mobiusGroupId);
        map.put("api_ids",moebiusApiIds);
        int retries = 3;
        int retryCount = 0;
        String mobiusURL= DataServiceUtil.mobiusUrl + "/v3/open/capacity/api";
        String errorId = null;
        String errorReason = null;
        String proper = "0.0";
        Gson gson = new Gson();
        String jsonStr = gson.toJson(map);
        while(retryCount < retries) {
            String res = OKHttpClient.doPost(mobiusURL, jsonStr);
            LOG.info(res);
            if(Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    break;
                } else {
                    retryCount++;
                }
            } else {
                retryCount++;
            }
        }
        if(!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(),AtlasErrorCode.BAD_REQUEST, "云平台能力关联api失败");
        }
    }

    public PageResult<ApiGroupV2> searchApiGroup(Parameters parameters, String projectId, String tenantId,Boolean publish){
        PageResult<ApiGroupV2> result = new PageResult<>();
        updateApiRelationStatus();
        String query = parameters.getQuery();
        List<ApiGroupV2> groups;
        try {
            groups = apiGroupDAO.searchApiGroup(parameters, projectId, tenantId, publish);
        } catch (SQLException e) {
            LOG.error("SQL执行异常", e);
            groups = new ArrayList<>();
        }
        if (groups==null||groups.size()==0){
            result.setLists(new ArrayList<>());
            return result;
        }
        result.setLists(groups);
        result.setTotalSize(groups.get(0).getCount());
        result.setCurrentSize(groups.size());
        return result;
    }

    public void updateApiRelationStatus(){
        Date date = DateUtils.addDays(new Date(System.currentTimeMillis()), -90);
        Timestamp timestamp = new Timestamp(date.getTime());
        apiGroupDAO.updateApiRelationStatusByTime(timestamp);
    }

    public ApiGroupInfo getApiGroupInfo(String groupId){
        ApiGroupInfo apiGroupInfo = apiGroupDAO.getApiGroupInfo(groupId);
        User user = userDAO.getUser(apiGroupInfo.getCreator());
        if (user!=null){
            apiGroupInfo.setCreator(user.getUsername());
        }
        Gson gson = new Gson();
        String approveJson = apiGroupInfo.getApproveJson();
        List list = gson.fromJson(approveJson, List.class);
        List<String> userNames = new ArrayList<>();
        if (list!=null&&list.size()!=0){
            userNames = userDAO.getUserNameByIds(list);
        }
        apiGroupInfo.setApprove(userNames);
        List<ApiCategory.Api> aPiByGroup = apiGroupDAO.getAPiByGroup(groupId);
        Map<String,ApiCategory> apiCategoryMap = new HashMap<>();
        aPiByGroup.forEach(api -> {
            String status = api.getStatus();
            if (ApiStatusEnum.UP.getName().equals(status)){
                api.setApiStatus(true);
            }else{
                api.setApiStatus(false);
            }
            if (apiCategoryMap.containsKey(api.getCategoryId())){
                apiCategoryMap.get(api.getCategoryId()).getApi().add(api);
            }else{
                ApiCategory apiCategory = new ApiCategory(api);
                List<ApiCategory.Api> apis = new ArrayList<>();
                apis.add(api);
                apiCategory.setApi(apis);
                apiCategoryMap.put(api.getCategoryId(),apiCategory);
            }
        });
        apiGroupInfo.setApis(new ArrayList<>(apiCategoryMap.values()));
        return apiGroupInfo;
    }

    public ApiGroupInfo getApiGroup(String groupId){
        ApiGroupInfo apiGroupInfo = apiGroupDAO.getApiGroupInfo(groupId);
        return apiGroupInfo;
    }

    @Transactional(rollbackFor=Exception.class)
    public void updateApiRelationVersion(ApiGroupRelation relation,String groupId) throws AtlasBaseException {
        List<String> updateId = relation.getUpdateId();
        if (updateId!=null&&updateId.size()!=0){
            List<ApiInfoV2> apiInfoByIds = dataShareDAO.getNoDraftApiInfoByIds(updateId);
            apiGroupDAO.updateApiRelationVersion(apiInfoByIds,groupId);
        }
        List<String> ignoreId = relation.getIgnoreId();
        if (ignoreId!=null&&ignoreId.size()!=0){
            apiGroupDAO.unUpdateApiRelationVersion(ignoreId,groupId);
        }
        addApiGroupLogs(ApiGroupLogEnum.UPLEVEL, Lists.newArrayList(groupId),AdminUtils.getUserData().getUserId());
        String groupMobiusId = apiGroupDAO.getApiGroupMobiusId(groupId);
        List<String> apiMobiusId = apiGroupDAO.getGroupRelationMobiusId(groupId);
        updateMobiusApiRelation(groupMobiusId,apiMobiusId);

    }

    @Transactional(rollbackFor=Exception.class)
    public void updateAllApiRelationVersion(ApiGroupV2 apiGroupV2) throws AtlasBaseException {
        String groupId = apiGroupV2.getId();
        List<ApiCategory.Api> aPiByGroup = apiGroupDAO.getAPiByGroup(groupId);
        List<String> updateId = aPiByGroup.stream().map(api -> api.getApiId()).collect(Collectors.toList());
        if (updateId==null||updateId.size()==0){
            return;
        }
        if (apiGroupV2.isUpdateStatus()){
            List<ApiInfoV2> apiInfoByIds = dataShareDAO.getNoDraftApiInfoByIds(updateId);
            apiGroupDAO.updateApiRelationVersion(apiInfoByIds,groupId);
        }else{
            apiGroupDAO.unUpdateApiRelationVersion(updateId,groupId);
        }
        addApiGroupLogs(ApiGroupLogEnum.UPLEVEL, Lists.newArrayList(groupId),AdminUtils.getUserData().getUserId());
        String groupMobiusId = apiGroupDAO.getApiGroupMobiusId(groupId);
        List<String> apiMobiusId = apiGroupDAO.getGroupRelationMobiusId(groupId);
        updateMobiusApiRelation(groupMobiusId,apiMobiusId);
    }

    public void updatePublish(String groupId,boolean publish) throws AtlasBaseException {
        String status;
        if (publish){
            publish(groupId);
            status="release";
        }else{
            unPublish(groupId);
            status="draft";
        }
        apiGroupDAO.updatePublish(groupId,publish);
        String apiGroupMobiusId = apiGroupDAO.getApiGroupMobiusId(groupId);
        updateMobiusGroupStatus(apiGroupMobiusId,status);


    }

    /**
     * 发布
     * @param groupId
     */
    public void publish(String groupId) throws AtlasBaseException {
        ApiGroupInfo apiGroup = getApiGroup(groupId);
        if (apiGroup==null||apiGroup.isPublish()){
            return;
        }
        addApiGroupLogs(ApiGroupLogEnum.PUBLISH, Lists.newArrayList(groupId),AdminUtils.getUserData().getUserId());

    }

    /**
     * 撤销发布
     * @param groupId
     */
    public void unPublish(String groupId) throws AtlasBaseException {
        ApiGroupInfo apiGroup = getApiGroup(groupId);
        if (apiGroup==null){
            return;
        }
        addApiGroupLogs(ApiGroupLogEnum.UNPUBLISH, Lists.newArrayList(groupId),AdminUtils.getUserData().getUserId());
    }

    public void updateMobiusGroupStatus(String mobiusGroupId,String status){
        Map<String,Object> map = new HashMap<>();
        map.put("id",mobiusGroupId);
        map.put("status",status);
        int retries = 3;
        int retryCount = 0;
        String mobiusURL= DataServiceUtil.mobiusUrl + "/v3/open/capacity/status";
        String errorId = null;
        String errorReason = null;
        String proper = "0.0";
        Gson gson = new Gson();
        String jsonStr = gson.toJson(map);
        while(retryCount < retries) {
            String res = OKHttpClient.doPut(mobiusURL, jsonStr);
            LOG.info(res);
            if(Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    break;
                } else {
                    retryCount++;
                }
            } else {
                retryCount++;
            }
        }
        if(!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(),AtlasErrorCode.BAD_REQUEST, "更新云平台能力失败");
        }
    }

    public void deleteApiGroup(List<String> apiGroupIds) throws AtlasBaseException {
        if (apiGroupIds==null||apiGroupIds.size()==0){
            return;
        }
        for (String api:apiGroupIds){
            publish(api);
        }
        List<String> apiMobiusIdsByIds = apiGroupDAO.getApiGroupMobiusIdsByIds(apiGroupIds);
        apiGroupDAO.deleteRelationByGroupIds(apiGroupIds);
        apiGroupDAO.deleteApiGroup(apiGroupIds);
        addApiGroupLogs(ApiGroupLogEnum.UNPUBLISH, apiGroupIds,AdminUtils.getUserData().getUserId());
        for (String mobiusGroupId:apiMobiusIdsByIds){
            deleteMobiusGroup(mobiusGroupId);
        }

    }


    public void deleteMobiusGroup(String mobiusGroupId){
        Map<String,String> map = new HashMap<>();
        String ticket = AdminUtils.getSSOTicket();
        map.put("X-SSO-FullticketId",ticket);
        int retries = 3;
        int retryCount = 0;
        String mobiusURL= DataServiceUtil.mobiusUrl + "/v3/open/capacity";
        mobiusURL=mobiusURL+"?id="+mobiusGroupId;
        String errorId = null;
        String errorReason = null;
        String proper = "0.0";
        while(retryCount < retries) {
            String res = OKHttpClient.doDelete(mobiusURL, map);
            LOG.info(res);
            if(Objects.nonNull(res)) {
                Map response = convertMobiusResponse(res);
                errorId = String.valueOf(response.get("error-id"));
                errorReason = String.valueOf(response.get("reason"));
                if (proper.equals(errorId)) {
                    break;
                } else {
                    retryCount++;
                }
            } else {
                retryCount++;
            }
        }
        if(!proper.equals(errorId)) {
            StringBuffer detail = new StringBuffer();
            detail.append("云平台返回错误码:");
            detail.append(errorId);
            detail.append("错误信息:");
            detail.append(errorReason);
            throw new AtlasBaseException(detail.toString(),AtlasErrorCode.BAD_REQUEST, "云平台能力删除失败");
        }
    }

    public List<String> getApiGroupNames(List<String> groupIds){
        if (groupIds==null||groupIds.size()==0){
            return new ArrayList<>();
        }
        List<String> names = apiGroupDAO.getApiNames(groupIds);
        return names;
    }

    public PageResult<ApiGroupStatusApi> getUpdateApi(String apiGroupId,int limit,int offset){
        updateApiRelationStatus();
        PageResult<ApiGroupStatusApi> pageResult = new PageResult<>();
        List<ApiGroupStatusApi> list = new ArrayList<>();
        pageResult.setLists(list);
        List<ApiVersion> updateApi = apiGroupDAO.getUpdateApi(apiGroupId,limit,offset);
        if (updateApi==null||updateApi.size()==0){
            return pageResult;
        }
        pageResult.setCurrentSize(updateApi.size());
        pageResult.setTotalSize(updateApi.get(0).getCount());

        List<String> apiIds = updateApi.stream().map(apiVersion -> apiVersion.getApiId()).collect(Collectors.toList());
        List<ApiInfoV2> apiInfoByIds = dataShareDAO.getNoDraftApiInfoByIds(apiIds);
        List<String> updateApiIds = new ArrayList<>();
        Map<String,ApiInfoV2> apiMap = new HashMap<>();
        apiInfoByIds.forEach(api->{
            apiMap.put(api.getGuid(),api);
        });
        for (ApiVersion apiVersion:updateApi){
            ApiGroupStatusApi apiGroupStatusApi = new ApiGroupStatusApi();
            ApiInfoV2 apiInfoV2 = apiMap.get(apiVersion.getApiId());
            ApiVersion newApiVersion = new ApiVersion(apiInfoV2);
            if (apiVersion.getVersion().equals(apiInfoV2.getVersion())){
                updateApiIds.add(apiVersion.getApiId());
                continue;
            }
            apiGroupStatusApi.setOldApi(apiVersion);
            apiGroupStatusApi.setNewApi(newApiVersion);
            list.add(apiGroupStatusApi);
        }
        return pageResult;

    }

    public void addApiGroupLogs(ApiGroupLogEnum apiLogEnum, List<String> apiGroupIds, String userId){
        Timestamp timestamp = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
        List<ApiGroupLog> apiGroupLogs = apiGroupIds.stream().map(apiId -> {
            ApiGroupLog apiGroupLog = new ApiGroupLog();
            apiGroupLog.setGroupId(apiId);
            apiGroupLog.setType(apiLogEnum.getName());
            apiGroupLog.setCreator(userId);
            apiGroupLog.setDate(timestamp);
            return apiGroupLog;
        }).collect(Collectors.toList());
        apiGroupDAO.addApiLogs(apiGroupLogs);
    }

    public PageResult<ApiGroupLog> getApiGroupLog(Parameters param,String groupId) throws AtlasBaseException {
        PageResult<ApiGroupLog> pageResult = new PageResult<>();
        String type=null;
        if ("api".contains(param.getQuery())){
            param.setQuery("");
        }else{
            type = ApiLogEnum.getName(param.getQuery());
        }
        List<ApiGroupLog> apiLogs= apiGroupDAO.getApiLog(param, groupId,type);
        for (ApiGroupLog log:apiLogs){
            String str = String.format(ApiGroupLogEnum.getStr(log.getType()),log.getCreator());
            log.setStr(str);
        }
        pageResult.setLists(apiLogs);
        if (apiLogs==null||apiLogs.size()==0){
            return pageResult;
        }
        pageResult.setCurrentSize(apiLogs.size());
        pageResult.setTotalSize(apiLogs.get(0).getTotal());
        return pageResult;
    }

    public List<ApiCategory> getAllApi(String search,String projectId){
        List<ApiCategory.Api> aPiByGroup = apiGroupDAO.getAllApi(search,projectId);
        Map<String,ApiCategory> apiCategoryMap = new HashMap<>();
        aPiByGroup.forEach(api -> {
            String status = api.getStatus();
            if (ApiStatusEnum.UP.equals(status)){
                api.setApiStatus(true);
            }else{
                api.setApiStatus(false);
            }
            if (apiCategoryMap.containsKey(api.getCategoryId())){
                apiCategoryMap.get(api.getCategoryId()).getApi().add(api);
            }else{
                ApiCategory apiCategory = new ApiCategory(api);
                List<ApiCategory.Api> apis = new ArrayList<>();
                apis.add(api);
                apiCategory.setApi(apis);
                apiCategoryMap.put(api.getCategoryId(),apiCategory);
            }
        });
        return new ArrayList<>(apiCategoryMap.values());
    }

    /**
     * 可成为管理者用户
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<UserIdAndName> getApprove(String tenantId) throws AtlasBaseException {
        SecuritySearch securitySearch = new SecuritySearch();
        securitySearch.setTenantId(tenantId);
        PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(0, -1, securitySearch);
        List<UserIdAndName> users = new ArrayList<>();
        for (UserAndModule userAndModule:userAndModules.getLists()){
            UserIdAndName user = new UserIdAndName();
            user.setUserName(userAndModule.getUserName());
            user.setAccount(userAndModule.getEmail());
            user.setUserId(userDAO.getUserIdByName(userAndModule.getUserName()));
            users.add(user);
        }
        return users;
    }
}
