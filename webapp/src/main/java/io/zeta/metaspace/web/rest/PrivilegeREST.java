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
 * @date 2019/2/19 11:27
 */
package io.zeta.metaspace.web.rest;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/19 11:27
 */

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.PrivilegeHeader;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.web.service.PrivilegeService;
import io.zeta.metaspace.web.service.UsersService;
import org.apache.atlas.exception.AtlasBaseException;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/privilege")
@Singleton
@Service
public class PrivilegeREST {

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private UsersService usersService;

    /**
     * 用户详情
     * @param userId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/users/{userId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public UserInfo UserInfo(@PathParam("userId") String userId) throws AtlasBaseException {
        try {
            return usersService.getUserInfoById(userId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取用户列表
     * @param parameterss
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<User> UserList(Parameters parameters) throws AtlasBaseException {
        try {
            return usersService.getUserList(parameters);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 新增权限方法
     * @param privilege
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response addPrivilege(PrivilegeHeader privilege) throws AtlasBaseException {
        try {
            privilegeService.addPrivilege(privilege);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取权限模块
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/modules")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Module> getModuleList() throws AtlasBaseException {
        try {
            return privilegeService.getAllModule();
        } catch (Exception e) {
            throw e;
        }
    }

    @DELETE
    @Path("/{privilegeId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public void deletePrivilege(@PathParam("privilegeId") String privilegeId) throws AtlasBaseException {
        try {
            privilegeService.delPrivilege(privilegeId);
        } catch (Exception e) {
            throw e;
        }
    }

    @PUT
    @Path("/{privilegeId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updatePrivilege(@PathParam("privilegeId") String privilegeId, PrivilegeInfo privilege) throws AtlasBaseException {
        try {
            privilegeService.updatePrivilege(privilegeId, privilege);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("/privileges")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<PrivilegeInfo> getPrivilegeList(@QueryParam("query")String query, @QueryParam("limit")int limit, @QueryParam("offset")int offset) throws AtlasBaseException {
        try {
            return privilegeService.getPrivilegeList(query, limit, offset);
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("/{privilegeId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PrivilegeInfo getPrivilegeInfo(@PathParam("privilegeId")String privilegeId) throws AtlasBaseException {
        try {
            return privilegeService.getPrivilegeInfo(privilegeId);
        } catch (Exception e) {
            throw e;
        }
    }
}
