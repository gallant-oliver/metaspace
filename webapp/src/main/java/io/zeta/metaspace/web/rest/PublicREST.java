package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.ApiHead;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.PublicService;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDatabaseService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
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

    @Autowired
    private SourceInfoDatabaseService sourceInfoDatabaseService;

    @Autowired
    private BusinessService businessService;

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
     * 获取关联关系-技术目录
     *
     * @param categoryGuid
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/technical/relations/{categoryGuid}/{tenantId}")
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

    /**
     * 业务对象列表
     *
     * @param categoryId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/business/relations/{categoryId}/{tenantId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessObject(@PathParam("categoryId") String categoryId, @PathParam("tenantId") String tenantId, Parameters parameters) throws AtlasBaseException {
        return publicService.getBusinessObject(categoryId, tenantId, parameters);
    }

    /**
     * 业务对象搜索
     *
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/business/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessList(Parameters parameters) throws AtlasBaseException {
        return publicService.getBusinessList(parameters);
    }

    /**
     * 获取源信息登记详情
     * @param tenantId
     * @param id
     * @param version
     * @return
     */
    @GET
    @Path("source/info/{id}/{tenantId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result getSourceInfoDetail(@PathParam("tenantId") String tenantId, @PathParam("id") String id, @QueryParam("version") @DefaultValue("0") String version) {
        return sourceInfoDatabaseService.getDatabaseInfoById(id, tenantId, Integer.parseInt(version));
    }

    /**
     * 业务对象详情
     *
     * @param businessId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}/{tenantId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BusinessInfo getBusiness(@PathParam("businessId") String businessId, @PathParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessInfo(businessId, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
        }
    }

    /**
     * 获取表详情
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/{guid}/{tenantId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Object getTableInfoById(@PathParam("guid") String guid, @PathParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getTableInfoById(guid, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表详情失败");
        }
    }

    /**
     * 获取关联的API列表
     *
     * @param businessId
     * @param parameters
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{businessId}/datashare/{tenantId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(@PathParam("businessId") String businessId, Parameters parameters, @PathParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessTableRelatedAPI(businessId, parameters, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取关联API失败");
        }
    }

    /**
     * 业务对象api展示列表
     *
     * @param businessId
     * @param isNew
     * @param up
     * @param down
     * @param limit
     * @param offset
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}/dataservice/{tenantId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getBusinessTableRelatedDataServiceAPI(@PathParam("businessId") String businessId, @DefaultValue("false") @QueryParam("new") boolean isNew,
                                                        @DefaultValue("true") @QueryParam("up") boolean up, @DefaultValue("true") @QueryParam("down") boolean down,
                                                        @DefaultValue("-1") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
                                                        @PathParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            Parameters parameters = new Parameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            PageResult<ApiHead> pageResult = businessService.getBusinessTableRelatedDataServiceAPI(businessId, parameters, isNew, up, down, tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取api展示列表失败");
        }
    }
}
