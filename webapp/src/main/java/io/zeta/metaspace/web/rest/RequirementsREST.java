package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.web.service.RequirementsService;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 */
@Path("requirements")
@Singleton
@Service
@RestController
public class RequirementsREST {

    @Autowired
    private RequirementsService requirementsService;

    @POST
    @Path("test")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public void test() {
    }
}
