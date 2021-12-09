package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.HealthCheckVo;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.web.service.HealthCheckService;
import io.zeta.metaspace.web.util.PassWordUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;

@Path("health/check")
@Singleton
@Service
public class HealthCheckREST {

    @Autowired
    private HealthCheckService healthCheckService;

    /**
     * 健康检查接口-不做权限校验
     * @return
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public HealthCheckVo healthCheck() {
        return healthCheckService.healthCheck();
    }

    @POST
    @Path("password")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getPassword(String password) {
        return ReturnUtil.success(PassWordUtils.aesEncode(password));
    }
}
