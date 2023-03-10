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

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.DELETE;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.dataquality.ReportError;
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableColumnRules;
import io.zeta.metaspace.model.result.TemplateResult;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.web.service.DataQualityService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
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

    /**
     * 添加模板
     *
     * @return String
     */
    @POST
    @Path("/template")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Response addTemplate(Template template,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), template.getTemplateName());
        try {
            dataQualityService.addTemplate(template,tenantId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"添加模板异常");
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
    @OperateType(DELETE)
    public Response deleteTemplate(@PathParam("templateId") String templateId) throws AtlasBaseException {
        Template template = dataQualityService.getTemplate(templateId);
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), template.getTemplateName());
        try {
            dataQualityService.deleteTemplate(templateId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"删除模板异常");
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
    @OperateType(UPDATE)
    public Response putTemplate(@PathParam("templateId") String templateId, Template template) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), template.getTemplateName());
        try {
            dataQualityService.updateTemplate(templateId, template);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"修改模板异常");
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
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取模板详情异常");
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
    public Response startTemplate(@PathParam("templateId") String templateId, @PathParam("templateStatus") int templateStatus) throws AtlasBaseException {
        try {
            dataQualityService.startTemplate(templateId, templateStatus);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"启动模板失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 暂停模板
     * @param templateId
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/template/status/disable/{templateId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Response stopTemplate(@PathParam("templateId") String templateId) throws AtlasBaseException {
        try {
            dataQualityService.stopTemplate(templateId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"暂停模板失败");
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
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取模板状态失败");
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
    public List<TemplateResult> getTemplates(@PathParam("tableId") String tableId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return dataQualityService.getTemplates(tableId,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取模板统计信息列表失败");
        }
    }

    @POST
    @Path("/reports/{tableGuid}/{templateId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getReports(@PathParam("tableGuid") String tableGuid, @PathParam("templateId") String templateId, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            PageResult pageResult = dataQualityService.getReports(tableGuid, templateId, parameters,tenantId);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取模块报表失败");
        }
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
            Report report = dataQualityService.getReport(reportId);
            return report;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取报表详情失败");
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
    public TableColumnRules getRules(@PathParam("tableId") String tableId, @PathParam("buildType") int buildType) throws AtlasBaseException {
        try {
            TableColumnRules rules = dataQualityService.getRules(tableId, buildType);
            return rules;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取计算规则失败");
        }
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
            String url = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace/quality/reports";
            return ExportDataPathUtils.generateURL(url, reportIds);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取下载报表url失败");
        }
    }

    @GET
    @Path("/reports/{downloadId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadReports(@PathParam("downloadId") String downloadId) throws AtlasBaseException {
        List<String> downloadList = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
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
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"下载报告失败");
        }
    }

    @PUT
    @Path("/report/alert/{reportId}/{status}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public void updateAlertStatus(@PathParam("reportId") String reportId,@PathParam("status") int status) throws AtlasBaseException {
        try {
            dataQualityService.updateAlertStatus(reportId, status);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"修改报告告警状态失败");
        }
    }
    
    @GET
    @Path("/template/{templateId}/percent")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ReportError getReportError(@PathParam("templateId") String templateId) throws AtlasBaseException {
        try {
            return dataQualityService.getLastReportError(templateId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取报表规则完成进度失败");
        }
    }


}
