package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.requirements.ApiCateDTO;
import io.zeta.metaspace.model.dto.requirements.DealDetailDTO;
import io.zeta.metaspace.web.service.RequirementsService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

/**
 *
 */
@Path("/requirements")
@Singleton
@Service
@Produces(Servlets.JSON_MEDIA_TYPE)
@Consumes(Servlets.JSON_MEDIA_TYPE)
public class RequirementsREST {

    @Autowired
    private RequirementsService requirementsService;

    @GET
    @Path("/deal/detail")
    public Result getDealDetail(@QueryParam("id") String id) {
        DealDetailDTO dealDetailDTO = requirementsService.getDealDetail(id);
        return ReturnUtil.success(dealDetailDTO);
    }

    @GET
    @Path("/category/{projectId}")
    public Result getCateategories(@PathParam("projectId") String projectId, @QueryParam("search") String search,
                                   @HeaderParam("tenantId") String tenantId) {
        List<ApiCateDTO> result = requirementsService.getCateategories(projectId, search, tenantId);
        return ReturnUtil.success(result);
    }

    @GET
    @Path("/api")
    public Result getCateategoryApis(@QueryParam("projectId") String projectId, @QueryParam("categoryId") String categoryId,
                                     @QueryParam("search") String search, @HeaderParam("tenantId") String tenantId) {
        List<ApiCateDTO> result = requirementsService.getCateategoryApis(projectId, categoryId, search, tenantId);
        return ReturnUtil.success(result);
    }

}
