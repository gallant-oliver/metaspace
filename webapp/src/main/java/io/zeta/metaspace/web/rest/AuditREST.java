package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiAudit;
import io.zeta.metaspace.model.share.AuditStatusEnum;
import io.zeta.metaspace.web.service.AuditService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Path("audit")
@Singleton
@Service
public class AuditREST {

    @Autowired
    private AuditService auditService;
    private static final Logger LOG = LoggerFactory.getLogger(AuditREST.class);

    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<ApiAudit> getPendingApiAudit(@HeaderParam("tenantId") String tenantId,
                                                   @DefaultValue("0") @QueryParam("offset") int offset,
                                                   @DefaultValue("10") @QueryParam("limit") int limit,
                                                   @QueryParam("search") String search,
                                                   @QueryParam("statuses") List<AuditStatusEnum> statuses,
                                                   @QueryParam("non-statuses") List<AuditStatusEnum> nonStatuses,
                                                   @QueryParam("applicant") String applicant) throws AtlasBaseException {
        try {
            Parameters parameters = new Parameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            parameters.setQuery(search);

            return auditService.getApiAuditList(parameters, tenantId, statuses,nonStatuses, applicant);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取审核记录列表失败");
        }
    }


    @PUT
    @Path("/{auditId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response processAudit(@HeaderParam("tenantId") String tenantId,
                                 @PathParam("auditId") String auditId,
                                 ApiAudit apiAudit) throws AtlasBaseException {
        try {
            ApiAudit oldApiAudit = auditService.getApiAuditById(auditId, tenantId);
            HttpRequestContext.get().auditLog(ModuleEnum.DATASHARE.getAlias(), MessageFormat.format("审核 Api : {0} {1}", oldApiAudit.getApiGuid(), oldApiAudit.getApiVersion()));

            auditService.updateApiAudit(tenantId, auditId, apiAudit.getStatus(), apiAudit.getReason());
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "处理审核失败");
        }
        return Response.status(200).entity("success").build();
    }

}
