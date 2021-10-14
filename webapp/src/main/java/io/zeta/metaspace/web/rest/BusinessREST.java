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

import com.google.common.base.Joiner;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.business.*;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.share.*;
import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.service.*;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.*;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.IOUtils;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;


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
    private BusinessCatalogueService businessCatalogueService;
    @Autowired
    MetaDataService metadataService;
    @Autowired
    DataShareService shareService;
    @Context
    private HttpServletResponse response;
    @Context
    private HttpServletRequest request;

    private static final int TECHNICAL_CATEGORY_TYPE = 0;

    @Autowired
    private SearchService searchService;

    /**
     * 添加业务对象
     *
     * @param categoryId
     * @param business
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Response addBusiness(@PathParam("categoryId") String categoryId, BusinessInfo business, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), business.getName());
            businessService.addBusiness(categoryId, business, tenantId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加业务对象失败");
        }
    }

    /**
     * 更新业务对象信息
     *
     * @param businessId
     * @param business
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{businessId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response updateBusiness(@PathParam("businessId") String businessId, BusinessInfo business, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), business.getName());
            businessService.updateBusiness(businessId, business, tenantId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新业务对象信息失败");
        }
    }

    @POST
    @Path("/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessList(Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByName(parameters, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "搜索业务对象失败");
        }
    }

    /**
     * 业务对象列表
     *
     * @param categoryId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/relations/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessListWithCondition(@PathParam("categoryId") String categoryId, Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByCategoryId(categoryId, parameters, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
        }
    }

    /**
     * 业务对象详情
     *
     * @param businessId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BusinessInfo getBusiness(@PathParam("businessId") String businessId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessInfo(businessId, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
        }
    }

    /**
     * 业务对象关联技术信息详情
     *
     * @param businessId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}/technical")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TechnologyInfo getBusinessRelatedTables(@PathParam("businessId") String businessId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getRelatedTableList(businessId, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "业务对象关联技术信息详情失败");
        }
    }

    /**
     * 获取关联的API列表
     *
     * @param businessId
     * @param parameters
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{businessId}/datashare")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(@PathParam("businessId") String businessId, Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessTableRelatedAPI(businessId, parameters, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取关联API失败");
        }
    }

    /**
     * 业务对象api展示列表
     *
     * @param businessId
     * @param isNew
     * @param up
     * @param down
     * @param limit
     * @param offset
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}/dataservice")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getBusinessTableRelatedDataServiceAPI(@PathParam("businessId") String businessId, @DefaultValue("false") @QueryParam("new") boolean isNew,
                                                        @DefaultValue("true") @QueryParam("up") boolean up, @DefaultValue("true") @QueryParam("down") boolean down,
                                                        @DefaultValue("-1") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
                                                        @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            Parameters parameters = new Parameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            PageResult<ApiHead> pageResult = businessService.getBusinessTableRelatedDataServiceAPI(businessId, parameters, isNew, up, down, tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取api展示列表失败");
        }
    }

    /**
     * api详情
     *
     * @param guid
     * @param version
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/dataservice/{apiGuid}/{version}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataServiceAPIInfo(@PathParam("apiGuid") String guid, @PathParam("version") String version) throws AtlasBaseException {
        try {
            ApiInfoV2 apiInfo = shareService.getApiInfoByVersion(guid, version);
            return ReturnUtil.success(apiInfo);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取详情失败:");
        }
    }

    @GET
    @Path("/datashare/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public APIInfo getAPIInfo(@PathParam("apiGuid") String guid) throws AtlasBaseException {
        try {
            return shareService.getAPIInfo(guid);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询失败:");
        }
    }

    /**
     * 测试API
     *
     * @param randomName
     * @param parameter
     * @return
     * @throws Exception
     */
    @POST
    @Path("/datashare/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<LinkedHashMap> testAPI(@PathParam("randomName") String randomName, QueryParameter parameter) throws Exception {
        try {
            List<LinkedHashMap> result = shareService.testAPI(randomName, parameter);
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "测试API失败:");
        }
    }

    @PUT
    @Path("/datashare/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void stopTestAPI(@PathParam("randomName") String randomName) throws Exception {
        try {
            shareService.cancelAPIThread(randomName);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "停止测试失败:");
        }
    }

    /**
     * 更新技术
     *
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
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新技术失败:");
        }
    }


    /**
     * 获取全部目录
     *
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategorycateQueryResult> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort,@QueryParam("type") Integer type, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        if(null==type){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录类型不能为空");
        }
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getCategories()");
            }
            return  businessCatalogueService.getAllCategories(type, tenantId);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加目录
     *
     * @param bussinessCatalogueInput
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public CategoryPrivilege createCategory(BussinessCatalogueInput bussinessCatalogueInput, @HeaderParam("tenantId") String tenantId) throws Exception {
        HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), bussinessCatalogueInput.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessCatalogueREST.createCategory()");
            }
            return businessCatalogueService.createCategory(bussinessCatalogueInput,tenantId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 单个或批量删除目录
     *
     * @param categoryGuids
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @DELETE
    @Path("/categories/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteCategory(List<String> categoryGuids, @HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        CategoryDeleteReturn deleteReturn = null;
        int item = 0;
        int categorys = 0;
        try {
            for (String categoryGuid : categoryGuids) {
                Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
                if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                    perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.deleteCategory(" + categoryGuid + ")");
                }
                deleteReturn = businessCatalogueService.deleteCategory(categoryGuid, tenantId, CATEGORY_TYPE);
                item += deleteReturn.getItem();
                categorys += deleteReturn.getCategory();
            }
            //设置删除的条数
            deleteReturn.setItem(item);
            deleteReturn.setCategory(categorys);

            return ReturnUtil.success(deleteReturn);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常:"+e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 修改目录信息
     *
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @PUT
    @Path("/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String updateCategory(@PathParam("categoryId") String categoryGuid, BussinessCatalogueInput categoryInfo, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        int type=categoryInfo.getCategoryType();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.CategoryEntity()");
            }
            categoryInfo.setGuid(categoryGuid);
            return businessCatalogueService.updateCategory(categoryInfo, type, tenantId);
        } catch (MyBatisSystemException e) {
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
    public Object getTableInfoById(@PathParam("guid") String guid, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getTableInfoById(guid, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表详情失败");
        }
    }


    /**
     * 获取技术目录
     *
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/technical/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getAllCategory(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return dataManageService.getTechnicalCategory(tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取技术目录失败");
        }
    }

    /**
     * 业务对象查询表关联
     *
     * @param relationQuery
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/technical/table/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableNameFilter(relationQuery, TECHNICAL_CATEGORY_TYPE, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "业务对象查询表关联失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取技术目录关联表
     *
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/technical/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getCategoryRelation(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getCategoryRelation(" + categoryGuid + ")");
            }

            return dataManageService.getRelationsByCategoryGuidFilter(categoryGuid, relationQuery, tenantId);
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
    @OperateType(DELETE)
    public Response deleteBusiness(@PathParam("businessId") String businessId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        BusinessInfo businessInfo = businessService.getBusinessInfo(businessId, tenantId);
        HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), businessInfo.getName());
        try {
            businessService.deleteBusiness(businessId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除失败");
        }
    }

    /**
     * 获取业务对象关联表
     *
     * @param businessId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{businessId}/tables")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TableHeader> getBussinessRelatedTableList(@PathParam("businessId") String businessId, Parameters parameters) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getBussinessRelatedTableList()");
            }
            return businessService.getPermissionBusinessRelatedTableList(businessId, parameters);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取表字段列表
     *
     * @param tableGuid
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/{guid}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getTableColumnList(@PathParam("guid") String tableGuid, Parameters parameters, @DefaultValue("columnName") @QueryParam("sortAttribute") final String sortAttribute, @DefaultValue("asc") @QueryParam("sort") final String sort) throws AtlasBaseException {
        try {
            return businessService.getTableColumnList(tableGuid, parameters, sortAttribute, sort, false);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表字段列表失败");
        }
    }

    @PUT
    @Path("/table/{guid}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String editTableColumnDisplayName(@PathParam("guid") String tableGuid, Column column) throws AtlasBaseException {
        try {
            businessService.editSingleColumnDisplayName(tableGuid, column);
            return "success";
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "编辑字段别名失败");
        }
    }

    /**
     * 编辑表显示名称
     *
     * @param tableHeader
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String editTableDisplayName(TableHeader tableHeader) throws AtlasBaseException {
        try {
            businessService.editTableDisplayName(tableHeader);
            return "success";
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "编辑表显示名称失败");
        }
    }

    /*@POST
    @Path("/table/{guid}/columns/check")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<ColumnCheckMessage> checkColumnName(@PathParam("guid") String tableGuid, List<String> columns) throws AtlasBaseException {
        try {
            return businessService.checkColumnName(tableGuid, columns);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }*/

    /**
     * 导入字段中文别名Excel
     *
     * @param tableGuid
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/excel/import/{guid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ColumnCheckMessage checkColumnName(@PathParam("guid") String tableGuid, @FormDataParam("file") InputStream fileInputStream,
                                              @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws AtlasBaseException {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            file = ExportDataPathUtils.fileCheck(name, fileInputStream);
            return businessService.importColumnWithDisplayText(tableGuid, file);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 下载编辑字段中文别名模板
     *
     * @param tableGuid
     * @throws AtlasBaseException
     * @throws IOException
     * @throws SQLException
     */
    @GET
    @Path("/excel/{tableGuid}/template")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadExcelTemplate(@PathParam("tableGuid") String tableGuid) throws AtlasBaseException, IOException, SQLException {
        try {
            File xlsxFile = businessService.exportExcel(tableGuid);
            httpServletResponse.setContentType("application/msexcel;charset=utf-8");
            httpServletResponse.setCharacterEncoding("utf-8");
            String fileName = new String(new String(xlsxFile.getName()).getBytes(), "ISO-8859-1");
            // Content-disposition属性设置成以附件方式进行下载
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            OutputStream os = httpServletResponse.getOutputStream();
            os.write(FileUtils.readFileToByteArray(xlsxFile));
            os.close();
            xlsxFile.delete();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "下载报告失败");
        }
    }

    /**
     * 导出目录
     *
     * @param ids
     * @return
     * @throws Exception
     */
    @POST
    @Path("/export/selected")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDownloadURL(List<String> ids) throws Exception {
        String url = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace/businesses/export/selected";
        //全局导出
        if (ids == null || ids.size() == 0) {
            DownloadUri uri = new DownloadUri();
            String downURL = url + "/" + "all";
            uri.setDownloadUri(downURL);
            return ReturnUtil.success(uri);
        }
        DownloadUri downloadUri = ExportDataPathUtils.generateURL(url, ids);
        return ReturnUtil.success(downloadUri);
    }

    @GET
    @Path("/export/selected/{downloadId}")
    @Valid
    public void exportSelected(@PathParam("downloadId") String downloadId, @QueryParam("tenantId") String tenantId) throws Exception {
        Integer type=1;
        File exportExcel;
        //全局导出
        String all = "all";
        if (all.equals(downloadId)) {
            exportExcel = businessCatalogueService.exportExcelAll(type, tenantId);
        } else {
            List<String> ids = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
            exportExcel = businessCatalogueService.exportExcel(ids, type, tenantId);
        }
        try {
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        } finally {
            exportExcel.delete();
        }
    }

    public static String filename(String filePath) throws UnsupportedEncodingException {
        String filename = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1);
        filename = URLEncoder.encode(filename, "UTF-8");
        return filename;
    }


    /**
     * 上传文件并校验
     *
     * @param categoryId
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadCategory(@FormDataParam("categoryId") String categoryId,
                                 @FormDataParam("type") Integer type,
                                 @DefaultValue("false") @FormDataParam("all") boolean all, @FormDataParam("direction") String direction,
                                 @HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {

        if(null==type){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录类型不能为空");
        }
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESSCATALOGUE.getAlias(), name);
            file = ExportDataPathUtils.fileCheck(name, fileInputStream);
            String upload;
            if (all) {
                upload = businessCatalogueService.uploadAllCategory(file, type, tenantId);
            } else {
                upload = businessCatalogueService.uploadCategory(categoryId, direction, file, type, tenantId);
            }
            HashMap<String, String> map = new HashMap<String, String>() {{
                put("upload", upload);
            }};
            return ReturnUtil.success(map);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导入失败:"+e.getMessage());
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 根据文件导入目录
     *
     * @param path
     * @param importCategory
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/import/{path}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result importCategory(@PathParam("path") String path, ImportCategory importCategory, @HeaderParam("tenantId") String tenantId) throws Exception {
        File file = null;
        try {
            String categoryId = importCategory.getCategoryId();
            String name;
            if (importCategory.isAll()) {
                name = "全部";
            } else if (categoryId == null || categoryId.length() == 0) {
                name = "一级目录";
            } else {
                name = businessCatalogueService.getCategoryNameById(categoryId, tenantId);
            }

            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "导入目录:" + name + "," + importCategory.getDirection());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + path);
            List<CategoryPrivilege> categoryPrivileges = null;
            if (importCategory.isAll()) {
                businessCatalogueService.importAllCategory(file, importCategory.getType(), tenantId);
            } else {
                categoryPrivileges = businessCatalogueService.importCategory(categoryId, importCategory.getDirection(), file, importCategory.isAuthorized(), importCategory.getType(), tenantId);
            }
            return ReturnUtil.success(categoryPrivileges);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败:"+e.getMessage());
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 变更目录结构
     *
     * @param moveCategory
     * @throws Exception
     */
    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/place/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result moveCategory(MoveCategory moveCategory, @HeaderParam("tenantId") String tenantId) throws Exception {
        try {
            if (moveCategory.getGuid() == null) {
                HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "变更目录结构：all");
            } else {
                CategoryEntityV2 category = dataManageService.getCategory(moveCategory.getGuid(), tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "变更目录结构：" + category.getName());
            }
            dataManageService.moveCategories(moveCategory, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "变更目录结构失败");
        }
    }

    /**
     * 获取排序后的目录
     *
     * @param sort
     * @param order
     * @param guid
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/sort/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result sortCategory(@QueryParam("sort") String sort, @DefaultValue("asc") @QueryParam("order") String order,
                               @QueryParam("guid") String guid, @HeaderParam("tenantId") String tenantId) throws Exception {
        try {
            SortCategory sortCategory = new SortCategory();
            sortCategory.setSort(sort);
            sortCategory.setOrder(order);
            sortCategory.setGuid(guid);
            List<RoleModulesCategories.Category> categories = dataManageService.sortCategory(sortCategory, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success(categories);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "目录排序并变更结构失败");
        }
    }

    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/excel/category/template")
    @Valid
    public void downloadCategoryTemplate() throws Exception {
        String fileName = TemplateEnum.CATEGORY_TEMPLATE.getFileName();
        InputStream inputStream = PoiExcelUtils.getTemplateInputStream(TemplateEnum.CATEGORY_TEMPLATE);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }

    @Permission({ModuleEnum.BUSINESS, ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/excel/allcategory/template")
    @Valid
    public void downloadAllCategoryTemplate() throws Exception {
        TemplateEnum templateEnum=TemplateEnum.ALL_CATEGORY_TEMPLATE;
        String fileName = templateEnum.getFileName();
        InputStream inputStream = PoiExcelUtils.getTemplateInputStream(templateEnum);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }

    /**
     * 导出业务对象
     *
     * @param ids
     * @param categoryId
     * @return
     * @throws Exception
     */
    @POST
    @Path("file/export/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getBusinessDownloadURL(List<String> ids, @PathParam("categoryId") String categoryId) throws Exception {
        try {
            String url = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace/businesses/file/export/" + categoryId;
            //全局导出
            if (ids == null || ids.size() == 0) {
                DownloadUri uri = new DownloadUri();
                String downURL = url + "/" + "all";
                uri.setDownloadUri(downURL);
                return ReturnUtil.success(uri);
            }
            DownloadUri downloadUri = ExportDataPathUtils.generateURL(url, ids);
            return ReturnUtil.success(downloadUri);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导出业务对象失败");
        }

    }

    /**
     * 导出业务对象
     *
     * @param downloadId
     * @param categoryId
     * @param tenantId
     * @throws Exception
     */
    @GET
    @Path("file/export/{categoryId}/{downloadId}")
    @Valid
    public void exportBusiness(@PathParam("downloadId") String downloadId, @PathParam("categoryId") String categoryId, @QueryParam("tenantId") String tenantId) throws Exception {
        File exportExcel;
        //全局导出
        String all = "all";
        if (all.equals(downloadId)) {
            exportExcel = businessService.exportExcelBusiness(null, categoryId, tenantId);
        } else {
            List<String> ids = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
            exportExcel = businessService.exportExcelBusiness(ids, categoryId, tenantId);
        }
        try {
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导出业务对象失败");
        } finally {
            exportExcel.delete();
        }
    }

    /**
     * 上传文件并校验
     *
     * @param tenantId
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws Exception
     */
    @POST
    @Path("/file/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadBusiness(@HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            file = ExportDataPathUtils.fileCheck(name, fileInputStream);
            Map<String, Object> map = businessService.uploadBusiness(file, tenantId);
            return ReturnUtil.success(map);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "文件异常");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 根据文件导入业务对象
     *
     * @param path
     * @param importCategory
     * @param tenantId
     * @return
     * @throws Exception
     */
    @POST
    @Path("file/import/{path}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result importBusiness(@PathParam("path") String path, ImportCategory importCategory, @HeaderParam("tenantId") String tenantId) throws Exception {
        File file = null;
        try {
            String categoryId = importCategory.getCategoryId();
            CategoryEntityV2 category = dataManageService.getCategory(categoryId, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "批量导入业务对象：" + category.getName());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + path);
            businessService.importBusiness(file, categoryId, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 批量删除业务对象
     *
     * @param businessId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public Result deleteBusinesses(List<String> businessId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        List<String> names = businessService.getNamesByIds(businessId, tenantId);
        if (names != null || names.size() != 0) {
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "批量删除业务对象:[" + Joiner.on("、").join(names) + "]");
        }
        try {
            businessService.deleteBusinesses(businessId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "批量删除失败");
        }
    }

    /**
     * 获取业务对象模板
     *
     * @throws AtlasBaseException
     */
    @GET
    @Path("/excel/file/template")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadBusinessTemplate() throws AtlasBaseException {
        try {
            String fileName = TemplateEnum.BUSINESS_TEMPLATE.getFileName();
            InputStream inputStream = PoiExcelUtils.getTemplateInputStream(TemplateEnum.BUSINESS_TEMPLATE);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导出模板文件异常");
        }
    }

    @POST
    @Path("/category/place")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    @Valid
    public Result migrateCategory(MigrateCategory migrateCategory, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            CategoryEntityV2 category = dataManageService.getCategory(migrateCategory.getCategoryId(), tenantId);
            CategoryEntityV2 parentCategory = dataManageService.getCategory(migrateCategory.getParentId(), tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "迁移目录" + category.getName() + "到" + parentCategory.getName());
            dataManageService.migrateCategory(migrateCategory.getCategoryId(), migrateCategory.getParentId(), CATEGORY_TYPE, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "目录迁移失败");
        }
    }

    @POST
    @Path("/place")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    @Valid
    public Result moveBusiness(CategoryItem item, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            if (item.getIds() == null || item.getIds().size() == 0) {
                return ReturnUtil.success();
            }
            List<String> namesByIds = businessService.getNamesByIds(item.getIds(), tenantId);
            String path = CategoryRelationUtils.getPath(item.getCategoryId(), tenantId);
            if (namesByIds != null || namesByIds.size() != 0) {
                HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "迁移业务对象:[" + Joiner.on("、").join(namesByIds) + "]到" + path);
            }
            businessService.moveBusinesses(item);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "迁移业务对象失败");
        }
    }

    /**
     * 获取目录迁移可迁移到的目录
     *
     * @param categoryId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category/place/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getMigrateCategory(@PathParam("categoryId") String categoryId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            List<CategoryPrivilege> migrateCategory = dataManageService.getMigrateCategory(categoryId, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success(migrateCategory);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取可以迁移到目录失败");
        }
    }

    /**
     * 数据预览
     *
     * @return TableShow
     */
    @POST
    @Path("/table/preview")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableShow selectData(GuidCount guidCount) throws AtlasBaseException, SQLException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.selectData(" + guidCount.getGuid() + ", " + guidCount.getCount() + " )");
            }
            TableShow tableShow = searchService.getTableShow(guidCount, true);
            return tableShow;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 表空值检查
     *
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("description/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result checkTable(@HeaderParam("tenantId") String tenantId,
                             @DefaultValue("-1") @QueryParam("limit") int limit,
                             @DefaultValue("0") @QueryParam("offset") int offset) throws AtlasBaseException {
        try {
            PageResult<Table> pageResult = businessService.checkTable(tenantId, limit, offset);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "表描述空值检测失败");
        }
    }

    /**
     * 添加权限字段关联
     *
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("description/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result checkColumn(@HeaderParam("tenantId") String tenantId,
                              @DefaultValue("-1") @QueryParam("limit") int limit,
                              @DefaultValue("0") @QueryParam("offset") int offset) throws AtlasBaseException {
        try {
            PageResult<Table> pageResult = businessService.checkColumn(tenantId, limit, offset);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "列描述空值检测失败");
        }
    }

    /**
     * 业务对象表描述空值检查下载
     *
     * @param tenantId
     * @throws Exception
     */
    @GET
    @Path("/excel/description")
    @Valid
    public void exportCheck(@HeaderParam("tenantId") String tenantId) throws Exception {
        File exportExcel = null;
        try {
            exportExcel = businessService.checkData2File(tenantId);
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "业务对象表描述空值检查下载失败");
        } finally {
            if (exportExcel != null) {
                exportExcel.delete();
            }
        }
    }
}
