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


import com.google.common.base.Joiner;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.datastandard.*;
import io.zeta.metaspace.model.enums.FileInfoPath;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataStandardService;
import io.zeta.metaspace.web.service.HdfsService;
import io.zeta.metaspace.web.service.fileinfo.FileInfoService;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.*;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.DELETE;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;


/**
 * 数据标准
 */
@Singleton
@Service
@Path("/datastandard")
public class DataStandardREST {
    
    private static final int MAX_EXCEL_FILE_SIZE = 10 * 1024 * 1024;
    private static final int CATEGORY_TYPE = 3;
    
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;
    @Autowired
    private DataStandardService dataStandardService;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FileInfoService fileInfoService;
    /**
     * 新增数据标准
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    @Valid
    public void insert(DataStandard dataStandard, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), dataStandard.getName());
    
        ObjectUtils.isTrueThen(dataStandard.getNumber(),
                v -> dataStandardService.verifyNumberExist(v, tenantId),
                v -> {
                    throw new AtlasBaseException(AtlasErrorCode.STANDARD_NUMBER_ALREADY_EXISTS);
                });
        ObjectUtils.isTrueThen(dataStandard.getName(),
                v -> dataStandardService.verifyNameExist(v, tenantId),
                v -> {
                    throw new AtlasBaseException(AtlasErrorCode.STANDARD_NAME_ALREADY_EXISTS);
                });
    
        dataStandardService.insert(dataStandard, tenantId);
    }
    
    /**
     * 编辑数据标准
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    @Valid
    public void update(DataStandard dataStandard, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), dataStandard.getName());
        dataStandardService.update(dataStandard, tenantId);
    }
    
    /**
     * 批量删除数据标准
     */
    @DELETE
    @Path("/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByNumberList(List<String> numberList, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), Joiner.on("、").join(numberList));
        dataStandardService.deleteByNumberList(numberList, tenantId);
    }
    
    @GET
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataStandard getById(@PathParam("id") String id, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        return dataStandardService.getById(id, tenantId);
    }
    
    /**
     * 分页查询指定目录下的数据标准
     */
    @POST
    @Path("/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> pagedByCategoryId(@HeaderParam("tenantId") String tenantId,
                                                      @PathParam("categoryId") String categoryId,
                                                      Parameters parameters) throws AtlasBaseException {
        return dataStandardService.queryPageByCatetoryId(categoryId, parameters, tenantId);
    }
    
