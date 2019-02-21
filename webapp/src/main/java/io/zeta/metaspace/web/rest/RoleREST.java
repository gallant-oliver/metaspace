package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.privilege.Privilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.service.PrivilegeService;
import io.zeta.metaspace.web.service.RoleService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.restlet.resource.Get;
import org.restlet.resource.Patch;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

@Path("role")
@Singleton
@Service
public class RoleREST {
    private static final Logger LOG = LoggerFactory.getLogger(RoleREST.class);
    @Autowired
    private RoleService roleService;
    @Autowired
    private PrivilegeService privilegeService;
    /**
     * 新增角色
     *
     * @return List<Database>
     */
    @POST
    @Path("")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String addRole(Role role) throws AtlasBaseException {
        try {
            return roleService.addRole(role);
        } catch (Exception e) {
            LOG.error("新增角色失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"新增角色失败");
        }
    }

    /**
     * 禁用角色&启用角色
     *
     * @return List<Database>
     */
    @Patch
    @Path("/{roleId}")
    public String updateRoleStatus(@PathParam("roleId")String roleId,@QueryParam("status")int status) throws AtlasBaseException {
        try {
            return roleService.updateRoleStatus(roleId,status);
        } catch (Exception e) {
            String s="";
            if(status==0)
                s="禁用角色失败";
            else
                s="启用角色失败";
            LOG.error(s, e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,s);
        }
    }

    /**
     * 删除角色
     *
     * @return List<Database>
     */
    @DELETE
    @Path("/{roleId}")
    public String deleteRole(@PathParam("roleId") String roleId) throws AtlasBaseException {
        try {
            return roleService.deleteRole(roleId);
        } catch (Exception e) {
            LOG.error("删除角色失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"删除角色失败");
        }
    }

    /**
     * 搜索成员
     *
     * @return List<Database>
     */
    @POST
    @Path("/{roleId}/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<User> getUsers(@PathParam("roleId") String roleId, Parameters parameters) throws AtlasBaseException {
        try {
            return roleService.getUsers(roleId, parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
        } catch (Exception e) {
            LOG.error("搜索成员失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"搜索成员失败");
        }
    }

    /**
     * 搜索角色
     *
     * @return List<Database>
     */
    @POST
    @Path("/roles")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Role> getRoles(Parameters parameters) throws AtlasBaseException {
        try {
            return roleService.getRoles(parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
        } catch (Exception e) {
            LOG.error("搜索角色失败失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"搜索角色失败失败");
        }
    }

    /**
     * 添加成员
     *
     * @return List<Database>
     */
    @POST
    @Path("/{roleId}/user")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String addUsers(@PathParam("roleId") String roleId,List<String> users) throws AtlasBaseException {
        try {
            return roleService.addUsers(roleId,users) ;
        } catch (Exception e) {
            LOG.error("添加成员失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"添加成员失败");
        }
    }

    /**
     * 移除成员
     *
     * @return List<Database>
     */
    @DELETE
    @Path("/{roleId}/user")
    public String removeUser(@PathParam("roleId") String roleId,List<String> users) throws AtlasBaseException {
        try {
            return roleService.removeUser(users);
        } catch (Exception e) {
            LOG.error("移除成员失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"移除成员失败");
        }
    }

    /**
     * 获取角色方案及授权范围(gai)
     *
     * @return List<Database>
     */
    @Get
    @Path("/{roleId}/privileges")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public RoleModulesCategories getPrivileges(@PathParam("roleId") String roleId) throws AtlasBaseException {
        try {
            return roleService.getPrivileges(roleId);
        } catch (Exception e) {
            LOG.error("获取角色方案及授权范围失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"获取角色方案及授权范围失败");
        }
    }
    /**
     * 修改角色方案及授权范围(gai)
     *
     * @return List<Database>
     */
    @Put
    @Path("/{roleId}/privileges")
    @Consumes(Servlets.JSON_MEDIA_TYPE)

    public String putPrivileges(@PathParam("roleId") String roleId,RoleModulesCategories roleModulesCategories) throws AtlasBaseException {
        try {
            return roleService.putPrivileges(roleId,roleModulesCategories);
        } catch (Exception e) {
            LOG.error("修改角色方案及授权范围失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"修改角色方案及授权范围失败");
        }
    }
    /**
     * 获取技术方案列表&搜索技术方案
     *
     * @return List<Database>
     */
    @POST
    @Path("/privileges")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Privilege> getPrivilege(Parameters parameters) throws AtlasBaseException {
        try {
            return privilegeService.getPrivilegeList(parameters.getQuery(),parameters.getLimit(),parameters.getOffset());
        } catch (Exception e) {
            LOG.error("搜索技术方案失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"搜索技术方案失败");
        }
    }
}
