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
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.service.DataShareGroupService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.MetaDataService;
import io.zeta.metaspace.web.service.SearchService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
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
    @Autowired
    private MetaDataService metaDataService;
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
    public Response insertAPIInfo(APIInfo info) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), info.getName());
        try {
            shareService.insertAPIInfo(info);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
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
    public boolean querySameName(APIInfo info) throws AtlasBaseException {
        try {
            String name = info.getName();
            return shareService.querySameName(name);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
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
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
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
    public Response updateAPIInfo(@PathParam("apiGuid") String guid, APIInfo info) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), info.getName());
        try {
            shareService.updateAPIInfo(guid, info);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
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
    public PageResult<APIInfoHeader> getAPIList(@PathParam("groupGuid")String guid, @PathParam("my")Integer my, @PathParam("publish")String publish, Parameters parameters) throws AtlasBaseException {
        try {
            return shareService.getAPIList(guid, my, publish, parameters);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
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
    public Response insertGroup(APIGroup group) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), group.getName());
        try {
            groupService.insertGroup(group);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
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
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
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
    public Response updateGroup(@PathParam("groupId") String guid, APIGroup group) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), group.getName());
        try {
            groupService.updateGroup(guid, group);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
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
    public List<APIGroup> updateGroup() throws AtlasBaseException {
        try {
            return groupService.getGroupList();
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
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
    public PageResult<Database> getDatabaseByQuery(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<Database> pageResult = searchService.getDatabasePageResultV2(parameters);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
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
    public PageResult<Database> getDatabaseAndTableByQuery(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<Database> pageResult = searchService.getDatabasePageResultV2(parameters);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * 获取库下所有表
     */
    /**
     * 根据库id返回表
     *
     * @return List<Database>
     */
    @POST
    @Path("/tables/{databaseId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TableInfo> getTableByDB(@PathParam("databaseId") String databaseId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<TableInfo> pageResult = searchService.getTableByDBWithQueryWithoutTmp(databaseId, parameters);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
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
    public List<Column> getTableColumns(@PathParam("tableGuid") String tableGuid) throws AtlasBaseException {
        try {
            return shareService.getTableColumnList(tableGuid);
        } catch (AtlasBaseException e) {
            throw e;
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
        } catch (AtlasBaseException e) {
            throw e;
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
        } catch (AtlasBaseException e) {
            throw e;
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
    public Response publish(List<String> apiGuidList) throws AtlasBaseException {
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

            shareService.publishAPI(apiGuidList);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw e;
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
    public Response unPublish(List<String> apiGuidList) throws AtlasBaseException {
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
            shareService.unpublishAPI(apiGuidList);
        } catch (AtlasBaseException e) {
            throw e;
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 测试API
     * @param randomName
     * @param parameter
     * @return
     * @throws Exception
     */
    @POST
    @Path("/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<LinkedHashMap> testAPI(@PathParam("randomName") String randomName, QueryParameter parameter) throws Exception {
        try {
            List<LinkedHashMap> result = shareService.testAPI(randomName, parameter);
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    @PUT
    @Path("/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void stopTestAPI(@PathParam("randomName") String randomName) throws Exception {
        try {
            shareService.cancelAPIThread(randomName);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    @GET
    @Path("/swagger/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public APIContent getSwagger(@PathParam("guid") String guid) throws Exception {
        try {
            List<String> list = new ArrayList<>();
            list.add(guid);
            APIContent content = shareService.generateAPIContent(list);
            return content;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API请求异常");
        }
    }

    @POST
    @Path("/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getUserList(Parameters parameters) throws Exception {
        try {
            return shareService.getUserList(parameters);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API请求异常");
        }
    }


    @PUT
    @Path("/manager/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateManager(@PathParam("apiGuid") String apiGuid, User user) throws Exception {
        try {
            shareService.updateManager(apiGuid, user);
            return Response.status(200).entity("success").build();
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API请求异常");
        }
    }

}
