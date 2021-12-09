package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.requirements.FeedbackResultDTO;
import io.zeta.metaspace.model.dto.requirements.RequirementDTO;
import io.zeta.metaspace.model.dto.requirements.RequirementsHandleDTO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.web.service.RequirementsService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;

/**
 * 需求管理 - 普通租户
 */
@Path("requirements")
@Singleton
@Service
public class RequirementsREST {


    @Autowired
    private RequirementsService requirementsService;


    @POST
    @Path("test")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public void test() {
    }



    /**
     * 需求详情
     *
     * @param
     * @throws
     */
    @GET
    @Path("/{requirementId}/detail")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result detail(@PathParam("requirementId") String requirementId) {
        try {
            RequirementDTO requirement = requirementsService.getRequirementById(requirementId);
            return ReturnUtil.success(requirement);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
        }
    }

    /**
     * 表是否为重要表、保密表
     *
     * @param
     * @throws
     */
    @GET
    @Path("/table/{tableId}/status")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result getTableStatus(@PathParam("tableId") String tableId) {
        try {
            SourceInfoDeriveTableInfo tableStatus = requirementsService.getTableStatus(tableId);
            return ReturnUtil.success(tableStatus);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
        }
    }

    /**
     * 需求处理
     *
     * @param
     * @throws
     */
    @POST
    @Path("/handle")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result handle(RequirementsHandleDTO resultDTO) {
        try {
            requirementsService.handle(resultDTO);
            return ReturnUtil.success();
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
        }
    }

    /**
     * 反馈结果
     *
     * @param
     * @throws
     */
    @GET
    @Path("/{requirementId}/feedback")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result feedback(@PathParam("requirementId") String requirementId, @QueryParam("resourceType") Integer resourceType) {
        try {
            FeedbackResultDTO result = requirementsService.getFeedbackResult(requirementId, resourceType);
            return ReturnUtil.success(result);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
        }
    }
}
