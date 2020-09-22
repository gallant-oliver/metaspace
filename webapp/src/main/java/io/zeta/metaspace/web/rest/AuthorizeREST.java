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

import com.google.common.base.Joiner;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.CategoryGroupAndUser;
import io.zeta.metaspace.model.result.CategoryGroupPrivilege;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.CategoryPrivilegeV2;
import io.zeta.metaspace.model.result.CategoryUpdate;
import io.zeta.metaspace.model.result.GroupPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.UpdateCategory;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupCategories;
import io.zeta.metaspace.model.usergroup.result.UserGroupListAndSearchResult;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.UserGroupService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
 * @Data 2020/3/27 17:21
 */
@Path("authorization")
@Singleton
@Service
public class AuthorizeREST {
    @Autowired
    UserGroupService userGroupService;

    @Autowired
    DataManageService dataManageService;
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
            @QueryParam("query") String query) throws AtlasBaseException {
        try {
            PageResult<UserGroupListAndSearchResult> pageResult = userGroupService.getUserGroupListAndSearch(tenantId, offset, limit, sortBy, order, query);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "用户组列表及搜索失败，您的租户ID为:" + tenantId + ",请检查好是否配置正确");
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
            UserGroupCategories privileges = userGroupService.getPrivileges(userGroupId, tenant, all);
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
            HttpRequestContext.get().auditLog(ModuleEnum.AUTHORIZATION.getAlias(), userGroupByID.getName());
            userGroupService.putPrivileges(uesrGroupId,userGroupCategories);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"修改用户组授权范围失败");
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
            HttpRequestContext.get().auditLog(ModuleEnum.AUTHORIZATION.getAlias(), "更新用户组："+userGroup.getName()+"目录权限");
            CategoryPrivilegeV2 categoryPrivilegeV2 = new CategoryPrivilegeV2(category);
            List<CategoryPrivilegeV2> categoryPrivilegeV2s = userGroupService.updatePrivileges(categoryPrivilegeV2, id, type, tenantId, category.isChild());
            return ReturnUtil.success(categoryPrivilegeV2s);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "更新用户组目录权限失败");
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
                                    @DefaultValue("0")@QueryParam("offset")int offset,@QueryParam("guid")String guid,@DefaultValue("false")@QueryParam("child")boolean child,
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
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "获取权限变更影响范围失败");
        }
    }

    /**
     * 分配权限
     * @param category
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{type}/add/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result addPrivileges(UpdateCategory category, @HeaderParam("tenantId")String tenantId,@PathParam("type") int type) throws AtlasBaseException {
        try{
            HttpRequestContext.get().auditLog(ModuleEnum.AUTHORIZATION.getAlias(), "分配权限");
            userGroupService.addUserGroupPrivilege(category,tenantId,type);
            return ReturnUtil.success();
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "分配用户组权限失败");
        }

    }

    /**
     * 获取用户组目录权限列表
     * @param tenantId
     * @param id

     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{id}/privilege/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getPrivilegeCategory(@HeaderParam("tenantId")String tenantId,@PathParam("id")String id,
                                       @DefaultValue("-1")@QueryParam("limit") int limit, @DefaultValue("0")@QueryParam("offset") int offset,
                                       @QueryParam("search") String search,@DefaultValue("false")@QueryParam("read")boolean read,
                                       @DefaultValue("false")@QueryParam("editCategory")boolean editCategory,@DefaultValue("false")@QueryParam("editItem")boolean editItem,
                                       @QueryParam("sortBy") String sortBy, @QueryParam("order") String order) throws AtlasBaseException {
        try{
            Parameters parameters = new Parameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            parameters.setSortby(sortBy);
            parameters.setOrder(order);
            parameters.setQuery(search);
            CategoryGroupPrivilege categoryGroupPrivilege = new CategoryGroupPrivilege();
            categoryGroupPrivilege.setRead(read);
            categoryGroupPrivilege.setEditCategory(editCategory);
            categoryGroupPrivilege.setEditItem(editItem);
            categoryGroupPrivilege.setGuid(id);
            PageResult<GroupPrivilege> userGroupByCategory = userGroupService.getUserGroupByCategory(categoryGroupPrivilege,parameters, tenantId);
            return ReturnUtil.success(userGroupByCategory);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "获取用户组目录权限列表失败");
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
            List<String> userGroupNames = userGroupService.getUserGroupByIDs(ids);
            CategoryEntityV2 category = dataManageService.getCategory(id, tenantId);
            if (userGroupNames!=null&&userGroupNames.size()!=0){
                HttpRequestContext.get().auditLog(ModuleEnum.AUTHORIZATION.getAlias(), "移除目录："+category.getName()+"的用户组："+Joiner.on("、").join(userGroupNames)+" 的权限");
            }
            userGroupService.deleteGroupPrivilege(ids,id,tenantId);
            return ReturnUtil.success();
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "移除用户组目录权限失败");
        }
    }

    /**
     * 获取用户组目录权限列表
     * @param tenantId
     * @param type
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getCategory(@HeaderParam("tenantId")String tenantId,@QueryParam("type") int type) throws AtlasBaseException {
        try{
            List<CategoryPrivilege> adminCategory = userGroupService.getAdminCategoryView(type, tenantId);
            return ReturnUtil.success(adminCategory);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "获取用户组目录权限列表失败");
        }
    }
}
