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

package org.apache.atlas.web.resources;

import com.sun.jersey.multipart.FormDataParam;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.authorize.AtlasAdminAccessRequest;
import org.apache.atlas.authorize.AtlasEntityAccessRequest;
import org.apache.atlas.authorize.AtlasPrivilege;
import org.apache.atlas.authorize.AtlasAuthorizationUtils;
import org.apache.atlas.discovery.SearchContext;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.impexp.*;
import org.apache.atlas.model.metrics.AtlasMetrics;
import org.apache.atlas.repository.impexp.*;
import org.apache.atlas.services.MetricsService;
import org.apache.atlas.type.BaseAtlasType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.util.SearchTracker;
import org.apache.atlas.utils.AtlasJson;
import org.apache.atlas.web.filters.AtlasCSRFPreventionFilter;
import org.apache.atlas.web.service.ServiceState;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Jersey Resource for admin operations
 */
@Path("admin")
@Singleton
@Service
public class AdminResource {
    private static final Logger LOG = LoggerFactory.getLogger(AdminResource.class);

    private static final String isCSRF_ENABLED                 = "atlas.rest-csrf.enabled";
    private static final String BROWSER_USER_AGENT_PARAM       = "atlas.rest-csrf.browser-useragents-regex";
    private static final String CUSTOM_METHODS_TO_IGNORE_PARAM = "atlas.rest-csrf.methods-to-ignore";
    private static final String CUSTOM_HEADER_PARAM            = "atlas.rest-csrf.custom-header";
    private static final String isEntityUpdateAllowed          = "atlas.entity.update.allowed";
    private static final String isEntityCreateAllowed          = "atlas.entity.create.allowed";
    private static final String editableEntityTypes            = "atlas.ui.editable.entity.types";
    private static final String DEFAULT_EDITABLE_ENTITY_TYPES  = "hdfs_path,hbase_table,hbase_column,hbase_column_family,kafka_topic,hbase_namespace";
    private static final List TIMEZONE_LIST  = Arrays.asList(TimeZone.getAvailableIDs());

    @Context
    private HttpServletRequest httpServletRequest;

    @Context
    private HttpServletResponse httpServletResponse;

    private Response version;

    private static Configuration            atlasProperties;
    private final  ServiceState             serviceState;
    private final  MetricsService           metricsService;
    private final  ExportService            exportService;
    private final  ImportService            importService;
    private final  SearchTracker            activeSearches;
    private final  AtlasTypeRegistry        typeRegistry;
    private final  MigrationProgressService migrationProgressService;
    private final  ReentrantLock            importExportOperationLock;

    static {
        try {
            atlasProperties = ApplicationProperties.get();
        } catch (Exception e) {
            LOG.info("Failed to load application properties", e);
        }
    }

    @Inject
    public AdminResource(ServiceState serviceState, MetricsService metricsService, AtlasTypeRegistry typeRegistry,
                         ExportService exportService, ImportService importService, SearchTracker activeSearches,
                         MigrationProgressService migrationProgressService) {
        this.serviceState               = serviceState;
        this.metricsService             = metricsService;
        this.exportService = exportService;
        this.importService = importService;
        this.activeSearches = activeSearches;
        this.typeRegistry = typeRegistry;
        this.migrationProgressService = migrationProgressService;
        importExportOperationLock = new ReentrantLock();
    }

