package io.zeta.metaspace.web.rest;

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

import static io.zeta.metaspace.web.model.CommonConstant.HEADER_TENANT_ID;

/**
 * 需求管理 - 公共租户
 */
@Singleton
@Service
@Path("public-tenant/requirements")
@Consumes(Servlets.JSON_MEDIA_TYPE)
@Produces(Servlets.JSON_MEDIA_TYPE)
public class RequirementsPublicTenantREST {
    
    @Autowired
    private RequirementsPublicTenantService publicTenantService;
    
    @GET
    @Path("/paged-resource")
    public PageResult<ResourceDTO> pagedResource(@HeaderParam(HEADER_TENANT_ID) String tenantId,
                                                 @QueryParam("tableId") String tableId,
                                                 Parameters parameters) {
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID无效!");
        return publicTenantService.pagedResource(tableId, parameters);
    }
}
