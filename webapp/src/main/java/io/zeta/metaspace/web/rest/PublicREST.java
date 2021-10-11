package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.PublicService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.RelationEntityV2;
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

    /**
     * 获取关联关系
     *
     * @param categoryGuid
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/relations/{categoryGuid}/{tenantId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getCategoryRelations(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery, @PathParam("tenantId") String tenantId) throws AtlasBaseException {
        return publicService.getCategoryRelations(categoryGuid, relationQuery, tenantId);
    }

    /**
     * 获取表关联
     *
     * @param relationQuery
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery) throws AtlasBaseException {
        return publicService.getQueryTables(relationQuery);
    }
}
