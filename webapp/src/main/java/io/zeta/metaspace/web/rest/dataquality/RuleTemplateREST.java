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
package io.zeta.metaspace.web.rest.dataquality;


import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RuleParameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.dataquality.RuleTemplateService;
import io.zeta.metaspace.web.service.dataquality.TaskManageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 规则模版
 */
@Singleton
@Service
@Path("/dataquality/ruleTemplate")
public class RuleTemplateREST {

    @Autowired
    private RuleTemplateService ruleTemplateService;
    @Autowired
    private TaskManageService taskManageService;

    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/categories")
    public List<RuleTemplateType> templateCategory(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return RuleTemplateType.all().stream().map(ruleTemplateCategory -> {
            String categoryId = ruleTemplateCategory.getRuleType();
            long count = ruleTemplateService.countByCategoryId(categoryId,tenantId);
            ruleTemplateCategory.setCount(count);
            return ruleTemplateCategory;
        }).collect(Collectors.toList());
    }
    
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/{ruleType}/rules")
    public PageResult<RuleTemplate> getRuleTemplate(@HeaderParam("tenantId") String tenantId,
                                                    @PathParam("ruleType") String categoryId,
                                                    RuleParameters parameters) throws AtlasBaseException {
        return ruleTemplateService.getRuleTemplate(categoryId, parameters, tenantId);
    }
    
    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RuleTemplate> search(@HeaderParam("tenantId") String tenantId,
                                           RuleParameters parameters) throws AtlasBaseException {
        return ruleTemplateService.search(parameters, tenantId);
    }

    @POST
    @Path("/{templateId}/report")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Report2RuleTemplate> getReportByRuleType(@PathParam("templateId")String templateId, Parameters parameters, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return ruleTemplateService.getReportByRuleType(templateId, parameters,tenantId);
    }

    /**
     * 报告规则记录详情
     * @param executionId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{executionId}/record")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<SubTaskRecord> getTaskRuleExecutionRecordList(@PathParam("executionId")String executionId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return taskManageService.getTaskRuleExecutionRecordList(executionId,"all",tenantId);
    }

    /**
     * 任务详情-基本信息
     * @param taskId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/task/{taskId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataQualityBasicInfo getTaskBasicInfo(@PathParam("taskId")String taskId) throws AtlasBaseException {
        return taskManageService.getTaskBasicInfo(taskId);
    }
}
