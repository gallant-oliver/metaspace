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

import com.gridsum.gdp.library.commons.utils.DateTimeUtils;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import com.itextpdf.text.DocumentException;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.source.Base64Info;
import io.zeta.metaspace.model.source.CodeInfo;
import io.zeta.metaspace.model.source.DataBaseInfo;
import io.zeta.metaspace.model.sourceinfo.AnalyticResult;
import io.zeta.metaspace.model.sourceinfo.Annex;
import io.zeta.metaspace.model.sourceinfo.CreateRequest;
import io.zeta.metaspace.model.sourceinfo.PublishRequest;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.service.HdfsService;
import io.zeta.metaspace.web.service.SourceService;
import io.zeta.metaspace.web.service.sourceinfo.AnnexService;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoFileService;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDatabaseService;
import io.zeta.metaspace.web.util.Base64Utils;
import io.zeta.metaspace.web.util.ReturnUtil;
import io.zeta.metaspace.web.util.office.excel.Excel2Pdf;
import io.zeta.metaspace.web.util.office.excel.ExcelObject;
import io.zeta.metaspace.web.util.office.word.DocConvertToPdf;
import io.zeta.metaspace.web.util.office.word.DocxConvertToPdf;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;


/**
 * @author wuqianhe
 * @Data 2020/7/19 15:15
 */
@Path("source/info")
@Singleton
@Service
public class SourceInfoDatabaseREST {
    private static Logger log = LoggerFactory.getLogger(SourceInfoDatabaseREST.class);
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private SourceInfoDatabaseService sourceInfoDatabaseService;
    @Autowired
    private SourceService sourceService;

    @Autowired
    private HdfsService hdfsService;
    @Autowired
    private AnnexService annexService;
    @Autowired
    private SourceInfoFileService sourceInfoFileService;

    //源信息登记-导入模板下载的hdfs路径
    private final String templatePath = "数据库登记模板.xlsx";

    @POST
    @Path("database")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result addDatabaseInfo(@HeaderParam("tenantId")String tenantId, CreateRequest createRequest){
        return sourceInfoDatabaseService.addDatabaseInfo(tenantId,createRequest.getDatabaseInfo(),
                createRequest.getApproveGroupId(),createRequest.getSubmitType());
    }

    @PUT
    @Path("publish")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result publishDatabaseInfo(@HeaderParam("tenantId")String tenantId, PublishRequest request){
        return sourceInfoDatabaseService.publish(request.getIdList(),request.getApproveGroupId(),tenantId);
    }

    @PUT
    @Path("database")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result updateDatabaseInfo(@HeaderParam("tenantId")String tenantId,CreateRequest createRequest){
        return sourceInfoDatabaseService.updateSourceInfo(createRequest.getDatabaseInfo(),tenantId,createRequest.getApproveGroupId(),createRequest.getSubmitType());
    }

    @PUT
    @Path("revoke/{id}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result revokeSourceInfo(@HeaderParam("tenantId")String tenantId, @PathParam("id") String id) throws Exception {
        return ReturnUtil.success();
    }

    @DELETE
    @Path("database")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result deleteDatabaseInfo(@HeaderParam("tenantId")String tenantId, PublishRequest request){
        return sourceInfoDatabaseService.delete(tenantId,request.getIdList());
    }

    @GET
    @Path("list")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result getSourceInfoList(@HeaderParam("tenantId")String tenantId,
                                                             @DefaultValue("0")@QueryParam("offset") int offset,
                                                             @DefaultValue ("10") @QueryParam("limit") int limit,
                                                             @QueryParam("name")String name,
                                                             @QueryParam("status")Status status){
        return sourceInfoDatabaseService.getDatabaseInfoList(tenantId,status,name,offset,limit);
    }

