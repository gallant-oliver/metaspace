package io.zeta.metaspace.web.rest.internal;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.Item;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiAudit;
import io.zeta.metaspace.model.share.ApiInfoV2;
import io.zeta.metaspace.model.share.AuditStatusEnum;
import io.zeta.metaspace.model.table.TableSource;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.service.AuditService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.TableService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
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

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Path("internal")
@Singleton
@Service
public class InternalREST {
    private static final Logger LOG = LoggerFactory.getLogger(InternalREST.class);
    @Autowired
    private UsersService usersService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private DataShareService dataShareService;
    @Autowired
    private TableService tableService;

    @GET
    @Path("/table/{tableId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableSource getTableInfo(@PathParam("tableId") String tableId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "InternalREST.getTableInfo()");
            }
            return tableService.getTableSource(tableId);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

}
