package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.tag.Tag2Table;
import io.zeta.metaspace.web.service.TableTagService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.ibatis.annotations.Delete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("table")
@Singleton
@Service
public class TagREST {
    private static final Logger LOG = LoggerFactory.getLogger(TagREST.class);
    @Autowired
    TableTagService tableTagService;
    @Path("/tag")
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String addTag(Map<String,String> request) throws AtlasBaseException {
    String tagname = request.get("tagname");
        tableTagService.addTag(tagname);
        return "success";
    }
    @Path("/tags")
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Tag> getTags(Parameters parameters) throws AtlasBaseException {
        List<Tag> tags = tableTagService.getTags(parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
        return tags;
    }
    @Path("/table2tab")
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String addtable2tab(Tag2Table tag2Table) throws AtlasBaseException {
        tableTagService.addTable2Tag(tag2Table.getTable(),tag2Table.getTagId());
        return "success";
    }
    @Path("/tag/{tagId}")
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String deleteTag(@PathParam("tagId") String tagId) throws AtlasBaseException {
        tableTagService.deleteTag(tagId);
        return "success";
    }
    @Path("/tag/tag2table/{tableguId}/{tagId}")
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)

    public String deletetag2table(@PathParam("tableguId") String tableguId,@PathParam("tagId") String tagId) throws AtlasBaseException {
        tableTagService.deleteTable2Tag(tableguId,tagId);
        return "success";
    }
}
