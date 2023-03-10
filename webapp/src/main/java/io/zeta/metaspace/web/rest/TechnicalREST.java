package io.zeta.metaspace.web.rest;

import com.google.common.base.Joiner;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.enums.FileInfoPath;
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
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.service.*;
import io.zeta.metaspace.web.service.fileinfo.FileInfoService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.RedisUtil;
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
import java.net.URLEncoder;
import java.util.*;
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
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FileInfoService fileInfoService;
    /**
     * ??????????????????????????????
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
     * ????????????-?????????
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
     * ????????????????????????????????????
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
     * ?????????????????????????????????
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
     * ????????????-?????????
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
     * ??????????????????
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
     * ??????????????????
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
     * ???????????? V2
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * ??????????????????????????? V2
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
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????id????????????");
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
            //?????????????????????
            deleteReturn.setItem(item);
            deleteReturn.setCategory(categorys);
            return ReturnUtil.success(deleteReturn);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * ?????????????????? V2
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * ??????????????????
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
        } finally {
            AtlasPerfTracer.log(perf);   
        }
    }

    /**
     * ???????????????
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
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "?????????????????????");
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
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), "?????????owner:[" + Joiner.on("???").join(tableNames) + "]");
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.addOwners()");
            }
            dataManageService.addTableOwner(tableOwner, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????????????????");
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
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????");
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
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????");
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
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????");
            }
            return Response.status(200).entity("???????????????").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????????????????");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * ????????????
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
        //????????????
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
        //????????????
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
     * ?????????????????????
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
            String name = new String(contentDispositionHeader.getFileName().getBytes("ISO8859-1"), "UTF-8");
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
            redisUtil.set(upload,name,CommonConstant.FILE_REDIS_TIME);
            return ReturnUtil.success(map);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * ????????????????????????
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
                name = "??????";
            } else if (categoryId == null || categoryId.length() == 0) {
                name = "????????????";
            } else {
                name = dataManageService.getCategoryNameById(categoryId, tenantId);
            }

            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "????????????:" + name + "," + importCategory.getDirection());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            List<CategoryPrivilege> categoryPrivileges = null;
            if (importCategory.isAll()) {
                dataManageService.importAllCategory(file, CATEGORY_TYPE, tenantId);
            } else {
                categoryPrivileges = dataManageService.importCategory(categoryId, importCategory.getDirection(), file, importCategory.isAuthorized(), CATEGORY_TYPE, tenantId);
            }
            fileInfoService.createFileRecord(upload, FileInfoPath.TECHNICAL_CATEGORY, file);
            return ReturnUtil.success(categoryPrivileges);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * ??????????????????
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
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "?????????????????????all");
            } else {
                CategoryEntityV2 category = dataManageService.getCategory(moveCategory.getGuid(), tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "?????????????????????" + category.getName());
            }
            dataManageService.moveCategories(moveCategory, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????????????????");
        }
    }

    /**
     * ????????????????????????
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
            List<CategoryPrivilege> categories = dataManageService.sortCategory(sortCategory, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success(categories);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "?????????????????????????????????");
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
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "????????????" + category.getName() + "???" + parentCategory.getName());
            dataManageService.migrateCategory(migrateCategory.getCategoryId(), migrateCategory.getParentId(), CATEGORY_TYPE, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????");
        }
    }

    /**
     * ????????????-????????????
     * ??????????????????????????????????????????????????????????????????????????????????????????
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
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "???????????????:[" + Joiner.on("???").join(tableNames) + "]???" + path);
            }
//            dataManageService.assignTablesToCategory(item.getCategoryId(), item.getIds(), tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "?????????????????????");
        }
    }

    /**
     * ???????????????????????????????????????
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
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "?????????????????????????????????");
        }
    }

}
