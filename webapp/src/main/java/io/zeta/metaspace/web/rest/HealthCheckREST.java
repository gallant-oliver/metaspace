package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.HealthCheckVo;
import io.zeta.metaspace.web.service.HealthCheckService;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
}
