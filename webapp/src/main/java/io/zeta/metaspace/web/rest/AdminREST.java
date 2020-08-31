package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.Item;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiAudit;
import io.zeta.metaspace.model.share.AuditStatusEnum;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.service.AuditService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.AdminUtils;
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
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
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
    @Autowired
    private AuditService auditService;

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
    public Item getUserItems(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return  usersService.getUserItems(tenantId);
        }catch (Exception e){
            LOG.warn("获取用户菜单失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"获取用户菜单失败");
        }

    }

    @GET
    @Path("/audit")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<ApiAudit> getPendingApiAudit(@HeaderParam("tenantId") String tenantId,
                                                   @DefaultValue("0") @QueryParam("offset") int offset,
                                                   @DefaultValue("10") @QueryParam("limit") int limit,
                                                   @QueryParam("statuses") List<AuditStatusEnum> statuses) throws AtlasBaseException {
        try {
            Parameters parameters = new Parameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);

            return auditService.getApiAuditList(parameters, tenantId, statuses, AdminUtils.getUserData().getUserId());
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取审核记录列表失败");
        }
    }

}
