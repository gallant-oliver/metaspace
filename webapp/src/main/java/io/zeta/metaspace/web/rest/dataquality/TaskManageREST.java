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
 * @date 2019/7/24 10:47
 */
package io.zeta.metaspace.web.rest.dataquality;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 10:47
 */

import com.google.common.base.Joiner;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.dataquality2.DataQualityBasicInfo;
import io.zeta.metaspace.model.dataquality2.DataQualityTask;
import io.zeta.metaspace.model.dataquality2.EditionTaskInfo;
import io.zeta.metaspace.model.dataquality2.ExecutionLog;
import io.zeta.metaspace.model.dataquality2.ExecutionLogHeader;
import io.zeta.metaspace.model.dataquality2.ExecutionReportData;
import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleHeader;
import io.zeta.metaspace.model.dataquality2.TaskExecutionReport;
import io.zeta.metaspace.model.dataquality2.TaskHeader;
import io.zeta.metaspace.model.dataquality2.TaskInfo;
import io.zeta.metaspace.model.dataquality2.TaskRuleExecutionRecord;
import io.zeta.metaspace.model.dataquality2.TaskRuleHeader;
import io.zeta.metaspace.model.dataquality2.TaskWarningHeader;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.SearchService;
import io.zeta.metaspace.web.service.dataquality.TaskManageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.*;

@Singleton
@Service
@Path("/dataquality/taskManage")
public class TaskManageREST {

    @Autowired
    TaskManageService taskManageService;

    @Autowired
    SearchService searchService;

    @Autowired
    BusinessService businessService;

    @Autowired
    DataManageService dataManageService;

    private static final int CategoryType = 4;

    /**
     * 获取任务列表
     * @param my
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{my}/tasks")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TaskHeader> getTaskList(@PathParam("my") Integer my, Parameters parameters) throws AtlasBaseException {
        return taskManageService.getTaskList(my, parameters);
    }

    /**
     * 报告详情
     * @param taskId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{taskId}/report")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TaskExecutionReport getTaskExecutionReport(@PathParam("taskId")String taskId) throws AtlasBaseException {
        return taskManageService.getTaskExecutionReport(taskId);
    }

    /**
     * 报告详情
     * @param taskId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{taskId}/{executionId}/report/pdf")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ExecutionReportData getReportData(@PathParam("taskId")String taskId, @PathParam("executionId")String executionId) throws AtlasBaseException {
        return taskManageService.getTaskReportData(taskId, executionId);
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
     * 删除任务
     * @param taskList
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/tasks")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteTaskList(List<String> taskList) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), "批量删除:[" + Joiner.on("、").join(taskList) + "]");
        taskManageService.deleteTaskList(taskList);
    }

    /**
     * 获取数据库列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/databases")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getDatabaseList(Parameters parameters) throws AtlasBaseException {
        return searchService.getDatabasePageResult(parameters);
    }

    /**
     * 获取库下所有表列表
     * @param databaseId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/tables/{databaseId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getTableList(@PathParam("databaseId")String databaseId, Parameters parameters) throws AtlasBaseException {
        return taskManageService.getTableList(databaseId, parameters);
    }

    /**
     * 获取表字段列表
     * @param tableGuid
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/{tableGuid}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getColumnList(@PathParam("tableGuid")String tableGuid, Parameters parameters) throws AtlasBaseException {
        return taskManageService.getColumnList(tableGuid, parameters);
    }

    /**
     * 搜索表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/search/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult searchTableList(Parameters parameters) throws AtlasBaseException {
        return searchService.getTablePageResultV2(parameters);
    }

    /**
     * 搜索字段
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/search/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult searchColumnList(Parameters parameters) throws AtlasBaseException {
        return searchService.getColumnPageResultV2(parameters);
    }

    /**
     * 获取规则分组列表
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/rule/groups")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getAll() throws AtlasBaseException {
        return dataManageService.getAll(CategoryType);
    }

    /**
     * 获取规则分组下对当前校验对象有效的校验规则
     * @param groupId
     * @param objType
     * @param objIdList
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{groupId}/{objType}/rules")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<RuleHeader> getRuleList(@PathParam("groupId")String groupId,@PathParam("objType")Integer objType, List<String> objIdList) throws AtlasBaseException {
        return taskManageService.getValidRuleList(groupId, objType, objIdList);
    }

    /**
     * 根据分组获取告警组
     * @param groupId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{groupId}/warningGroup")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TaskWarningHeader.WarningGroupHeader> getWarningGroup(@PathParam("groupId")String groupId) throws AtlasBaseException {
        return taskManageService.getWarningGroupList(groupId);
    }

    @GET
    @Path("/warningGroup")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TaskWarningHeader.WarningGroupHeader> getAllWarningGroupList() throws AtlasBaseException {
        return taskManageService.getAllWarningGroup();
    }

    /**
     * 添加任务
     * @param taskInfo
     * @throws AtlasBaseException
     */
    @POST
    @Path("/task")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public void addTask(TaskInfo taskInfo) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), taskInfo.getTaskName());
        taskManageService.addTask(taskInfo);
    }

    @GET
    @Path("/info/{taskId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public EditionTaskInfo getTaskInfo(@PathParam("taskId")String taskId) throws AtlasBaseException {
        return taskManageService.getTaskInfo(taskId);
    }


    /**
     * 修改任务
     * @param taskInfo
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/task")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public void updateTask(DataQualityTask taskInfo) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), taskInfo.getName());
        taskManageService.updateTask(taskInfo);
    }

    /**
     * 开启任务
     * @param taskId
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{taskId}/enable")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void startTask(@PathParam("taskId")String taskId) throws AtlasBaseException {
        taskManageService.startTask(taskId);
    }

    /**
     * 关闭任务
     * @param taskId
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{taskId}/disable")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void stopTask(@PathParam("taskId")String taskId) throws AtlasBaseException {
        taskManageService.stopTask(taskId);
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

    /**
     * 立即执行任务
     * @param taskId
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{taskId}/execute")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void startTaskNow(@PathParam("taskId")String taskId) throws AtlasBaseException {
        taskManageService.startTaskNow(taskId);
    }

    /**
     * 任务规则列表
     * @param taskId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{taskId}/rules")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TaskRuleHeader> getRuleList(@PathParam("taskId")String taskId, Parameters parameters) throws AtlasBaseException {
        return taskManageService.getRuleList(taskId, parameters);
    }

    /**
     * 任务日志列表
     * @param taskId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{taskId}/log")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<ExecutionLogHeader> getExecutionLogList(@PathParam("taskId")String taskId, Parameters parameters) throws AtlasBaseException {
        return taskManageService.getExecutionLogList(taskId, parameters);
    }

    /**
     * 任务某次执行日志详情
     * @param ruleExecutionId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{ruleExecutionId}/log")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ExecutionLog getExecutionLogList(@PathParam("ruleExecutionId")String ruleExecutionId) throws AtlasBaseException {
        return taskManageService.getExecutionLogList(ruleExecutionId);
    }

}