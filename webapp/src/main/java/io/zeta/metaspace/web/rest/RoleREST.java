package io.zeta.metaspace.web.rest;


import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

import com.google.common.base.Joiner;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserWithRole;
import io.zeta.metaspace.web.service.PrivilegeService;
import io.zeta.metaspace.web.service.RoleService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;


@Path("role")
@Singleton
@Service
public class RoleREST {
    @Autowired
    private RoleService roleService;
    @Autowired
    private PrivilegeService privilegeService;


    @Context
    private HttpServletRequest request;

    /**
     * 新增角色
     *
     * @return List<Database>
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public String addRole(Role role) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.ROLE.getAlias(), role.getRoleName());
        try {
            return roleService.addRole(role);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "新增角色失败");
        }
    }

    /**
     * 禁用角色&启用角色
     *
     * @return List<Database>
     */
    @PUT
    @Path("/{roleId}/{status}")
    @OperateType(UPDATE)
    public String updateRoleStatus(@PathParam("roleId") String roleId, @PathParam("status") int status) throws AtlasBaseException {
        try {
            Role role = roleService.getRoleById(roleId);
            HttpRequestContext.get().auditLog(ModuleEnum.ROLE.getAlias(), role.getRoleName());
            return roleService.updateRoleStatus(roleId, status);
        } catch (Exception e) {
            String s = "";
            if (status == 0)
                s = "禁用角色失败";
            else
                s = "启用角色失败";
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, s);
        }
    }

    /**
     * 删除角色
     *
     * @return List<Database>
     */
    @DELETE
    @Path("/{roleId}")
    @OperateType(OperateTypeEnum.DELETE)
    public String deleteRole(@PathParam("roleId") String roleId) throws AtlasBaseException {
        try {
            Role role = roleService.getRoleById(roleId);
            HttpRequestContext.get().auditLog(ModuleEnum.ROLE.getAlias(), role.getRoleName());
            return roleService.deleteRole(roleId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除角色失败");
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
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "搜索成员失败");
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
            return roleService.getRoles(parameters.getQuery(), parameters.getOffset(), parameters.getLimit(), true);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "搜索角色失败");
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
    @OperateType(UPDATE)
    public String addUserList(@PathParam("roleId") String roleId,List<String> users) throws AtlasBaseException {
        try {
            Role role = roleService.getRoleById(roleId);
            HttpRequestContext.get().auditLog(ModuleEnum.USER.getAlias(), "角色:" + role.getRoleName() + ",添加用户:[" + Joiner.on("、").join(users)+"]");
            return roleService.addUsers(roleId,users) ;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加成员失败");
        }
    }

    /**
     * 移除成员
     *
     * @return List<Database>
     */
    @DELETE
    @Path("/{roleId}/user")
    @OperateType(UPDATE)
    public String removeUser(@PathParam("roleId") String roleId,List<String> users) throws AtlasBaseException {
        try {
            Role role = roleService.getRoleById(roleId);
            HttpRequestContext.get().auditLog(ModuleEnum.USER.getAlias(), "角色:" + role.getRoleName() + ",移除用户:[" + Joiner.on("、").join(users)+"]");
            return roleService.removeUser(roleId,users);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "移除成员失败");
        }
    }

    /**
     * 获取角色方案及授权范围(gai)
     *
     * @return List<Database>
     */
    @GET
    @Path("/{roleId}/privileges")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public RoleModulesCategories getPrivileges(@PathParam("roleId") String roleId) throws AtlasBaseException {
        try {
            return roleService.getPrivileges(roleId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取角色方案及授权范围失败");
        }
    }
    /**
     * 修改角色方案及授权范围(gai)
     *
     * @return List<Database>
     */
    @PUT
    @Path("/{roleId}/privileges")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public String putPrivileges(@PathParam("roleId") String roleId, RoleModulesCategories roleModulesCategories) throws AtlasBaseException {
        try {
            Role role = roleService.getRoleById(roleId);
            HttpRequestContext.get().auditLog(ModuleEnum.USER.getAlias(), role.getRoleName());
            return roleService.putPrivileges(roleId,roleModulesCategories);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "修改角色方案及授权范围失败");
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
    public PageResult<PrivilegeInfo> getPrivilege(Parameters parameters) throws AtlasBaseException {
        try {
            return privilegeService.getPrivilegeList(parameters);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "搜索技术方案失败");
        }
    }
    /**
     * 获取全部角色
     *
     * @return List<Database>
     */
    @POST
    @Path("/users/{roleId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<User> getAllUsers(Parameters parameters,@PathParam("roleId") String roleId) throws AtlasBaseException {
        try {
            return roleService.getAllUsers(parameters,roleId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取全部用户失败");
        }
    }

    /**
     * 编辑角色
     */
    @PUT
    @Path("/{roleId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public String editRole(Role role) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.USER.getAlias(), role.getRoleName());
            return roleService.editRole(role);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "编辑角色失败");
        }
    }

    @GET
    @Path("/roles/sso")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Role> getRoles(@QueryParam("query") @DefaultValue("")String query, @QueryParam("offset") @DefaultValue("0")Long offset, @QueryParam("limit") @DefaultValue("-1")Long limit) throws AtlasBaseException {
        try {
            return roleService.getRoles(query, offset, limit, false);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "搜索角色失败");
        }
    }

    /**
     * 添加成员
     *
     * @return List<Database>
     */
    @POST
    @Path("/users/sso")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Map addUsers(List<UserWithRole> userWithRole) throws AtlasBaseException {
        try {
            roleService.addRoleToUser(userWithRole);
            Map result = new HashMap();
            result.put("errorCode", "200");
            result.put("message", "Success");
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加成员失败");
        }
    }

    /**
     * 移除成员
     *
     * @return List<Database>
     */
    @PUT
    @Path("/users/sso")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Map removeUsers(List<UserWithRole> userWithRole) throws AtlasBaseException {
        try {
            roleService.removeUserRole(userWithRole);
            Map result = new HashMap();
            result.put("errorCode","200");
            result.put("message","Success");
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除成员失败");
        }
    }

    @GET
    @Path("/roles/sso/incr")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Map getIncrRoles(@QueryParam("startTime") String startTime) throws AtlasBaseException {
        try {
            Map result = new HashMap();
            List<Role> roleList =  roleService.getIncrRoles(startTime);
            result.put("data", roleList);
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String queryTime = df.format(System.currentTimeMillis());
            result.put("queryTime", queryTime);
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取角色失败");
        }
    }

    @POST
    @Path("/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void updateUserInfo() throws AtlasBaseException {
        try {
            roleService.updateUserInfo();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取角色失败");
        }
    }
}
