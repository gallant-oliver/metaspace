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
import io.zeta.metaspace.model.dataquality2.WarningGroup;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.filter.OperateLogInterceptor;
import io.zeta.metaspace.web.service.dataquality.WarningGroupService;
import org.apache.atlas.exception.AtlasBaseException;
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


    private void log(String content) {
        request.setAttribute(OperateLogInterceptor.OPERATELOG_OBJECT, "(数据质量告警组) " + content);
    }

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
        log(warningGroup.getName());
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
        log(warningGroup.getName());
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
        log("批量删除:[" + Joiner.on("、").join(idList) + "]");
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
        log(id);
        warningGroupService.deleteById(id);
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
     * 关闭规则告警
     * @param executionId
     * @param executionIdList
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{executionId}/warnings")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void closeTaskRuleExecutionWarning(@PathParam("executionId")String executionId, List<String> executionIdList) throws AtlasBaseException {
        warningGroupService.closeRuleExecutionWarning(0, executionId, executionIdList);
    }

    /**
     * 关闭任务告警
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
     * 关闭规则告警
     * @param executionId
     * @param executionIdList
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{executionId}/errors")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void closeTaskRuleExecutionError(@PathParam("executionId")String executionId, List<String> executionIdList) throws AtlasBaseException {
        warningGroupService.closeRuleExecutionWarning(1, executionId, executionIdList);
    }
}