    /**
     * 源信息数据库登记 模板下载
     * @param tenantId
     * @return
     */
    @GET
    @Path("/model/download")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public void downloadTemplate(@HeaderParam("tenantId")String tenantId) throws UnsupportedEncodingException {
        //根据模板路径获取 (id=1的为模板id)
        Annex annex = annexService.findByAnnexId("1");
        String filename = "";
        String path = templatePath;
        if(annex != null && StringUtils.isNotBlank(annex.getPath())){
            filename = annex.getFileName();
            path = annex.getPath();
            log.info("附件表设置了模板记录：{}",path);
        }else{
            filename = FilenameUtils.getName(templatePath);
        }

        try{
            setDownloadResponseheader(filename);
            InputStream inputStream = hdfsService.getFileInputStream(path);
            IOUtils.copyBytes(inputStream, httpServletResponse.getOutputStream(), 4096, true);
        }catch(Exception e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, e, "模板文件下载失败");
        }
    }

    /**
     *  上传文件到 hdfs
     * @param tenantId 租户id
     * @return
     */
    @POST
    @Path("/file/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result uploadFile(@FormDataParam("file") InputStream fileInputStream,
                             @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
                             @HeaderParam("tenantId")String tenantId){
        try{
            //tenantId 使用租户id作为上传文件子目录
            String fileName = new String(contentDispositionHeader.getFileName().getBytes("ISO8859-1"), "UTF-8");
            String uploadDir = tenantId + "/" + DateTimeUtils.formatTime(System.currentTimeMillis(),"yyyyMMddHHmmss");

            File file = new File(fileName);
            FileUtils.copyInputStreamToFile(fileInputStream, file);
            long fileSize = file.length();//contentDispositionHeader.getSize();

            String uploadPath = hdfsService.uploadFile(new FileInputStream(file),fileName,uploadDir);
            //组装附件表的字段
            String annexId = UUIDUtils.alphaUUID();

            String fileType = FilenameUtils.getExtension(fileName);
            //保存数据到表 annex

            Annex annex = new Annex(annexId,fileName,fileType,uploadPath,fileSize);
            annexService.saveRecord(annex);
            return ReturnUtil.success("success",annexId);
        }catch (Exception e){
            throw new AtlasBaseException("文件上传失败", AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, e, "文件上传失败");
        }
    }

    /**
     * 源信息文件解析
     * @param tenantId 租户 id
     * @param annexParam 附件 
     * @return
     */
    @POST
    @Path("/file/explain")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result parseFile(@HeaderParam("tenantId")String tenantId,Annex annexParam){
        //根据附件id 获取文件的路径和文件名
        String annexId =annexParam.getAnnexId();
        log.info("解析文件的id:{}",annexId);
        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.EMPTY_RESULTS);
        }
        String filePath = annex.getPath();
        try{
            //根据文件路径 解析excel文件
            List<String[]> excelDataList =  hdfsService.readExcelFile(filePath);
            // 跟source_info、db-info对比获取比对结果
            List<AnalyticResult> results = sourceInfoFileService.getFileParsedResult(excelDataList,tenantId);
            return ReturnUtil.success(results);
        }catch (IOException e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, e, "文件解析失败");
        }
    }

    /**
     * 执行源信息文件导入
     * @param tenantId
     * @param duplicatePolicy
     * @return
     */
    @POST
    @Path("/file/import/{duplicatePolicy}")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result executeImportFile(@HeaderParam("tenantId")String tenantId,
                                    @PathParam("duplicatePolicy") String duplicatePolicy,
                                    @FormParam("annexId") String annexId){
        //"IGNORE"-忽略 有重复名称则不导入 ，"STOP"-停止 终止本次导入操作
        if("STOP".equalsIgnoreCase(duplicatePolicy)){
            return ReturnUtil.success("终止操作.");
        }
        log.info("executeImportFile 文件的id:{}",annexId);
        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.EMPTY_RESULTS);
        }
        String filePath = annex.getPath();
        try{
            //根据文件路径 解析excel文件
            List<String[]> excelDataList =  hdfsService.readExcelFile(filePath);
            // 跟source_info、db-info对比获取比对结果
            return sourceInfoFileService.executeImportParsedResult(excelDataList,annexId, tenantId);
        }catch (IOException e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, e, "文件导入失败");
        }

    }


    /**
     * 下载附件
     * @param tenantId 租户 id
     * @param annexId 附件 id
     * @return
     */
    @GET
    @Path("/file/download/{annexId}")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public void downloadFile(@HeaderParam("tenantId")String tenantId,@PathParam("annexId") String annexId){
        //根据附件id 获取文件的路径和文件名
        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.EMPTY_RESULTS);
        }
        String filePath = annex.getPath();
        String filename = annex.getFileName();

        try{
            setDownloadResponseheader(filename);
            InputStream inputStream = hdfsService.getFileInputStream(filePath);
            IOUtils.copyBytes(inputStream, httpServletResponse.getOutputStream(), 4096, true);
        }catch(Exception e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, e, "文件下载失败");
        }

    }

    /**
     *  根据文件id获取文件流
     * @param tenantId
     * @param annexId 文件 id
     * @return
     */
    @GET
    @Path("/fileStream")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result getFileStream(@HeaderParam("tenantId")String tenantId,@QueryParam("annexId") String annexId){
        //根据附件id 获取文件的路径
        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.EMPTY_RESULTS);
        }
        String filePath = annex.getPath();
        try(InputStream inputStream = hdfsService.getFileInputStream(filePath);){
            return ReturnUtil.success(inputStream);
        }catch (IOException e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, e, "获取文件流失败");
        }

    }

    /**
     * 根据文件id获取文件信息
     * @param tenantId 租户 id
     * @param annexId 文件 id
     * @return
     */
    @GET
    @Path("/file")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result getFileInfo(@HeaderParam("tenantId")String tenantId,@QueryParam("annexId") String annexId){
        //根据附件id 获取文件的路径
        Annex annex = annexService.findByAnnexId(annexId);
        return ReturnUtil.success(annex);
    }

    /**
     * 获取预览的文件 base64
     * @param tenantId
     * @param annexId
     * @return
     */
    @GET
    @Path("/file/preview")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result queryPreviewFileStream(@HeaderParam("tenantId")String tenantId,@QueryParam("annexId") String annexId){
        //根据附件id 获取文件的路径
        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.EMPTY_RESULTS);
        }
        String filePath = annex.getPath();
        String fileType = annex.getFileType();
        File tmpFile = null;
        try(InputStream in = hdfsService.getFileInputStream(filePath);){
            tmpFile = File.createTempFile("sourceFileConvert"+System.currentTimeMillis(),"pdf");
            String base64String = "";
            if("xls".equalsIgnoreCase(fileType) || "xlsx".equalsIgnoreCase(fileType)){
                Excel2Pdf excel2Pdf = new Excel2Pdf(Arrays.asList(
                        new ExcelObject(in)
                ), new FileOutputStream(tmpFile));
                excel2Pdf.convert();
                base64String = Base64Utils.fileToBase64(tmpFile.getAbsolutePath());
            }else if("doc".equalsIgnoreCase(fileType)){
                DocConvertToPdf.docToPdf(in,tmpFile);
                base64String = Base64Utils.fileToBase64(tmpFile.getAbsolutePath());
            }else if("docx".equalsIgnoreCase(fileType)){
                DocxConvertToPdf.convertDocxToPdf(in,new FileOutputStream(tmpFile));
                base64String = Base64Utils.fileToBase64(tmpFile.getAbsolutePath());
            }else{
                base64String = Base64Utils.streamToBase64(in);
            }
            Base64Info info = new Base64Info();
            String finalType = StringUtils.containsAny(fileType,"xls","xlsx","doc","docx") ? "pdf" : fileType;
            info.getInstance(finalType,base64String);
            return ReturnUtil.success("success",info);
        }catch (IOException | DocumentException e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, e, "获取文件流失败");
        }finally {
            if(tmpFile != null && tmpFile.exists()){
                tmpFile.delete();
            }
        }
    }
    /**
     * 设置下载文件的响应头
     * @param filename
     * @throws UnsupportedEncodingException
     */
    private void setDownloadResponseheader(String filename) throws UnsupportedEncodingException {
        filename = URLEncoder.encode(filename, "UTF-8");
        httpServletResponse.setContentType("application/force-download");
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        httpServletResponse.addHeader("Content-Disposition", "attachment;fileName=" + filename);
    }

    @GET
    @Path("{id}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result getSourceInfoDetail(@HeaderParam("tenantId")String tenantId, @PathParam("id") String id,@QueryParam("version") @DefaultValue("0") String version){
        return sourceInfoDatabaseService.getDatabaseInfoById(id,tenantId,Integer.parseInt(version));
    }


    /**
     * 获取数据源下未登记的数据库
     *
     * @param dataSourceId
     * @return
     */
    @GET
    @Path("database")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDatabaseByType(@HeaderParam("tenantId") String tenantId, @QueryParam("dataSourceId") String dataSourceId) {
        List<DataBaseInfo> dataBaseInfoList = sourceService.getDatabaseByType(dataSourceId, tenantId);
        return ReturnUtil.success(dataBaseInfoList);
    }

    /**
     * 获取可用用户列表
     *
     * @return
     */
    @GET
    @Path("user/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserList(@HeaderParam("tenantId") String tenantId) {
        List<User> userList = sourceService.getUserList();
        return ReturnUtil.success(userList);
    }

    /**
     * 获取源信息状态列表
     *
     * @return
     */
    @GET
    @Path("status/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getStatusList() {
        List<CodeInfo> codeInfoList = sourceService.getStatusList();
        return ReturnUtil.success(codeInfoList);
    }
}
