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

import org.apache.atlas.AtlasException;
import org.apache.atlas.util.AtlasRepositoryConfiguration;
import org.apache.atlas.web.util.DateTimeHelper;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;


@Component
public class SSOFilter implements Filter {
    private static final Logger LOG       = LoggerFactory.getLogger(AuditFilter.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("SSOFilter initialization started");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {


        HttpServletResponse httpServletResponse=(HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        StringBuffer requestURL = httpServletRequest.getRequestURL();
        if(httpServletRequest.getParameter("ticket")!=null){
            String ticket = httpServletRequest.getParameter("ticket");
            httpServletResponse.setHeader("s-ticket",ticket);
            httpServletResponse.sendRedirect("https://sso-cas.gridsumdissector.com/api/v2/validate?service="+requestURL);

        }else {
            httpServletResponse.sendRedirect("https://sso-cas.gridsumdissector.com/login?service=" + requestURL);
        }
    }

    private String formatName(String oldName, String requestId) {
        return oldName + " - " + requestId;
    }

    private void recordAudit(HttpServletRequest httpRequest, Date when, String who, int httpStatus, long timeTaken) {
        final String fromAddress = httpRequest.getRemoteAddr();
        final String whatRequest = httpRequest.getMethod();
        final String whatURL     = Servlets.getRequestURL(httpRequest);
        final String whatUrlPath = httpRequest.getRequestURL().toString(); //url path without query string

        if (!isOperationExcludedFromAudit(whatRequest, whatUrlPath.toLowerCase(), null)) {
            audit(new AuditLog(who, fromAddress, whatRequest, whatURL, when, httpStatus, timeTaken));
        } else {
            if(LOG.isDebugEnabled()) {
                LOG.debug(" Skipping Audit for {} ", whatURL);
            }
        }
    }

    public static void audit(AuditLog auditLog) {
        if (AUDIT_LOG.isInfoEnabled() && auditLog != null) {
            AUDIT_LOG.info(auditLog.toString());
        }
    }

    boolean isOperationExcludedFromAudit(String requestHttpMethod, String requestOperation, Configuration config) {
        try {
            return AtlasRepositoryConfiguration.isExcludedFromAudit(config, requestHttpMethod, requestOperation);
        } catch (AtlasException e) {
            return false;
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
        private final Date   requestTime;
        private       int    httpStatus;
        private       long   timeTaken;

        public AuditLog(String userName, String fromAddress, String requestMethod, String requestUrl) {
            this(userName, fromAddress, requestMethod, requestUrl, new Date());
        }

        public AuditLog(String userName, String fromAddress, String requestMethod, String requestUrl, Date requestTime) {
            this(userName, fromAddress, requestMethod, requestUrl, requestTime, HttpServletResponse.SC_OK, 0);
        }

        public AuditLog(String userName, String fromAddress, String requestMethod, String requestUrl, Date requestTime, int httpStatus, long timeTaken) {
            this.userName      = userName;
            this.fromAddress   = fromAddress;
            this.requestMethod = requestMethod;
            this.requestUrl    = requestUrl;
            this.requestTime   = requestTime;
            this.httpStatus    = httpStatus;
            this.timeTaken     = timeTaken;
        }

        public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }

        public void setTimeTaken(long timeTaken) { this.timeTaken = timeTaken; }

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
