package io.zeta.metaspace.web.rest;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.approve.ApproveType;
import io.zeta.metaspace.model.dto.indices.*;
import io.zeta.metaspace.model.enums.IndexState;
import io.zeta.metaspace.model.enums.IndexType;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.po.indices.IndexInfoPO;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.indexmanager.IndexService;
import io.zeta.metaspace.web.util.CategoryUtil;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryDeleteReturn;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

//import org.springframework.util.CollectionUtils;

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
    @POST
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo, @HeaderParam("tenantId") String tenantId) throws Exception {

        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.createCategory()");
            }
            CategoryPrivilege categoryPrivilege = dataManageService.createCategory(categoryInfo, CATEGORY_TYPE, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "指标域：" + categoryInfo.getName());
            return categoryPrivilege;
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 编辑指标域
     *
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public String updateCategory(@PathParam("categoryId") String categoryGuid, CategoryInfoV2 categoryInfo, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {

        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.updateCategory()");
            }
            categoryInfo.setGuid(categoryGuid);
            String result = dataManageService.updateCategory(categoryInfo, CATEGORY_TYPE, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "指标域：" + categoryInfo.getName());
            return result;
        } catch (MyBatisSystemException e) {
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
    @DELETE
    @Path("/categories/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteCategory(@PathParam("categoryGuid") String categoryGuid, @QueryParam("deleteIndex") boolean deleteIndex, @HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        CategoryDeleteReturn deleteReturn = null;
        try {
            Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.deleteCategory(" + categoryGuid + ")");
            }
            deleteReturn = deleteIndexField(categoryGuid, tenantId, CATEGORY_TYPE, deleteIndex);
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
    @OperateType(OperateTypeEnum.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public CategoryDeleteReturn deleteIndexField(String guid, String tenantId, int type, boolean deleteIndex) throws Exception {
        List<String> indexFields = dataManageService.getChildIndexFields(guid, tenantId);
        indexFields.add(guid);
        if (deleteIndex) {
            //目录校验
            List<String> indexIds = indexService.getIndexIds(indexFields, tenantId, IndexState.PUBLISH.getValue(), IndexState.APPROVAL.getValue());
            if (!CollectionUtils.isEmpty(indexIds)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录中存在状态为(已发布、审核中)的指标，不允许删除");
            }
            //删除目录下所有指标
            indexService.deleteIndexByIndexFieldId(indexFields, tenantId);
        } else {
            //将指定指标域下所有指标都转移到默认域
            indexService.removeIndexToAnotherIndexField(indexFields, tenantId, CategoryUtil.indexFieldId);
        }
        List<CategoryEntityV2> categoryEntityV2s = dataManageService.queryCategoryEntitysByGuids(indexFields, tenantId);
        //删除目录
        CategoryDeleteReturn deleteReturn = dataManageService.deleteCategory(guid, tenantId, type);
        if (CollectionUtils.isNotEmpty(categoryEntityV2s)) {
            StringBuilder sb = new StringBuilder();
            sb.append("指标域：");
            categoryEntityV2s.forEach(x -> sb.append(x.getName()).append(","));
            sb.deleteCharAt(sb.lastIndexOf(","));
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), sb.toString());
        }
        return deleteReturn;
    }

    /**
     * 下载指标域模板
     *
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
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "下载模板文件异常");
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
        String url = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace/indices/export/selected";
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
     *
     * @throws AtlasBaseException
     */
    @GET
    @Path("/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public IndexFieldDTO getIndexFieldInfo(@PathParam("categoryId") String categoryId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            IndexFieldDTO indexFieldDTO = indexService.getIndexFieldInfo(categoryId, tenantId, CATEGORY_TYPE);
            return indexFieldDTO;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取指标域详情失败");
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
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadIndexField(@HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                   @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
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
    @POST
    @Path("/import/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result importIndexField(@PathParam("upload") String upload, @HeaderParam("tenantId") String tenantId) throws Exception {
        File file = null;
        try {

            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);

            dataManageService.importBatchIndexField(file, CATEGORY_TYPE, tenantId);

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
     *
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getCategories()");
            }
            List<CategoryPrivilege> allByUserGroup = dataManageService.getAllByUserGroup(CATEGORY_TYPE, tenantId);
            return allByUserGroup;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加指标
     *
     * @return
     * @throws Exception
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result addIndex(IndexDTO indexDTO, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        if (Objects.isNull(indexDTO)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数错误");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.addIndex()");
            }
            IndexResposeDTO iard = indexService.addIndex(indexDTO, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "指标：" + indexDTO.getIndexName());
            return ReturnUtil.success("添加成功", iard);
        } catch (Exception e) {
            PERF_LOG.error("指标添加失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 编辑指标
     *
     * @return
     * @throws Exception
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result editIndex(IndexDTO indexDTO, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        if (Objects.isNull(indexDTO)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数错误");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.editIndex()");
            }
            IndexResposeDTO iard = indexService.editIndex(indexDTO, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "指标：" + indexDTO.getIndexName());
            return ReturnUtil.success("编辑成功", iard);
        } catch (AtlasBaseException e) {
            PERF_LOG.error("编辑指标失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
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
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteIndex(RequestDTO<DeleteIndexInfoDTO> requestDTO, @HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.deleteIndex(" + requestDTO.getDtoList().toString() + ")");
            }
            if (!Objects.isNull(requestDTO)) {
                List<DeleteIndexInfoDTO> deleteList = requestDTO.getDtoList();

                if (!CollectionUtils.isEmpty(deleteList)) {
                    List<DeleteIndexInfoDTO> deleteIndexInfoDTOs = deleteList.stream().filter(x -> (IndexState.CREATE.getValue() == x.getIndexState() || IndexState.OFFLINE.getValue() == x.getIndexState())).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(deleteIndexInfoDTOs)) {
                        List<IndexInfoPO> indexInfoPOS = indexService.deleteIndex(deleteIndexInfoDTOs, tenantId);
                        StringBuilder content = new StringBuilder();
                        content.append("指标：");
                        if (CollectionUtils.isNotEmpty(indexInfoPOS)) {
                            indexInfoPOS.forEach(x -> content.append(x.getIndexName()).append(","));
                            content.deleteCharAt(content.lastIndexOf(","));
                        }
                        HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), content.toString());
                    }
                }
            }
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
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalIndex(@QueryParam("indexType") int indexType, @HeaderParam("tenantId") String tenantId) throws Exception {
        if (!IndexType.contains(indexType)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型参数错误");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalIndex(" + indexType + ")");
            }
            List<OptionalIndexDTO> optionalIndexDTOs = indexService.getOptionalIndex(indexType, CATEGORY_TYPE, tenantId);
            return ReturnUtil.success(optionalIndexDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

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
            List<OptionalDataSourceDTO> optionalDataSourceDTOs = indexService.getOptionalDataSource(tenantId);
            return ReturnUtil.success(optionalDataSourceDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/dataSource/db")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalDb(OptionalRequestDTO optionalRequestDTO, @HeaderParam("tenantId") String tenantId) throws Exception {
        if (Objects.isNull(optionalRequestDTO)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不能为空");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalDb(" + optionalRequestDTO.getDataSourceId() + ")");
            }
            List<String> optionalDbs = indexService.getOptionalDb(optionalRequestDTO.getDataSourceId(), tenantId);
            return ReturnUtil.success(optionalDbs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/dataSource/db/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalTable(OptionalRequestDTO optionalRequestDTO, @HeaderParam("tenantId") String tenantId) throws Exception {
        if (Objects.isNull(optionalRequestDTO)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不能为空");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalTable(" + optionalRequestDTO.getDataSourceId() + "," + optionalRequestDTO.getDbName() + ")");
            }
            List<OptionalTableDTO> optionalTableDTOs = indexService.getOptionalTable(optionalRequestDTO.getDataSourceId(), optionalRequestDTO.getDbName());
            return ReturnUtil.success(optionalTableDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/dataSource/db/table/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalColumn(OptionalRequestDTO optionalRequestDTO, @HeaderParam("tenantId") String tenantId) throws Exception {
        if (Objects.isNull(optionalRequestDTO)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不能为空");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getOptionalColumn(" + optionalRequestDTO.getTableId() + ")");
            }
            List<OptionalColumnDTO> optionalColumnDTOs = indexService.getOptionalColumn(optionalRequestDTO.getTableId());
            return ReturnUtil.success(optionalColumnDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("/{indexId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public IndexInfoDTO getIndexInfo(@PathParam("indexId") String indexId, @QueryParam("indexType") int indexType, @QueryParam("version") int version, @HeaderParam("tenantId") String tenantId) throws Exception {
        if (Objects.isNull(indexId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标id不能为空");
        }
        if (!IndexType.contains(indexType)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型不正确");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.getIndexInfo(" + indexId + ")");
            }
            IndexInfoDTO indexInfoDTO = indexService.getIndexInfo(indexId, indexType, version, CATEGORY_TYPE, tenantId);
            return indexInfoDTO;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @PUT
    @Path("/sendApprove")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result indexSendApprove(RequestDTO<PublishIndexDTO> requestDTO, @HeaderParam("tenantId") String tenantId) throws Exception {
        if (Objects.isNull(requestDTO)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数错误");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.indexPublish(" + requestDTO.getDtoList().toString() + ")");
            }
            List<PublishIndexDTO> dtoList = requestDTO.getDtoList();
            if (!CollectionUtils.isEmpty(dtoList)) {
                List<PublishIndexDTO> approveList = null;
                String approveType = dtoList.get(0).getApproveType();
                if (ApproveType.PUBLISH.getCode().equals(approveType)) {
                    approveList = dtoList.stream().filter(x -> (IndexState.CREATE.getValue() == x.getIndexState() || IndexState.OFFLINE.getValue() == x.getIndexState())).collect(Collectors.toList());
                } else if (ApproveType.OFFLINE.getCode().equals(approveType)) {
                    approveList = dtoList.stream().filter(x -> IndexState.PUBLISH.getValue() == x.getIndexState()).collect(Collectors.toList());
                } else {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "送审类型错误");
                }
                if (!CollectionUtils.isEmpty(approveList)) {
                    indexService.indexSendApprove(approveList, tenantId);
                    StringBuilder sb = new StringBuilder();
                    sb.append("指标送审：");
                    approveList.forEach(x -> sb.append(x.getIndexName()).append(","));
                    sb.deleteCharAt(sb.lastIndexOf(","));
                    HttpRequestContext.get().auditLog(ModuleEnum.APPROVERMANAGE.getAlias(), sb.toString());
                }
            }
            return ReturnUtil.success("success");
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/{indexId}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result publishHistory(PageQueryDTO pageQueryDTO, @PathParam("indexId") String indexId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        if (Objects.isNull(indexId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标id不能为空");
        }
        if (!IndexType.contains(pageQueryDTO.getIndexType())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标类型不存在");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.publishHistory(" + indexId + "," + pageQueryDTO.getIndexType() + ")");
            }
            List<IndexInfoDTO> indexInfoDTOS = indexService.publishHistory(indexId, pageQueryDTO, CATEGORY_TYPE, tenantId);
            DataDTO<IndexInfoDTO> data = new DataDTO<>();
            if (CollectionUtils.isEmpty(indexInfoDTOS)) {
                data.setTotalSize(0);
                data.setCurrentSize(0);
            } else {
                data.setLists(indexInfoDTOS);
                data.setTotalSize(indexInfoDTOS.get(0).getTotal());
                data.setCurrentSize(indexInfoDTOS.size());
            }
            data.setOffset(pageQueryDTO.getOffset());
            return ReturnUtil.success(data);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/pagequery")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result pageQuery(PageQueryDTO pageQueryDTO, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        if (Objects.isNull(pageQueryDTO)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数错误");
        }
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.pageQuery(" + pageQueryDTO + ")");
            }
            List<IndexInfoDTO> indexInfoDTOS = indexService.pageQuery(pageQueryDTO, CATEGORY_TYPE, tenantId);
            DataDTO<IndexInfoDTO> data = new DataDTO<>();
            if (CollectionUtils.isEmpty(indexInfoDTOS)) {
                data.setTotalSize(0);
                data.setCurrentSize(0);
            } else {
                data.setLists(indexInfoDTOS);
                data.setTotalSize(indexInfoDTOS.get(0).getTotal());
                data.setCurrentSize(indexInfoDTOS.size());
            }
            data.setOffset(pageQueryDTO.getOffset());
            return ReturnUtil.success(data);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取指标链路
     *
     * @param tenantId
     * @param map
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("links")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getIndexFieldInfo(@HeaderParam("tenantId") String tenantId, Map<String, String> map) throws AtlasBaseException {
        try {
            String indexId = map.get("indexId"); //指标ID
            String indexType = map.get("indexType"); //指标类型 ，指标类型(1 原子指标，2派生指标，3复合指标)
            String version = map.get("version"); //指标类型 ，指标类型(1 原子指标，2派生指标，3复合指标)
            IndexLinkDto indexLink = indexService.getIndexLink(indexId, Integer.parseInt(indexType), version, tenantId);
            return ReturnUtil.success(indexLink);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取指标链路失败");
        }
    }


    /**
     * 下载原子指标模板
     *
     * @param tenantId
     * @throws Exception
     */
    @Permission({ModuleEnum.TECHNICAL, ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/download/excel/atom")
    public void downLoadExcelAtom(@QueryParam("tenantId") String tenantId) throws Exception {
        XSSFWorkbook workbook = indexService.downLoadExcelAtom(tenantId);
        // 应用程序强制下载
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode("原子指标.xlsx", "UTF-8") + "\"");
        workbook.write(response.getOutputStream());
    }

    /**
     * 下载派生指标模板
     *
     * @param tenantId
     * @throws Exception
     */
    @Permission({ModuleEnum.TECHNICAL, ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/download/excel/derive")
    public void downLoadExcelDerive(@QueryParam("tenantId") String tenantId) throws Exception {
        XSSFWorkbook workbook = indexService.downLoadExcelDerive(tenantId);
        // 应用程序强制下载
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode("派生指标.xlsx", "UTF-8") + "\"");
        workbook.write(response.getOutputStream());
    }

    /**
     * 下载复合指标模板
     *
     * @param tenantId
     * @throws Exception
     */
    @Permission({ModuleEnum.TECHNICAL, ModuleEnum.AUTHORIZATION})
    @GET
    @Path("/download/excel/composite")
    public void downLoadExcel(@QueryParam("tenantId") String tenantId) throws Exception {
        XSSFWorkbook workbook = indexService.downLoadExcelComposite(tenantId);
        // 应用程序强制下载
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode("复合指标.xlsx", "UTF-8") + "\"");
        workbook.write(response.getOutputStream());
    }

    /**
     * 上传原子指标模板
     * multipart/form-data Content-Type
     *
     * @param tenantId
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     */
    @POST
    @Path("/upload/excel/atom")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadExcelAtom(@HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                  @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            file = ExportDataPathUtils.fileCheckUuid(name, fileInputStream);
            return ReturnUtil.success("上传成功", indexService.uploadExcelAtom(tenantId, file));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 上传派生指标模板
     *
     * @param tenantId
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     */
    @POST
    @Path("/upload/excel/derive")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadExcelDerive(@HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                    @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            file = ExportDataPathUtils.fileCheckUuid(name, fileInputStream);
            return ReturnUtil.success("上传成功", indexService.uploadExcelDerive(tenantId, file));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 上传复合指标模板
     *
     * @param tenantId
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     */
    @POST
    @Path("/upload/excel/composite")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadExcelComposite(@HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                       @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            file = ExportDataPathUtils.fileCheckUuid(name, fileInputStream);
            return ReturnUtil.success("上传成功", indexService.uploadExcelComposite(tenantId, file));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 导入原子指标数据
     *
     * @param upload
     * @return
     * @throws Exception
     */
    @POST
    @Path("/import/atom/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result importAtomIndex(@PathParam("upload") String upload, @HeaderParam("tenantId") String tenantId) throws Exception {
        File file = null;
        try {
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            indexService.importBatchAtomIndex(file, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "批量导入原子指标数据");
            return ReturnUtil.success(null);
        } catch (Exception e) {
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "批量导入原子指标数据");
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 导入派生指标数据
     *
     * @param upload
     * @return
     * @throws Exception
     */
    @POST
    @Path("/import/derive/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result importDeriveIndex(@PathParam("upload") String upload, @HeaderParam("tenantId") String tenantId) throws Exception {
        File file = null;
        try {
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            indexService.importBatchDeriveIndex(file, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "批量导入派生指标数据");
            return ReturnUtil.success(null);
        } catch (Exception e) {
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "批量导入派生指标数据");
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 导入复合指标数据
     *
     * @param upload
     * @return
     * @throws Exception
     */
    @POST
    @Path("/import/composite/{upload}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result importCompositeIndex(@PathParam("upload") String upload, @HeaderParam("tenantId") String tenantId) throws Exception {
        File file = null;
        try {
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            indexService.importBatchCompositeIndex(file, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "批量导入复合指标数据");
            return ReturnUtil.success(null);
        } catch (Exception e) {
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), "批量导入派生指标数据");
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入失败");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

}
