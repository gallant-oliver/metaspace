package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.web.service.RequirementsService;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * 需求管理 - 普通租户
 */
@Path("requirements")
@Singleton
@Service
@Consumes(Servlets.JSON_MEDIA_TYPE)
@Produces(Servlets.JSON_MEDIA_TYPE)
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
