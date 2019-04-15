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

import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIGroup;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.web.service.DataShareGroupService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.MetaDataService;
import io.zeta.metaspace.web.service.SearchService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response insertAPIInfo(APIInfo info) throws AtlasBaseException {
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
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response deleteAPIINfo(@PathParam("apiGuid")String guid) throws AtlasBaseException {
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
     * @param guid
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateAPIInfo(@PathParam("apiGuid")String guid, APIInfo info) throws AtlasBaseException {
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
     * @param group
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/group")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response insertGroup(APIGroup group) throws AtlasBaseException {
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
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/group/{groupId}")
    public Response deleteGroup(@PathParam("groupId")String guid) throws AtlasBaseException {
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
     * @param guid
     * @param group
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/group/{groupId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateGroup(@PathParam("groupId")String guid, APIGroup group) throws AtlasBaseException {
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
            PageResult<Database> pageResult = searchService.getActiveDatabase(parameters);
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
            PageResult<TableInfo> pageResult = searchService.getTableByDBWithQuery(databaseId, parameters);
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
            List<Column> columnList = metaDataService.getTableInfoById(tableGuid).getColumns();
            return columnList;
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * 收藏/取消收藏API
     * @param apiGuid
     * @param status
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/star/{apiGuid}/{status}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateStarStatus(@PathParam("apiGuid") String apiGuid, @PathParam("status") Integer status) throws AtlasBaseException {
        try {
            shareService.updateStarStatus(apiGuid, status);
        } catch (AtlasBaseException e) {
            throw e;
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 发布/撤销API
     * @param status
     * @param apiGuidList
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/publish/{status}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updatePublishStatus(@PathParam("status") Integer status, List<String> apiGuidList) throws AtlasBaseException {
        try {
            shareService.updatePublishStatus(apiGuidList, status);
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
    public List<Map> testAPI(@PathParam("randomName") String randomName, QueryParameter parameter) throws Exception {
        try {
            List<Map> result = shareService.testAPI(randomName, parameter);
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
    @Path("/data/{version}/{url}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Map> queryAPIData(@PathParam("url") String url) throws Exception {
        try {
            return shareService.queryAPIData(url, httpServletRequest);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API请求异常");
        }
    }

}
