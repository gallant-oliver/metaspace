// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================


package io.zeta.metaspace.web.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.utils.SSLClient;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.FilterUtils;
import io.zeta.metaspace.web.util.GuavaUtils;
import org.apache.atlas.web.filters.AuditLog;
import org.apache.atlas.web.util.Servlets;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


@Component
public class SSOFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(SSOFilter.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    private String loginURL = SSOConfig.getLoginURL();
    private String TICKET_KEY = "X-SSO-FullticketId";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        Date date = new Date();
        Long startTime = System.currentTimeMillis();
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String userName = "unknown";
        String requestURL = httpServletRequest.getRequestURL().toString();
        if (FilterUtils.isSkipUrl(requestURL)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String ticket = httpServletRequest.getHeader(TICKET_KEY);
            if (ticket == null || ticket.equals("")) {
                ticket = httpServletRequest.getParameter(TICKET_KEY);
            }
            if (ticket == null || ticket.equals("")) {
                loginSkip(httpServletResponse, loginURL);
                return;
            }
            Map data = GuavaUtils.getUserInfo(ticket);
            userName = data.getOrDefault("LoginEmail", userName).toString();
            //给新用户授予访客权限
            ServletContext servletContext = request.getServletContext();
            WebApplicationContext requiredWebApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
            UsersService usersService = (UsersService) requiredWebApplicationContext.getBean("getUserService");
            usersService.addUser(data);

        } catch (Exception e) {
            LOG.error("权限校验失败",e);
            loginSkip(httpServletResponse, loginURL);
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;
            AuditLog auditLog = new AuditLog(userName, httpServletRequest.getRemoteAddr(), httpServletRequest.getMethod(), Servlets.getRequestURL(httpServletRequest), date, httpServletResponse.getStatus(), timeTaken);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Servlets.getRequestPayload(httpServletRequest));
            }
            AUDIT_LOG.info(auditLog.toString());
        }
        filterChain.doFilter(request, response);
    }


    private void loginSkip(HttpServletResponse httpServletResponse, String loginURL) throws IOException {
        httpServletResponse.setStatus(401);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("text/plain;charset=utf-8");
        PrintWriter writer = httpServletResponse.getWriter();
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("error", "请检查用户登陆状态");
        hashMap.put("data", loginURL + "?service=");
        String j = new Gson().toJson(hashMap);
        writer.print(j);
    }


    public static void audit(AuditLog auditLog) {
        if (AUDIT_LOG.isInfoEnabled() && auditLog != null) {
            AUDIT_LOG.info(auditLog.toString());
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
