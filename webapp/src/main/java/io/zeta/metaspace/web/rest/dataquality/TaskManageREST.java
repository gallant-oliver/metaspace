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
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dataquality.Schedule;
import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.metadata.ColumnParameters;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.Queue;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.SearchService;
import io.zeta.metaspace.web.service.dataquality.RuleTemplateService;
import io.zeta.metaspace.web.service.dataquality.TaskManageService;
import io.zeta.metaspace.web.task.util.LivyTaskSubmitHelper;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.DELETE;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Singleton
@Service
@Path("/dataquality/taskManage")
public class TaskManageREST {

    @Context
    private HttpServletResponse response;
    @Autowired
    TaskManageService taskManageService;
    @Autowired
    SearchService searchService;
    @Autowired
    BusinessService businessService;
    @Autowired
    DataManageService dataManageService;
    @Autowired
    RuleTemplateService ruleTemplateService;
    @Autowired
    LivyTaskSubmitHelper livyTaskSubmitHelper;
    @Autowired
    DataShareService shareService;

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
    public PageResult<TaskHeader> getTaskList(@PathParam("my") Integer my, Parameters parameters, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return taskManageService.getTaskList(my, parameters,tenantId);
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
    @Path("/{taskId}/{executionId}/{subtaskId}/report/pdf")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ExecutionReportData getReportData(@PathParam("taskId")String taskId, @PathParam("executionId")String executionId,@PathParam("subtaskId")String subtaskId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return taskManageService.getTaskReportData(taskId, executionId,subtaskId,tenantId);
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
    public List<SubTaskRecord> getTaskRuleExecutionRecordList(@PathParam("executionId")String executionId, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        List<SubTaskRecord> subTaskRecords=taskManageService.getTaskRuleExecutionRecordList(executionId,"all",tenantId); //查看全部子任务
        return subTaskRecords;
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
        List<String> taskNameList = new ArrayList<>();
        for (String guid : taskList) {
            DataQualityBasicInfo info = taskManageService.getTaskBasicInfo(guid);
            if(null != info)
                taskNameList.add(info.getName());
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), "批量删除:[" + Joiner.on("、").join(taskNameList) + "]");
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
    public PageResult getDatabaseList(Parameters parameters,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        return searchService.getDatabasePageResult(true, parameters, tenantId, AdminUtils.getUserData().getAccount());
    }

    /**
     * 获取HIVE库下所有表列表
     * @param dbName
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/tables/{dbName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getTableList(@PathParam("dbName")String dbName, Parameters parameters) throws AtlasBaseException {
        return taskManageService.getTableList(dbName, parameters);
    }

    /**
     * 获取表字段列表
     * @param dbName
     * @param tableName
     * @param parameters
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/{dbName}/{tableName}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getColumnList(@PathParam("dbName")String dbName,@PathParam("tableName")String tableName, ColumnParameters parameters, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return taskManageService.getColumnList(dbName,tableName, parameters,tenantId);
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
    public PageResult searchTableList(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return searchService.getTablePageResultV2(true, parameters,tenantId,AdminUtils.getUserData().getAccount());
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
    public PageResult searchColumnList(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return searchService.getColumnPageResultV2(true, parameters,tenantId,AdminUtils.getUserData().getAccount());
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
    public List<CategoryPrivilege> getAll(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return dataManageService.getAllByUserGroup(CategoryType, tenantId);
    }

    /**
     * 获取规则分组下对当前校验对象有效的校验规则
     * @param groupId
     * @param objType
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{groupId}/{objType}/rules")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<RuleHeader> getRuleList(@PathParam("groupId")String groupId,@PathParam("objType")String objType,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return taskManageService.getValidRuleList(groupId, objType,tenantId);
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
    public List<TaskWarningHeader.WarningGroupHeader> getWarningGroup(@PathParam("groupId")String groupId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return taskManageService.getWarningGroupList(groupId,tenantId);
    }

    @GET
    @Path("/warningGroup")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TaskWarningHeader.WarningGroupHeader> getAllWarningGroupList(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return taskManageService.getAllWarningGroup(tenantId);
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
    public void addTask(TaskInfo taskInfo,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATAQUALITY.getAlias(), taskInfo.getTaskName());
        taskManageService.addDataQualityTask(taskInfo, tenantId);
    }

    @GET
    @Path("/info/{taskId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public EditionTaskInfo getTaskInfo(@PathParam("taskId")String taskId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return taskManageService.getTaskInfo(taskId,tenantId);
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
     * 开启任务(激活任务)
     *
     * @param taskId
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{taskId}/enable")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void enableTask(@PathParam("taskId") String taskId) throws AtlasBaseException {
        taskManageService.enableTask(taskId);
    }
    
    /**
     * 关闭任务(使任务不可用)
     *
     * @param taskId
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{taskId}/disable")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void disableTask(@PathParam("taskId") String taskId) throws AtlasBaseException {
        taskManageService.disableTask(taskId);
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
     *
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
     * 立即终止任务
     *
     * @param taskId
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{taskId}/cancel")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void stopTaskNow(@PathParam("taskId")String taskId) throws AtlasBaseException, InterruptedException {
        taskManageService.stopTaskNow(taskId);
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

    @POST
    @Path("/{ruleExecutionId}/relation")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response addReport2RuleType(@PathParam("ruleExecutionId")String ruleExecutionId, List<String> ruleTemplateList) throws AtlasBaseException {
        ruleTemplateService.addReport2RuleType(ruleExecutionId, ruleTemplateList);
        return Response.status(200).entity("success").build();
    }

    @GET
    @Path("/pools")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getPools(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        List<Queue> pools = taskManageService.getPools(tenantId);
        return ReturnUtil.success(pools);
    }

    /**
     * 获取数据源
     * @param parameters
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Permission({ModuleEnum.TASKMANAGE})
    public PageResult getDataSourceList(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            PageResult oracleDataSourceList = taskManageService.getDataSourceList(parameters, tenantId);

            return oracleDataSourceList;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取数据源失败");
        }
    }

    /**
     * 获取规则分组下对当前校验对象有效的校验规则
     * @param objType
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{objType}/rules")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<RuleHeader> getAllRuleList(@PathParam("objType")String objType, @HeaderParam("tenantId")String tenantId, @QueryParam("limit")int limit,@QueryParam("offset")int offset,
                                           @QueryParam("search")String search) throws AtlasBaseException {
        Parameters parameters = new Parameters();
        parameters.setLimit(limit);
        parameters.setOffset(offset);
        parameters.setQuery(search);
        return taskManageService.searchRules(parameters, objType,tenantId);
    }

    @POST
    @Path("/schedule/preview")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result schedulePreview(Schedule schedule) throws AtlasBaseException {
        return ReturnUtil.success(taskManageService.schedulePreview(schedule));
    }

    @POST
    @Path("/schedule/test")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result checkPreview(Schedule schedule) throws AtlasBaseException {
        return ReturnUtil.success(taskManageService.checkPreview(schedule));
    }


    /**
     * 获取错误结果
     * @param ruleExecutionId
     * @param tenantId
     * @param offset
     * @param limit
     * @param tableId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("{ruleExecutionId}/data")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataByList(@PathParam("ruleExecutionId")String ruleExecutionId, @HeaderParam("tenantId")String tenantId,
                                                  @QueryParam("offset")int offset, @QueryParam("limit")int limit, @QueryParam("tableId")String tableId) throws AtlasBaseException, IOException {
        Parameters parameters = new Parameters();
        parameters.setOffset(offset);
        parameters.setLimit(limit);
        ErrorData errorData = taskManageService.getErrorData(parameters, ruleExecutionId, tableId, tenantId);
        return ReturnUtil.success(errorData);
    }


    /**
     * 获取错误结果
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{executionId}/{subtaskId}/report/data")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downErrorData(@PathParam("executionId")String executionId, @HeaderParam("tenantIds")String tenantIds,
                                @PathParam("subtaskId")String subtaskId,@QueryParam("tenantId")String tenantId) throws AtlasBaseException, IOException {
        File exportExcel = taskManageService.exportExcelErrorData(executionId,subtaskId, tenantId);
        try {
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
        }catch (Exception e){
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST,e,"下载异常");
        }
    }

    public static String filename(String filePath) throws UnsupportedEncodingException {
        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
        filename = URLEncoder.encode(filename, "UTF-8");
        return filename;
    }

    @POST
    @Path("/{executionId}/{subtaskId}/report/download")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response downTaskReportData(@PathParam("executionId")String executionId, @HeaderParam("tenantId")String tenantId,
                                       @PathParam("subtaskId")String subtaskId,
                                       @FormDataParam("file") InputStream fileInputStream,
                                       @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws Exception {
        String name = new String(contentDispositionHeader.getFileName().getBytes("ISO-8859-1"), "UTF-8");
        try {
            File exportExcel = taskManageService.downTaskReportData(executionId,subtaskId,tenantId,fileInputStream,name);
            String filePath = exportExcel.getAbsolutePath();
            String fileName = filename(filePath);
            InputStream inputStream = new FileInputStream(filePath);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            IOUtils.copyBytes(inputStream, response.getOutputStream(), 4096, true);
            return Response.ok().build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"导入文件错误");
        }
    }
}
