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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/3/26 16:10
 */
package io.zeta.metaspace.web.rest;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/26 16:10
 */

import com.google.common.base.Joiner;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.ColumnParameters;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.Queue;
import io.zeta.metaspace.model.share.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.web.service.*;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;

@Path("datashare")
@Singleton
@Service
public class DataShareREST {
    @Autowired
    private DataShareGroupService groupService;
    @Autowired
    private DataShareService shareService;
    @Autowired
    private SearchService searchService;
    @Context
    private HttpServletRequest httpServletRequest;

    /**
     * 创建API
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Response insertAPIInfo(APIInfo info,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), info.getName());
        try {
            shareService.insertAPIInfo(info,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"添加失败");
        }
        return Response.status(200).entity("success").build();
    }

    @POST
    @Path("/check/datatype")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response checkDataType(List<QueryParameter.Field> fields) throws AtlasBaseException {
        shareService.checkDataType(fields);
        return Response.status(200).entity("success").build();
    }

    /**
     * 同名校验
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/same")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean querySameName(APIInfo info,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            String name = info.getName();
            return shareService.querySameName(name,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"查询失败");
        }
    }

    /**
     * 删除API
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public Response deleteAPIINfo(@PathParam("apiGuid") String guid) throws AtlasBaseException {
        APIInfo apiInfo = shareService.getAPIInfo(guid);
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), apiInfo.getName());
        try {
            shareService.deleteAPIInfo(guid);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"删除失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 修改API信息
     *
     * @param guid
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response updateAPIInfo(@PathParam("apiGuid") String guid, APIInfo info,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), info.getName());
        try {
            shareService.updateAPIInfo(guid, info,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"添加失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 获取API详情
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public APIInfo getAPIInfo(@PathParam("apiGuid")String guid) throws AtlasBaseException {
        try {
            return shareService.getAPIInfo(guid);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"查询失败");
        }
    }

    /**
     * 查询API列表
     * @param guid
     * @param my
     * @param publish
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{groupGuid}/{my}/{publish}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<APIInfoHeader> getAPIList(@PathParam("groupGuid")String guid, @PathParam("my")Integer my, @PathParam("publish")String publish, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return shareService.getAPIList(guid, my, publish, parameters,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"查询失败");
        }
    }

    /**
     * 创建API分组
     *
     * @param group
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/group")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Response insertGroup(APIGroup group,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), group.getName());
        try {
            groupService.insertGroup(group,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"添加失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 删除API分组
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/group/{groupId}")
    @OperateType(DELETE)
    public Response deleteGroup(@PathParam("groupId") String guid) throws AtlasBaseException {
        String groupName = groupService.getGroupName(guid);
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), groupName);
        try {
            groupService.deleteGroup(guid);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"删除失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 更新API分组信息
     *
     * @param guid
     * @param group
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/group/{groupId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response updateGroup(@PathParam("groupId") String guid, APIGroup group,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), group.getName());
        try {
            groupService.updateGroup(guid, group,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"添加失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 获取API分组列表
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/groups")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<APIGroup> updateGroup(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return groupService.getGroupList(tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取API分组列表失败");
        }
    }

    /**
     * 获取库列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/databases")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getDatabaseByQuery(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            PageResult<Database> pageResult = searchService.getDatabasePageResultV2(parameters,tenantId);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取库列表失败");
        }
    }

    /**
     * 搜库
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/search/databases")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getDatabaseAndTableByQuery(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            PageResult<Database> pageResult = searchService.getDatabasePageResultV2(parameters,tenantId);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"搜索库列表失败");
        }
    }

    /**
     * 根据库id返回表
     *
     * @return List<Database>
     */
    @POST
    @Path("/tables/{databaseId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TableInfo> getTableByDB(@PathParam("databaseId") String databaseId, Parameters parameters,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            PageResult<TableInfo> pageResult = searchService.getTableByDBWithQueryWithoutTmp(databaseId, parameters,tenantId);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取库下表失败");
        }
    }

    /**
     * 获取表字段
     * @param tableGuid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/columns/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Column> getTableColumns(@PathParam("tableGuid") String tableGuid,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return shareService.getTableColumnList(tableGuid,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取表字段失败");
        }
    }

    /**
     * 收藏API
     * @param apiGuid
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/star/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response starAPI(@PathParam("apiGuid") String apiGuid) throws AtlasBaseException {
        try {
            shareService.starAPI(apiGuid);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"收藏API失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 取消收藏API
     * @param apiGuid
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/star/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response unStarAPI(@PathParam("apiGuid") String apiGuid) throws AtlasBaseException {
        try {
            shareService.unStarAPI(apiGuid);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"取消收藏API失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 发布API
     * @param apiGuidList
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/publish")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response publish(List<String> apiGuidList,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {

            List<String> apiNameList = new ArrayList<>();
            for (String apiGuid : apiGuidList) {
                APIInfo apiInfo = shareService.getAPIInfo(apiGuid);
                if(null != apiInfo) {
                    apiNameList.add(apiInfo.getName());
                } else {
                    apiNameList.add(apiGuid);
                }
            }
            HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), "批量发布API:[" + Joiner.on("、").join(apiNameList) + "]");

            shareService.publishAPI(apiGuidList,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"更新发布状态失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 撤销发布API
     * @param apiGuidList
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/publish")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response unPublish(List<String> apiGuidList,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            List<String> apiNameList = new ArrayList<>();
            for (String apiGuid : apiGuidList) {
                APIInfo apiInfo = shareService.getAPIInfo(apiGuid);
                if(null != apiInfo) {
                    apiNameList.add(apiInfo.getName());
                } else {
                    apiNameList.add(apiGuid);
                }
            }
            HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), "批量撤销发布API:[" + Joiner.on("、").join(apiNameList) + "]");
            shareService.unpublishAPI(apiGuidList,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"更新发布状态失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 测试Hive API
     * @param randomName
     * @param parameter
     * @return
     * @throws Exception
     */
    @POST
    @Path("/test/hive/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<LinkedHashMap> testAPI(@PathParam("randomName") String randomName, HiveQueryParameter parameter) throws Exception {
        try {
            List<LinkedHashMap> result = shareService.testAPI(randomName, parameter);
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"测试api失败");
        }
    }

    /**
     * 测试Oracle API
     * @param randomName
     * @param parameter
     * @return
     * @throws Exception
     */
    @POST
    @Path("/test/oracle/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<LinkedHashMap> testAPI(@PathParam("randomName") String randomName, RelationalQueryParameter parameter) throws Exception {
        try {
            List<LinkedHashMap> result = shareService.testAPI(randomName, parameter);
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"测试api失败");
        }
    }

    @PUT
    @Path("/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void stopTestAPI(@PathParam("randomName") String randomName) throws Exception {
        try {
            shareService.cancelAPIThread(randomName);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"停止测试api失败");
        }
    }

    @GET
    @Path("/swagger/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public APIContent getSwagger(@PathParam("guid") String guid,@HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            List<String> list = new ArrayList<>();
            list.add(guid);
            APIContent content = shareService.generateAPIContent(list,tenantId);
            return content;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"API请求异常");
        }
    }

    @POST
    @Path("/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getUserList(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            return shareService.getUserList(parameters,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取用户列表失败");
        }
    }


    @PUT
    @Path("/manager/{apiGuid}/{userId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateManager(@PathParam("apiGuid") String apiGuid, @PathParam("userId") String userId) throws Exception {
        try {
            shareService.updateManager(apiGuid, userId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"变更管理者失败");
        }
    }

    @POST
    @Path("/{type}/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getDataSourceList(Parameters parameters,@HeaderParam("tenantId")String tenantId,@PathParam("type")String type) throws AtlasBaseException {
        try {
            return shareService.getDataSourceList(parameters,type,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据源列表失败");
        }
    }

    @POST
    @Path("/oracle/{sourceId}/schemas")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Permission({ModuleEnum.TASKMANAGE})
    public PageResult getSchemaList(@PathParam("sourceId") String sourceId, ColumnParameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return shareService.getDataList(DataShareService.SEARCH_TYPE.SCHEMA, parameters,tenantId, sourceId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据源schema失败，请检查数据源配置或者确认查询schema存在");
        }
    }

    @POST
    @Path("/oracle/{sourceId}/{schemaName}/tables")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Permission({ModuleEnum.TASKMANAGE})
    public PageResult getTableList(@PathParam("sourceId") String sourceId, @PathParam("schemaName") String schemaName, ColumnParameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return shareService.getDataList(DataShareService.SEARCH_TYPE.TABLE, parameters,tenantId, sourceId, schemaName);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取schema下的表失败，请检查数据源配置或者确认相关表存在");
        }
    }

    @POST
    @Path("/oracle/{sourceId}/{schemaName}/{tableName}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Permission({ModuleEnum.TASKMANAGE})
    public PageResult getColumnList(@PathParam("sourceId") String sourceId, @PathParam("schemaName") String schemaName, @PathParam("tableName") String tableName, ColumnParameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return shareService.getDataList(DataShareService.SEARCH_TYPE.COLUMN, parameters,tenantId, sourceId, schemaName, tableName);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取对应表的列信息失败");
        }
    }

    @GET
    @Path("/pools")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getPools(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            List<Queue> pools = shareService.getPools(tenantId);
            return ReturnUtil.success(pools);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取资源池失败");
        }

    }

    /**
     * 新增项目
     * @param tenantId
     * @param projectInfo
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/project")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result insertProject(@HeaderParam("tenantId")String tenantId,ProjectInfo projectInfo) throws AtlasBaseException {
        try{
            HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "新增项目："+projectInfo.getName());
            shareService.insertProject(projectInfo,tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"新建项目失败");
        }

    }

    /**
     * 查询项目
     * @param tenantId
     * @param offset
     * @param limit
     * @param sort
     * @param order
     * @param search
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/project")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result searchProject(@HeaderParam("tenantId") String tenantId,
                                @DefaultValue("0") @QueryParam("offset") int offset,
                                @DefaultValue("10") @QueryParam("limit") int limit,
                                @DefaultValue("createTime")@QueryParam("sort") String sort,
                                @DefaultValue("desc") @QueryParam("order") String order,
                                @QueryParam("search") String search) throws AtlasBaseException {
        try{
            Parameters parameters = new Parameters();
            parameters.setOffset(offset);
            parameters.setLimit(limit);
            parameters.setQuery(search);
            parameters.setOrder(order);
            parameters.setSortby(sort);
            PageResult<ProjectInfo> projectInfoPageResult = shareService.searchProject(parameters, tenantId);
            return ReturnUtil.success(projectInfoPageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取项目列表失败");
        }
    }

    /**
     * 编辑项目
     * @param projectInfo
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/project")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateProject(ProjectInfo projectInfo,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try{
            HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "更新项目："+projectInfo.getName());
            shareService.updateProject(projectInfo,tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"编辑项目失败");
        }

    }

    /**
     * 新增权限用户组
     * @param userGroups
     * @param projectId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/project/{id}/userGroups")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result addUserGroups(List<String> userGroups,@PathParam("id")String projectId) throws AtlasBaseException {
        try{
            ProjectInfo projectInfo = shareService.getProjectInfoById(projectId);
            HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "新增项目用户组权限："+projectInfo.getName());
            shareService.addUserGroups(userGroups,projectId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"新增权限用户组失败");
        }

    }

    /**
     * 获取权限用户组列表
     * @param isPrivilege
     * @param projectId
     * @param search
     * @param limit
     * @param offset
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/project/userGroups")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroups(@QueryParam("isPrivilege")boolean isPrivilege,@QueryParam("projectId")String projectId,
                                @QueryParam("search")String search,
                                @DefaultValue("-1")@QueryParam("limit")int limit,
                                @DefaultValue("0")@QueryParam("offset")int offset,
                                @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try{
            Parameters parameters = new Parameters();
            parameters.setOffset(offset);
            parameters.setLimit(limit);
            parameters.setQuery(search);
            PageResult<UserGroupIdAndName> userGroups = shareService.getUserGroups(isPrivilege, projectId, parameters, tenantId);
            return ReturnUtil.success(userGroups);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取用户组列表失败");
        }

    }

    /**
     * 批量删除权限用户组
     * @param userGroups
     * @param projectId
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/project/{id}/userGroups")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public Result deleteUserGroups(List<String> userGroups,@PathParam("id")String projectId) throws AtlasBaseException {
        try{
            ProjectInfo projectInfo = shareService.getProjectInfoById(projectId);
            HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "移除项目用户组权限："+projectInfo.getName());
            shareService.deleteUserGroups(userGroups,projectId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"删除权限用户组失败");
        }

    }

    /**
     * 批量删除项目
     * @param projectIds
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/project")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public Result deleteProject(List<String> projectIds) throws AtlasBaseException {
        try{
            List<String> projectNames = shareService.getProjectInfoByIds(projectIds).stream().map(projectInfo -> projectInfo.getName()).collect(Collectors.toList());
            if (projectNames!=null){
                HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "删除项目:[" + Joiner.on("、").join(projectNames) + "]");
            }

            shareService.deleteProject(projectIds);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"删除项目失败");
        }
    }

    /**
     * 可成为管理者用户
     * @param tenantId
     * @return
     * @throws Exception
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/project/manager")
    public Result getManager(@HeaderParam("tenantId")String tenantId) throws Exception {
        try{
            List<UserIdAndName> managers = shareService.getManager(tenantId);
            return ReturnUtil.success(managers);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取变更管理员列表失败");
        }

    }
}