    /**
     * Fetches the thread stack dump for this application.
     *
     * @return json representing the thread stack dump.
     */
    @GET
    @Path("stack")
    @Produces(MediaType.TEXT_PLAIN)
    public String getThreadDump() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AdminResource.getThreadDump()");
        }

        ThreadGroup topThreadGroup = Thread.currentThread().getThreadGroup();

        while (topThreadGroup.getParent() != null) {
            topThreadGroup = topThreadGroup.getParent();
        }
        Thread[] threads = new Thread[topThreadGroup.activeCount()];

        int nr = topThreadGroup.enumerate(threads);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nr; i++) {
            builder.append(threads[i].getName()).append("\nState: ").
                    append(threads[i].getState()).append("\n");
            String stackTrace = StringUtils.join(threads[i].getStackTrace(), "\n");
            builder.append(stackTrace);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AdminResource.getThreadDump()");
        }

        return builder.toString();
    }

    /**
     * Fetches the version for this application.
     *
     * @return json representing the version.
     */
    @GET
    @Path("/version")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response getVersion()throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AdminResource.getVersion()");
        }

        if (version == null) {
            try {
                PropertiesConfiguration configProperties = new PropertiesConfiguration("atlas-buildinfo.properties");

                Map<String, Object> response = new HashMap<String, Object>();
                response.put("version", configProperties.getString("build.version", "UNKNOWN"));
                response.put("buildTime",configProperties.getString("build.time", "UNKNOWN"));
                version = Response.ok(AtlasJson.toV1Json(response)).build();

            } catch (ConfigurationException e) {
                throw new WebApplicationException(Servlets.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AdminResource.getVersion()");
        }

        return version;
    }

    @GET
    @Path("status")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response getStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AdminResource.getStatus()");
        }

        Map<String, Object> responseData = new HashMap() {{
                put(AtlasClient.STATUS, serviceState.getState().toString());
            }};

        if(serviceState.isInstanceInMigration()) {
            MigrationStatus status = migrationProgressService.getStatus();
            if (status != null) {
                responseData.put("MigrationStatus", status);
            }
        }

        Response response = Response.ok(AtlasJson.toV1Json(responseData)).build();

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AdminResource.getStatus()");
        }

        return response;
    }

    @GET
    @Path("session")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response getUserProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AdminResource.getUserProfile()");
        }

        Response response;

        boolean isEntityUpdateAccessAllowed = false;
        boolean isEntityCreateAccessAllowed = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        Set<String> groups = new HashSet<>();
        if (auth != null) {
            userName = auth.getName();
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            for (GrantedAuthority c : authorities) {
                groups.add(c.getAuthority());
            }

            isEntityUpdateAccessAllowed = AtlasAuthorizationUtils.isAccessAllowed(new AtlasEntityAccessRequest(typeRegistry, AtlasPrivilege.ENTITY_UPDATE));
            isEntityCreateAccessAllowed = AtlasAuthorizationUtils.isAccessAllowed(new AtlasEntityAccessRequest(typeRegistry, AtlasPrivilege.ENTITY_CREATE));
        }

        Map<String, Object> responseData = new HashMap<>();

        responseData.put(isCSRF_ENABLED, AtlasCSRFPreventionFilter.isCSRF_ENABLED);
        responseData.put(BROWSER_USER_AGENT_PARAM, AtlasCSRFPreventionFilter.BROWSER_USER_AGENTS_DEFAULT);
        responseData.put(CUSTOM_METHODS_TO_IGNORE_PARAM, AtlasCSRFPreventionFilter.METHODS_TO_IGNORE_DEFAULT);
        responseData.put(CUSTOM_HEADER_PARAM, AtlasCSRFPreventionFilter.HEADER_DEFAULT);
        responseData.put(isEntityUpdateAllowed, isEntityUpdateAccessAllowed);
        responseData.put(isEntityCreateAllowed, isEntityCreateAccessAllowed);
        responseData.put(editableEntityTypes, getEditableEntityTypes(atlasProperties));
        responseData.put("userName", userName);
        responseData.put("groups", groups);
        responseData.put("timezones", TIMEZONE_LIST);

        response = Response.ok(AtlasJson.toV1Json(responseData)).build();

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AdminResource.getUserProfile()");
        }

        return response;
    }

    @GET
    @Path("metrics")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public AtlasMetrics getMetrics(@QueryParam("ignoreCache") boolean ignoreCache) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AdminResource.getMetrics()");
        }

        AtlasMetrics metrics = metricsService.getMetrics(ignoreCache);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AdminResource.getMetrics()");
        }

        return metrics;
    }

    private void releaseExportImportLock() {
        importExportOperationLock.unlock();
    }

    @POST
    @Path("/export")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Response export(AtlasExportRequest request) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AdminResource.export()");
        }

        AtlasAuthorizationUtils.verifyAccess(new AtlasAdminAccessRequest(AtlasPrivilege.ADMIN_EXPORT), "export");

        acquireExportImportLock("export");

        ZipSink exportSink = null;
        try {
            exportSink = new ZipSink(httpServletResponse.getOutputStream());
            AtlasExportResult result = exportService.run(exportSink, request, Servlets.getUserName(httpServletRequest),
                                                         Servlets.getHostName(httpServletRequest),
                                                         AtlasAuthorizationUtils.getRequestIpAddress(httpServletRequest));

            exportSink.close();

            httpServletResponse.addHeader("Content-Encoding","gzip");
            httpServletResponse.setContentType("application/zip");
            httpServletResponse.setHeader("Content-Disposition",
                                          "attachment; filename=" + result.getClass().getSimpleName());
            httpServletResponse.setHeader("Transfer-Encoding", "chunked");

            httpServletResponse.getOutputStream().flush();
            return Response.ok().build();
        } catch (IOException excp) {
            LOG.error("export() failed", excp);

            throw new AtlasBaseException(excp);
        } finally {
            releaseExportImportLock();

            if (exportSink != null) {
                exportSink.close();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AdminResource.export()");
            }
        }
    }

    @POST
    @Path("/import")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public AtlasImportResult importData(@DefaultValue("{}") @FormDataParam("request") String jsonData,
                                        @FormDataParam("data") InputStream inputStream) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AdminResource.importData(jsonData={}, inputStream={})", jsonData, (inputStream != null));
        }

        AtlasAuthorizationUtils.verifyAccess(new AtlasAdminAccessRequest(AtlasPrivilege.ADMIN_IMPORT), "importData");

        acquireExportImportLock("import");
        AtlasImportResult result;

        try {
            AtlasImportRequest request = BaseAtlasType.fromJson(jsonData, AtlasImportRequest.class);
            ZipSource zipSource = new ZipSource(inputStream);

            result = importService.run(zipSource, request, Servlets.getUserName(httpServletRequest),
                    Servlets.getHostName(httpServletRequest),
                    AtlasAuthorizationUtils.getRequestIpAddress(httpServletRequest));
        } catch (Exception excp) {
            LOG.error("importData(binary) failed", excp);

            throw new AtlasBaseException(excp);
        } finally {
            releaseExportImportLock();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AdminResource.importData(binary)");
            }
        }

        return result;
    }

    @POST
    @Path("/importfile")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public AtlasImportResult importFile(String jsonData) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AdminResource.importFile()");
        }

        AtlasAuthorizationUtils.verifyAccess(new AtlasAdminAccessRequest(AtlasPrivilege.ADMIN_IMPORT), "importFile");

        acquireExportImportLock("importFile");

        AtlasImportResult result;

        try {
            AtlasImportRequest request = BaseAtlasType.fromJson(jsonData, AtlasImportRequest.class);
            result = importService.run(request, Servlets.getUserName(httpServletRequest),
                                       Servlets.getHostName(httpServletRequest),
                                       AtlasAuthorizationUtils.getRequestIpAddress(httpServletRequest));
        } catch (Exception excp) {
            LOG.error("importFile() failed", excp);

            throw new AtlasBaseException(excp);
        } finally {
            releaseExportImportLock();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AdminResource.importFile()");
            }
        }

        return result;
    }

    @GET
    @Path("activeSearches")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<String> getActiveSearches() {
        return activeSearches.getActiveSearches();
    }

    @DELETE
    @Path("activeSearches/{id}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean terminateActiveSearch(@PathParam("id") String searchId) {
        SearchContext terminate = activeSearches.terminate(searchId);
        return null != terminate;
    }

    private String getEditableEntityTypes(Configuration config) {
        String ret = DEFAULT_EDITABLE_ENTITY_TYPES;

        if (config != null && config.containsKey(editableEntityTypes)) {
            Object value = config.getProperty(editableEntityTypes);

            if (value instanceof String) {
                ret = (String) value;
            } else if (value instanceof Collection) {
                StringBuilder sb = new StringBuilder();

                for (Object elem : ((Collection) value)) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }

                    sb.append(elem.toString());
                }

                ret = sb.toString();
            }
        }

        return ret;
    }

    private void acquireExportImportLock(String activity) throws AtlasBaseException {
        boolean alreadyLocked = importExportOperationLock.isLocked();
        if (alreadyLocked) {
            LOG.warn("Another export or import is currently in progress..aborting this " + activity, Thread.currentThread().getName());

            throw new AtlasBaseException(AtlasErrorCode.FAILED_TO_OBTAIN_IMPORT_EXPORT_LOCK);
        }

        importExportOperationLock.lock();
    }
}
