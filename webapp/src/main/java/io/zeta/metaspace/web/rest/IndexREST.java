package io.zeta.metaspace.web.rest;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.indices.*;
import io.zeta.metaspace.model.enums.IndexType;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.indexmanager.IndexService;
import io.zeta.metaspace.web.service.indexmanager.IndexServiceImpl;
import io.zeta.metaspace.web.util.CategoryUtil;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryDeleteReturn;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.hadoop.io.IOUtils;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Singleton
@Service
@Path("/indices")
public class IndexREST {

    private static final Logger PERF_LOG = LoggerFactory.getLogger(IndexREST.class);

    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private IndexService indexService;

    @Context
    private HttpServletResponse response;
    @Context
    private HttpServletRequest request;

    //目录类型    指标域
    private static final int CATEGORY_TYPE = 5;
    /**
     * 添加指标域
     *
     * @param categoryInfo
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.NORMDESIGN,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo, @HeaderParam("tenantId")String tenantId) throws Exception {
        HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), categoryInfo.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.createCategory()");
            }
            return dataManageService.createCategory(categoryInfo, CATEGORY_TYPE,tenantId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     *  编辑指标域
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @Permission({ModuleEnum.NORMDESIGN,ModuleEnum.AUTHORIZATION})
    @PUT
    @Path("/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public String updateCategory(@PathParam("categoryId") String categoryGuid,CategoryInfoV2 categoryInfo,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.updateCategory()");
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
     * 删除指标域
     *
     * @param categoryGuid
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.NORMDESIGN, ModuleEnum.AUTHORIZATION})
    @DELETE
    @Path("/categories/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteCategory(@PathParam("categoryGuid") String categoryGuid,@QueryParam("deleteIndex") boolean deleteIndex, @HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        CategoryDeleteReturn deleteReturn = null;
        try {
            Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.deleteCategory(" + categoryGuid + ")");
            }
            deleteReturn = deleteIndexField(categoryGuid, tenantId, CATEGORY_TYPE,deleteIndex);
            return ReturnUtil.success(deleteReturn);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 删除指标域
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryDeleteReturn deleteIndexField(String guid,String tenantId,int type,boolean deleteIndex) throws Exception {
        if(deleteIndex){
            //删除目录下所有指标
            indexService.deleteIndexByIndexFieldId(guid,tenantId);
        }else {
            //将指定指标域下所有指标都转移到默认域
            indexService.removeIndexToAnotherIndexField(guid,tenantId, CategoryUtil.indexFieldId);
        }
        //删除目录
        CategoryDeleteReturn deleteReturn =dataManageService.deleteCategory(guid,tenantId,type);
        return deleteReturn;
    }

    /**
     * 下载指标域模板
     * @throws AtlasBaseException
     */
    @GET
    @Path("/excel/categories/template")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadBusinessTemplate() throws AtlasBaseException {
        try {
            String fileName = TemplateEnum.INDEX_FIELD_TEMPLATE.getFileName();
            InputStream inputStream = PoiExcelUtils.getTemplateInputStream(TemplateEnum.INDEX_FIELD_TEMPLATE);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"下载模板文件异常");
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
        String url = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace/indices/export/selected";
        //全局导出
        if (ids==null||ids.size()==0){
            DownloadUri uri = new DownloadUri();
            String downURL = url + "/" + "all";
            uri.setDownloadUri(downURL);
            return  ReturnUtil.success(uri);
        }
        DownloadUri downloadUri = ExportDataPathUtils.generateURL(url, ids);
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域当前只支持全局导出");
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
     * 获取指标域详情
     * @throws AtlasBaseException
     */
    @GET
    @Path("/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public IndexFieldDTO getIndexFieldInfo(@PathParam("categoryId") String categoryId, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
       try {
           IndexFieldDTO indexFieldDTO=indexService.getIndexFieldInfo(categoryId,tenantId,CATEGORY_TYPE);
           return indexFieldDTO;
       }catch (Exception e) {
           throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e, "获取指标域详情失败");
       }
    }

