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
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.utils.SSLClient;
import io.zeta.metaspace.web.service.UsersService;
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


@Component
public class SSOFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(SSOFilter.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    private String loginURL = SSOConfig.getLoginURL();
    private String infoURL = SSOConfig.getInfoURL();
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
        if (!requestURL.contains("/api/metaspace")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (requestURL.contains("v2/entity/uniqueAttribute/type/") || requestURL.endsWith("api/metaspace/v2/entity/") || requestURL.contains("/api/metaspace/admin/status")) {
            filterChain.doFilter(request, response);
            return;
        }
        String ticket = httpServletRequest.getHeader(TICKET_KEY);
        if (ticket == null || ticket == "") {
            ticket = httpServletRequest.getParameter(TICKET_KEY);
        }
        if (ticket == null || ticket.equals("")) {
            loginSkip(httpServletResponse, loginURL);
            return;
        }
        try {
            HashMap<String, String> header = new HashMap<>();
            header.put("ticket", ticket);
            String s = SSLClient.doGet(infoURL, header);
            Gson gson = new Gson();
            JSONObject jsonObject = gson.fromJson(s, JSONObject.class);
            Object message = jsonObject.get("message");
            if (message == null || (!message.toString().equals("Success"))) {
                LOG.warn("用户信息获取失败");
                loginSkip(httpServletResponse, loginURL);
                return;
            }
            Map data = (Map) jsonObject.get("data");
            if (data == null) {
                loginSkip(httpServletResponse, loginURL);
                LOG.warn("用户信息获取失败");
                return;
            }
            userName = data.getOrDefault("LoginEmail", userName).toString();
            HttpSession session = httpServletRequest.getSession();
            session.setAttribute("user", data);
            session.setAttribute("SSOTicket", ticket);
            //给新用户授予访客权限
            ServletContext servletContext = request.getServletContext();
            WebApplicationContext requiredWebApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
            UsersService usersService = (UsersService) requiredWebApplicationContext.getBean("getUserService");
            usersService.addUser(data);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            httpServletResponse.setStatus(500);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("text/html;charset=utf-8");
            PrintWriter writer = httpServletResponse.getWriter();
            HashMap<String, String> hashMap = new HashMap();
            hashMap.put("error", "sso异常");
            String j = new Gson().toJson(hashMap);
            writer.print(j);
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;
            AuditLog auditLog = new AuditLog(userName, httpServletRequest.getRemoteAddr(), httpServletRequest.getMethod(), Servlets.getRequestURL(httpServletRequest), date, httpServletResponse.getStatus(), timeTaken);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Servlets.getRequestPayload(httpServletRequest));
            }
            AUDIT_LOG.info(auditLog.toString());
        }

    }


    private void loginSkip(HttpServletResponse httpServletResponse, String loginURL) throws IOException {
        httpServletResponse.setStatus(401);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("text/html;charset=utf-8");
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
