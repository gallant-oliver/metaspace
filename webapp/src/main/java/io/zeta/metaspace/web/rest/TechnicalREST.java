package io.zeta.metaspace.web.rest;

import com.google.common.base.Joiner;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.CategoryItem;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.share.Organization;
import io.zeta.metaspace.model.table.DataSourceHeader;
import io.zeta.metaspace.model.table.DatabaseHeader;
import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.service.CategoryRelationUtils;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.MetaDataService;
import io.zeta.metaspace.web.service.SearchService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.*;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.io.IOUtils;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;


@Path("technical")
@Singleton
@Service
@Slf4j
public class TechnicalREST {

    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.TechnicalREST");
    @Autowired
    SearchService searchService;
    private static int CATEGORY_TYPE = 0;
    private AtomicBoolean updating = new AtomicBoolean(false);

    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private MetaDataService metaDataService;

    @Context
    private HttpServletResponse response;
    private static final int MAX_EXCEL_FILE_SIZE = 10 * 1024 * 1024;
    @Context
    private HttpServletRequest request;

    /**
     * 添加关联表时搜数据源
     *
     * @return List<DatabaseHeader>
     */
    @POST
    @Path("/search/datasource/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataSourceHeader> getAllDataSource(Parameters parameters, @PathParam("categoryId") String categoryId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        PageResult<DataSourceHeader> pageResult = searchService.getTechnicalDataSourcePageResultV2(parameters, categoryId, tenantId);
        return pageResult;
    }

    /**
     * 添加关联-库搜索
     *
     * @return List<DatabaseHeader>
     */
    @POST
    @Path("/search/database/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DatabaseHeader> getAllDatabase(Parameters parameters, @PathParam("categoryId") String categoryId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        PageResult<DatabaseHeader> pageResult = searchService.getTechnicalDatabasePageResultV2(parameters, null, categoryId, tenantId);
        return pageResult;
    }


    /**
     * 根据数据源获取数据库列表
     * @param parameters
     * @param sourceId
     * @param categoryId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/search/database/{sourceId}/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DatabaseHeader> getAllDatabase(Parameters parameters, @PathParam("sourceId") String sourceId, @PathParam("categoryId") String categoryId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        PageResult<DatabaseHeader> pageResult = searchService.getTechnicalDatabasePageResultV2(parameters, sourceId, categoryId, tenantId);
        return pageResult;
    }

    /**
     * 添加关联表时根据库查表
     *
     * @return List<AddRelationTable>
     */
    @POST
    @Path("/search/database/table/{databaseGuid}/{categoryId}/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<AddRelationTable> getAllDatabaseByDB(Parameters parameters, @PathParam("databaseGuid") String databaseGuid, @PathParam("categoryId") String categotyId, @HeaderParam("tenantId") String tenantId,@PathParam("sourceId") String sourceId) throws AtlasBaseException {
        PageResult<AddRelationTable> pageResult = searchService.getTechnicalTablePageResultByDB(parameters, databaseGuid, categotyId, tenantId, sourceId);
        return pageResult;
    }

    /**
     * 添加关联-表搜表
     *
     * @return List<Table>
     */
    @POST
    @Path("/search/table/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<AddRelationTable> getTableByQuery(Parameters parameters, @PathParam("categoryId") String categoryId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        PageResult<AddRelationTable> pageResult = searchService.getTechnicalTablePageResultV2(parameters, categoryId, tenantId);
        return pageResult;
    }

