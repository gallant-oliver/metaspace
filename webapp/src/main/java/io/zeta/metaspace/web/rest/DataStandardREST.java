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


import static io.zeta.metaspace.web.service.DataStandardService.filename;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.model.business.ColumnCheckMessage;
import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

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

    @POST
    @Path("/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void insert(DataStandard dataStandard) throws AtlasBaseException {
        dataStandardService.insert(dataStandard);
    }


    @GET
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void get(@PathParam("id") String id) throws AtlasBaseException {
        dataStandardService.get(id);
    }

    @POST
    @Path("/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> queryByCatetoryId(@PathParam("categoryId") String categoryId, Parameters parameters) throws AtlasBaseException {
        return dataStandardService.queryPageByCatetoryId(categoryId, parameters);
    }


    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> search(Parameters parameters) throws AtlasBaseException {
        return dataStandardService.search(parameters);
    }

    @POST
    @Path("/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataStandard> history(@PathParam("number") String number, Parameters parameters) throws AtlasBaseException {
        return dataStandardService.history(number, parameters);
    }

    @DELETE
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void delete(@PathParam("id") String id) throws AtlasBaseException {
        dataStandardService.delete(id);
    }


    @DELETE
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void categoryId(@PathParam("ids") List<String> ids) throws AtlasBaseException {
        dataStandardService.deleteList(ids);
    }

    @PUT
    @Path("/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void update(DataStandard dataStandard) throws AtlasBaseException {
        dataStandardService.update(dataStandard);
    }


    @GET
    @Path("/download/template")
    public void downloadTemplate(@PathParam("downloadId") String downloadId) throws Exception {
        String homeDir = System.getProperty("atlas.home");
        String filePath = homeDir + "/template/数据标准导入模版.xls";
        String fileName = filename(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
    }

    @POST
    @Path("/export/selected")
    public void export(List<String> ids) throws Exception {
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
    public void export(@PathParam("categoryId") String categoryId) throws Exception {
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
    public void importDataStandard(@PathParam("categoryId") String categoryId,
                                   @FormDataParam("file") InputStream fileInputStream) throws Exception {
        dataStandardService.importDataStandard(fileInputStream);
    }


}
