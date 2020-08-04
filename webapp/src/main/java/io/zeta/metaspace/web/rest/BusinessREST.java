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

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;

import com.google.common.base.Joiner;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.BusinessTableList;
import io.zeta.metaspace.model.business.ColumnCheckMessage;
import io.zeta.metaspace.model.business.ColumnPrivilegeRelation;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.CategoryItem;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.GuidCount;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableHeader;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.result.TableShow;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.CategoryRelationUtils;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.MetaDataService;
import io.zeta.metaspace.web.service.SearchService;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryDeleteReturn;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.ImportCategory;
import org.apache.atlas.model.metadata.MigrateCategory;
import org.apache.atlas.model.metadata.MoveCategory;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.model.metadata.SortCategory;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Singleton
@Service
@Path("/businesses")
public class BusinessREST {
    private static final Logger PERF_LOG = LoggerFactory.getLogger(BusinessREST.class);
    private static final int CATEGORY_TYPE = 1;
    private static final int MAX_EXCEL_FILE_SIZE = 10*1024*1024;
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
    @Context
    private HttpServletResponse response;
    @Context
    private HttpServletRequest request;

    private static final int TECHNICAL_CATEGORY_TYPE = 0;

    @Autowired
    private SearchService searchService;

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
    @OperateType(INSERT)
    public Response addBusiness(@PathParam("categoryId") String categoryId, BusinessInfo business,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), business.getName());
            businessService.addBusiness(categoryId, business,tenantId);
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
    @OperateType(UPDATE)
    public Response updateBusiness(@PathParam("businessId") String businessId, BusinessInfo business,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), business.getName());
            businessService.updateBusiness(businessId, business,tenantId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
        }
    }

    @POST
    @Path("/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessList(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByName(parameters,tenantId);
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
    public PageResult<BusinessInfoHeader> getBusinessListWithCondition(@PathParam("categoryId") String categoryId, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByCategoryId(categoryId, parameters,tenantId);
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
    public BusinessInfo getBusiness(@PathParam("businessId") String businessId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessInfo(businessId,tenantId);
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
    public TechnologyInfo getBusinessRelatedTables(@PathParam("businessId") String businessId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getRelatedTableList(businessId,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    @POST
    @Path("/{businessId}/datashare")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(@PathParam("businessId") String businessId,Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessTableRelatedAPI(businessId, parameters,tenantId);
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
    public List<LinkedHashMap> testAPI(@PathParam("randomName") String randomName, QueryParameter parameter) throws Exception {
        try {
            List<LinkedHashMap> result = shareService.testAPI(randomName, parameter);
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
    public List<CategoryPrivilege> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getCategories()");
            }
            return TenantService.defaultTenant.equals(tenantId)?dataManageService.getAll(CATEGORY_TYPE):dataManageService.getAllByUserGroup(CATEGORY_TYPE, tenantId);
        }  finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加目录
     *
     * @param categoryInfo
     * @return
     * @throws Exception
     */
    @POST
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo, @HeaderParam("tenantId")String tenantId) throws Exception {
        HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), categoryInfo.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.createMetadataCategory()");
            }
            return dataManageService.createCategory(categoryInfo, CATEGORY_TYPE,tenantId);
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
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteCategory(@PathParam("categoryGuid") String categoryGuid,@HeaderParam("tenantId")String tenantId) throws Exception {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.deleteCategory(" + categoryGuid + ")");
            }
            CategoryDeleteReturn deleteReturn = dataManageService.deleteCategory(categoryGuid, tenantId, CATEGORY_TYPE);
            return ReturnUtil.success(deleteReturn);
        }  catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
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
    public String updateCategory(@PathParam("categoryId") String categoryGuid,CategoryInfoV2 categoryInfo,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.CategoryEntity()");
            }
            categoryInfo.setGuid(categoryGuid);
            return dataManageService.updateCategory(categoryInfo, CATEGORY_TYPE,tenantId);
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
    public Table getTableInfoById(@PathParam("guid") String guid,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return businessService.getTableInfoById(guid,tenantId);
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
    public List<CategoryPrivilege> getAllCategory(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return TenantService.defaultTenant.equals(tenantId) ?dataManageService.getAll(TECHNICAL_CATEGORY_TYPE):dataManageService.getAllByUserGroup(TECHNICAL_CATEGORY_TYPE,tenantId);
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
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableNameFilter(relationQuery, TECHNICAL_CATEGORY_TYPE,tenantId);
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
    public PageResult<RelationEntityV2> getCategoryRelation(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getCategoryRelation(" + categoryGuid + ")");
            }

            return dataManageService.getRelationsByCategoryGuidFilter(categoryGuid, relationQuery,tenantId);
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
    public Response deleteBusiness(@PathParam("businessId") String businessId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        BusinessInfo businessInfo = businessService.getBusinessInfo(businessId,tenantId);
        HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), businessInfo.getName());
        try {
            businessService.deleteBusiness(businessId);
            return Response.status(200).entity("success").build();
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

    /**
     * 获取业务对象关联表
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
            return businessService.getTableColumnList(tableGuid, parameters, sortAttribute, sort);
        } catch (AtlasBaseException e) {
            throw e;
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
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * 编辑表显示名称
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
        } catch (AtlasBaseException e) {
            throw e;
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
            String name =URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            file = ExportDataPathUtils.fileCheck(name,fileInputStream);
            return businessService.importColumnWithDisplayText(tableGuid, file);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 下载编辑字段中文别名模板
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
            String fileName = new String( new String(xlsxFile.getName()).getBytes(), "ISO-8859-1");
            // Content-disposition属性设置成以附件方式进行下载
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            OutputStream os = httpServletResponse.getOutputStream();
            os.write(FileUtils.readFileToByteArray(xlsxFile));
            os.close();
            xlsxFile.delete();
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "下载报告失败");
        }
    }

    /**
     * 导出目录
     * @param ids
     * @return
     * @throws Exception
     */
    @POST
    @Path("/export/selected")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDownloadURL(List<String> ids) throws Exception {
        //全局导出
        if (ids==null||ids.size()==0){
            DownloadUri uri = new DownloadUri();
            String downURL = request.getRequestURL().toString() + "/" + "all";
            uri.setDownloadUri(downURL);
            return  ReturnUtil.success(uri);
        }
        DownloadUri downloadUri = ExportDataPathUtils.generateURL(request.getRequestURL().toString(), ids);
        return ReturnUtil.success(downloadUri);
    }

    @GET
    @Path("/export/selected/{downloadId}")
    @Valid
    public void exportSelected(@PathParam("downloadId") String downloadId,@QueryParam("tenantId")String tenantId) throws Exception {
        File exportExcel;
        //全局导出
        String all = "all";
        if (all.equals(downloadId)){
            exportExcel = dataManageService.exportExcelAll(CATEGORY_TYPE,tenantId);
        }else{
            List<String> ids = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
            exportExcel = dataManageService.exportExcel(ids, CATEGORY_TYPE,tenantId);
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
     * @param categoryId
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws Exception
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadCategory(@FormDataParam("categoryId") String categoryId,
                                 @DefaultValue("false")@FormDataParam("all") boolean all,@FormDataParam("direction")String direction,
                                 @HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(),  name);
            file = ExportDataPathUtils.fileCheck(name,fileInputStream);
            String upload;
            if (all){
                upload = dataManageService.uploadAllCategory(file,CATEGORY_TYPE,tenantId);
            }else{
                upload = dataManageService.uploadCategory(categoryId,direction,file,CATEGORY_TYPE,tenantId);
            }
            HashMap<String, String> map = new HashMap<String, String>() {{
                put("upload", upload);
            }};
            return ReturnUtil.success(map);
        } catch (AtlasBaseException e) {
            PERF_LOG.error("导入失败",e);
            throw e;
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 根据文件导入目录
     * @param upload
     * @param importCategory
     * @return
     * @throws Exception
     */
    @POST
    @Path("/import/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result importCategory(@PathParam("upload")String upload, ImportCategory importCategory, @HeaderParam("tenantId")String tenantId) throws Exception {
        File file = null;
        try {
            String categoryId = importCategory.getCategoryId();
            String name;
            if (importCategory.isAll()){
                name="全部";
            }else if (categoryId==null||categoryId.length()==0){
                name="一级目录";
            }else{
                name  = dataManageService.getCategoryNameById(categoryId,tenantId);
            }

            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(),  "导入目录:"+name+","+importCategory.getDirection());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            if (importCategory.isAll()){
                dataManageService.importAllCategory(file,CATEGORY_TYPE,tenantId);
            }else{
                dataManageService.importCategory(categoryId,importCategory.getDirection(), file,CATEGORY_TYPE,tenantId);
            }
            return ReturnUtil.success();
        } catch (AtlasBaseException e) {
            PERF_LOG.error("导入失败",e);
            throw e;
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }
    /**
     * 变更目录结构
     * @param moveCategory
     * @throws Exception
     */
    @POST
    @Path("/move/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result moveCategory(MoveCategory moveCategory, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            if(moveCategory.getGuid()==null){
                HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "变更目录结构：all");
            }else{
                CategoryEntityV2 category = dataManageService.getCategory(moveCategory.getGuid(), tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "变更目录结构："+category.getName());
            }
            dataManageService.moveCategories(moveCategory,CATEGORY_TYPE,tenantId);
            return ReturnUtil.success();
        }catch (AtlasBaseException e){
            PERF_LOG.error("变更目录结构失败",e);
            throw e;
        }catch (Exception e){
            PERF_LOG.error("变更目录结构失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"变更目录结构失败");
        }
    }

    /**
     * 获取排序后的目录
     * @param sort
     * @param order
     * @param guid
     * @return
     * @throws Exception
     */
    @GET
    @Path("/sort/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result sortCategory(@QueryParam("sort")String sort, @DefaultValue("asc")@QueryParam("order")String order,
                               @QueryParam("guid")String guid,@HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            SortCategory sortCategory = new SortCategory();
            sortCategory.setSort(sort);
            sortCategory.setOrder(order);
            sortCategory.setGuid(guid);
            List<RoleModulesCategories.Category> categories = dataManageService.sortCategory(sortCategory, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success(categories);
        }catch (Exception e){
            PERF_LOG.error("目录排序并变更结构失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"目录排序并变更结构失败");
        }
    }

    @GET
    @Path("/download/category/template")
    @Valid
    public void downloadCategoryTemplate() throws Exception {
        String homeDir = System.getProperty("atlas.home");
        String filePath = homeDir + "/conf/category_template.xlsx";
        String fileName = filename(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }

    /**
     * 导出业务对象
     * @param ids
     * @param categoryId
     * @return
     * @throws Exception
     */
    @POST
    @Path("file/export/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getBusinessDownloadURL(List<String> ids,@PathParam("categoryId")String categoryId) throws Exception {
        try{
            //全局导出
            if (ids==null||ids.size()==0){
                DownloadUri uri = new DownloadUri();
                String downURL = request.getRequestURL().toString() + "/" + "all";
                uri.setDownloadUri(downURL);
                return  ReturnUtil.success(uri);
            }
            DownloadUri downloadUri = ExportDataPathUtils.generateURL(request.getRequestURL().toString(), ids);
            return ReturnUtil.success(downloadUri);
        }catch (Exception e){
            PERF_LOG.error("导出业务对象失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"导出业务对象失败");
        }

    }

    /**
     * 导出业务对象
     * @param downloadId
     * @param categoryId
     * @param tenantId
     * @throws Exception
     */
    @GET
    @Path("file/export/{categoryId}/{downloadId}")
    @Valid
    public void exportBusiness(@PathParam("downloadId") String downloadId,@PathParam("categoryId") String categoryId,@QueryParam("tenantId") String tenantId) throws Exception {
        File exportExcel;
        //全局导出
        String all = "all";
        if (all.equals(downloadId)){
            exportExcel = businessService.exportExcelBusiness(null,categoryId,tenantId);
        }else{
            List<String> ids = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
            exportExcel = businessService.exportExcelBusiness(ids, categoryId,tenantId);
        }
        try {
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        }catch(Exception e){
            PERF_LOG.error("导出业务对象失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"导出业务对象失败");
        } finally {
            exportExcel.delete();
        }
    }

    /**
     * 上传文件并校验
     * @param tenantId
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws Exception
     */
    @POST
    @Path("/file/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadBusiness(@HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            file = ExportDataPathUtils.fileCheck(name,fileInputStream);
            Map<String,Object> map = businessService.uploadBusiness(file, tenantId);
            return ReturnUtil.success(map);
        } catch (AtlasBaseException e) {
            PERF_LOG.error("文件异常",e);
            throw e;
        }catch(Exception e){
            PERF_LOG.error("文件异常",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"文件异常");
        }  finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 根据文件导入业务对象
     * @param upload
     * @param importCategory
     * @param tenantId
     * @return
     * @throws Exception
     */
    @POST
    @Path("file/import/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result importBusiness(@PathParam("upload")String upload, ImportCategory importCategory, @HeaderParam("tenantId")String tenantId) throws Exception {
        File file = null;
        try {
            String categoryId = importCategory.getCategoryId();
            CategoryEntityV2 category = dataManageService.getCategory(categoryId, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(),  "批量导入业务对象："+category.getName());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            businessService.importBusiness(file,categoryId,tenantId);
            return ReturnUtil.success();
        } catch (AtlasBaseException e) {
            PERF_LOG.error("导入失败",e);
            throw e;
        }catch(Exception e){
            PERF_LOG.error("导入失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"导入失败");
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 批量删除业务对象
     * @param businessId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public Result deleteBusinesses(List<String> businessId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        List<String> names = businessService.getNamesByIds(businessId,tenantId);
        if (names!=null||names.size()!=0){
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "批量删除业务对象:[" + Joiner.on("、").join(names) + "]");
        }
        try {
            businessService.deleteBusinesses(businessId);
            return ReturnUtil.success();
        } catch (AtlasBaseException e) {
            PERF_LOG.error("批量删除失败",e);
            throw e;
        } catch (Exception e) {
            PERF_LOG.error("批量删除失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"批量删除失败");
        }
    }

    /**
     * 获取业务对象模板
     * @throws AtlasBaseException
     */
    @GET
    @Path("/download/file/template")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadBusinessTemplate() throws AtlasBaseException {
        try {
            String homeDir = System.getProperty("atlas.home");
            String filePath = homeDir + "/conf/business_template.xlsx";
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        }catch (Exception e){
            PERF_LOG.error("导出模板文件异常",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"导出模板文件异常");
        }
    }

    @POST
    @Path("/category/move")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    @Valid
    public Result migrateCategory(MigrateCategory migrateCategory, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            CategoryEntityV2 category = dataManageService.getCategory(migrateCategory.getCategoryId(), tenantId);
            CategoryEntityV2 parentCategory = dataManageService.getCategory(migrateCategory.getParentId(), tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "迁移目录" + category.getName() + "到" + parentCategory.getName());
            dataManageService.migrateCategory(migrateCategory.getCategoryId(), migrateCategory.getParentId(),CATEGORY_TYPE, tenantId);
            return ReturnUtil.success();
        } catch (AtlasBaseException e) {
            PERF_LOG.error("目录迁移失败", e);
            throw e;
        } catch (Exception e) {
            PERF_LOG.error("目录迁移失败", e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e, "目录迁移失败");
        }
    }

    @POST
    @Path("/move")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    @Valid
    public Result moveBusiness(CategoryItem item, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            if (item.getIds()==null||item.getIds().size()==0){
                return ReturnUtil.success();
            }
            List<String> namesByIds = businessService.getNamesByIds(item.getIds(),tenantId);
            String path = CategoryRelationUtils.getPath(item.getCategoryId(), tenantId);
            if(namesByIds!=null||namesByIds.size()!=0){
                HttpRequestContext.get().auditLog(ModuleEnum.BUSINESS.getAlias(), "迁移业务对象:[" + Joiner.on("、").join(namesByIds) + "]到"+path);
            }
            businessService.moveBusinesses(item);
            return ReturnUtil.success();
        } catch (AtlasBaseException e) {
            PERF_LOG.error("迁移业务对象失败", e);
            throw e;
        } catch (Exception e) {
            PERF_LOG.error("迁移业务对象失败", e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e, "迁移业务对象失败");
        }
    }

    /**
     * 获取目录迁移可迁移到的目录
     * @param categoryId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category/move/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getMigrateCategory(@PathParam("categoryId") String categoryId, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            List<CategoryPrivilege> migrateCategory = dataManageService.getMigrateCategory(categoryId, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success(migrateCategory);
        } catch (AtlasBaseException e) {
            PERF_LOG.error("获取可以迁移到目录失败", e);
            throw e;
        } catch (Exception e) {
            PERF_LOG.error("获取可以迁移到目录失败", e);
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
            TableShow tableShow = searchService.getTableShow(guidCount,true);
            return tableShow;
        } catch (AtlasBaseException e) {
            PERF_LOG.error("查询数据失败",e);
            throw e;
        } catch (Exception e) {
            PERF_LOG.error("查询数据失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"查询数据失败");
        }finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 添加权限字段关联
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("check/description/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result checkTable(@HeaderParam("tenantId")String tenantId,
                               @DefaultValue("-1")@QueryParam("limit") int limit,
                               @DefaultValue("0")@QueryParam("offset") int offset) throws AtlasBaseException {
        try {
            PageResult<Table> pageResult = businessService.checkTable(tenantId, limit, offset);
            return ReturnUtil.success(pageResult);
        }  catch (AtlasBaseException e) {
            PERF_LOG.error("表描述空值检测失败",e);
            throw e;
        } catch (Exception e) {
            PERF_LOG.error("表描述空值检测失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"表描述空值检测失败");
        }
    }

    /**
     * 添加权限字段关联
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("check/description/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result checkColumn(@HeaderParam("tenantId")String tenantId,
                               @DefaultValue("-1")@QueryParam("limit") int limit,
                               @DefaultValue("0")@QueryParam("offset") int offset) throws AtlasBaseException {
        try {
            PageResult<Table> pageResult = businessService.checkColumn(tenantId, limit, offset);
            return ReturnUtil.success(pageResult);
        } catch (AtlasBaseException e) {
            PERF_LOG.error("列描述空值检测失败",e);
            throw e;
        } catch (Exception e) {
            PERF_LOG.error("列描述空值检测失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"列描述空值检测失败");
        }
    }

    /**
     * 业务对象表描述空值检查下载
     * @param tenantId
     * @throws Exception
     */
    @GET
    @Path("/download/check/description")
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
        }catch(Exception e){
            PERF_LOG.error("业务对象表描述空值检查下载失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"业务对象表描述空值检查下载失败");
        } finally {
            if (exportExcel!=null) {
                exportExcel.delete();
            }
        }
    }

    /**
     * 业务对象表描述空值检查下载
     * @throws Exception
     */
    @GET
    @Path("/download/column/{tableGuid}")
    @Valid
    public void exportColumn(@PathParam("tableGuid")String tableGuid) throws Exception {
        File exportExcel = null;
        try {
            exportExcel = businessService.exportExcelColumn(tableGuid);
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        }catch(Exception e){
            PERF_LOG.error("业务对象表描述空值检查下载失败",e);
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"业务对象表描述空值检查下载失败");
        } finally {
            if (exportExcel!=null) {
                exportExcel.delete();
            }
        }
    }
}
