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


package org.apache.atlas.web.filters;

import com.google.gson.Gson;
import org.apache.atlas.util.SSLClient;
import org.apache.atlas.web.util.DateTimeHelper;
import org.apache.atlas.web.util.Servlets;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@Component
public class SSOFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(AuditFilter.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    private final Long startTime = System.currentTimeMillis();
    private final Date date = new Date();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("SSOFilter initialization started");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        Enumeration<String> attributeNames = httpServletRequest.getSession().getAttributeNames();
        while(attributeNames.hasMoreElements()){
            String s = attributeNames.nextElement();
        }
        try {
            String requestURL = httpServletRequest.getRequestURL().toString();
            String[] split = requestURL.split("/");
            String welcome = split[0]+"//"+split[2]+""+"/welcome.jsp";
            if(requestURL.contains("/css/")||requestURL.contains("/img/")||requestURL.contains("/libs/") ||requestURL.contains("/js/")){
                filterChain.doFilter(request, response);
            }
                else if (httpServletRequest.getSession().getAttribute("user") != null) {
                Map user = (Map) httpServletRequest.getSession().getAttribute("user");
                String ticket = user == null ? "" : user.get("Ticket").toString();
                HashMap<String, String> header = new HashMap<>();
                header.put("ticket", ticket);
                String s = SSLClient.doGet("https://sso-cas.gridsumdissector.com/api/v2/info", header);
                Gson gson = new Gson();
                JSONObject jsonObject = gson.fromJson(s, JSONObject.class);
                Object message = jsonObject.get("message");
                if (message != null & (message.toString().equals("Success"))){
                    filterChain.doFilter(request, response);
                }else{
                    httpServletRequest.getSession().removeAttribute("user");
                    httpServletResponse.sendRedirect("https://sso-cas.gridsumdissector.com/login?service=" + welcome);
                }
            } else if (httpServletRequest.getParameter("ticket") != null) {
                String ticket = httpServletRequest.getParameter("ticket");
                HashMap<String, String> header = new HashMap<>();
                header.put("s-ticket", ticket);
                String s = SSLClient.doGet("https://sso-cas.gridsumdissector.com/api/v2/validate", header);
                Gson gson = new Gson();
                JSONObject jsonObject = gson.fromJson(s, JSONObject.class);
                Object message = jsonObject.get("message");
                if (message == null | (!message.toString().equals("Success"))) {
                    LOG.warn("用户信息获取失败");
                } else {
                    Map data = (Map) jsonObject.get("data");
                    if (data != null) {
                        HttpSession session = httpServletRequest.getSession();
                        session.setAttribute("user", data);
                        httpServletResponse.sendRedirect(requestURL.toString());
                    } else {
                        LOG.warn("用户信息获取失败");
                    }
                }
            } else {
                httpServletResponse.sendRedirect("https://sso-cas.gridsumdissector.com/login?service=" + welcome);
            }
        } finally {
            Map user = (Map) httpServletRequest.getSession().getAttribute("user");
            String username = user == null ? "" : user.get("LoginEmail").toString();
            long timeTaken = System.currentTimeMillis() - startTime;
            AuditLog auditLog = new AuditLog(username, httpServletRequest.getRemoteAddr(), httpServletRequest.getMethod(), Servlets.getRequestURL(httpServletRequest), date, httpServletResponse.getStatus(), timeTaken);
            AUDIT_LOG.info(auditLog.toString());
        }
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

    public static class AuditLog {
        private static final char FIELD_SEP = '|';

        private final String userName;
        private final String fromAddress;
        private final String requestMethod;
        private final String requestUrl;
        private final Date requestTime;
        private int httpStatus;
        private long timeTaken;

        public AuditLog(String userName, String fromAddress, String requestMethod, String requestUrl, Date requestTime, int httpStatus, long timeTaken) {
            this.userName = userName;
            this.fromAddress = fromAddress;
            this.requestMethod = requestMethod;
            this.requestUrl = requestUrl;
            this.requestTime = requestTime;
            this.httpStatus = httpStatus;
            this.timeTaken = timeTaken;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(DateTimeHelper.formatDateUTC(requestTime))
                    .append(FIELD_SEP).append(userName)
                    .append(FIELD_SEP).append(fromAddress)
                    .append(FIELD_SEP).append(requestMethod)
                    .append(FIELD_SEP).append(requestUrl)
                    .append(FIELD_SEP).append(httpStatus)
                    .append(FIELD_SEP).append(timeTaken);

            return sb.toString();
        }
    }
}
