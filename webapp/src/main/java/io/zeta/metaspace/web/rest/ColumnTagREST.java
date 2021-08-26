package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.table.column.tag.CreateTagRequest;
import io.zeta.metaspace.model.table.column.tag.DeleteRelationRequest;
import io.zeta.metaspace.model.table.column.tag.UpdateRelationRequest;
import io.zeta.metaspace.web.service.ColumnTagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;

@Path("column/tag")
@Singleton
@Service
public class ColumnTagREST {
    @Autowired
    private ColumnTagService columnTagService;

    @GET
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result getColumnTag(@HeaderParam("tenantId")String tenantId,@QueryParam("columnId")String columnId){
        return columnTagService.getColumnTag(tenantId,columnId);
    }

    @POST
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result createTag(@HeaderParam("tenantId")String tenantId, CreateTagRequest request){
        return columnTagService.createTag(tenantId,request.getTagName());
    }

    @POST
    @Path("/relation")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result updateRelation(@HeaderParam("tenantId")String tenantId, UpdateRelationRequest request){
        return columnTagService.createTagRelationToColumn(tenantId,request.getColumnId(),request.getTagIdList());
    }

    @DELETE
    @Path("/relation")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result deleteTagRelation(@HeaderParam("tenantId")String tenantId, DeleteRelationRequest request){
        return columnTagService.deleteTagRelationToColumn(tenantId,request.getColumnId(),request.getTagId());
    }

    @GET
    @Path("/duplicate")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result checkDuplicateNameTag(@HeaderParam("tenantId")String tenantId,@QueryParam("tagName")String tagName){
        return columnTagService.checkDuplicateNameTag(tenantId,tagName);
    }

    @GET
    @Path("/test")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result test(){
        return null;
    }
}
