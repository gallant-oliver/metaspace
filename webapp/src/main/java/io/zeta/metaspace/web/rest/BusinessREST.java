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
 * @date 2019/2/13 10:09
 */
package io.zeta.metaspace.web.rest;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/13 10:09
 */

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.BusinessQueryParameter;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.MetaDataService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.core.Response;


@Singleton
@Service
@Path("")
public class BusinessREST {
    private static final Logger PERF_LOG = LoggerFactory.getLogger(BusinessREST.class);
    private static final int CATEGORY_TYPE = 1;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;

    @Autowired
    private BusinessService businessService;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    MetaDataService metadataService;

    /**
     * 添加业务对象
     * @param categoryId
     * @param business
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/businesses/category/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response addBusiness(@PathParam("categoryId") String categoryId, BusinessInfo business) throws AtlasBaseException {
        try {
            businessService.addBusiness(categoryId, business);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 更新业务对象信息
     * @param businessId
     * @param business
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/businesses/{businessId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateBusiness(@PathParam("businessId") String businessId, BusinessInfo business) throws AtlasBaseException {
        try {
            businessService.updateBusiness(businessId, business);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("/businesses/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<BusinessInfo> getBusinessList(@PathParam("categoryId") String categoryId, @QueryParam("limit") final int limit, @QueryParam("limit") final int offset) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByCategoryId(categoryId, limit, offset);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 搜索business列表(业务对象)
     * @param categoryId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/businesses/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessListWithCondition(@PathParam("categoryId") String categoryId, Parameters parameters) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByName(categoryId, parameters);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        }
    }

    /**
     * 业务对象详情
     * @param businessId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/businesses/{businessId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BusinessInfo getBusiness(@PathParam("businessId") String businessId) throws AtlasBaseException {
        try {
            return businessService.getBusinessInfo(businessId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取全部目录
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/businesses/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryEntityV2> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getCategories()");
            }
            return dataManageService.getAll(CATEGORY_TYPE);
        }  finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加目录
     * @param categoryInfo
     * @return
     * @throws Exception
     */
    @POST
    @Path("/businesses/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryEntityV2 createCategory(CategoryInfoV2 categoryInfo) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.createMetadataCategory()");
            }
            return dataManageService.createCategory(categoryInfo, CATEGORY_TYPE);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除目录
     * @param categoryGuid
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("/businesses/categories/{categoryGuid}")
    public Response deleteCategory(@PathParam("categoryGuid") String categoryGuid) throws Exception {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.deleteCategory(" + categoryGuid + ")");
            }
            dataManageService.deleteCategory(categoryGuid);
        }  catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 修改目录信息
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/businesses/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryEntityV2 updateCategory(@PathParam("categoryId") String categoryGuid,CategoryInfoV2 categoryInfo) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.CategoryEntity()");
            }
            categoryInfo.setGuid(categoryGuid);
            return dataManageService.updateCategory(categoryInfo);
        }  catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取全部二级目录
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/businessManage/departments")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryEntityV2> getAllDepartment() throws AtlasBaseException {
        try {
            return dataManageService.getAllDepartments(CATEGORY_TYPE);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 业务对象搜索(业务对象管理)
     * @param parameter
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/businessManage")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessListWithManage(BusinessQueryParameter parameter) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByCondition(parameter);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 更新技术
     * @param businessId
     * @param tableIdList
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/businessManage/{businessId}")
    public Response updateTechnicalInfo(@PathParam("businessId") String businessId, List<String> tableIdList) throws AtlasBaseException {
        try {
            businessService.addBusinessAndTableRelation(businessId, tableIdList);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
        }
    }
}