    /**
     * 获取全部目录
     *
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getCategories()");
            }
            return dataManageService.getTechnicalCategory(tenantId);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取用户目录
     *
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/user/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilegeV2> getUserCategories(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getUserCategories()");
            }
            return dataManageService.getUserCategories(tenantId);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加目录 V2
     *
     * @param categoryInfo
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.TECHNICAL,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo, @HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.createMetadataCategory()");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), categoryInfo.getName());
            return dataManageService.createCategory(categoryInfo, CATEGORY_TYPE, tenantId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 单个或批量删除目录 V2
     *
     * @param categoryGuids
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.TECHNICAL, ModuleEnum.AUTHORIZATION})
    @DELETE
    @Path("/category/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public Result deleteCategory(List<String> categoryGuids, @HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        CategoryDeleteReturn deleteReturn = null;
        int item = 0;
        int categorys = 0;
        try {
            if (CollectionUtils.isEmpty(categoryGuids)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录id不能为空");
            }
            for (String categoryGuid : categoryGuids) {
                Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
                if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                    perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.deleteCategory(" + categoryGuid + ")");
                }
                CategoryEntityV2 category = dataManageService.getCategory(categoryGuid, tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), category.getName());
                deleteReturn = dataManageService.deleteCategory(categoryGuid, tenantId, CATEGORY_TYPE);
                item += deleteReturn.getItem();
                categorys += deleteReturn.getCategory();
            }
            //设置删除的条数
            deleteReturn.setItem(item);
            deleteReturn.setCategory(categorys);
            return ReturnUtil.success(deleteReturn);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 修改目录信息 V2
     *
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @Permission({ModuleEnum.TECHNICAL,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/update/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public String updateCategory(CategoryInfoV2 categoryInfo, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.CategoryEntity()");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), categoryInfo.getName());
            return dataManageService.updateCategory(categoryInfo, CATEGORY_TYPE, tenantId);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取关联关系
     *
     * @param categoryGuid
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/relations/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getCategoryRelations(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.getCategoryRelations(" + categoryGuid + ")");
            }
            return dataManageService.getRelationsByCategoryGuid(categoryGuid, relationQuery, tenantId);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);   
        }
    }

    /**
     * 获取表关联
     *
     * @param relationQuery
     * @return
     * @throws AtlasBaseException
     *
     */
    @POST
    @Path("/table/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableName(relationQuery, CATEGORY_TYPE, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "搜索关联表失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/owner/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response addOwners(TableOwner tableOwner, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        List<String> tableNames = metaDataService.getTableNames(tableOwner.getTables());
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), "修改表owner:[" + Joiner.on("、").join(tableNames) + "]");
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.addOwners()");
            }
            dataManageService.addTableOwner(tableOwner, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加组织架构失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    @POST
    @Path("/organization/{pId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Organization> getOrganization(@PathParam("pId") String pId, Parameters parameters) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getOrganization()");
            }
            return dataManageService.getOrganizationByPid(pId, parameters);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/organization")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Organization> getOrganizationByName(Parameters parameters) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getOrganizationByName()");
            }
            return dataManageService.getOrganizationByName(parameters);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @PUT
    @Path("/organization")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateOrganization() throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (!updating.getAndSet(true)) {
                try {
                    dataManageService.updateOrganization();
                } finally {
                    updating.set(false);
                }
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "正在更新组织架构！");
            }
            return Response.status(200).entity("更新成功！").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新组织架构失败");
        } finally {
            AtlasPerfTracer.log(perf);
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
        String url = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace/technical/export/selected";
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
        File exportExcel;
        //全局导出
        String all = "all";
        if (all.equals(downloadId)) {
            exportExcel = dataManageService.exportExcelAll(CATEGORY_TYPE, tenantId);
        } else {
            List<String> ids = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
            exportExcel = dataManageService.exportExcel(ids, CATEGORY_TYPE, tenantId);
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
    @Permission({ModuleEnum.TECHNICAL,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadCategory(@FormDataParam("categoryId") String categoryId,
                                 @DefaultValue("false") @FormDataParam("all") boolean all, @FormDataParam("direction") String direction,
                                 @HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), name);
            file = ExportDataPathUtils.fileCheck(name, fileInputStream);
            String upload;
            if (all) {
                upload = dataManageService.uploadAllCategory(file, CATEGORY_TYPE, tenantId);
            } else {
                upload = dataManageService.uploadCategory(categoryId, direction, file, CATEGORY_TYPE, tenantId);
            }
            HashMap<String, String> map = new HashMap<String, String>() {{
                put("upload", upload);
            }};
            return ReturnUtil.success(map);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 根据文件导入目录
     *
     * @param upload
     * @param importCategory
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.TECHNICAL,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/import/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result importCategory(@PathParam("upload") String upload, ImportCategory importCategory, @HeaderParam("tenantId") String tenantId) throws Exception {
        File file = null;
        try {
            String categoryId = importCategory.getCategoryId();
            String name;
            if (importCategory.isAll()) {
                name = "全部";
            } else if (categoryId == null || categoryId.length() == 0) {
                name = "一级目录";
            } else {
                name = dataManageService.getCategoryNameById(categoryId, tenantId);
            }

            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "导入目录:" + name + "," + importCategory.getDirection());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            List<CategoryPrivilege> categoryPrivileges = null;
            if (importCategory.isAll()) {
                dataManageService.importAllCategory(file, CATEGORY_TYPE, tenantId);
            } else {
                categoryPrivileges = dataManageService.importCategory(categoryId, importCategory.getDirection(), file, importCategory.isAuthorized(), CATEGORY_TYPE, tenantId);
            }
            return ReturnUtil.success(categoryPrivileges);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
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
    @Permission({ModuleEnum.TECHNICAL,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/move/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result moveCategory(MoveCategory moveCategory, @HeaderParam("tenantId") String tenantId) throws Exception {
        try {
            if (moveCategory.getGuid() == null) {
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "变更目录结构：all");
            } else {
                CategoryEntityV2 category = dataManageService.getCategory(moveCategory.getGuid(), tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "变更目录结构：" + category.getName());
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
    @Permission({ModuleEnum.TECHNICAL,ModuleEnum.AUTHORIZATION})
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

    @Permission({ModuleEnum.TECHNICAL,ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/download/category/template")
    @Valid
    public void downloadCategoryTemplate() throws Exception {
        String fileName = TemplateEnum.CATEGORY_TEMPLATE.getFileName();
        InputStream inputStream = PoiExcelUtils.getTemplateInputStream(TemplateEnum.CATEGORY_TEMPLATE);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }

    @POST
    @Path("/category/move")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Valid
    public Result migrateCategory(MigrateCategory migrateCategory, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            CategoryEntityV2 category = dataManageService.getCategory(migrateCategory.getCategoryId(), tenantId);
            CategoryEntityV2 parentCategory = dataManageService.getCategory(migrateCategory.getParentId(), tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "迁移目录" + category.getName() + "到" + parentCategory.getName());
            dataManageService.migrateCategory(migrateCategory.getCategoryId(), migrateCategory.getParentId(), CATEGORY_TYPE, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "目录迁移失败");
        }
    }

    /**
     * 添加关联-迁移数据
     * 页面上已经隐藏了迁移数据的按钮，所以这个接口已废弃，不再维护
     * @param item
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @Deprecated
    @POST
    @Path("move")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result moveTableToCategory(CategoryItem item, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            if (item.getIds() == null || item.getIds().size() == 0) {
                return ReturnUtil.success();
            }
            String path = CategoryRelationUtils.getPath(item.getCategoryId(), tenantId);
            List<String> tableNames = metaDataService.getTableNames(item.getIds());
            if (CollectionUtils.isNotEmpty(tableNames)) {
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "迁移表关联:[" + Joiner.on("、").join(tableNames) + "]到" + path);
            }
//            dataManageService.assignTablesToCategory(item.getCategoryId(), item.getIds(), tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "迁移表关联失败");
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
    @Path("/category/move/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result getMigrateCategory(@PathParam("categoryId") String categoryId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            List<CategoryPrivilege> migrateCategory = dataManageService.getMigrateCategory(categoryId, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success(migrateCategory);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取可以迁移到目录失败");
        }
    }

}
