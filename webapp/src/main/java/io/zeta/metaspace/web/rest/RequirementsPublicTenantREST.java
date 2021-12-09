package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.requirements.FeedbackResultDTO;
import io.zeta.metaspace.model.dto.requirements.ResourceDTO;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.RequirementsPublicTenantService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.inject.Singleton;
import javax.ws.rs.*;

import java.util.List;

import static io.zeta.metaspace.web.model.CommonConstant.HEADER_TENANT_ID;

/**
 * 需求管理 - 公共租户
 */
@Singleton
@Service
@Path("public/tenant/requirements")
@Consumes(Servlets.JSON_MEDIA_TYPE)
@Produces(Servlets.JSON_MEDIA_TYPE)
public class RequirementsPublicTenantREST {
    
    @Autowired
    private RequirementsPublicTenantService publicTenantService;
    
    @GET
    @Path("/paged/resource")
    public PageResult<ResourceDTO> pagedResource(@HeaderParam(HEADER_TENANT_ID) String tenantId,
                                                 @QueryParam("tableId") String tableId,
                                                 Parameters parameters) {
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID无效!");
        return publicTenantService.pagedResource(tableId, parameters);
    }

    /**
     * 需求下发
     *
     * @param
     * @throws
     */
    @POST
    @Path("/grant/{requirementId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result grant(@PathParam("requirementId") String requirementId) {
        try {
            publicTenantService.grant(requirementId);
            return ReturnUtil.success();
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
        }
    }

    /**
     * 删除需求
     *
     * @param
     * @throws
     */
    @DELETE
    @Path("")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result delete(List<String> guids) {
        try {
            publicTenantService.deleteRequirements(guids);
            return ReturnUtil.success();
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求删除失败");
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
            FeedbackResultDTO result = publicTenantService.getFeedbackResult(requirementId, resourceType);
            return ReturnUtil.success(result);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"获取需求反馈结果失败");
        }
    }
}
