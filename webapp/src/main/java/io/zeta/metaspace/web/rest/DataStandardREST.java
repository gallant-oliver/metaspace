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
import io.zeta.metaspace.model.datastandard.CategoryAndDataStandard;
import io.zeta.metaspace.model.datastandard.DataStandToRule;
import io.zeta.metaspace.model.datastandard.DataStandToTable;
import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.datastandard.DataStandardQuery;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataQualityService;
import io.zeta.metaspace.web.service.DataStandardService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;


/**
 * 数据标准
 */
@Singleton
@Service
@Path("/datastandard")
public class DataStandardREST {

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Autowired
    private DataStandardService dataStandardService;

    @Autowired
    private DataQualityService dataQualityService;

    @Autowired
    private DataManageService dataManageService;

    private static final int MAX_EXCEL_FILE_SIZE = 10*1024*1024;


    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    @Valid
    public void insert(DataStandard dataStandard,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), dataStandard.getContent());
        List<DataStandard> oldList = dataStandardService.getByNumber(dataStandard.getNumber(),tenantId);
        if (!oldList.isEmpty()) {
            throw new AtlasBaseException("标准编号已存在");
        }
        dataStandardService.insert(dataStandard,tenantId);
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    @Valid
    public void update(DataStandard dataStandard,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), dataStandard.getContent());
        dataStandardService.update(dataStandard,tenantId);
    }

    @DELETE
    @Path("/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByNumberList(List<String> numberList,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(),  Joiner.on("、").join(numberList));
        dataStandardService.deleteByNumberList(numberList,tenantId);
    }

    @GET
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataStandard getById(@PathParam("id") String id,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        return dataStandardService.getById(id,tenantId);
    }

    @POST
    @Path("/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> queryByCatetoryId(@PathParam("categoryId") String categoryId, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return dataStandardService.queryPageByCatetoryId(categoryId, parameters,tenantId);
    }

    @DELETE
    @Path("/{number}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByNumber(@PathParam("number") String number,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(), number);
        dataStandardService.deleteByNumber(number,tenantId);
    }


    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> search(DataStandardQuery parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return dataStandardService.search(parameters,tenantId);
    }

    @POST
    @Path("/history/{number}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> history(@PathParam("number") String number, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return dataStandardService.history(number, parameters,tenantId);
    }

    @GET
    @Path("/download/template")
    @Valid
    public void downloadTemplate() throws Exception {
        String homeDir = System.getProperty("atlas.home");
        String filePath = homeDir + "/conf/data_standard_template.xlsx";
        String fileName = filename(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }

    public static String filename(String filePath) throws UnsupportedEncodingException {
        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
        filename = URLEncoder.encode(filename, "UTF-8");
        return filename;
    }

    @POST
    @Path("/export/selected")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DownloadUri getDownloadURL(List<String> ids) throws Exception {
        return ExportDataPathUtils.generateURL(request.getRequestURL().toString(), ids);
    }

    @GET
    @Path("/export/selected/{downloadId}")
    @Valid
    @OperateType(UPDATE)
    public void exportSelected(@PathParam("downloadId") String downloadId,@QueryParam("tenantId") String tenantId) throws Exception {
        List<String> ids = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
        File exportExcel = dataStandardService.exportExcel(ids,tenantId);
        try {
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
            HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(),  fileName);
        } finally {
            exportExcel.delete();
        }
    }

    @GET
    @Path("/export/category/{categoryId}")
    @Valid
    public void exportCategoryId(@PathParam("categoryId") String categoryId,@QueryParam("tenantId") String tenantId) throws Exception {
        File exportExcel = dataStandardService.exportExcel(categoryId,tenantId);
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

    @POST
    @Path("/import/{categoryId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response importDataStandard(@PathParam("categoryId") String categoryId,
                                       @FormDataParam("file") InputStream fileInputStream,
                                       @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,@HeaderParam("tenantId")String tenantId) throws Exception {
        File file = null;
        try {
            String name =URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            HttpRequestContext.get().auditLog(ModuleEnum.DATASTANDARD.getAlias(),  name);
            if(!(name.endsWith(".xlsx") || name.endsWith(".xls"))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式错误");
            }

            file = new File(name);
            FileUtils.copyInputStreamToFile(fileInputStream, file);
            if(file.length() > MAX_EXCEL_FILE_SIZE) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件大小不能超过10M");
            }
            dataStandardService.importDataStandard(categoryId, file,tenantId);
            return Response.ok().build();
        } catch (AtlasBaseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取元数据关联失败"+e.getMessage());
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据质量关联失败"+e.getMessage());
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取所有目录和数据标准失败："+e.getMessage());
        }
    }
}
