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
 * @date 2019/4/12 17:14
 */
package io.zeta.metaspace.web.rest.timelimit;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/12 17:14
 */

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.timelimit.TimeLimitOperEnum;
import io.zeta.metaspace.model.timelimit.TimeLimitRequest;
import io.zeta.metaspace.model.timelimit.TimeLimitSearch;
import io.zeta.metaspace.web.service.timelimit.TimeLimitService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.zeta.metaspace.model.operatelog.OperateType;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Path("timelimit")
@Singleton
@Service
public class TimeLimitREST {

    @Autowired
    private TimeLimitService timeLimitService;

    @PUT
    @Path("/add")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @OperateType(INSERT)
    public Result add(TimeLimitRequest request, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            timeLimitService.addTimeLimit(request,tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.TIMELIMIT.getAlias(), "添加时间限定"+request.getName());
            return ReturnUtil.success(); //无异常返回成功信息
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/list")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @OperateType(UPDATE)
    public Result list(TimeLimitSearch search, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            return ReturnUtil.success(timeLimitService.search(search,tenantId)); //无异常返回成功信息
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/edit")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Result edit(TimeLimitRequest request, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            timeLimitService.editTimeLimit(request,tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.TIMELIMIT.getAlias(), "编辑时间限定"+request.getName());
            return ReturnUtil.success(); //无异常返回成功信息
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/operate")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @OperateType(OperateTypeEnum.DELETE)
    public Result operate(TimeLimitRequest request, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            if(TimeLimitOperEnum.PUBLISH.getCode().equals(request.getType())){  //发布操作，暂时不需要
                timeLimitService.publish(request,tenantId);
            }else if(TimeLimitOperEnum.CANCEL.getCode().equals(request.getType())){ //下线，暂时不需要
                timeLimitService.cancel(request,tenantId);
            }else{ //删除
                timeLimitService.delTimeLimit(request,tenantId);
                HttpRequestContext.get().auditLog(ModuleEnum.TIMELIMIT.getAlias(), "删除时间限定"+request.getName());
            }
            return ReturnUtil.success(); //无异常返回成功信息
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/relation")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Result relation(TimeLimitSearch request, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            return ReturnUtil.success(timeLimitService.realtion(request,tenantId)); //无异常返回成功信息
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 此接口为发布历史接口，暂时不需要
     * @param request
     * @param tenantId
     * @return
     * @throws Exception
     */
    @POST
    @Path("/history")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Result history(TimeLimitSearch request, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            return ReturnUtil.success(timeLimitService.history(request,tenantId)); //无异常返回成功信息
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }


}