    /**
     * 根据标准编码删除数据标准
     */
    @DELETE
    @Path("/{number}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByNumber(@PathParam("number") String number,
                               @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), number);
        dataStandardService.deleteByNumber(number, tenantId);
    }
    
    /**
     * 模糊分页查询数据标准
     */
    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> search(@HeaderParam("tenantId") String tenantId,
                                           DataStandardQuery parameters) throws AtlasBaseException {
        return dataStandardService.search(parameters, tenantId);
    }
    
    /**
     * 历史版本分页查询
     */
    @POST
    @Path("/history/{number}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> history(@HeaderParam("tenantId") String tenantId,
                                            @PathParam("number") String number,
                                            Parameters parameters) throws AtlasBaseException {
        return dataStandardService.history(number, parameters, tenantId);
    }
    
    /**
     * 模板获取
     */
    @GET
    @Path("/download/template")
    @Valid
    public void downloadTemplate() throws Exception {
        String fileName = TemplateEnum.DATA_STANDARD_TEMPLATE.getFileName();
        InputStream inputStream = PoiExcelUtils.getTemplateInputStream(TemplateEnum.DATA_STANDARD_TEMPLATE);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=".concat(fileName));
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }
    
    private static String filename(String filePath) throws UnsupportedEncodingException {
        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
        filename = URLEncoder.encode(filename, "UTF-8");
        return filename;
    }
    
    @POST
    @Path("/export/selected")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DownloadUri getDownloadURL(List<String> ids) throws Exception {
        String url = MetaspaceConfig.getMetaspaceUrl().concat("/api/metaspace/datastandard/export/selected");
        return ExportDataPathUtils.generateURL(url, ids);
    }
    
    @GET
    @Path("/export/selected/{downloadId}")
    @Valid
    @OperateType(UPDATE)
    public void exportSelected(@PathParam("downloadId") String downloadId, @QueryParam("tenantId") String tenantId) throws Exception {
        List<String> ids = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
        Assert.isTrue(CollectionUtils.isNotEmpty(ids), "所选数据标准ID集合为空,导出失败!");
        File exportExcel = dataStandardService.exportExcel(ids, tenantId);
        try {
            String fileName = buildExportResponse(exportExcel);
            HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), fileName);
        } finally {
            exportExcel.delete();
        }
    }
    
    @GET
    @Path("/export/category/{categoryId}")
    @Valid
    public void exportCategoryId(@PathParam("categoryId") String categoryId, @QueryParam("tenantId") String tenantId) throws Exception {
        Assert.isTrue(StringUtils.isNotBlank(categoryId), "目录ID无效,导出失败!");
        File exportExcel = dataStandardService.exportExcel(categoryId, tenantId);
        try {
            buildExportResponse(exportExcel);
        } finally {
            exportExcel.delete();
        }
    }
    
    private String buildExportResponse(File exportExcel) throws IOException {
        String filePath = exportExcel.getAbsolutePath();
        String fileName = filename(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=".concat(fileName));
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        return fileName;
    }
    
    @POST
    @Path("/import/{categoryId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response importDataStandard(@PathParam("categoryId") String categoryId,
                                       @FormDataParam("file") InputStream fileInputStream,
                                       @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
                                       @HeaderParam("tenantId") String tenantId) throws Exception {
        String fileName = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), fileName);
    
        File file = null;
        try {
            file = ExportDataPathUtils.fileCheck(fileName, fileInputStream);
            dataStandardService.importDataStandard(categoryId, file, tenantId);
            return Response.ok().build();
        } catch (AtlasBaseException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "导入文件错误");
        } finally {
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }


    /**
     * 指定分类的目录列表
     *
     * @param categoryType
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category/{categoryType}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getAll(@PathParam("categoryType") Integer categoryType,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return dataStandardService.getCategory(categoryType,tenantId);
    }

    /**
     * 添加目录
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
    public CategoryPrivilege insert(CategoryInfoV2 categoryInfo,@HeaderParam("tenantId")String tenantId) throws Exception {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), categoryInfo.getName());
        return dataStandardService.addCategory(categoryInfo,tenantId);
    }

    /**
     * 删除目录
     *
     * @param categoryGuid
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("/category/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Valid
    @OperateType(DELETE)
    public void delete(@PathParam("categoryGuid") String categoryGuid,@HeaderParam("tenantId")String tenantId) throws Exception {
        CategoryEntityV2 category = dataManageService.getCategory(categoryGuid,tenantId);
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), category.getName());
        dataStandardService.deleteCategory(categoryGuid,tenantId);
    }

    /**
     * 修改目录信息
     *
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public void update(CategoryInfoV2 categoryInfo,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), categoryInfo.getName());
        dataStandardService.updateCategory(categoryInfo,tenantId);
    }

    /**
     * 获取元数据关联
     * @param number
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/{number}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public PageResult<DataStandToTable> getTableByNumber(@PathParam("number") String number,Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return dataStandardService.getTableByNumber(number,parameters,tenantId);

        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取元数据关联失败");
        }
    }

    /**
     * 获取数据质量关联
     * @param number
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/rule/{number}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public PageResult<DataStandToRule> getRuleByNumber(@PathParam("number") String number,Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return dataStandardService.getRuleByNumber(number,parameters,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据质量关联失败");
        }
    }


    @GET
    @Path("/category/standard")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public List<CategoryAndDataStandard> getCategoryAndStandard(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return dataStandardService.getCategoryAndStandard(tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取所有目录和数据标准失败");
        }
    }

    /**
     * 导出目录
     * @param ids
     * @return
     * @throws Exception
     */
    @POST
    @Path("/export/selected/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDownloadURLCategory(List<String> ids) throws Exception {
        String url = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace/datastandard/export/selected/category";
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
    @Path("/export/selected/category/{downloadId}")
    @Valid
    public void exportSelectedCategory(@PathParam("downloadId") String downloadId,@QueryParam("tenantId") String tenantId) throws Exception {
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
            buildExportResponse(exportExcel);
        } finally {
            exportExcel.delete();
        }
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
            String name = new String(contentDispositionHeader.getFileName().getBytes("ISO8859-1"), "UTF-8");
            HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), name);
            file = ExportDataPathUtils.fileCheck(name, fileInputStream);
            String upload;
            if (all) {
                upload = dataManageService.uploadAllCategory(file, CATEGORY_TYPE, tenantId);
            } else {
                upload = dataManageService.uploadCategory(categoryId, direction, file, CATEGORY_TYPE, tenantId);
            }
            HashMap<String, String> map = new HashMap<String, String>() {
                private static final long serialVersionUID = 7196218586038548425L;
        
                {
                    put("upload", upload);
                }
            };
            redisUtil.set(upload, name, CommonConstant.FILE_REDIS_TIME);
            return ReturnUtil.success(map);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"导入失败");
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
    @Transactional(rollbackFor = Exception.class)
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

            HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(),  "导入目录:"+name+","+importCategory.getDirection());
            file = new File(ExportDataPathUtils.tmpFilePath + File.separatorChar + upload);
            List<CategoryPrivilege> categoryPrivileges=null;
            if (importCategory.isAll()){
                dataManageService.importAllCategory(file,CATEGORY_TYPE,tenantId);
            }else{
                categoryPrivileges=dataManageService.importCategory(categoryId,importCategory.getDirection(), file,importCategory.isAuthorized(),CATEGORY_TYPE,tenantId);
            }
            fileInfoService.createFileRecord(upload, FileInfoPath.STANDARD_CATEGORY,file);
            return ReturnUtil.success(categoryPrivileges);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"导入失败");
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
                HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), "变更目录结构：all");
            }else{
                CategoryEntityV2 category = dataManageService.getCategory(moveCategory.getGuid(), tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), "变更目录结构："+category.getName());
            }
            dataManageService.moveCategories(moveCategory,CATEGORY_TYPE,tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"变更目录结构失败");
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
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"目录排序并变更结构失败");
        }
    }

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
}
