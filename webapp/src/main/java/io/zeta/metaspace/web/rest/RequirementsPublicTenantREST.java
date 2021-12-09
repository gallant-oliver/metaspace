package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.dto.requirements.RequirementDTO;
import io.zeta.metaspace.model.dto.requirements.ResourceDTO;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.RequirementsPublicTenantService;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.inject.Singleton;
import javax.ws.rs.*;

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
    public PageResult<ResourceDTO> pagedResource(@QueryParam("tableId") String tableId,
                                                 Parameters parameters) {
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID无效!");
        return publicTenantService.pagedResource(tableId, parameters);
    }
    
    @POST
    @Path("/create/resource")
    public void createdResource(RequirementDTO requirementDTO) {
        Assert.notNull(requirementDTO, "需求对象为空");
        publicTenantService.createdResource(requirementDTO);
    }
    
    @PUT
    @Path("/edit/resource")
    public void editedResource(RequirementDTO requirementDTO) {
        Assert.notNull(requirementDTO, "需求对象为空");
        publicTenantService.editedResource(requirementDTO);
    }
    
}
