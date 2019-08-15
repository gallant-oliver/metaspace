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


import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.DELETE;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

import com.google.common.base.Joiner;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.dataquality2.ErrorInfo;
import io.zeta.metaspace.model.dataquality2.WarningGroup;
import io.zeta.metaspace.model.dataquality2.WarningInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.filter.OperateLogInterceptor;
import io.zeta.metaspace.web.model.ModuleEnum;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.service.dataquality.WarningGroupService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.service.UserService;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;


/**
 * 告警组
 */
@Singleton
@Service
@Path("/dataquality/warninggroup")
public class WarningGroupREST {

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Autowired
    private WarningGroupService warningGroupService;

    @Autowired
    private UsersService usersService;



    /**
     * 添加告警组
     * @param warningGroup
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    @Valid
    public void insert(WarningGroup warningGroup) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATA_QUALITY.getAlias(), warningGroup.getName());
        WarningGroup old = warningGroupService.getByName(warningGroup.getName());
        if (old != null) {
            throw new AtlasBaseException("告警组名已存在");
        }
        warningGroupService.insert(warningGroup);
    }

    /**
     * 编辑告警组
     * @param warningGroup
     * @throws AtlasBaseException
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    @Valid
    public void update(WarningGroup warningGroup) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATA_QUALITY.getAlias(), warningGroup.getName());
        warningGroupService.update(warningGroup);
    }

    /**
     * 批量删除告警组
     * @param idList
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/batch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByIdList(List<String> idList) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATA_QUALITY.getAlias(), "批量删除:[" + Joiner.on("、").join(idList) + "]");
        warningGroupService.deleteByIdList(idList);
    }

    /**
     * 告警组详情
     * @param id
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public WarningGroup getById(@PathParam("id") String id) throws AtlasBaseException {
        return warningGroupService.getById(id);
    }

    /**
     * 删除告警组
     * @param id
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteById(@PathParam("id") String id) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.DATA_QUALITY.getAlias(), id);
        warningGroupService.deleteById(id);
    }

    /**
     * 获取告警组列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<WarningGroup> getWarningGroupList(Parameters parameters) throws AtlasBaseException {
        return warningGroupService.getWarningGroupList(parameters);
    }

    /**
     * 搜索告警组列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<WarningGroup> search(Parameters parameters) throws AtlasBaseException {
        return warningGroupService.search(parameters);
    }

    /**
     * 获取告警列表
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/warning/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getWarningList(Parameters parameters) throws AtlasBaseException {
        return warningGroupService.getWarningList(parameters);
    }

    /**
     * 获取异常列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/error/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getErrorWarningList(Parameters parameters) throws AtlasBaseException {
        return warningGroupService.getErrorWarningList(parameters);
    }

    /**
     * 关闭任务告警
     * @param executionIdList
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/warnings")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void closeTaskWarning(List<String> executionIdList) throws AtlasBaseException {
        warningGroupService.closeTaskExecutionWarning(0, executionIdList);
    }


    /**
     * 关闭任务异常
     * @param executionIdList
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/errors")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void closeTaskError(List<String> executionIdList) throws AtlasBaseException {
        warningGroupService.closeTaskExecutionWarning(1, executionIdList);
    }


    /**
     * 获取告警详情
     * @param executionId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{executionId}/warning")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public WarningInfo getWarningInfo(@PathParam("executionId")String executionId) throws AtlasBaseException {
        return warningGroupService.getWarningInfo(executionId);
    }

    @GET
    @Path("/{executionId}/error")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ErrorInfo getErrorInfo(@PathParam("executionId")String executionId) throws AtlasBaseException {
        return warningGroupService.getErrorInfo(executionId);
    }

    /**
     * 获取用户列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<User> UserList(Parameters parameters) throws AtlasBaseException {
        try {
            return usersService.getUserList(parameters);
        } catch (Exception e) {
            throw e;
        }
    }
}
