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
 * @date 2019/3/4 9:55
 */
package io.zeta.metaspace.web.rest;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/4 9:55
 */

import io.zeta.metaspace.model.homepage.CategoryDBInfo;
import io.zeta.metaspace.model.homepage.DataDistribution;
import io.zeta.metaspace.model.homepage.RoleUseInfo;
import io.zeta.metaspace.model.homepage.TableUseInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.service.HomePageService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("home")
@Singleton
@Service
public class HomePageREST {

    @Autowired
    HomePageService homePageService;

    /**
     * 获取数据表使用次数与占比topN
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/rank")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TableUseInfo> getTableUsedInfo(Parameters parameters) throws AtlasBaseException {
        try {
            return homePageService.getTableRelatedInfo(parameters);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    /**
     * 获取系统角色用户数与占比topN
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/role/rank")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RoleUseInfo> getRoleUsedInfo(Parameters parameters) throws AtlasBaseException {
        try {
            return homePageService.getRoleRelatedInfo(parameters);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    /**
     * 获取角色列表
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/roles")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Role> getAllRole() throws AtlasBaseException {
        try {
            return homePageService.getAllRole();
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    /**
     * 获取系统角色的用户
     * @param roleId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/role/{roleId}/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<User> getUsersByRoleId(@PathParam("roleId") String roleId, Parameters parameters) throws AtlasBaseException {
        try {
            return homePageService.getUserListByRoleId(roleId, parameters);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    /**
     * 获取已补充/未补充技术信息的业务对象占比
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/business/supplement")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<DataDistribution> getDataDistribution() throws AtlasBaseException {
        try {
            return homePageService.getDataDistribution();
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    /**
     * 获取贴源层系统列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/source/system")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<CategoryDBInfo> getCategoryRelatedDB(Parameters parameters) throws AtlasBaseException {
        try {
            return homePageService.getCategoryRelatedDB(parameters);
        }  catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

}
