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
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.CategoryGroupAndUser;
import io.zeta.metaspace.model.result.CategoryPrivilegeV2;
import io.zeta.metaspace.model.result.CategoryUpdate;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.UpdateCategory;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupCategories;
import io.zeta.metaspace.model.usergroup.result.UserGroupListAndSearchResult;
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
            @QueryParam("query") String query) throws AtlasBaseException {
        try {
            LOG.info("获取用户组列表及搜索时，租户ID为:" + tenantId);
            PageResult<UserGroupListAndSearchResult> pageResult = userGroupService.getUserGroupListAndSearch(tenantId, offset, limit, sortBy, order, query);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            LOG.error("用户组列表及搜索失败", e);
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
            HttpRequestContext.get().auditLog(ModuleEnum.AUTHORIZATION.getAlias(), userGroupByID.getName());
            userGroupService.putPrivileges(uesrGroupId,userGroupCategories);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("修改角色方案及授权范围失败", e);
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
    public Result updatePrivileges(CategoryPrivilegeV2 category,@HeaderParam("tenantId")String tenantId,@PathParam("id")String id,@PathParam("type")int type) throws AtlasBaseException {
        try{
            UserGroup userGroup = userGroupService.getUserGroupByID(id);
            HttpRequestContext.get().auditLog(ModuleEnum.AUTHORIZATION.getAlias(), "更新用户组："+userGroup.getName()+"目录权限");
            userGroupService.updatePrivileges(category,id,type,tenantId);
            return ReturnUtil.success();
        }catch (Exception e){
            LOG.error("更新用户组目录权限失败",e);
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
                                    @DefaultValue("0")@QueryParam("offset")int offset,@QueryParam("guid")String guid,
                                    @QueryParam("read")boolean read,@QueryParam("editCategory")boolean editCategory,@QueryParam("editItem")boolean editItem) throws AtlasBaseException {
        try{
            CategoryPrivilegeV2 category = new CategoryPrivilegeV2();
            category.setGuid(guid);
            category.setRead(read);
            category.setEditCategory(editCategory);
            category.setEditItem(editItem);
            PageResult<CategoryUpdate> updateCategory = userGroupService.getUpdateCategory(category, id, type, tenantId, limit, offset);
            return ReturnUtil.success(updateCategory);
        }catch (Exception e){
            LOG.error("获取权限变更影响范围失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "获取权限变更影响范围失败");
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
            HttpRequestContext.get().auditLog(ModuleEnum.AUTHORIZATION.getAlias(), "新增用户组："+userGroup.getName()+"目录权限");

            userGroupService.addPrivileges(category,id,0,tenantId);
            return ReturnUtil.success();
        }catch (Exception e){
            LOG.error("分配用户组权限失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "分配用户组权限失败");
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
            privilegeCategory = isAdd?userGroupService.getNoPrivilegeCategory(id,type,tenantId,true):userGroupService.getPrivilegeCategory(id, type, tenantId,true);

            return ReturnUtil.success(privilegeCategory);
        }catch (Exception e){
            LOG.error("获取用户组目录权限列表失败",e);
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
            UserGroup userGroup = userGroupService.getUserGroupByID(id);
            HttpRequestContext.get().auditLog(ModuleEnum.AUTHORIZATION.getAlias(), "移除用户组："+userGroup.getName()+"目录权限");
            userGroupService.deleteCategoryPrivilege(ids,id,tenantId);
            return ReturnUtil.success();
        }catch (Exception e){
            LOG.error("移除用户组目录权限失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "移除用户组目录权限失败");
        }
    }
}
