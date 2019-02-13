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
 * @date 2019/1/7 20:49
 */
package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.ReportResult;
import io.zeta.metaspace.model.result.TableColumnRules;
import io.zeta.metaspace.model.result.TemplateResult;
import io.zeta.metaspace.web.service.DataQualityService;
import io.zeta.metaspace.web.service.DataQualityV2Service;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.annotation.Generated;
import javax.inject.Singleton;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("quality")
@Singleton
@Service
public class DataQualityREST {
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;

    @Autowired
    private DataQualityService dataQualityService;
    @Autowired
    private DataQualityV2Service dataQualityV2Service;


    /**
     * 添加模板
     *
     * @return String
     */
    @POST
    @Path("/template")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String addTemplate(Template template) throws AtlasBaseException {
        try {
            dataQualityService.addTemplate(template);
            return "success";
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加模板异常");
        }
    }

    /**
     * 删除模板
     *
     * @return String
     */
    @DELETE
    @Path("/template/{templateId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String deleteTemplate(@PathParam("templateId") String templateId) throws AtlasBaseException {
        try {
            dataQualityService.deleteTemplate(templateId);
            return "success";
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加模板异常");
        }
    }

    /**
     * 修改模板
     *
     * @return String
     */
    @PUT
    @Path("/template/{templateId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String putTemplate(@PathParam("templateId") String templateId, Template template) throws AtlasBaseException {
        try {
            dataQualityService.updateTemplate(templateId, template);
            return "success";
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加模板异常");
        }
    }

    /**
     * 查看（克隆）模板详情
     *
     * @return Template
     */
    @GET
    @Path("/template/{templateId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Template getTemplate(@PathParam("templateId") String templateId) throws AtlasBaseException {
        try {
            return dataQualityService.viewTemplate(templateId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加模板异常");
        }
    }

    /**
     * 开始模板
     *
     * @return String
     */
    @PUT
    @Path("/template/status/enable/{templateId}/{templateStatus}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String startTemplate(@PathParam("templateId") String templateId, @PathParam("templateStatus") int templateStatus) throws AtlasBaseException {
        try {
            dataQualityService.startTemplate(templateId, templateStatus);
            return "success";
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    @PUT
    @Path("/template/status/disable/{templateId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String stopTemplate(@PathParam("templateId") String templateId) throws AtlasBaseException {
        try {
            dataQualityService.stopTemplate(templateId);
            return "success";
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    @GET
    @Path("/template/status/{templateId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Integer getTemplateStatus(@PathParam("templateId") String templateId) throws AtlasBaseException {
        try {
            return dataQualityService.getTemplateStatus(templateId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    /**
     * 获取数据质量模板统计信息列表
     *
     * @return List<TemplateResult>
     */
    @GET
    @Path("/templates/{tableId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TemplateResult> getTemplates(@PathParam("tableId") String tableId) {
        try {
            return dataQualityV2Service.getTemplates(tableId);
        } catch (SQLException e) {

        }
        return null;
    }

    /**
     * 获取一个模板的所有报表
     *
     * @return List<ReportResult>
     */
    @GET
    @Path("/reports/{templateId}/{offset}/{limit}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Map getReports(@PathParam("templateId") String templateId, @PathParam("offset") int offset, @PathParam("limit") int limit) {
        try {
            Map reports = dataQualityV2Service.getReports(templateId, offset, limit);
            return reports;
        } catch (SQLException e) {


        }
        return null;
    }

    /**
     * 获取报表详情
     *
     * @return Report
     */
    @GET
    @Path("/report/{reportId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Report getReport(@PathParam("reportId") String reportId) throws AtlasBaseException {
        try {
            Report report = dataQualityV2Service.getReport(reportId);
            return report;
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"sql异常");
        } catch (AtlasBaseException e) {
            throw e;
        }

    }

    /**
     * 根据生成方式获取规则
     *
     * @return TableColumnRules
     */
    @GET
    @Path("/rules/{tableId}/{buildType}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableColumnRules getRules(@PathParam("tableId") String tableId, @PathParam("buildType") int buildType) {
        try {
            TableColumnRules rules = dataQualityV2Service.getRules(tableId, buildType);
            return rules;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AtlasBaseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载报表
     *
     * @return DownloadUri
     */
    @POST
    @Path("/reports")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DownloadUri getDownloadURL(List<String> reportIds) throws AtlasBaseException {
        try {
            String downloadId = UUID.randomUUID().toString();
            String address = httpServletRequest.getRequestURL().toString();
            String downURL = address + "/" + downloadId;
            dataQualityService.getDownloadList(reportIds, downloadId);
            DownloadUri uri = new DownloadUri();
            uri.setDownloadUri(downURL);
            return uri;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/reports/{downloadId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadReports(@PathParam("downloadId") String downloadId) throws AtlasBaseException,IOException,SQLException {
        List<String> downloadList = dataQualityService.getDownloadList(null, downloadId);
        try {
            File zipFile = dataQualityService.exportExcel(downloadList);
            httpServletResponse.setContentType("application/msexcel;charset=utf-8");
            httpServletResponse.setCharacterEncoding("utf-8");
            long time = System.currentTimeMillis();
            String fileName = new String( new String(time + ".zip").getBytes(), "ISO-8859-1");
            // Content-disposition属性设置成以附件方式进行下载
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            OutputStream os = httpServletResponse.getOutputStream();
            os.write(FileUtils.readFileToByteArray(zipFile));
            os.close();
            zipFile.delete();
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    @PUT
    @Path("/report/alert/{reportId}/{status}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public void updateAlertStatus(@PathParam("reportId") String reportId,@PathParam("status") int status) throws AtlasBaseException {
        try {
            dataQualityService.updateAlertStatus(reportId, status);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    @GET
    @Path("/templates/percent/{templateId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Float getFinishedPercent(@PathParam("templateId") String templateId) throws AtlasBaseException {
        try {
            return dataQualityService.getFinishedPercent(templateId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

}
