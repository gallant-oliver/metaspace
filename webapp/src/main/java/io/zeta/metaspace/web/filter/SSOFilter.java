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


import com.google.gson.Gson;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.FilterUtils;
import io.zeta.metaspace.web.util.GuavaUtils;
import org.apache.atlas.web.filters.AuditLog;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


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
        HttpRequestContext httpRequestContext = HttpRequestContext.get();
        httpRequestContext.setRequest(httpServletRequest);
        httpRequestContext.setIp(getIpAdress(httpServletRequest));
        try {
            if (FilterUtils.isSkipUrl(requestURL)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (httpServletRequest.getMethod().equals("OPTIONS")) {
                filterChain.doFilter(request, response);
                return;
            }
            String loginData = loginURL + "?service=";
            try {
                String ticket = httpServletRequest.getHeader(TICKET_KEY);
                if (ticket == null || ticket.equals("")) {
                    ticket = httpServletRequest.getParameter(TICKET_KEY);
                }
                if (ticket == null || ticket.equals("")) {
                    loginSkip(401, httpServletResponse, "认证票据不能为空", loginData);
                    return;
                }
                Map data = GuavaUtils.getUserInfo(ticket);
                userName = data.getOrDefault("LoginEmail", userName).toString();
                //给新用户授予访客权限
                ServletContext servletContext = request.getServletContext();
                WebApplicationContext requiredWebApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
                UsersService usersService = (UsersService) requiredWebApplicationContext.getBean("getUserService");
                String userId = data.get("AccountGuid").toString();


            } catch (Exception e) {
                LOG.error("认证校验失败", e);
                loginSkip(401, httpServletResponse, "认证校验失败" + e.getMessage(), loginData);
                return;
            }
            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                LOG.error("错误内容如下", e);
                loginSkip(400, httpServletResponse, e.getMessage(), "");
            }
        }finally {
            long timeTaken = System.currentTimeMillis() - startTime;
            AuditLog auditLog = new AuditLog(userName, getIpAdress(httpServletRequest), httpServletRequest.getMethod(), Servlets.getRequestURL(httpServletRequest), date, httpServletResponse.getStatus(), timeTaken);
            if(requestURL.contains("/roles/sso/incr")) {
                return;
            }
            AUDIT_LOG.info(auditLog.toString());
        }


    }

    private void loginSkip(int status, HttpServletResponse httpServletResponse, String errorMessage, String data) throws IOException {
        httpServletResponse.setStatus(status);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json;charset=utf-8");
        PrintWriter writer = httpServletResponse.getWriter();
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("error", errorMessage);
        hashMap.put("data", data);
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

    private String getIpAdress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
