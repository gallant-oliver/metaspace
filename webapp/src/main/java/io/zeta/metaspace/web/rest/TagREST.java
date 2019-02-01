package io.zeta.metaspace.web.rest;

import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Map;
import java.util.UUID;

@Path("/table")
@Singleton
@Service
public class TagREST {
    private static final Logger LOG = LoggerFactory.getLogger(TagREST.class);
    @Path("/tag")
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String addTag(Map<String,String> request) throws Exception {
    String tagname = request.get("tagname");

        return "success";
    }

}
