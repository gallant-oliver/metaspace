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
import io.zeta.metaspace.model.result.PageResult;
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

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
            @QueryParam("search") String search) throws AtlasBaseException {
        try {
            LOG.info("获取用户组列表及搜索时，租户ID为:" + tenantId);
            PageResult<UserGroupListAndSearchResult> pageResult = userGroupService.getUserGroupListAndSearch(tenantId, offset, limit, sortBy, order, search);
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
            HttpRequestContext.get().auditLog(ModuleEnum.USER.getAlias(), userGroupByID.getName());
            userGroupService.putPrivileges(uesrGroupId,userGroupCategories);
            return ReturnUtil.success();
        } catch (Exception e) {
            LOG.error("修改角色方案及授权范围失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"修改用户组授权范围失败");
        }
    }
}
