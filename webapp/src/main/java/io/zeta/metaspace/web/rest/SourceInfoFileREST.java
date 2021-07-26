package io.zeta.metaspace.web.rest;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.sourceinfo.AnalyticResult;
import io.zeta.metaspace.model.sourceinfo.Annex;
import io.zeta.metaspace.web.service.HdfsService;
import io.zeta.metaspace.web.service.sourceinfo.AnnexService;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoFileService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 源信息登记 涉及文件上传下载、解析等操作
 */
@Singleton
@Service
@Path("")
public class SourceInfoFileREST {
    @Autowired
    private HdfsService hdfsService;
    @Autowired
    private AnnexService annexService;
    @Autowired
    private SourceInfoFileService sourceInfoFileService;
    @Context
    private HttpServletResponse response;
    //源信息登记-导入模板下载的hdfs路径
    private final String templatePath = "数据库登记模板.xlsx";
    /**
     * 源信息数据库登记 模板下载
     * @param tenantId
     * @return
     */
    @GET
    @Path("/source/info/model/download")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public void downloadTemplate(@HeaderParam("tenantId")String tenantId) throws UnsupportedEncodingException {
        //根据模板路径获取
        String filename = FilenameUtils.getName(templatePath);
        try{
            setDownloadResponseheader(filename);
            InputStream inputStream = hdfsService.getFileInputStream(templatePath);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        }catch(Exception e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "模板文件下载失败");
        }
    }

    /**
     *  上传文件到 hdfs
     * @param tenantId 租户id
     * @param file 文件
     * @return
     */
    @POST
    @Path("/source/info/file/upload")
    @Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
    @Produces({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
    public Result uploadFile(@HeaderParam("tenantId")String tenantId,@RequestParam("file") MultipartFile file){
        try{
            //tenantId 使用租户id作为上传文件子目录
            String uploadPath = hdfsService.uploadFile(file,tenantId);
            //组装附件表的字段
            String annexId = UUIDUtils.alphaUUID();
            String fileName = file.getOriginalFilename();
            String fileType = FilenameUtils.getExtension(fileName);
            //保存数据到表 annex
            Annex annex = new Annex(annexId,fileName,fileType,uploadPath);
            annexService.saveRecord(annex);
            return ReturnUtil.success(annexId);
        }catch (IOException e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "文件上传失败");
        }
    }

    /**
     * 源信息文件解析
     * @param tenantId 租户 id
     * @param annexId 附件 id
     * @return
     */
    @POST
    @Path("/source/info/file/explain")
    @Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
    @Produces({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
    public Result parseFile(@HeaderParam("tenantId")String tenantId,@RequestParam("annexId") String annexId){
        //根据附件id 获取文件的路径和文件名
        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.BAD_REQUEST);
        }
        String filePath = annex.getPath();
        try{
            //根据文件路径 解析excel文件
            List<String[]> excelDataList =  hdfsService.readExcelFile(filePath);
            // 跟source_info、db-info对比获取比对结果
            List<AnalyticResult> results = sourceInfoFileService.getFileParsedResult(excelDataList);
            return ReturnUtil.success(results);
        }catch (IOException e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "文件解析失败");
        }
    }

    /**
     * 执行源信息文件导入
     * @param tenantId
     * @param duplicatePolicy
     * @return
     */
    @POST
    @Path("/source/info/file/import/{duplicatePolicy}")
    @Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
    @Produces({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
    public Result executeImportFile(@HeaderParam("tenantId")String tenantId,
                                    @RequestParam("annexId") String annexId,
                                    @PathParam("duplicatePolicy") String duplicatePolicy){
        //"IGNORE"-忽略 有重复名称则不导入 ，"STOP"-停止 终止本次导入操作
        if("STOP".equalsIgnoreCase(duplicatePolicy)){
            return ReturnUtil.success("终止操作.");
        }

        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.BAD_REQUEST);
        }
        String filePath = annex.getPath();
        try{
            //根据文件路径 解析excel文件
            List<String[]> excelDataList =  hdfsService.readExcelFile(filePath);
            // 跟source_info、db-info对比获取比对结果
            int n = sourceInfoFileService.executeImportParsedResult(excelDataList,annexId, tenantId);
            return ReturnUtil.success(n);
        }catch (IOException e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "文件解析失败");
        }

    }


    /**
     * 下载附件
     * @param tenantId 租户 id
     * @param annexId 附件 id
     * @return
     */
    @GET
    @Path("/source/info/file/download/{annexId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public void downloadFile(@HeaderParam("tenantId")String tenantId,@PathParam("annexId") String annexId){
        //根据附件id 获取文件的路径和文件名
        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.BAD_REQUEST);
        }
        String filePath = annex.getPath();
        String filename = annex.getFileName();

        try{
            setDownloadResponseheader(filename);
            InputStream inputStream = hdfsService.getFileInputStream(filePath);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        }catch(Exception e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "文件下载失败");
        }

    }

    /**
     *  根据文件id获取文件流
     * @param tenantId
     * @param annexId 文件 id
     * @return
     */
    @GET
    @Path("/source/info/fileStream")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result getFileStream(@HeaderParam("tenantId")String tenantId,@RequestParam("annexId") String annexId){
        //根据附件id 获取文件的路径
        Annex annex = annexService.findByAnnexId(annexId);
        if(annex == null){
            throw new AtlasBaseException("没有找到对应的附件", AtlasErrorCode.BAD_REQUEST);
        }
        String filePath = annex.getPath();
        try{
            InputStream inputStream = hdfsService.getFileInputStream(filePath);

            return ReturnUtil.success(inputStream);
        }catch (IOException e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取文件流失败");
        }

    }

    /**
     * 根据文件id获取文件信息
     * @param tenantId 租户 id
     * @param annexId 文件 id
     * @return
     */
    @GET
    @Path("/source/info/file")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result getFileInfo(@HeaderParam("tenantId")String tenantId,@RequestParam("annexId") String annexId){
        //根据附件id 获取文件的路径
        Annex annex = annexService.findByAnnexId(annexId);
        return ReturnUtil.success(annex);
    }

    /**
     * 设置下载文件的响应头
     * @param filename
     * @throws UnsupportedEncodingException
     */
    private void setDownloadResponseheader(String filename) throws UnsupportedEncodingException {
        filename = URLEncoder.encode(filename, "UTF-8");
        response.setContentType("application/force-download");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.addHeader("Content-Disposition", "attachment;fileName=" + filename);
    }
}
