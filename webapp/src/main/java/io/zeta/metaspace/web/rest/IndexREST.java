package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.service.DataManageService;
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

import java.io.*;
import java.net.URLEncoder;
import java.util.List;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;

@Singleton
@Service
@Path("/indices")
public class IndexREST {

    private static final Logger PERF_LOG = LoggerFactory.getLogger(IndexREST.class);

    @Autowired
    private DataManageService dataManageService;

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
    //@Permission({ModuleEnum.INDEX,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo, @HeaderParam("tenantId")String tenantId) throws Exception {
        //HttpRequestContext.get().auditLog(ModuleEnum.INDEX.getAlias(), categoryInfo.getName());
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
    //@Permission({ModuleEnum.INDEX,ModuleEnum.AUTHORIZATION})
    @PUT
    @Path("/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
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
    //@Permission({ModuleEnum.INDEX, ModuleEnum.AUTHORIZATION})
    @DELETE
    @Path("/categories/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
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
        }else {
            //将目录下所有指标都转移到默认域
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
            /*FileOutputStream fos = new FileOutputStream("F:\\work\\metaspace\\1.11.0\\index_field_template.xlsx");

            byte[] b = new byte[1024];
            int length;
            while((length= inputStream.read(b))>0){
                fos.write(b,0,length);
            }
            inputStream.close();
            fos.close();*/
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"下载模板文件异常");
        }
    }

    /**
     * 指标域全局导出
     * @param tenantId
     * @throws Exception
     */
    @GET
    @Path("/export/indexField/all")
    @Valid
    public void exportSelected(@HeaderParam("tenantId")String tenantId) throws Exception {
        File exportExcel = dataManageService.exportExcelAll(CATEGORY_TYPE,tenantId);
        //全局导出
        try {
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            /*FileOutputStream fos = new FileOutputStream("F:\\work\\metaspace\\1.11.0\\index_field_all.xlsx");
            byte[] b = new byte[1024];
            int length;
            while((length= inputStream.read(b))>0){
                fos.write(b,0,length);
            }
            inputStream.close();
            fos.close();*/
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

}
