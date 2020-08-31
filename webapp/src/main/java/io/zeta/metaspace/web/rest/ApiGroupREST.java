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

package io.zeta.metaspace.web.rest;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

import com.google.common.base.Joiner;
import io.zeta.metaspace.model.apigroup.ApiCategory;
import io.zeta.metaspace.model.apigroup.ApiGroupInfo;
import io.zeta.metaspace.model.apigroup.ApiGroupLog;
import io.zeta.metaspace.model.apigroup.ApiGroupRelation;
import io.zeta.metaspace.model.apigroup.ApiGroupStatusApi;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.apigroup.ApiGroupV2;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiLog;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.web.service.ApiGroupService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * @author lixiang03
 * @Data 2020/8/10 18:51
 */
@Path("apigroup")
@Singleton
@Service
public class ApiGroupREST {
    private static final Logger LOG = LoggerFactory.getLogger(ApiGroupREST.class);
    @Autowired
    ApiGroupService apiGroupService;

    /**
     * 创建api分组
     * @param apiGroup
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result insertApiGroup(ApiGroupV2 apiGroup, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), apiGroup.getName());
            apiGroupService.insertApiGroup(apiGroup,tenantId);
        }catch (AtlasBaseException e){
            LOG.error("新建api分组失败",e);
            throw e;
        }catch (Exception e){
            LOG.error("新建api分组失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "新建api分组失败");
        }
        return ReturnUtil.success();
    }

    /**
     * 更新api分组
     * @param apiGroup
     * @param groupId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("{groupId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateApiGroup(ApiGroupV2 apiGroup, @PathParam ("groupId")String groupId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), apiGroup.getName());
            apiGroup.setId(groupId);
            apiGroupService.updateApiGroup(apiGroup,tenantId);
        }catch (AtlasBaseException e){
            LOG.error("更新api分组失败",e);
            throw e;
        }catch (Exception e){
            LOG.error("更新api分组失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "新建api分组失败");
        }
        return ReturnUtil.success();
    }

    /**
     * 查询api分组
     * @param projectId
     * @param search
     * @param limit
     * @param offset
     * @param sortBy
     * @param order
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("{projectId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result searchApiGroup(@PathParam("projectId")String projectId, @QueryParam("search")String search,@QueryParam("publish")String publish,
                                 @DefaultValue("-1")@QueryParam("limit") int limit,@DefaultValue("0")@QueryParam("offset")int offset,
                                 @QueryParam("sortBy")String sortBy,@QueryParam("order")String order,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            Parameters parameters = new Parameters();
            parameters.setQuery(search);
            parameters.setSortby(sortBy);
            parameters.setOrder(order);
            parameters.setOffset(offset);
            parameters.setLimit(limit);
            Boolean bool=null;
            if (publish!=null&&publish.length()!=0){
                bool = Boolean.valueOf(publish);
            }
            PageResult<ApiGroupV2> result = apiGroupService.searchApiGroup(parameters, projectId, tenantId,bool);
            return ReturnUtil.success(result);
        }catch (Exception e){
            LOG.error("查询api分组失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "查询api分组失败");
        }

    }

    /**
     * 获取api分组详情
     * @param groupId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("info/{groupId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApiGroupInfo(@PathParam ("groupId")String groupId) throws AtlasBaseException {
        try {
            ApiGroupInfo apiGroupInfo = apiGroupService.getApiGroupInfo(groupId);
            return ReturnUtil.success(apiGroupInfo);
        }catch (Exception e){
            LOG.error("获取api分组详情失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "获取api分组详情失败");
        }
    }

    /**
     * 升级api
     * @param relation
     * @param groupId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("version/{groupId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateApiRelationVersion(ApiGroupRelation relation,@PathParam ("groupId")String groupId) throws AtlasBaseException {
        try {
            ApiGroupInfo apiGroup = apiGroupService.getApiGroup(groupId);
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "变更\""+apiGroup.getName()+"\"关联的api版本");
            apiGroupService.updateApiRelationVersion(relation,groupId);
            return ReturnUtil.success();
        }catch (Exception e){
            LOG.error("升级api失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "升级api失败");
        }
    }

    /**
     * 升级全部api
     * @param apiGroupV2
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("version/all")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateAllApiRelationVersion(ApiGroupV2 apiGroupV2) throws AtlasBaseException {
        try {
            ApiGroupInfo apiGroup = apiGroupService.getApiGroup(apiGroupV2.getId());
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "变更\""+apiGroup.getName()+"\"关联的api版本");
            apiGroupService.updateAllApiRelationVersion(apiGroupV2);
            return ReturnUtil.success();
        }catch (Exception e){
            LOG.error("升级api失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "升级api失败");
        }
    }

    /**
     * 变更api分组发布状态
     * @param apiGroupV2
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("status")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updatePublish(ApiGroupV2 apiGroupV2) throws AtlasBaseException {
        try {
            ApiGroupInfo apiGroup = apiGroupService.getApiGroup(apiGroupV2.getId());
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "将\""+apiGroup.getName()+"\"的发布状态变为："+apiGroupV2.isPublish());
            apiGroupService.updatePublish(apiGroupV2.getId(),apiGroupV2.isPublish());
            return ReturnUtil.success();
        }catch (Exception e){
            LOG.error("变更api分组状态",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "变更api分组状态");
        }
    }

    /**
     * 批量删除api分组
     * @param ids
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteApiGroup(List<String> ids) throws AtlasBaseException {
        try {
            List<String> names = apiGroupService.getApiGroupNames(ids);
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "批量删除apiGroup:[" + Joiner.on("、").join(names) + "]");
            apiGroupService.deleteApiGroup(ids);
            return ReturnUtil.success();
        }catch (Exception e){
            LOG.error("更新api分组失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "新建api分组失败");
        }
    }

    /**
     * 获取升级api列表
     * @param apiGroupId
     * @param limit
     * @param offset
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("status/{groupId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUpdateApi(@PathParam("groupId") String apiGroupId,@DefaultValue("-1")@QueryParam("limit") int limit,
                               @DefaultValue("0")@QueryParam("offset")int offset) throws AtlasBaseException {
        try {
            PageResult<ApiGroupStatusApi> updateApi = apiGroupService.getUpdateApi(apiGroupId, limit, offset);
            return ReturnUtil.success(updateApi);
        }catch (Exception e){
            LOG.error("获取升级api列表失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "获取升级api列表失败");
        }
    }

    /**
     * 获取api分组日志
     * @param groupId
     * @param offset
     * @param limit
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/logs/{groupId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApiLog(@PathParam("groupId")String groupId,
                            @DefaultValue("0")@QueryParam("offset")int offset,
                            @DefaultValue("-1")@QueryParam("limit") int limit) throws AtlasBaseException {
        try {
            Parameters param = new Parameters();
            param.setOffset(offset);
            param.setLimit(limit);
            PageResult<ApiGroupLog> pageResult = apiGroupService.getApiGroupLog(param, groupId);
            return ReturnUtil.success(pageResult);
        } catch (AtlasBaseException e){
            LOG.error("获取api分组日志失败",e);
            throw e;
        }  catch (Exception e) {
            LOG.error("获取api分组日志失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"获取api分组日志失败:"+e.getMessage());
        }
    }

    /**
     * 获取项目下全部api
     * @param search
     * @param projectId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/all/api/{projectId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getAllApi(@QueryParam("search")String search,@PathParam("projectId")String projectId) throws AtlasBaseException {
        try {
            List<ApiCategory> allApi = apiGroupService.getAllApi(search,projectId);
            return ReturnUtil.success(allApi);
        } catch (Exception e) {
            LOG.error("获取项目下全部api失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"获取项目下全部api失败:"+e.getMessage());
        }
    }

    /**
     * 获取审批人列表
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/approve")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApprove(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            List<UserIdAndName> approve = apiGroupService.getApprove(tenantId);
            return ReturnUtil.success(approve);
        } catch (Exception e) {
            LOG.error("获取审批人列表失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"获取审批人列表失败:"+e.getMessage());
        }
    }
}
