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
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.util.SSLClient;
import org.apache.atlas.web.util.DateTimeHelper;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.configuration.Configuration;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
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
    private Configuration conf;
    private String loginURL;
    private String infoURL;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("SSOFilter initialization started");
        try {
            conf = ApplicationProperties.get();
            loginURL = conf.getString("sso.login.url");
            infoURL = conf.getString("sso.info.url");
            if (loginURL == null || infoURL == null || loginURL.equals("") || infoURL.equals("")) {
                LOG.warn("loginURL/infoURL config error");
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE,"sso.login.url,sso.info.url");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        try {
            String requestURL = httpServletRequest.getRequestURL().toString();
            if (requestURL.contains("/api/metaspace")) {
                String ticket ;
                if(httpServletRequest.getHeader("X-SSO-FullticketId")==null||httpServletRequest.getHeader("X-SSO-FullticketId")==""){
                    ticket=httpServletRequest.getParameter("X-SSO-FullticketId");
                }else{
                    ticket = httpServletRequest.getHeader("X-SSO-FullticketId");
                }
                if (ticket != null && ticket != "") {
                    HashMap<String, String> header = new HashMap<>();
                    header.put("ticket", ticket);
                    String s = SSLClient.doGet(infoURL, header);
                    Gson gson = new Gson();
                    JSONObject jsonObject = gson.fromJson(s, JSONObject.class);
                    Object message = jsonObject.get("message");
                    if (message == null || (!message.toString().equals("Success"))) {
                        LOG.warn("用户信息获取失败");
                        loginSkip(httpServletResponse, loginURL);
                    } else {
                        Map data = (Map) jsonObject.get("data");
                        if (data != null) {
                            HttpSession session = httpServletRequest.getSession();
                            session.setAttribute("user", data);
                            filterChain.doFilter(request, response);
                        } else {
                            loginSkip(httpServletResponse, loginURL);
                            LOG.warn("用户信息获取失败");
                        }
                    }
                } else {
                    loginSkip(httpServletResponse, loginURL);
                }
            }else{
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            httpServletResponse.setStatus(500);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("text/html;charset=utf-8");
            PrintWriter writer = httpServletResponse.getWriter();
            HashMap<String, String> hashMap = new HashMap();
            hashMap.put("error", "sso配置错误");
            String j = new Gson().toJson(hashMap);
            writer.print(j);
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;
            AuditLog auditLog = new AuditLog( httpServletRequest.getRemoteAddr(), httpServletRequest.getMethod(), Servlets.getRequestURL(httpServletRequest), date, httpServletResponse.getStatus(), timeTaken);
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
        hashMap.put("data", loginURL+"?service=");
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

    public static class AuditLog {
        private static final char FIELD_SEP = '|';

        private final String fromAddress;
        private final String requestMethod;
        private final String requestUrl;
        private final Date requestTime;
        private int httpStatus;
        private long timeTaken;

        public AuditLog( String fromAddress, String requestMethod, String requestUrl, Date requestTime, int httpStatus, long timeTaken) {

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
                    .append(FIELD_SEP).append(fromAddress)
                    .append(FIELD_SEP).append(requestMethod)
                    .append(FIELD_SEP).append(requestUrl)
                    .append(FIELD_SEP).append(httpStatus)
                    .append(FIELD_SEP).append(timeTaken);

            return sb.toString();
        }
    }
}
