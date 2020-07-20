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

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ProjectHeader;
import io.zeta.metaspace.model.share.ProjectInfo;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupCategories;
import io.zeta.metaspace.model.usergroup.result.MemberListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupMemberSearch;
import io.zeta.metaspace.model.datasource.SourceAndPrivilege;
import io.zeta.metaspace.model.datasource.DataSourceIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupPrivileges;
import io.zeta.metaspace.web.service.UserGroupService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
 * @Data 2020/2/24 11:14
 */
@Path("userGroups")
@Singleton
@Service
public class UserGroupREST {
    private static final Logger LOG = LoggerFactory.getLogger(UserGroupREST.class);
    @Autowired
    UserGroupService userGroupService;
    /**
     * 一.用户组列表及搜索
     */

    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroupListAndSearch(
            @HeaderParam("tenantId") String tenantId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("10") @QueryParam("limit") int limit,
            @DefaultValue("createTime")@QueryParam("sortBy") String sortBy,
            @DefaultValue("desc") @QueryParam("order") String order,
            @QueryParam("search") String search) throws AtlasBaseException {
        try {
            LOG.info("获取用户组列表及搜索时，租户ID为:" + tenantId);
            PageResult<UserGroupListAndSearchResult> pageResult = userGroupService.getUserGroupListAndSearch(tenantId, offset, limit, sortBy, order, search);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            LOG.error("用户组列表及搜索失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"用户组列表及搜索失败，您的租户ID为:" + tenantId + ",请检查好是否配置正确");
        }
    }


    /**
     * 二.用户组详情
     */
    @GET
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroupByID(@PathParam("id") String id) throws AtlasBaseException {

        try {
            LOG.info("获取用户组详情时，用户组ID为:" + id);
            UserGroup pageResult = userGroupService.getUserGroupByID(id);
            return ReturnUtil.success(pageResult);
        }catch (Exception e) {
            LOG.error("获取用户组详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"获取用户组详情失败，您的用户组ID为:" + id + ",请检查好是否配置正确");
        }
    }


    /**
     * 三.新建用户组
     */

    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result addUserGroup(@HeaderParam("tenantId") String tenantId, UserGroup userGroup) throws AtlasBaseException {
        try {
            LOG.info("新建用户组时，您的租户ID为:" + tenantId + ",用户组名称为:" + userGroup.getName() + ",描述信息为：" + userGroup.getDescription());
            userGroupService.addUserGroup(tenantId, userGroup);
            return ReturnUtil.success();
        } catch (AtlasBaseException e) {
            LOG.error("新建用户组失败", e);
            throw e;
        } catch (Exception e) {
            LOG.error("新建用户组失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"新建用户组失败");
        }
    }

    /**
     * 四.删除用户组信息
     */
    @DELETE
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteUserGroupByID(@PathParam("id") String id) throws AtlasBaseException {
        try {
            LOG.info("删除用户组信息时，您的用户组ID为:" + id);
            userGroupService.deleteUserGroupByID(id);
            return ReturnUtil.success();
        }catch (Exception e) {
            LOG.error("删除用户组信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"删除用户组信息失败");
        }
    }


    /**
     * 五.用户组成员列表及搜索
     */

    @GET
    @Path("/{id}/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroupMemberListAndSearch(
            @PathParam("id") String id,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("10") @QueryParam("limit") int limit,
            @QueryParam("search") String search) throws AtlasBaseException {
        try {
            LOG.info("获取用户组成员列表及搜索");
            PageResult<MemberListAndSearchResult> pageResult = userGroupService.getUserGroupMemberListAndSearch(id,offset, limit, search);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            LOG.error("获取用户组成员列表及搜索失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"用户组成员列表获取失败");
        }
    }


    /**
     * 六.用户组添加成员列表及搜索
     */

    @GET
    @Path("/{id}/add/members")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroupMemberSearch(
            @HeaderParam("tenantId") String tenantId,
            @PathParam("id") String groupId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("-1") @QueryParam("limit") int limit,
            @QueryParam("search") String search) throws AtlasBaseException {
        try {
            LOG.info("获取用户组添加成员列表及搜索时，您的租户ID为:" + tenantId + ",用户组ID为:" + groupId);
            PageResult<UserGroupMemberSearch> pageResult = userGroupService.getUserGroupMemberSearch(tenantId, groupId, offset, limit, search);
            return ReturnUtil.success(pageResult);
        } catch (AtlasBaseException e) {
            LOG.error("获取用户组添加成员列表及搜索失败", e);
            throw e;
        } catch (Exception e) {
            LOG.error("获取用户组添加成员列表及搜索失败", e);
            throw new AtlasBaseException("获取用户组添加成员列表及搜索失败，您的租户ID为:" + tenantId + ",用户组ID为:" + groupId + ",请检查好是否配置正确"+e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"用户组成员列表获取失败");
        }
    }


    /**
     * 七.用户组添加成员
     */
    @POST
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result addUserByGroupId(
            @PathParam("id") String groupId,
            Map<String,List<String>> map) throws AtlasBaseException {

        try {
            List<String> userIds = map.get("userIds");
            LOG.info("用户组添加成员时，您的用户组ID为:" + groupId);
            userGroupService.addUserGroupByID(groupId, userIds);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("用户组添加成员失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"用户组添加成员失败");
        }
    }


    /**
     * 八.用户组移除成员
     */

    @DELETE
    @Path("/{id}/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteUserByGroupId(@PathParam("id") String groupId,Map<String,List<String>> map) throws AtlasBaseException {
        try {
            List<String> userIds = map.get("userIds");
            LOG.info("用户组移除成员时，您的用户组ID为:" + groupId);
            userGroupService.deleteUserByGroupId(groupId, userIds);
            return ReturnUtil.success();
        }catch (Exception e) {
            LOG.error("用户组移除成员失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"用户组移除成员失败");
        }
    }


    /**
     * 获取用户组方案及授权范围(gai)
     *
     * @return List<Database>
     */
    @GET
    @Path("/{userGroupId}/privileges")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getPrivileges(@PathParam("userGroupId") String userGroupId,@HeaderParam("tenantId")String tenant,@DefaultValue("false")@QueryParam("all") boolean all) throws AtlasBaseException {
        try {
            UserGroupCategories privileges = userGroupService.getPrivileges(userGroupId,tenant,all);
            return ReturnUtil.success(privileges);
        }
        catch(AtlasBaseException e){
            LOG.error("获取角色方案及授权范围失败", e);
            throw e;
        }
        catch (Exception e) {
            LOG.error("获取角色方案及授权范围失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"获取用户组授权范围失败");
        }
    }

    /**
     * 修改用户组方案及授权范围(gai)
     *
     * @return List<Database>
     */
    @PUT
    @Path("/{uesrGroupId}/privileges")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result putPrivileges(@PathParam("uesrGroupId") String uesrGroupId, UserGroupCategories userGroupCategories) throws AtlasBaseException {
        try {
            UserGroup userGroupByID = userGroupService.getUserGroupByID(uesrGroupId);
            HttpRequestContext.get().auditLog(ModuleEnum.USER.getAlias(), userGroupByID.getName());
            userGroupService.putPrivileges(uesrGroupId,userGroupCategories);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("修改角色方案及授权范围失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"修改用户组授权范围失败");
        }
    }

    /**
     * 修改用户组管理信息
     */

    @PUT
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result updateUserGroupInformation(@PathParam("id") String groupId, UserGroup userGroup) throws AtlasBaseException {
        try {

            userGroupService.updateUserGroupInformation(groupId, userGroup);
            return ReturnUtil.success();
        }catch (Exception e) {
            LOG.error("修改用户组管理信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"修改用户组管理信息失败");
        }
    }

    /**
     * 获取权限数据源
     * @param groupId
     * @param offset
     * @param limit
     * @param search
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{id}/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataSourceListAndSearch(
            @PathParam("id") String groupId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("10") @QueryParam("limit") int limit,
            @QueryParam("search") String search) throws AtlasBaseException {

        try {
            PageResult<SourceAndPrivilege> pageResult = userGroupService.getSourceBySearch(groupId, offset, limit, search);
            return ReturnUtil.success(pageResult);
        }catch (Exception e) {
            LOG.error("用户组数据源列表及搜索失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"用户组数据源列表及搜索失败");
        }
    }

    /**
     * 无权限数据源
     * @param tenantId
     * @param groupId
     * @param offset
     * @param limit
     * @param search
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{id}/add/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroupAddProjectsSearch(
            @HeaderParam("tenantId") String tenantId,
            @PathParam("id") String groupId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("10") @QueryParam("limit") int limit,
            @QueryParam("search") String search) throws AtlasBaseException {

        try {
            PageResult<DataSourceIdAndName> pageResult = userGroupService.getNoSourceBySearch(tenantId, groupId, offset, limit, search);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            LOG.error("获取用户组添加数据源列表及搜索失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"获取用户组添加数据源列表及搜索失败: ");
        }
    }

    /**
     * 添加数据源
     * @param groupId
     * @param privileges
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{id}/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result addDataSourceByGroupId(@PathParam("id") String groupId, UserGroupPrivileges privileges) throws AtlasBaseException {
        try {
            userGroupService.addDataSourceByGroupId(groupId, privileges);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("用户组添加数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"用户组添加数据源失败");
        }
    }

    /**
     * 修改数据源权限
     * @param groupId
     * @param privileges
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{id}/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result updateDataSourceByGroupId(@PathParam("id") String groupId, UserGroupPrivileges privileges) throws AtlasBaseException {

        try {
            userGroupService.updateDataSourceByGroupId(groupId, privileges);
            return ReturnUtil.success();
        }catch (Exception e) {
            LOG.error("用户组修改项目权限失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"用户组修改项目权限失败");
        }
    }

    /**删除数据源权限
     * @param groupId
     * @param sourceIds
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/{userGroupId}/datasource/delete")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteDataSourceByGroupId(@PathParam("userGroupId") String groupId, List<String> sourceIds) throws AtlasBaseException {
        try {
            userGroupService.deleteDataSourceByGroupId(groupId, sourceIds);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("用户组移除数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"用户组移除数据源失败");
        }
    }

    /**
     * 权限列表
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/privileges")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataSourcePrivileges() throws AtlasBaseException {
        try {
            List<Map<String, String>> pageResult = userGroupService.getDataSourcePrivileges();
            return ReturnUtil.success(pageResult);
        }catch (Exception e) {
            LOG.error("获取数据源权限列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"获取数据源权限列表失败");
        }
    }

    /**
     * 添加项目
     * @param groupId
     * @param projectIds
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{id}/project")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result addProjectByGroupId(@PathParam("id") String groupId, List<String> projectIds) throws AtlasBaseException {
        try {
            UserGroup userGroup = userGroupService.getUserGroupByID(groupId);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "添加用户组项目权限："+userGroup.getName());
            userGroupService.addProjectByGroupId(groupId, projectIds);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("用户组添加数据源失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"用户组添加数据源失败");
        }
    }

    /**
     * 获取项目
     * @param isPrivilege
     * @param groupId
     * @param search
     * @param limit
     * @param offset
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/project")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getProject(@QueryParam("isPrivilege")boolean isPrivilege,@QueryParam("groupId")String groupId,
                                @QueryParam("search")String search,
                                @DefaultValue("-1")@QueryParam("limit")int limit,
                                @DefaultValue("0")@QueryParam("offset")int offset,
                                @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {

        try {
            Parameters parameters = new Parameters();
            parameters.setOffset(offset);
            parameters.setLimit(limit);
            parameters.setQuery(search);
            PageResult<ProjectHeader> userGroups = userGroupService.getProject(isPrivilege, groupId, parameters, tenantId);
            return ReturnUtil.success(userGroups);
        }catch (Exception e) {
            LOG.error("用户组数据源列表及搜索失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"用户组数据源列表及搜索失败");
        }
    }

    /**
     * 删除项目权限
     * @param projects
     * @param userGroupId
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/{id}/project")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteProject(List<String> projects,@PathParam("id")String userGroupId) throws AtlasBaseException {
        try{
            UserGroup userGroup = userGroupService.getUserGroupByID(userGroupId);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "删除用户组项目权限："+userGroup.getName());
            userGroupService.deleteProject(projects,userGroupId);
            return ReturnUtil.success();
        }catch (AtlasBaseException e){
            LOG.error("删除权限用户组权限失败",e);
            throw e;
        }catch (Exception e){
            LOG.error("删除权限用户组权限失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e, "删除权限用户组权限失败");
        }

    }
}
