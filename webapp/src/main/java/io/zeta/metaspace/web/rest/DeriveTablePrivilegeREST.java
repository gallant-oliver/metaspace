package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.CreateRequest;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.DeleteRequest;
import io.zeta.metaspace.model.table.column.tag.CreateTagRequest;
import io.zeta.metaspace.model.table.column.tag.DeleteRelationRequest;
import io.zeta.metaspace.model.table.column.tag.UpdateRelationRequest;
import io.zeta.metaspace.web.service.ColumnTagService;
import io.zeta.metaspace.web.service.DeriveTablePrivilegeService;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

@Path("derivetable")
@Singleton
@Service
public class DeriveTablePrivilegeREST {
    @Autowired
    private DeriveTablePrivilegeService deriveTablePrivilegeService;

    @GET
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result getColumnTag(@HeaderParam("tenantId")String tenantId,@QueryParam("columnId")String columnId){
        return null;
    }

    @POST
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result createDeriveTableUserGroupRelation(@HeaderParam("tenantId")String tenantId, CreateRequest request){
        return deriveTablePrivilegeService.createRelation(tenantId,request);
    }


    @DELETE
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result deleteRelations(@HeaderParam("tenantId")String tenantId, List<String> groupTableRelationIds){
        return deriveTablePrivilegeService.deleteRelations(groupTableRelationIds);
    }

}
