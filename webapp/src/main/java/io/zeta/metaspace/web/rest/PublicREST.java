package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.web.service.PublicService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;

/**
 * 公共租户接口
 */

@Path("public")
@Singleton
@Service
public class PublicREST {

    @Autowired
    private PublicService publicService;

    /**
     * 获取所有目录
     *
     * @param type
     */
    @GET
    @Path("/category/{type}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getCategory(@PathParam("type") Integer type) {
        return ReturnUtil.success(publicService.getCategory(type));
    }
}
