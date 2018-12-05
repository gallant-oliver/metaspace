package org.zeta.metaspace.web.rest;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.zeta.metaspace.utils.SSLClient;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.HashMap;
import java.util.Map;

@Path("user")
@Singleton
@Service
public class AdminREST {
    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.DiscoveryREST");
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;

    @GET
    @Path("/info")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Map loginInfo() throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "AdminREST.loginInfo()");
            }
            Map user = (Map) httpServletRequest.getSession().getAttribute("user");
            return user;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("/logout")
    public String loginOut() throws AtlasBaseException, AtlasException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "AdminREST.loginOut()");
            }
            Configuration conf = ApplicationProperties.get();
            String logoutURL = conf.getString("sso.logout.url");
            if (logoutURL == null || logoutURL.equals("") ) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE,"sso.logout.url");
            }
            HashMap<String, String> header = new HashMap<>();
            header.put("ticket", httpServletRequest.getHeader("X-SSO-FullticketId"));
            SSLClient.doDelete(logoutURL, header);
            return "success";
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

}
