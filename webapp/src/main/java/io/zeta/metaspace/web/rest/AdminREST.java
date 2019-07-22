package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.result.Item;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.service.UsersService;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("user")
@Singleton
@Service
public class AdminREST {
    private static final Logger LOG = LoggerFactory.getLogger(AdminREST.class);
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private UsersService usersService;

    @GET
    @Path("/info")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Map loginInfo() throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "AdminREST.loginInfo()");
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
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "AdminREST.loginOut()");
            }
            Configuration conf = ApplicationProperties.get();
            String logoutURL = conf.getString("sso.logout.url");
            if (logoutURL == null || logoutURL.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.logout.url");
            }
            HashMap<String, String> header = new HashMap<>();
            header.put("ticket", httpServletRequest.getHeader("X-SSO-FullticketId"));
            OKHttpClient.doDelete(logoutURL, header);
            return "success";
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("item")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Item getUserItems() throws AtlasBaseException {
        try {
            return  usersService.getUserItems();
        }catch (Exception e){
            LOG.warn("获取用户菜单失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"获取用户菜单失败");
        }

    }

}
