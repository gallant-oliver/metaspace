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

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.enums.SubmitType;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForList;
import io.zeta.metaspace.model.sourceinfo.PublishRequest;
import io.zeta.metaspace.web.service.UserGroupService;
import io.zeta.metaspace.web.service.UsersService;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;


/**
 * @author wuqianhe
 * @Data 2020/7/19 15:15
 */
@Path("source/info")
@Singleton
@Service
public class SourceInfoREST {
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserGroupService userGroupService;

    @POST
    @Path("database")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result addDatabaseInfo(@HeaderParam("tenantId")String tenantId, @QueryParam("submitType") SubmitType submitType ,DatabaseInfo databaseInfo){
        return null;
    }

    @PUT
    @Path("publish")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result publishDatabaseInfo(@HeaderParam("tenantId")String tenantId, PublishRequest request){
        return null;
    }

    @PUT
    @Path("database")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result updateDatabaseInfo(@HeaderParam("tenantId")String tenantId, @QueryParam("submitType") SubmitType submitType, DatabaseInfo databaseInfo){
        return null;
    }

    @PUT
    @Path("revoke/{id}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result revokeSourceInfo(@HeaderParam("tenantId")String tenantId, @PathParam("id") String id){
        return null;
    }

    @DELETE
    @Path("database")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteDatabaseInfo(@HeaderParam("tenantId")String tenantId, PublishRequest request){
        return null;
    }

    @GET
    @Path("list")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DatabaseInfoForList> getSourceInfoList(@HeaderParam("tenantId")String tenantId,
                                                             @DefaultValue("0")@QueryParam("offset") int offset,
                                                             @DefaultValue ("10") @QueryParam("limit") int limit,
                                                             @QueryParam("name")String name,
                                                             @QueryParam("status")Status status){
        return null;
    }

    @GET
    @Path("{id}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getSourceInfoDetail(@HeaderParam("tenantId")String tenantId, @PathParam("id") String id){
        return null;
    }


}
