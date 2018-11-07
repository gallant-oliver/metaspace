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

package org.apache.atlas.web.rest;

import com.google.common.base.Strings;
import com.google.common.base.VerifyException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.common.filetable.ColumnExt;
import org.apache.atlas.web.common.filetable.Constants;
import org.apache.atlas.web.common.filetable.CsvEncode;
import org.apache.atlas.web.common.filetable.FileType;
import org.apache.atlas.web.common.filetable.PreviewUploadFactory;
import org.apache.atlas.web.common.filetable.UploadConfig;
import org.apache.atlas.web.common.filetable.UploadFileCache;
import org.apache.atlas.web.common.filetable.UploadFileInfo;
import org.apache.atlas.web.common.filetable.UploadPreview;
import org.apache.atlas.web.config.FiletableConfig;
import org.apache.atlas.web.model.filetable.TaskInfo;
import org.apache.atlas.web.model.filetable.UploadConfigRequestBody;
import org.apache.atlas.web.model.filetable.UploadJobInfo;
import org.apache.atlas.web.model.filetable.UploadResponseBody;
import org.apache.atlas.web.model.filetable.Workbook;
import org.apache.atlas.web.service.filetable.JobService;
import org.apache.atlas.web.service.PreviewUploadService;
import org.apache.atlas.web.service.UploadJobService;
import org.apache.atlas.web.service.filetable.TaskService;
import org.apache.atlas.web.util.Servlets;
import org.apache.atlas.web.util.StringUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("file/table")
@Singleton
@Service
public class FileTableREST {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTableREST.class);

    @Context
    protected HttpServletRequest request;

    @Inject
    private UploadJobService uploadJobService;

    @Inject
    private JobService jobService;

    @Inject
    private TaskService taskService;


    /**
     * 上传本地数据文件到OpenBI，文件大小不超过100兆
     * 文件暂存到临时目录
     * 返回前100行预览数据
     */
    @POST
    @Path("/upload")
    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN, "text/csv",
               "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public UploadResponseBody upload() {
        LOGGER.info("Upload Start...");

        // 返回jobid和filePath
        UploadFileInfo uploadFileInfo = null;
        try {
            // 转存请求内容到服务tmp目录
            Map<String, java.io.File> fileMap = dumpRequestContent2ServerTmpDir(request);
            String fileName = "";
            Iterator<String> it = fileMap.keySet().iterator();
            if (it.hasNext()) {
                fileName = it.next();
            }
            // 从tmp目录把文件转存到UPLOAD目录，创建上传文件信息
            uploadFileInfo = uploadJobService.uploadFile(fileMap.get(fileName));

            String fileType = StringUtils.obtainFileType(fileName);
            PreviewUploadService previewUploadService = PreviewUploadFactory.create(fileType);
            // 解析上传文件生成workbook
            Workbook workbook = previewUploadService.parser(uploadFileInfo.getFilePath());
            workbook.setCreateTime(System.currentTimeMillis());
            // 在缓存中绑定job与workbook对象
            UploadFileCache uploadFileCache = UploadFileCache.create();
            uploadFileCache.put(uploadFileInfo.getJobId(), workbook);
            LOGGER.info(uploadFileCache.toString());

            //生成预览信息
            UploadPreview preview = previewUploadService.previewUpload(uploadFileInfo.getJobId(), Constants.PREVIEW_SIZE);
            UploadResponseBody response = new UploadResponseBody();
            response.setRequestId(uploadFileInfo.getJobId());
            response.setUploadPreview(preview);

            LOGGER.info("Upload End...");

            return response;
        } catch (Exception csvException) {
            UploadFileCache uploadFileCache = UploadFileCache.create();
            uploadFileCache.remove(uploadFileInfo.getJobId());
            LOGGER.info(uploadFileCache.toString());

            java.io.File uploadFile = new java.io.File(uploadFileInfo.getFilePath());
            uploadFile.delete();
            LOGGER.error(uploadFileInfo.getJobId() + "_" + csvException.getMessage(), csvException);
            throw new RuntimeException(csvException);
        }
    }


    public static Map<String, java.io.File> dumpRequestContent2ServerTmpDir(HttpServletRequest request) {
        LOGGER.info("Store uploaded file to temp file.");

        Map<String, java.io.File> fileMap = Maps.newHashMap();
        //必须使用临时文件存储，输入流不能被使用1次以上
        java.io.File tmpFile = null;
        try {
            tmpFile = java.io.File.createTempFile("data_", "_upload");
            // 最大文件100M
            long maxFileSize = FiletableConfig.getUploadMaxFileSize();
            int contentLength = request.getContentLength();
            if (contentLength > maxFileSize) {
                throw new VerifyException("ContentLength[" + contentLength + "] should 0<=ContentLength<=" + maxFileSize);
            }

            String fileName = null;
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                LOGGER.info("Multipart Upload");
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> items = upload.parseRequest(request);
                // 目前一次只允许上传一个文件
                FileItem fileToBeUpload = items.get(0);
                FileUtils.copyInputStreamToFile(fileToBeUpload.getInputStream(), tmpFile);
                fileName = fileToBeUpload.getName();
            } else {
                LOGGER.info("Body Upload");
                FileUtils.copyInputStreamToFile(request.getInputStream(), tmpFile);
                String contentDisposition = request.getHeader("content-disposition");
                fileName = StringUtils.obtainFileName(contentDisposition);
                if (null == fileName) {
                    throw new VerifyException("get the uploaded file name failed.");
                }
                fileName = fileName.toLowerCase();
            }

            if (!tmpFile.exists()) {
                throw new RuntimeException("File not exist: " + tmpFile.getAbsolutePath());
            }
            fileMap.put(fileName, tmpFile);

        } catch (FileUploadException | IOException e) {
            LOGGER.error("dump request content to server failed.", e);
            if (tmpFile != null) {
                tmpFile.delete();
            }
            throw new RuntimeException(e);
        }
        return fileMap;
    }

    /**
     * 生成预览数据，默认生成100行
     *
     * @param
     * @return
     */
    @POST
    @Path("/preview")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public UploadResponseBody preview(UploadConfigRequestBody requestBody) throws AtlasBaseException {

        UploadConfig uploadConfig = requestBody.getUploadConfig();
        String jobId = requestBody.getRequestId();
        String filePath = StringUtils.obtainFilePath(jobId);
        if (uploadConfig.getFileType() == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "fileType is not null!");
        }
        if (FileType.ZIP.equals(uploadConfig.getFileType()) || FileType.CSV.equals(uploadConfig.getFileType())) {
            //为了检测编码是否在允许范围内
            CsvEncode.of(uploadConfig.getFileEncode());
        }
        try {
            UploadPreview preview = PreviewUploadFactory.create(uploadConfig.getFileType().name()).previewUpload(jobId, uploadConfig, Constants.PREVIEW_SIZE);
            UploadResponseBody response = new UploadResponseBody();
            response.setRequestId(requestBody.getRequestId());
            response.setUploadPreview(preview);
            LOGGER.info("UploadPreview:[{}]", ToStringBuilder.reflectionToString(preview));
            return response;
        } catch (Exception csvException) {
            UploadFileCache uploadFileCache = UploadFileCache.create();
            uploadFileCache.remove(jobId);
            LOGGER.info(uploadFileCache.toString());
            File uploadFile = new File(filePath);
            uploadFile.delete();
            throw new RuntimeException(csvException);
        }
    }


    /**
     * 生成数据上传任务
     * 从提交的请求中得到任务配置，目标表schema，以及column的mapping关系
     * 从临时目录中读取数据，根据配置生成parquet文件
     * 加载parquet文件到临时表
     * 把数据加载到目标表的hdfs中
     *
     * @param json
     * @return
     */
    @POST
    @Path("/submit")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TaskInfo upload(UploadConfigRequestBody requestBody) throws AtlasBaseException, SQLException {

        LOGGER.info("upload requestBody[{}]", ToStringBuilder.reflectionToString(requestBody.getUploadConfig()));
        List<ColumnExt> columns = requestBody.getUploadConfig().getColumns();
        for (ColumnExt column : columns) {
            LOGGER.debug("upload column[{}]", ToStringBuilder.reflectionToString(column));
        }

        UploadConfig uploadConfig = requestBody.getUploadConfig();
        if (Strings.isNullOrEmpty(uploadConfig.getTableName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "table must have value");
        }
        if (uploadConfig.getFileType() == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "fileType must have value");
        }
        if (uploadConfig.getActionType() == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "actionType must have value");
        }
        boolean sheetNamesIsNull = (FileType.XLSX.equals(uploadConfig.getFileType()) || FileType.XLS.equals(uploadConfig.getFileType()))
                                  && uploadConfig.getSheetName() == null;
        if (sheetNamesIsNull) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "sheetName must have value");
        }
        if (!Constants.SQL_TABLE_OR_COLUMN_NAME_PATTERN.matcher(uploadConfig.getTableName()).matches()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "表名只能由字母下划线组成:" + uploadConfig.getTableName());
        }

        UploadJobInfo jobInfo = jobService.newUploadJobInfo(requestBody.getRequestId(), uploadConfig);
        TaskInfo taskInfo = jobInfo.getTaskInfo();
        return taskInfo;
    }

    @GET
    @Path("/state/{taskId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TaskInfo state(@PathParam("taskId") String taskId) throws AtlasBaseException, IOException {
        if (org.apache.commons.lang3.StringUtils.isBlank(taskId)) {
            throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMS, "taskId is null");
        }
        TaskInfo taskInfo = taskService.getByTaskId(taskId);
        if (com.google.common.util.concurrent.Service.State.TERMINATED.equals(taskInfo.getState())) {
            taskInfo.setProgress(1.0f);
        }
        return taskInfo;
    }


}

