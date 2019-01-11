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
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.List;

@Path("quality")
@Singleton
@Service
public class DataQualityREST {
    @Context
    private HttpServletRequest httpServletRequest;

    @Autowired
    private final DataQualityService qualityService;

    public DataQualityREST(DataQualityService qualityService) {
        this.qualityService = qualityService;
    }

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
            qualityService.addTemplate(template);
            return "success";
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw  e;
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
            qualityService.deleteTemplate(templateId);
            return "success";
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw  e;
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
    public String putTemplate(@PathParam("templateId") String templateId,Template template) throws AtlasBaseException {
        try {
            qualityService.updateTemplate(templateId, template);
            return "success";
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw  e;
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
            return qualityService.viewTemplate(templateId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw  e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加模板异常");
        }
    }
    /**
     * 变更模板状态
     *
     * @return String
     */
    @PUT
    @Path("/template/status/{templateId}/{templateStatus}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String putTemplateStatus(@PathParam("templateId") String templateId,@PathParam("templateStatus") int templateStatus){
        return "success";
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
    public List<TemplateResult> getTemplates(@PathParam("tableId") String tableId){
        return null;
    }
    /**
     * 获取一个模板的所有报表
     *
     * @return List<ReportResult>
     */
    @GET
    @Path("/reports/{templateId}/{offect}/{limit}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<ReportResult> getReports(@PathParam("templateId") String templateId, @PathParam("offect") int offect, @PathParam("limit") int limit){
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
    public Report getReport(@PathParam("reportId") String reportId){
        return null;
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
    public TableColumnRules getRules(@PathParam("tableId") String tableId,@PathParam("buildType") String buildType){
        return null;
    }
    /**
     * 下载报表
     *
     * @return DownloadUri
     */
    @GET
    @Path("/quality/reports")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DownloadUri downloadReports(List<String> reportIds){
        return null;
    }
}
