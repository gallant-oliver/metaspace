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


import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;
import static io.zeta.metaspace.web.service.DataStandardService.filename;

import com.google.common.base.Joiner;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.datastandard.DataStandardQuery;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.filter.OperateLogInterceptor;
import io.zeta.metaspace.web.service.DataQualityService;
import io.zeta.metaspace.web.service.DataStandardService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


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

    private static final int MAX_EXCEL_FILE_SIZE = 10*1024*1024;


    private void log(String content) {
        request.setAttribute(OperateLogInterceptor.OPERATELOG_OBJECT, "(数据标准) " + content);
    }

    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public void insert(DataStandard dataStandard) throws AtlasBaseException {
        log(dataStandard.getContent());
        List<DataStandard> oldList = dataStandardService.getByNumber(dataStandard.getNumber());
        if (!oldList.isEmpty()) {
            throw new AtlasBaseException("标准编号已存在");
        }
        dataStandardService.insert(dataStandard);
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public void update(DataStandard dataStandard) throws AtlasBaseException {
        log(dataStandard.getContent());
        dataStandardService.update(dataStandard);
    }

    @DELETE
    @Path("/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByNumberList(List<String> numberList) throws AtlasBaseException {
        log("批量删除:[" + Joiner.on("、").join(numberList) + "]");
        dataStandardService.deleteByNumberList(numberList);
    }

    @GET
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataStandard getById(@PathParam("id") String id) throws AtlasBaseException {
        return dataStandardService.getById(id);
    }

    @POST
    @Path("/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> queryByCatetoryId(@PathParam("categoryId") String categoryId, Parameters parameters) throws AtlasBaseException {
        return dataStandardService.queryPageByCatetoryId(categoryId, parameters);
    }

    @DELETE
    @Path("/{number}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByNumber(@PathParam("number") String number) throws AtlasBaseException {
        log(number);
        dataStandardService.deleteByNumber(number);
    }


    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> search(@PathParam("number") String categoryId, DataStandardQuery parameters) throws AtlasBaseException {
        return dataStandardService.search(parameters);
    }

    @POST
    @Path("/history/{number}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> history(@PathParam("number") String number, Parameters parameters) throws AtlasBaseException {
        return dataStandardService.history(number, parameters);
    }

    @GET
    @Path("/download/template")
    public void downloadTemplate(@PathParam("downloadId") String downloadId) throws Exception {
        String homeDir = System.getProperty("atlas.home");
        String filePath = homeDir + "/conf/数据标准导入模版.xlsx";
        String fileName = filename(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }

    @POST
    @Path("/export/selected")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DownloadUri getDownloadURL(List<String> ids) throws Exception {
        String downloadId = UUID.randomUUID().toString();
        String address = request.getRequestURL().toString();
        String downURL = address + "/" + downloadId;
        dataQualityService.getDownloadList(ids, downloadId);
        DownloadUri uri = new DownloadUri();
        uri.setDownloadUri(downURL);
        return uri;
    }

    @GET
    @Path("/export/selected/{downloadId}")
    public void exportSelected(@PathParam("downloadId") String downloadId) throws Exception {
        List<String> ids = dataQualityService.getDownloadList(null, downloadId);
        File exportExcel = dataStandardService.exportExcel(ids);
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

    @GET
    @Path("/export/category/{categoryId}")
    public void exportCategoryId(@PathParam("categoryId") String categoryId) throws Exception {
        File exportExcel = dataStandardService.exportExcel(categoryId);
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
    public Response importDataStandard(@PathParam("categoryId") String categoryId,
                                       @FormDataParam("file") InputStream fileInputStream,
                                       @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        File file = null;
        try {
            String name =URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            if(!(name.endsWith(".xlsx") || name.endsWith(".xls"))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式错误");
            }

            file = new File(name);
            FileUtils.copyInputStreamToFile(fileInputStream, file);
            if(file.length() > MAX_EXCEL_FILE_SIZE) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件大小不能超过10M");
            }
            dataStandardService.importDataStandard(categoryId, fileInputStream);
            return Response.ok().build();
        } catch (AtlasBaseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }
}
