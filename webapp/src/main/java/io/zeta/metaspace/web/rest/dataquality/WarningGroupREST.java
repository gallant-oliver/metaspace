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
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.service.dataquality.WarningGroupService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
    public void insert(WarningGroup warningGroup, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.ALARMGROUPMANAGE.getAlias(), "添加告警组：" + warningGroup.getName());
        WarningGroup old = warningGroupService.getByName(warningGroup.getName(),null,tenantId);
        if (old != null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"告警组名已存在");
        }
        warningGroupService.insert(warningGroup,tenantId);
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
    public void update(WarningGroup warningGroup,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        WarningGroup old = warningGroupService.getByName(warningGroup.getName(),warningGroup.getId(),tenantId);
        if (old != null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"告警组名已存在");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.ALARMGROUPMANAGE.getAlias(), "更新告警组：" + warningGroup.getName());
        warningGroupService.update(warningGroup);
    }

    /**
     * 删除告警组
     * @param warningGroupsLis
     * @throws AtlasBaseException
     */
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public void deleteByIdList(List<WarningGroup> warningGroupsLis) throws AtlasBaseException {
        if(null == warningGroupsLis || warningGroupsLis.size()==0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除告警组列表不能为空");
        }
        List<String> nameList = warningGroupsLis.stream().map(warningGroup -> warningGroup.getName()).collect(Collectors.toList());
        List<String> idList = warningGroupsLis.stream().map(warningGroup -> warningGroup.getId()).collect(Collectors.toList());
        HttpRequestContext.get().auditLog(ModuleEnum.ALARMGROUPMANAGE.getAlias(), "批量删除告警组:" + Joiner.on("、").join(nameList));
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
     * 获取告警组列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<WarningGroup> getWarningGroupList(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return warningGroupService.getWarningGroupList(parameters,tenantId);
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
    public PageResult<WarningGroup> search(Parameters parameters,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        return warningGroupService.search(parameters,tenantId);
    }

    /**
     * 获取告警列表
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/warning/{warnType}/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getWarningList(Parameters parameters,@PathParam("warnType")int warnType, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        return warningGroupService.getWarns(parameters,tenantId,warnType);
    }

    /**
     * 获取异常列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/error/{errorType}/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getErrorWarningList(@PathParam("errorType")int errorType, Parameters parameters,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        return warningGroupService.getErrors(parameters,tenantId,errorType);
    }

    /**
     * 关闭任务告警
     * @param warnNos
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/warnings")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void closeTaskWarning(List<String> warnNos) throws AtlasBaseException {
        warningGroupService.closeWarns(warnNos);
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
        warningGroupService.closeErrors(executionIdList);
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
    public WarningInfo getWarningInfo(@HeaderParam("tenantId") String tenantId,@PathParam("executionId")String executionId) throws AtlasBaseException {
        return warningGroupService.getWarningInfo(executionId,tenantId);
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
    public PageResult<User> userList(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return TenantService.defaultTenant.equals(tenantId)?usersService.getUserList(parameters) : usersService.getUserListV2(tenantId, parameters);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取用户列表失败");
        }
    }
}
