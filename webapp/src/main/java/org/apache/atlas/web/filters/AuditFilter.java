/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.web.filters;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasException;
import org.apache.atlas.RequestContext;
import org.apache.atlas.authorize.AtlasAuthorizationUtils;
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
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * This records audit information as part of the filter after processing the request
 * and also introduces a UUID into request and response for tracing requests in logs.
 */
@Component
public class AuditFilter implements Filter {
    private static final Logger LOG       = LoggerFactory.getLogger(AuditFilter.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("AuditFilter initialization started");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
    throws IOException, ServletException {
        final long                startTime          = System.currentTimeMillis();
        final Date                requestTime         = new Date();
        final HttpServletRequest  httpRequest        = (HttpServletRequest) request;
        final HttpServletResponse httpResponse       = (HttpServletResponse) response;
        final String              requestId          = UUID.randomUUID().toString();
        final Thread              currentThread      = Thread.currentThread();
        final String              oldName            = currentThread.getName();
        final String              user               = AtlasAuthorizationUtils.getCurrentUserName();
        final Set<String>         userGroups         = AtlasAuthorizationUtils.getCurrentUserGroups();

        try {
            currentThread.setName(formatName(oldName, requestId));

            RequestContext.clear();
            RequestContext requestContext = RequestContext.get();
            requestContext.setUser(user, userGroups);
            requestContext.setClientIPAddress(AtlasAuthorizationUtils.getRequestIpAddress(httpRequest));
            filterChain.doFilter(request, response);
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;

            recordAudit(httpRequest, requestTime, user, httpResponse.getStatus(), timeTaken);

            // put the request id into the response so users can trace logs for this request
            httpResponse.setHeader(AtlasClient.REQUEST_ID, requestId);
            currentThread.setName(oldName);
            RequestContext.clear();
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
}
