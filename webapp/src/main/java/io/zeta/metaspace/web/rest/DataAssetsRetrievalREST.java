package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.dataassets.BussinessObjectList;
import io.zeta.metaspace.model.dataassets.DomainInfo;
import io.zeta.metaspace.model.dataassets.ThemeInfo;
import io.zeta.metaspace.model.metadata.GuidCount;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.web.service.DataAssetsRetrievalService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/11/10 14:35
 * @Description
 */

@Singleton
@Service
@Path("/dataassets/retrieval")
public class DataAssetsRetrievalREST {
    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.DataAssetsRetrievalREST");

    @Autowired
    private DataAssetsRetrievalService dataAssetsRetrievalService;
    /**
     * 查询主题域（即一级业务目录）信息列表
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/domains")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getThemeDomains(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            List<DomainInfo> result = dataAssetsRetrievalService.getThemeDomains(tenantId);
            return ReturnUtil.success(result);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取主题域列表失败:" + e.getMessage());
        }
    }

    /**
     * 查询主题（即二级业务目录）信息列表
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{domainId}/themes")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getThemes(@PathParam("domainId") String domainId,
                            @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            List<ThemeInfo> result = dataAssetsRetrievalService.getThemes(domainId, tenantId);
            return ReturnUtil.success(result);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取主题列表失败:" + e.getMessage());
        }
    }

    /**
     * 查询主题（即二级业务目录）下业务对象列表
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{themeId}/businesses")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getBusinesses(@PathParam("themeId") String themeId, @DefaultValue("-1") @QueryParam("limit") int limit,
                                @DefaultValue("0") @QueryParam("offset") int offset,
                                @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            BussinessObjectList result = dataAssetsRetrievalService.getBusinesses(themeId, tenantId, limit, offset);
            return ReturnUtil.success(result);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败:" + e.getMessage());
        }
    }

    /**
     * 查询业务对象下挂载表列表
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}/tables")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BusinessInfo getTables(@QueryParam("businessId") String businessId,
                                  RelationQuery relationQuery,
                                  @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return null;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
        }
    }

    /**
     * 查询表字段信息
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{tableId}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BusinessInfo getColumns(@QueryParam("tableId") String tableId,
                                   RelationQuery relationQuery,
                                   @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return null;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
        }
    }

    /**
     * 表数据预览
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/preview")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BusinessInfo dataPreview(GuidCount guidCount) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.selectRDBMSData(" + guidCount.getGuid() + ", " + guidCount.getCount() + " )");
            }
            //TableShow tableShow = .getRDBMSTableShow(guidCount);
            return null;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
        }
    }

    /**
     * 数据资产搜索
     *
     * @param type 搜索类型：0全部；1业务对象；2数据表
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BusinessInfo search(@QueryParam("type") int type,
                               RelationQuery relationQuery,
                               @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return null;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
        }
    }
}
