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
import io.zeta.metaspace.model.business.BusinessTableList;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.ColumnQuery;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.MetaDataService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.List;
import java.util.Map;
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
@Path("/businesses")
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
    @Autowired
    DataShareService shareService;

    private static final int TECHNICAL_CATEGORY_TYPE = 0;

    /**
     * 添加业务对象
     * @param categoryId
     * @param business
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/{categoryId}")
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
    @Path("/{businessId}")
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

    @POST
    @Path("/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessList(Parameters parameters) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByName(parameters);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 业务对象列表
     * @param categoryId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/relations/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessListWithCondition(@PathParam("categoryId") String categoryId, Parameters parameters) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByCategoryId(categoryId, parameters);
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
    @Path("/{businessId}")
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
     * 业务对象关联技术信息详情
     * @param businessId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}/technical")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TechnologyInfo getBusinessRelatedTables(@PathParam("businessId") String businessId) throws AtlasBaseException {
        try {
            return businessService.getRelatedTableList(businessId);
        } catch (Exception e) {
            throw e;
        }
    }

    @POST
    @Path("/{businessId}/datashare")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(@PathParam("businessId") String businessId,Parameters parameters) throws AtlasBaseException {
        try {
            return businessService.getBusinessTableRelatedAPI(businessId, parameters);
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("/datashare/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public APIInfo getAPIInfo(@PathParam("apiGuid")String guid) throws AtlasBaseException {
        try {
            return shareService.getAPIInfo(guid);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    /**
     * 测试API
     * @param randomName
     * @param parameter
     * @return
     * @throws Exception
     */
    @POST
    @Path("/datashare/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Map> testAPI(@PathParam("randomName") String randomName, QueryParameter parameter) throws Exception {
        try {
            List<Map> result = shareService.testAPI(randomName, parameter);
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    @PUT
    @Path("/datashare/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void stopTestAPI(@PathParam("randomName") String randomName) throws Exception {
        try {
            shareService.cancelAPIThread(randomName);
        } catch (AtlasBaseException e) {
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
    @Path("/{businessId}/technical")
    public Response updateTechnicalInfo(@PathParam("businessId") String businessId, BusinessTableList tableIdList) throws AtlasBaseException {
        try {
            businessService.addBusinessAndTableRelation(businessId, tableIdList);
            return Response.status(200).entity("success").build();
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
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort) throws AtlasBaseException {
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
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo) throws Exception {
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
    @Path("/categories/{categoryGuid}")
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
    @Path("/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String updateCategory(@PathParam("categoryId") String categoryGuid,CategoryInfoV2 categoryInfo) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.CategoryEntity()");
            }
            categoryInfo.setGuid(categoryGuid);
            return dataManageService.updateCategory(categoryInfo, CATEGORY_TYPE);
        }  catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 获取表详情
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table getTableInfoById(@PathParam("guid") String guid) throws AtlasBaseException {
        return metadataService.getTableInfoById(guid);
    }


    /**
     * 获取技术目录
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/technical/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getAllCategory() throws AtlasBaseException {
        try {
            return dataManageService.getAll(TECHNICAL_CATEGORY_TYPE);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 业务对象查询表关联
     * @param relationQuery
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/technical/table/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableNameFilter(relationQuery, TECHNICAL_CATEGORY_TYPE);
        } catch (Exception e) {
            throw e;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取技术目录关联表
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/technical/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getCategoryRelation(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getCategoryRelation(" + categoryGuid + ")");
            }
            return dataManageService.getRelationsByCategoryGuidFilter(categoryGuid, relationQuery);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @DELETE
    @Path("/{businessId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response deleteBusiness(@PathParam("businessId") String businessId) throws AtlasBaseException {
        try {
            businessService.deleteBusiness(businessId);
            return Response.status(200).entity("success").build();
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

}
