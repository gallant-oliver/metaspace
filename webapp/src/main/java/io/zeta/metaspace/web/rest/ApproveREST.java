package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveParas;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.inject.Singleton;
import javax.ws.rs.*;

@Path("approve")
@Singleton
@Service
public class ApproveREST {

    @Autowired
    private ApproveService approveService;



    @POST
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApproveList(
            @HeaderParam("tenantId") String tenantId, ApproveParas params) throws AtlasBaseException {
        try {
            PageResult<ApproveItem> pageResult =  approveService.search(params, tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException("审批项列表及搜索失败", AtlasErrorCode.BAD_REQUEST, "审批项列表及搜索失败");
        }
    }


    @POST
    @Path("/deal")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deal(
            @HeaderParam("tenantId") String tenantId, ApproveParas params) throws AtlasBaseException {
        try {
            approveService.deal(params, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"审批失败");
        }
    }

    @GET
    @Path("/detail")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result detail(
            @HeaderParam("tenantId") String tenantId, @QueryParam("id") String id,@QueryParam("businessType") String businessType,@QueryParam("version") int version,@QueryParam("moduleId") String moduleId) throws AtlasBaseException {
        try {
            Object detailInfo = approveService.ApproveObjectDetail(tenantId, id, businessType, version, moduleId);
            return ReturnUtil.success(detailInfo);
        }catch (AtlasBaseException e){  //业务异常
            throw e;
        }
        catch (Exception e) {  //系统异常
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"获取指标详情失败");
        }
    }


}
