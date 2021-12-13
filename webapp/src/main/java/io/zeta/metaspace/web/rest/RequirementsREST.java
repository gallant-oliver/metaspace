package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.metadata.TableExtInfo;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.RequirementsService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;

/**
 * 需求管理 - 普通租户
 */
@Path("/requirements")
@Singleton
@Service
@Produces(Servlets.JSON_MEDIA_TYPE)
@Consumes(Servlets.JSON_MEDIA_TYPE)
public class RequirementsREST {


    @Autowired
    private RequirementsService requirementsService;

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
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"查询需求详情失败");
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
            TableExtInfo tableStatus = requirementsService.getTableStatus(tableId);
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

    /**
     * 处理详情查询
     */
    @GET
    @Path("/deal/detail")
    public Result getDealDetail(@QueryParam("id") String id) {
        DealDetailDTO dealDetailDTO = requirementsService.getDealDetail(id);
        return ReturnUtil.success(dealDetailDTO);
    }

    /**
     * 需求反馈-目录列表查询
     */
    @GET
    @Path("/category/{projectId}")
    public Result getCateategories(@PathParam("projectId") String projectId, @QueryParam("search") String search,
                                   @HeaderParam("tenantId") String tenantId) {
        List<ApiCateDTO> result = requirementsService.getCategories(projectId, search, tenantId);
        return ReturnUtil.success(result);
    }

    /**
     * 需求反馈-API列表查询
     */
    @GET
    @Path("/api")
    public Result getCateategoryApis(@QueryParam("projectId") String projectId, @QueryParam("categoryId") String categoryId,
                                     @QueryParam("search") String search, @HeaderParam("tenantId") String tenantId) {
        List<ApiCateDTO> result = requirementsService.getCategoryApis(projectId, categoryId, search, tenantId);
        return ReturnUtil.success(result);
    }

    /**
     * 需求反馈提交
     */
    @POST
    @Path("/feedback")
    @OperateType(INSERT)
    public Result requirementsFeedback(RequirementsFeedbackCommit commitInput) {
        HttpRequestContext.get().auditLog(ModuleEnum.REQUIREMENTMANAGEMENT.getAlias(), "需求反馈：" + commitInput.getRequirementsId());
        requirementsService.feedback(commitInput);
        return ReturnUtil.success("success");
    }


    /**
     * 需求处理列表
     * @param param
     * @param tenantId
     * @return
     */
    @POST
    @Path("handle/list")
    public PageResult getHandleListPage(RequireListParam param, @HeaderParam("tenantId") String tenantId) {
        return requirementsService.getHandleListPage(param, tenantId);
    }

    /**
     * 需求反馈列表
     * @param param
     * @param tenantId
     * @return
     */
    @POST
    @Path("return/list")
    public PageResult getReturnListPage(RequireListParam param, @HeaderParam("tenantId") String tenantId) {
        return requirementsService.getReturnListPage(param, tenantId);
    }
}