    /**
     * 上传文件并校验
     *
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.NORMDESIGN,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadIndexField(@HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), name);
            file = ExportDataPathUtils.fileCheck(name, fileInputStream);
            String upload = dataManageService.uploadIndexField(file, CATEGORY_TYPE, tenantId);
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
     * 根据文件导入指标域
     *
     * @param upload
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.NORMDESIGN,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/import/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result importIndexField(@PathParam("upload") String upload, @HeaderParam("tenantId") String tenantId) throws Exception {
        File file = null;
        try {

            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "指标域批量导入" );
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);

            dataManageService.importBatchIndexField(file,CATEGORY_TYPE,tenantId);

            return ReturnUtil.success(null);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 获取全部指标域
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
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getCategories()");
            }
            return dataManageService.getAllByUserGroup(CATEGORY_TYPE, tenantId);
        }  finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加指标
     *
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.NORMDESIGN,ModuleEnum.AUTHORIZATION})
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result addIndex(IndexDTO indexDTO, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        if(Objects.isNull(indexDTO)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数错误");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), indexDTO.getIndexName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.addIndex()");
            }
            IndexResposeDTO iard=indexService.addIndex(indexDTO,tenantId);
            return ReturnUtil.success("添加成功",iard);
        } catch (Exception e) {
            PERF_LOG.error("指标添加失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标添加失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 编辑指标
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.NORMDESIGN,ModuleEnum.AUTHORIZATION})
    @Path("/{indexId}")
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result editIndex(IndexDTO indexDTO,@PathParam("indexId") String indexId, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        if(Objects.isNull(indexDTO)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数错误");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), indexDTO.getIndexName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.editIndex()");
            }
            IndexResposeDTO iard=indexService.editIndex(indexDTO,tenantId);
            return ReturnUtil.success("编辑成功",iard);
        } catch (Exception e) {
            PERF_LOG.error("编辑指标失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "编辑指标失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除指标
     *
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.NORMDESIGN, ModuleEnum.AUTHORIZATION})
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteIndex(DeleteRequestDTO<DeleteIndexInfoDTO> deleteList, @HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            Servlets.validateQueryParamLength("deleteList", deleteList.toString());
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.deleteIndex(" + deleteList.toString() + ")");
            }
            indexService.deleteIndex(deleteList, tenantId);
            return ReturnUtil.success("删除成功");
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 可选指标列表
     *
     * @return
     * @throws Exception
     */
    @Permission({ModuleEnum.NORMDESIGN, ModuleEnum.AUTHORIZATION})
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalIndex(@QueryParam("indexType") int indexType, @HeaderParam("tenantId") String tenantId) throws Exception {
        if(!IndexType.contains(indexType)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型参数错误");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalIndex(" + indexType + ")");
            }
            List<OptionalIndexDTO> optionalIndexDTOs=indexService.getOptionalIndex(indexType,CATEGORY_TYPE, tenantId);
            return ReturnUtil.success(optionalIndexDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @Permission({ModuleEnum.NORMDESIGN, ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/dataSource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalDataSource(@HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalDataSource()");
            }
            List<OptionalDataSourceDTO> optionalDataSourceDTOs=indexService.getOptionalDataSource(tenantId);
            return ReturnUtil.success(optionalDataSourceDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @Permission({ModuleEnum.NORMDESIGN, ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/dataSource/db")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalDb(@QueryParam("dataSourceId") String dataSourceId,@HeaderParam("tenantId") String tenantId) throws Exception {
        if(Objects.isNull(dataSourceId)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不能为空");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalDb("+dataSourceId+")");
            }
            List<String> optionalDbs=indexService.getOptionalDb(dataSourceId,tenantId);
            return ReturnUtil.success(optionalDbs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @Permission({ModuleEnum.NORMDESIGN, ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/dataSource/db/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalTable(@QueryParam("dataSourceId") String dataSourceId,@QueryParam("dbName") String dbName,@HeaderParam("tenantId") String tenantId) throws Exception {
        if(Objects.isNull(dataSourceId)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不能为空");
        }
        if(Objects.isNull(dbName)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库名称不能为空");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalTable("+dataSourceId+","+dbName+")");
            }
            List<OptionalTableDTO> optionalTableDTOs=indexService.getOptionalTable(dataSourceId,dbName);
            return ReturnUtil.success(optionalTableDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @Permission({ModuleEnum.NORMDESIGN, ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/dataSource/db/table/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalColumn(@QueryParam("tableId") String tableId,@HeaderParam("tenantId") String tenantId) throws Exception {
        if(Objects.isNull(tableId)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "表id不能为空");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalColumn("+tableId+")");
            }
            List<OptionalColumnDTO> optionalColumnDTOs=indexService.getOptionalColumn(tableId);
            return ReturnUtil.success(optionalColumnDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @Permission({ModuleEnum.NORMDESIGN, ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/{indexId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public IndexInfoDTO getIndexInfo(@PathParam("indexId") String indexId,@QueryParam("indexType") int indexType,@HeaderParam("tenantId") String tenantId) throws Exception {
        if(Objects.isNull(indexId)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标id不能为空");
        }
        if(!IndexType.contains(indexType)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型不正确");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getIndexInfo("+indexId+")");
            }
            IndexInfoDTO indexInfoDTO=indexService.getIndexInfo(indexId,indexType,CATEGORY_TYPE,tenantId);
            return indexInfoDTO;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }



}
