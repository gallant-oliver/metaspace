package org.apache.atlas.web.rest;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.Database;
import org.apache.atlas.model.metadata.Parameters;
import org.apache.atlas.model.result.PageResult;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.service.SearchService;
import org.apache.atlas.web.util.Servlets;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.Cookie;
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
    @Path("/logout")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String loginOut() throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "AdminREST.loginOut()");
            }
            Cookie[] cookies = httpServletRequest.getCookies();
            Map<String,Cookie> cookieMap =new HashMap();
            if(cookies!=null){
                for (Cookie cookie : cookies) {
                    cookieMap.put(cookie.getName(),cookie);
                }
            }
            Cookie cookie = cookieMap.get("metaspace-ticket");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            httpServletResponse.addCookie(cookie);
            httpServletRequest.getSession().removeAttribute("user");
            return "sucess";
        } catch (Exception e){
            return "fail";
        }finally {
            AtlasPerfTracer.log(perf);
        }
    }

}
