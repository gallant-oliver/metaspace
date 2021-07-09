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

import com.google.common.collect.Lists;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.CategoryGroupAndUser;
import io.zeta.metaspace.model.result.CategoryGroupPrivilege;
import io.zeta.metaspace.model.result.CategoryPrivilegeV2;
import io.zeta.metaspace.model.result.CategoryUpdate;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.UpdateCategory;
import io.zeta.metaspace.model.share.ProjectHeader;
import io.zeta.metaspace.model.usergroup.AuthMenuEnum;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            PageResult<UserGroupListAndSearchResult> pageResult = userGroupService.getUserGroupListAndSearch(tenantId, offset, limit, sortBy, order, search);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"用户组列表及搜索失败，您的租户ID为:" + tenantId + ",请检查好是否配置正确");
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
            UserGroup pageResult = userGroupService.getUserGroupByID(id);
            return ReturnUtil.success(pageResult);
        }catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"获取用户组详情失败，您的用户组ID为:" + id + ",请检查好是否配置正确");
        }
    }


    /**
     * 三.新建用户组
     */

    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result addUserGroup(@HeaderParam("tenantId") String tenantId, UserGroup userGroup) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "新建用户组："+userGroup.getName());
            userGroupService.addUserGroup(tenantId, userGroup);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"新建用户组失败");
        }
    }

    /**
     * 四.删除用户组信息
     */
    @DELETE
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteUserGroupByID(@PathParam("id") String id) throws AtlasBaseException {
        try {
            UserGroup userGroupByID = userGroupService.getUserGroupByID(id);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "删除用户组："+userGroupByID.getName());
            userGroupService.deleteUserGroupByID(id);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"删除用户组信息失败");
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
            @QueryParam("search") String search,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            PageResult<MemberListAndSearchResult> pageResult = userGroupService.getUserGroupMemberListAndSearch(id,offset, limit, search,tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"用户组成员列表获取失败");
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
            PageResult<UserGroupMemberSearch> pageResult = userGroupService.getUserGroupMemberSearch(tenantId, groupId, offset, limit, search);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
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
    @OperateType(UPDATE)
    public Result addUserByGroupId(
            @PathParam("id") String groupId,
            Map<String,List<String>> map) throws AtlasBaseException {
        UserGroup userGroupByID = userGroupService.getUserGroupByID(groupId);
        HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "用户组添加成员："+userGroupByID.getName());

        try {
            List<String> userIds = map.get("userIds");
            userGroupService.addUserGroupByID(groupId, userIds);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"用户组添加成员失败");
        }
    }


    /**
     * 八.用户组移除成员
     */

    @DELETE
    @Path("/{id}/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result deleteUserByGroupId(@PathParam("id") String groupId,Map<String,List<String>> map) throws AtlasBaseException {
        try {
            UserGroup userGroupByID = userGroupService.getUserGroupByID(groupId);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "用户组移除成员："+userGroupByID.getName());
            List<String> userIds = map.get("userIds");
            userGroupService.deleteUserByGroupId(groupId, userIds);
            return ReturnUtil.success();
        }catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"用户组移除成员失败");
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
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取用户组授权范围失败");
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
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), userGroupByID.getName());
            userGroupService.putPrivileges(uesrGroupId,userGroupCategories);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"修改用户组授权范围失败");
        }
    }

    /**
     * 修改用户组管理信息
     */

    @PUT
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateUserGroupInformation(@PathParam("id") String groupId, UserGroup userGroup,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "修改用户组信息"+userGroup.getName());

            userGroupService.updateUserGroupInformation(groupId, userGroup,tenantId);
            return ReturnUtil.success();
        }catch (AtlasBaseException e){
            throw e;
        }catch (Exception e) {
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
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"用户组数据源列表及搜索失败");
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
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取用户组添加数据源列表及搜索失败");
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
    @OperateType(UPDATE)
    public Result addDataSourceByGroupId(@PathParam("id") String groupId, UserGroupPrivileges privileges) throws AtlasBaseException {
        try {
            UserGroup userGroupByID = userGroupService.getUserGroupByID(groupId);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "用户组添加数据源权限："+userGroupByID.getName());
            userGroupService.addDataSourceByGroupId(groupId, privileges);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"用户组添加数据源失败");
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
    @OperateType(UPDATE)
    public Result updateDataSourceByGroupId(@PathParam("id") String groupId, UserGroupPrivileges privileges) throws AtlasBaseException {

        try {
            UserGroup userGroupByID = userGroupService.getUserGroupByID(groupId);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "用户组修改项目权限："+userGroupByID.getName());
            userGroupService.updateDataSourceByGroupId(groupId, privileges);
            return ReturnUtil.success();
        }catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"用户组修改项目权限失败");
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
    @OperateType(UPDATE)
    public Result deleteDataSourceByGroupId(@PathParam("userGroupId") String groupId, List<String> sourceIds) throws AtlasBaseException {
        try {
            UserGroup userGroupByID = userGroupService.getUserGroupByID(groupId);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "用户组移除数据源："+userGroupByID.getName());
            userGroupService.deleteDataSourceByGroupId(groupId, sourceIds);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"用户组移除数据源失败");
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
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据源权限列表失败");
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
    @OperateType(UPDATE)
    public Result addProjectByGroupId(@PathParam("id") String groupId, List<String> projectIds) throws AtlasBaseException {
        try {
            UserGroup userGroup = userGroupService.getUserGroupByID(groupId);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "添加用户组项目权限："+userGroup.getName());
            userGroupService.addProjectByGroupId(groupId, projectIds);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"用户组添加数据源失败");
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
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"用户组数据源列表及搜索失败");
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
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"删除权限用户组权限失败");
        }

    }

    /**
     * 更新用户组目录权限
     * @param category
     * @param tenantId
     * @param id
     * @param type
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{id}/{type}/update/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updatePrivileges(CategoryGroupPrivilege category, @HeaderParam("tenantId")String tenantId, @PathParam("id")String id, @PathParam("type")int type) throws AtlasBaseException {
        try{
            UserGroup userGroup = userGroupService.getUserGroupByID(id);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "更新用户组："+userGroup.getName()+"目录权限");
            CategoryPrivilegeV2 categoryPrivilegeV2 = new CategoryPrivilegeV2(category);
            List<CategoryPrivilegeV2> categoryPrivilegeV2s = userGroupService.updatePrivileges(Lists.newArrayList(categoryPrivilegeV2), id, type, tenantId, category.isChild());
            return ReturnUtil.success(categoryPrivilegeV2s);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"更新用户组目录权限失败");
        }
    }

    /**
     * 获取权限影响范围
     * @param tenantId
     * @param id
     * @param type
     * @param limit
     * @param offset
     * @param guid
     * @param read
     * @param editCategory
     * @param editItem
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{id}/{type}/update/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUpdateCategory(@HeaderParam("tenantId")String tenantId,@PathParam("id")String id,
                                    @PathParam("type")int type,@DefaultValue("-1")@QueryParam("limit")int limit,
                                    @DefaultValue("0")@QueryParam("offset")int offset,@QueryParam("guid")String guid,@DefaultValue("true")@QueryParam("child")boolean child,
                                    @QueryParam("read")boolean read,@QueryParam("editCategory")boolean editCategory,@QueryParam("editItem")boolean editItem) throws AtlasBaseException {
        try{
            CategoryPrivilegeV2 category = new CategoryPrivilegeV2();
            category.setGuid(guid);
            category.setRead(read);
            category.setEditCategory(editCategory);
            category.setEditItem(editItem);
            PageResult<CategoryUpdate> updateCategory = userGroupService.getUpdateCategory(category, id, type, tenantId, limit, offset,child);
            return ReturnUtil.success(updateCategory);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取权限变更影响范围失败");
        }
    }

    /**
     * 分配用户组权限
     * @param category
     * @param tenantId
     * @param id
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{id}/{type}/add/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result addPrivileges(UpdateCategory category, @HeaderParam("tenantId")String tenantId, @PathParam("id")String id) throws AtlasBaseException {
        try{
            UserGroup userGroup = userGroupService.getUserGroupByID(id);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "新增用户组："+userGroup.getName()+"目录权限");

            userGroupService.addPrivileges(category,id,tenantId);
            return ReturnUtil.success();
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"分配用户组权限失败");
        }

    }

    /**
     * 获取用户组目录权限列表
     * @param tenantId
     * @param id
     * @param type
     * @param isAdd
     * @param isAll
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{id}/privilege/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getPrivilegeCategory(@HeaderParam("tenantId")String tenantId,@PathParam("id")String id,@QueryParam("type") int type,
                                       @DefaultValue("false")@QueryParam("isAdd")boolean isAdd,
                                       @DefaultValue("false")@QueryParam("isAll")boolean isAll) throws AtlasBaseException {
        try{
            List<CategoryGroupAndUser> privilegeCategory=null;
            privilegeCategory = isAdd?userGroupService.getNoPrivilegeCategory(id,type,tenantId,false):userGroupService.getPrivilegeCategory(id, type, tenantId,false);

            return ReturnUtil.success(privilegeCategory);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取用户组目录权限列表失败");
        }
    }

    /**
     * 移除用户组目录权限
     * @param ids
     * @param tenantId
     * @param id
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/delete/category/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteCategoryPrivilege(List<String> ids,@HeaderParam("tenantId")String tenantId,@PathParam("id")String id) throws AtlasBaseException {
        try{
            UserGroup userGroup = userGroupService.getUserGroupByID(id);
            HttpRequestContext.get().auditLog(ModuleEnum.USERGROUP.getAlias(), "移除用户组："+userGroup.getName()+"目录权限");
            userGroupService.deleteCategoryPrivilege(ids,id,tenantId);
            return ReturnUtil.success();
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取用户组目录权限列表失败");
        }
    }

    /**
     * 获取用户组-配置权限-页签列表
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/auth/menus")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getPrivilegeCategory() throws AtlasBaseException {
        try{

            List<Integer> userGroupAuthMenus = Arrays.asList(MetaspaceConfig.getUserGroupAuthMenus());
            List<String> userGroupAuthMenuList = Arrays.stream(AuthMenuEnum.values()).filter(authMenuEnum -> MetaspaceConfig.getDataService() || !AuthMenuEnum.PROJECT_PERMISSIONS.equals(authMenuEnum))
                    .filter(authMenuEnum -> userGroupAuthMenus.contains(authMenuEnum.getNum())).map(AuthMenuEnum::getName).collect(Collectors.toList());

            return ReturnUtil.success(userGroupAuthMenuList);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取用户组目录权限列表失败");
        }
    }

}
