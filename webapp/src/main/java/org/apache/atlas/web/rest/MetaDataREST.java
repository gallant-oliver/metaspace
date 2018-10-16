package org.apache.atlas.web.rest;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.Database;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.List;

@Path("metadata")
@Singleton
@Service
public class MetaDataREST {
    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.DiscoveryREST");

    @Context
    private HttpServletRequest httpServletRequest;

    /**
     * 返回全部的库、表、字段
     * @return List<Database>
     */
    @GET
    @Path("/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Database> searchUsingDSL() throws AtlasBaseException {
        return null;
    }

}
