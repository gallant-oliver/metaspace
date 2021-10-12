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
 * @author fanjiajia
 * @date 2021/9/28
 */


import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.business.BussinessCatalogueInput;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.web.service.*;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.ImportCategory;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
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
import java.util.Objects;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;


@Singleton
@Service
@Path("/businessescata")
public class BusinessCatalogueREST {
    private static final Logger PERF_LOG = LoggerFactory.getLogger(BusinessCatalogueREST.class);
    private static final int CATEGORY_TYPE = 1;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;

    @Autowired
    private BusinessService businessService;
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

    @Autowired
    private SearchService searchService;


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
                                 @FormDataParam("type") int type,
                                 @DefaultValue("false") @FormDataParam("all") boolean all, @FormDataParam("direction") String direction,
                                 @HeaderParam("tenantId") String tenantId, @FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
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
    public void exportSelected(@PathParam("downloadId") String downloadId, @QueryParam("tenantId") String tenantId,@QueryParam("type") int type) throws Exception {
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
    public List<CategorycateQueryResult> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort,@QueryParam("type") int type, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getCategories()");
            }
            return  businessCatalogueService.getAllCategories(type, tenantId);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

}
