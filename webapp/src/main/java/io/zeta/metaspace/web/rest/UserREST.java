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
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupListAndSearchResult;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.service.UserGroupService;
import io.zeta.metaspace.web.service.UsersService;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * @author lixiang03
 * @Data 2020/3/3 15:19
 */
@Path("users")
@Singleton
@Service
public class UserREST {
    private static final Logger LOG = LoggerFactory.getLogger(UserREST.class);
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserGroupService userGroupService;

    @GET
    @Path("{userId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserItems(@HeaderParam("tenantId")String tenantId, @PathParam("userId") String userId) throws AtlasBaseException {
        try {
            return ReturnUtil.success(TenantService.defaultTenant.equals(tenantId)? usersService.getUserInfoById(userId) : usersService.getUserInfoByIdV2(tenantId, userId));
        } catch (AtlasBaseException e){
            throw e;
        }catch (Exception e){
            LOG.warn("获取用户信息失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户信息失败");
        }
    }

    @GET
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserList(@HeaderParam ("tenantId")String tenantId, @QueryParam("query") String query, @DefaultValue("0")@QueryParam("offset") int offset, @DefaultValue ("10") @QueryParam("limit") int limit) throws AtlasBaseException {
        try {
            Parameters parameters = new Parameters();
            parameters.setQuery(query);
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            return ReturnUtil.success(usersService.getUserListV2(tenantId,parameters));
        } catch (AtlasBaseException e){
            LOG.warn("获取用户信息失败",e);
            throw e;
        }catch (Exception e){
            LOG.warn("获取用户信息失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"获取用户信息失败");
        }
    }

    @PUT
    @Path("{userId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result updateGroupByUser(@PathParam("userId") String userId, Map<String,List<String>> map,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            List<String> userGroups=map.get("userGroups");
            usersService.updateGroupByUser(userId,userGroups,tenantId);
            return ReturnUtil.success();
        }catch (Exception e){
            LOG.warn("获取用户信息失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户信息失败");
        }
    }
    /**
     * 一.用户组列表及搜索
     */

    @GET
    @Path("userGroups")
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

}
