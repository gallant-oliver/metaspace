package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.SearchService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;

@Path("technical")
@Singleton
@Service
public class TechnicalREST {
    @Autowired
    SearchService searchService;
    private static final Logger LOG = LoggerFactory.getLogger(TechnicalREST.class);
    /**
     * 返回全部的库
     *
     * @return List<Database>
     */
    @POST
    @Path("/search/database/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getAllDatabase(Parameters parameters,@PathParam("categoryId") String categoryId) throws AtlasBaseException {
            PageResult<Database> pageResult = searchService.getTechnicalDatabasePageResult(parameters,categoryId);
            return pageResult;
    }
    /**
     * 根据搜索条件返回表
     *
     * @return List<Table>
     */
    @POST
    @Path("/search/table/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Table> getTableByQuery(Parameters parameters,@PathParam("categoryId") String categoryId) throws AtlasBaseException {
            PageResult<Table> pageResult = searchService.getTechnicalTablePageResult(parameters,categoryId);
            return pageResult;
    }
}
