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

import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIGroup;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.web.service.DataShareGroupService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.SearchService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("datashare")
@Singleton
@Service
public class DataShareREST {

    @Autowired
    DataShareGroupService groupService;
    @Autowired
    DataShareService shareService;
    @Autowired
    private SearchService searchService;


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

    @POST
    @Path("/databases")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getDatabaseByQuery(Parameters parameters) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            PageResult<Database> pageResult = searchService.getDatabasePageResult(parameters);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据库id返回表
     *
     * @return List<Database>
     */
    @POST
    @Path("/tables/{databaseId}/{offset}/{limit}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Table> getTableByDB(@PathParam("databaseId") String databaseId, @PathParam("offset") long offset, @PathParam("limit") long limit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            PageResult<Table> pageResult = searchService.getTableByDB(databaseId, offset, limit);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
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
    public int updateStarStatus(@PathParam("apiGuid") String apiGuid, @PathParam("status") Integer status) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            return shareService.updateStarStatus(apiGuid, status);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @PUT
    @Path("/publish/{status}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public int updatePublishStatus(@PathParam("status") Integer status, List<String> apiGuidList) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            return shareService.updatePublishStatus(apiGuidList, status);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
}
