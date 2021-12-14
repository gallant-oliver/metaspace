package io.zeta.metaspace.web.rest.openapi;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.dataquality.TaskDTO;
import io.zeta.metaspace.model.dto.dataquality.TaskInstanceDTO;
import io.zeta.metaspace.model.dto.dataquality.TasksDTO;
import io.zeta.metaspace.model.enums.TaskExecuteStatus;
import io.zeta.metaspace.web.service.dataquality.TaskExecuteService;
import io.zeta.metaspace.web.service.dataquality.TaskManageService;
import io.zeta.metaspace.web.util.ObjectUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.zeta.metaspace.web.model.CommonConstant.HEADER_TENANT_ID;

/**
 * 给任务调用系统提供的任务管理模块 REST API
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-29
 */
@Slf4j
@Singleton
@Service
@Consumes(Servlets.JSON_MEDIA_TYPE)
@Produces(Servlets.JSON_MEDIA_TYPE)
@Path("/open-api/task-manage")
public class TaskManageOpenREST {
    
    @Context
    private HttpServletResponse response;
    @Autowired
    private TaskManageService taskManageService;
    @Autowired
    private TaskExecuteService taskExecuteService;
    
    @GET
    @Path("/tasks")
    public Result listEnableTasks(@HeaderParam(HEADER_TENANT_ID) String tenantId) {
        try {
            Assert.isTrue(StringUtils.isNotBlank(tenantId), "租户ID不能为空字符串");
            List<TaskDTO> list = Optional.ofNullable(taskManageService.listEnableTasks(tenantId))
                    .orElse(Collections.emptyList());
            return ReturnUtil.success(
                    TasksDTO.builder()
                            .total(list.size())
                            .taskList(list)
                            .build()
            );
        } catch (Exception e) {
            log.error("listEnableTasks error:", e);
            return ReturnUtil.error(
                    String.valueOf(AtlasErrorCode.BAD_REQUEST.getHttpCode().getStatusCode()),
                    e.getMessage());
        }
    }
    
    @POST
    @Path("/start/{taskId}")
    public Result startTask(@HeaderParam(HEADER_TENANT_ID) String tenantId,
                            @PathParam("taskId") String taskId) {
        try {
            Assert.isTrue(StringUtils.isNotBlank(tenantId), "租户ID不能为空字符串");
            Assert.isTrue(StringUtils.isNotBlank(taskId), "任务ID不能为空字符串");
            String instanceId = taskManageService.startTaskNow(taskId);
            return ReturnUtil.success(
                    TaskInstanceDTO.builder()
                            .instanceId(instanceId)
                            .instanceName(instanceId)
                            .build()
            );
        } catch (AtlasBaseException e) {
            log.error("startTask error:", e);
            return ReturnUtil.error(
                    String.valueOf(AtlasErrorCode.BAD_REQUEST.getHttpCode().getStatusCode()),
                    e.getMessage());
        }
    }
    
    @GET
    @Path("/stop/{instanceId}")
    public Result stopTask(@HeaderParam(HEADER_TENANT_ID) String tenantId,
                           @PathParam("instanceId") String instanceId) {
        try {
            Assert.isTrue(StringUtils.isNotBlank(tenantId), "租户ID不能为空字符串");
            Assert.isTrue(StringUtils.isNotBlank(instanceId), "任务执行记录ID不能为空字符串");
            String taskId = taskExecuteService.queryTaskIdByExecuteId(instanceId);
            taskManageService.stopTaskNow(taskId);
            return ReturnUtil.success(
                    TaskInstanceDTO.builder()
                            .instanceId(instanceId)
                            .instanceName(instanceId)
                            .build()
            );
        } catch (Exception e) {
            log.error("stopTask error:", e);
            return ReturnUtil.error(
                    String.valueOf(AtlasErrorCode.BAD_REQUEST.getHttpCode().getStatusCode()),
                    e.getMessage());
        }
    }
    
    @GET
    @Path("/status/{instanceId}")
    public Result queryTaskStatus(@HeaderParam(HEADER_TENANT_ID) String tenantId,
                                  @PathParam("instanceId") String instanceId) {
        try {
            Assert.isTrue(StringUtils.isNotBlank(tenantId), "租户ID不能为空字符串");
            Assert.isTrue(StringUtils.isNotBlank(instanceId), "任务执行记录ID不能为空字符串");
            TaskExecuteStatus status = taskExecuteService.queryTaskExecuteStatus(instanceId);
            return ReturnUtil.success(
                    TaskInstanceDTO.builder()
                            .instanceId(instanceId)
                            .instanceName(instanceId)
                            .state(status.name())
                            .build()
            );
        } catch (Exception e) {
            log.error("queryTaskStatus error:", e);
            return ReturnUtil.error(
                    String.valueOf(AtlasErrorCode.BAD_REQUEST.getHttpCode().getStatusCode()),
                    e.getMessage());
        }
    }
    
    @GET
    @Path("/log/download/{executionId}")
    public void downloadLog(@HeaderParam(HEADER_TENANT_ID) String tenantId,
                            @PathParam("executionId") String executionId) {
        File logFile = null;
        try {
            logFile = taskExecuteService.createExecutionLog(executionId);
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=".concat(logFile.getName()));
            IOUtils.copyBytes(new FileInputStream(logFile.getAbsolutePath()),
                    response.getOutputStream(), 4096, true);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("日志文件下载异常:", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "日志文件下载异常");
        } finally {
            ObjectUtils.isTrueThen(
                    logFile,
                    v -> Objects.nonNull(v) && v.exists(),
                    v -> log.debug("成功删除日志文件：{}", v.delete())
            );
        }
    }
}
