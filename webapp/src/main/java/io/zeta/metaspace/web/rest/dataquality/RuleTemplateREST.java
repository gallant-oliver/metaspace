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
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.dataquality.RuleTemplateService;
import io.zeta.metaspace.web.service.dataquality.TaskManageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


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
    public List<RuleTemplateType> templateCategory() throws AtlasBaseException {
        return RuleTemplateType.all().stream().map(ruleTemplateCategory -> {
            Integer categoryId = ruleTemplateCategory.getRuleType();
            long count = ruleTemplateService.countByCategoryId(categoryId);
            ruleTemplateCategory.setCount(count);
            return ruleTemplateCategory;
        }).collect(Collectors.toList());
    }

    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/{ruleType}/rules")
    public PageResult<RuleTemplate> getRuleTemplate(@PathParam("ruleType")Integer categoryId, Parameters parameters) throws AtlasBaseException {
        return ruleTemplateService.getRuleTemplate(categoryId, parameters);
    }

    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RuleTemplate> search(Parameters parameters) throws AtlasBaseException {
        return ruleTemplateService.search(parameters);
    }

    @POST
    @Path("/{templateId}/report")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Report2RuleTemplate> getReportByRuleType(@PathParam("templateId")String templateId,Parameters parameters) throws AtlasBaseException {
        return ruleTemplateService.getReportByRuleType(templateId, parameters);
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
    public List<TaskRuleExecutionRecord> getTaskRuleExecutionRecordList(@PathParam("executionId")String executionId) throws AtlasBaseException {
        return taskManageService.getTaskRuleExecutionRecordList(executionId);
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
