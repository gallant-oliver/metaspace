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
import io.zeta.metaspace.model.homepage.*;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.service.HomePageService;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

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
    public PageResult<TableUseInfo> getTableUsedInfo(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return homePageService.getTableRelatedInfo(parameters,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据失败");
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
    public List<DataDistribution> getDataDistribution(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return homePageService.getDataDistribution(tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据失败");
        }
    }

    /**
     * 获取数据标准一级目录下的多有数量
     *
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/datastandard/count")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<CategoryPrivilege> getDataStandardCountList(@HeaderParam("tenantId") String tenantId,
                                                                  @QueryParam("limit") int limit,
                                                                  @QueryParam("offset") int offset) throws AtlasBaseException {
        try {
            return homePageService.getDataStandardCountList(tenantId, limit, offset);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取数据失败");
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
    public TimeDBTB getTimeDbTb(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        synchronized (HomePageREST.class) {
            return homePageService.getTimeDbTb(tenantId);
        }
    }

    /**
     * 获取数据库数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/database/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getDBTotals(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return homePageService.getDBTotals(tenantId);
    }

    /**
     * 获取数据表数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/table/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getTBTotals(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return homePageService.getTBTotals(tenantId);
    }

    /**
     * 获取业务对象数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/business/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getBusinessTotals(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return homePageService.getBusinessTotals(tenantId);
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
    public PageResult<CategoryDBInfo> getCategoryRelatedDB(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return homePageService.getCategoryRelatedDB(parameters,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据失败");
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
    public PageResult<CategoryDBInfo> getChildCategoryRelatedDB(@PathParam("systemId") String categoryGuid, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return homePageService.getChildCategoryRelatedDB(categoryGuid, parameters,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据失败");
        }
    }


    @GET
    @Path("/refreshcache")
    public Response refreshCache() throws AtlasBaseException {
        try {
            homePageService.refreshCache();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据失败");
        }
        return Response.status(200).entity("success").build();
    }

    @GET
    @Path("/getProjectInfo")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<HomeProjectInfo> getProjectInfo(@HeaderParam("tenantId") String tenantId,
                                                      @QueryParam("limit") long limit, @QueryParam("offset") long offset) {
        String userId = AdminUtils.getUserData().getUserId();
        return homePageService.getHomeProjectInfo(tenantId, userId, limit, offset);
    }

    @GET
    @Path("/getTaskInfo")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getTaskInfo(@HeaderParam("tenantId") String tenantId) {
        return homePageService.getTaskHomeInfo(tenantId);
    }
}
