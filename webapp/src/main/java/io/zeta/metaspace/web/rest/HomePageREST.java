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


import io.zeta.metaspace.model.homepage.*;
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

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

@Path("home")
@Singleton
@Service
public class HomePageREST {

    @Autowired
    private HomePageService homePageService;

    /**
     * 获取数据表使用次数与占比topN
     *
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
     *
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
     *
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
     *
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
     *
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
     * 获取时间和总量
     *
     * @return List<Database>
     */
    @GET
    @Path("/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TimeDBTB getTimeDbTb() throws AtlasBaseException {
        return homePageService.getTimeDbTb();
    }

    /**
     * 获取数据库数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/database/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getDBTotals() throws AtlasBaseException {
        return homePageService.getDBTotals();
    }

    /**
     * 获取数据表数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/table/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getTBTotals() throws AtlasBaseException {
        return homePageService.getTBTotals();
    }

    /**
     * 获取业务对象数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/business/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getBusinessTotals() throws AtlasBaseException {
        return homePageService.getBusinessTotals();
    }

    /**
     * 测试生成某天统计
     *
     * @return List<Database>
     */
    @GET
    @Path("/test/product/{date}")
    public String testProduct(@PathParam("date") String date) throws AtlasBaseException {
        homePageService.testProduct(date);
        return "success";
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
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    /**
     * 获取贴源层子系统列表
     *
     * @param categoryGuid
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/source/system/{systemId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<CategoryDBInfo> getChildCategoryRelatedDB(@PathParam("systemId") String categoryGuid, Parameters parameters) throws AtlasBaseException {
        try {
            return homePageService.getChildCategoryRelatedDB(categoryGuid, parameters);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

}
