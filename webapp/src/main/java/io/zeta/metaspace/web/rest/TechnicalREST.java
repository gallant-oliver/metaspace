package io.zeta.metaspace.web.rest;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;

import com.google.common.base.Joiner;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.AddRelationTable;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.share.Organization;
import io.zeta.metaspace.model.table.DatabaseHeader;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.SearchService;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.ImportCategory;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("technical")
@Singleton
@Service
public class TechnicalREST {

    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.TechnicalREST");
    @Autowired
    SearchService searchService;
    private static int CATEGORY_TYPE = 0;
    private AtomicBoolean updating = new AtomicBoolean(false);

    @Autowired
    private DataManageService dataManageService;
    private static final Logger LOG = LoggerFactory.getLogger(TechnicalREST.class);

    @Context
    private HttpServletResponse response;
    private static final int MAX_EXCEL_FILE_SIZE = 10*1024*1024;
    @Context
    private HttpServletRequest request;

    /**
     * 添加关联表时搜库
     *
     * @return List<DatabaseHeader>
     */
    @POST
    @Path("/search/database/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DatabaseHeader> getAllDatabase(Parameters parameters, @PathParam("categoryId") String categoryId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        PageResult<DatabaseHeader> pageResult = searchService.getTechnicalDatabasePageResultV2(parameters, categoryId,tenantId);
        return pageResult;
    }
    /**
     * 添加关联表时根据库搜表
     *
     * @return List<AddRelationTable>
     */
    @POST
    @Path("/search/database/table/{databaseGuid}/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<AddRelationTable> getAllDatabaseByDB(Parameters parameters, @PathParam("databaseGuid") String databaseGuid,@PathParam("categoryId") String categotyId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        PageResult<AddRelationTable> pageResult = searchService.getTechnicalTablePageResultByDB(parameters, databaseGuid,categotyId,tenantId);
        return pageResult;
    }
    /**
     * 添加关联表时搜表
     *
     * @return List<Table>
     */
    @POST
    @Path("/search/table/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<AddRelationTable> getTableByQuery(Parameters parameters, @PathParam("categoryId") String categoryId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        PageResult<AddRelationTable> pageResult = searchService.getTechnicalTablePageResultV2(parameters, categoryId,tenantId);
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
    public List<CategoryPrivilege> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getCategories()");
            }
            return TenantService.defaultTenant.equals(tenantId) ? dataManageService.getAll(CATEGORY_TYPE) : dataManageService.getAllByUserGroup(CATEGORY_TYPE, tenantId);
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
    @POST
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo,@HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.createMetadataCategory()");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), categoryInfo.getName());
            return dataManageService.createCategory(categoryInfo, CATEGORY_TYPE,tenantId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除目录 V2
     *
     * @param categoryGuid
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("/category/{categoryGuid}")
    @OperateType(DELETE)
    public Response deleteCategory(@PathParam("categoryGuid") String categoryGuid,@HeaderParam("tenantId")String tenantId) throws Exception {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.deleteCategory(" + categoryGuid + ")");
            }
            CategoryEntityV2 category = dataManageService.getCategory(categoryGuid,tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), category.getName());
            dataManageService.deleteCategory(categoryGuid,tenantId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 修改目录信息 V2
     *
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/update/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public String updateCategory(CategoryInfoV2 categoryInfo,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.CategoryEntity()");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), categoryInfo.getName());
            return dataManageService.updateCategory(categoryInfo, CATEGORY_TYPE,tenantId);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加关联
     *
     * @param categoryGuid
     * @param relations
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/{categoryGuid}/assignedEntities")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Response assignTableToCategory(@PathParam("categoryGuid") String categoryGuid, List<RelationEntityV2> relations,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.assignTableToCategory(" + categoryGuid + ")");
            }
            String categoryName = dataManageService.getCategoryNameById(categoryGuid,tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), categoryName);
            dataManageService.assignTablesToCategory(categoryGuid, relations);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
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
    public PageResult<RelationEntityV2> getCategoryRelations(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.getCategoryRelations(" + categoryGuid + ")");
            }
            return dataManageService.getRelationsByCategoryGuid(categoryGuid, relationQuery,tenantId);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除关联关系
     *
     * @param relationshipList
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/category/relation")
    @OperateType(DELETE)
    public Response removeRelationAssignmentFromTables(List<RelationEntityV2> relationshipList,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "TechnicalREST.removeRelationAssignmentFromTables(" + relationshipList + ")");
            }
            List<String> categoryNameList = new ArrayList<>();
            for (RelationEntityV2 relationEntity : relationshipList) {
                String guid = relationEntity.getRelationshipGuid();
                String categoryName = dataManageService.getCategoryNameByRelationId(guid,tenantId);
                if(categoryName != null)
                    categoryNameList.add(categoryName);
            }
            if(categoryNameList!=null && categoryNameList.size()>0) {
                HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), "批量删除:[" + Joiner.on("、").join(categoryNameList) + "]中的表关联");
            }
            dataManageService.removeRelationAssignmentFromTablesV2(relationshipList,tenantId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 获取表关联
     *
     * @param relationQuery
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableName(relationQuery, CATEGORY_TYPE,tenantId);
        } catch (Exception e) {
            throw e;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/owner/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response addOwners(TableOwner tableOwner,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.addOwners()");
            }
            dataManageService.addTableOwner(tableOwner,tenantId);
        } catch (Exception e) {
            throw e;
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
            throw e;
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
            throw e;
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
                dataManageService.updateOrganization();
                updating.set(false);
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "正在更新组织架构！");
            }
            return Response.status(200).entity("更新成功！").build();
        } catch (AtlasBaseException e) {
            throw e;
        } finally {
            AtlasPerfTracer.log(perf);
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
    public Result exportSelected(@PathParam("downloadId") String downloadId,@QueryParam("tenantId")String tenantId) throws Exception {
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
            return ReturnUtil.success();
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
            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(),  name);
            String suffix1 = ".xlsx";
            String suffix2 = ".xls";
            if(!(name.endsWith(suffix1) || name.endsWith(suffix2))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式错误");
            }

            file = new File(name);
            FileUtils.copyInputStreamToFile(fileInputStream, file);
            if(file.length() > MAX_EXCEL_FILE_SIZE) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件大小不能超过10M");
            }
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
            LOG.error("导入失败",e);
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
    public Result importCategory(@PathParam("upload")String upload, ImportCategory importCategory,@HeaderParam("tenantId")String tenantId) throws Exception {
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

            HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(),  "导入目录:"+name+","+importCategory.getDirection());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            if (importCategory.isAll()){
                dataManageService.importAllCategory(file,CATEGORY_TYPE,tenantId);
            }else{
                dataManageService.importCategory(categoryId,importCategory.getDirection(), file,CATEGORY_TYPE,tenantId);
            }
            return ReturnUtil.success();
        } catch (AtlasBaseException e) {
            LOG.error("导入失败",e);
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
    public Result moveCategory(MoveCategory moveCategory,@HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            if(moveCategory.getGuid()==null){
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "变更目录结构：all");
            }else{
                CategoryEntityV2 category = dataManageService.getCategory(moveCategory.getGuid(),tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.TECHNICAL.getAlias(), "变更目录结构："+category.getName());
            }
            dataManageService.moveCategories(moveCategory,CATEGORY_TYPE,tenantId);
            return ReturnUtil.success();
        }catch (AtlasBaseException e){
            LOG.error("变更目录结构失败",e);
            throw e;
        }catch (Exception e){
            LOG.error("变更目录结构失败",e);
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
            LOG.error("目录排序并变更结构失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"目录排序并变更结构失败");
        }
    }

    @GET
    @Path("/download/category/template")
    @Valid
    public Result downloadCategoryTemplate() throws Exception {
        String homeDir = System.getProperty("atlas.home");
        String filePath = homeDir + "/conf/category_template.xlsx";
        String fileName = filename(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        return ReturnUtil.success();
    }
}
