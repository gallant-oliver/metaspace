package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.dataassets.ColumnInfo;
import io.zeta.metaspace.model.dataassets.DataAssets;
import io.zeta.metaspace.model.dataassets.TableInfo;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableShow;
import io.zeta.metaspace.web.service.DataAssetsRetrievalService;
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
    public BusinessInfo getThemeDomains(//RelationQuery relationQuery,
                                        @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return null;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
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
    public BusinessInfo getThemes(@QueryParam("domainId") String domainId,
                                  //RelationQuery relationQuery,
                                  @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return null;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
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
    public BusinessInfo getBusinesses(@QueryParam("themeId") String themeId,
                                      //RelationQuery relationQuery,
                                      @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return null;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败");
        }
    }

    /**
     * 查询业务对象下数据表
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/business/{businessId}/tables")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TableInfo> getTableInfos(@PathParam("businessId") String businessId,
                                               @QueryParam("limit") int limit,
                                               @QueryParam("offset") int offset,
                                               @QueryParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.getTableInfos(" + businessId + " )");
            }
            return dataAssetsRetrievalService.getTableInfoByBusinessId(businessId, tenantId, limit, offset);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "字段信息查询失败");
        }
        finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 查询数据表字段信息
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/{tableId}/columnInfos")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<ColumnInfo> getColumnInfos(@PathParam("tableId") String tableId,
                                           @QueryParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.getColumnInfos(" + tableId + " )");
            }
            return dataAssetsRetrievalService.getColumnInfoByTableId(tableId, tenantId);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "字段信息查询失败");
        }
        finally {
            AtlasPerfTracer.log(perf);
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
    @Path("/table/{tableId}/preview")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableShow dataPreview(@PathParam("tableId") String tableId,
                                 @QueryParam("count") int count) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.dataPreview(" + tableId + ", " + count + " )");
            }

            return dataAssetsRetrievalService.dataPreview(tableId, count);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据失败");
        }
        finally {
            AtlasPerfTracer.log(perf);
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
    public PageResult<DataAssets> search(@QueryParam("limit") int limit,
                                         @QueryParam("offset") int offset,
                                         @QueryParam("type") int type,
                                         @QueryParam("name") String name,
                                         @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.search(" + type + " [搜索类型：0全部；1业务对象；2数据表] )");
            }
            return dataAssetsRetrievalService.search(type, offset, limit, tenantId, name);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据失败");
        }
        finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 数据资产查询
     *
     * @param type 搜索类型：1业务对象；2数据表
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataAssets getDataAssetsById(@PathParam("id") String id,
                                        @QueryParam("type") int type,
                                        @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.search(" + type + " [类型：1业务对象；2数据表] )");
            }
            return dataAssetsRetrievalService.getDataAssetsById(id, type, tenantId);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据失败");
        }
        finally {
            AtlasPerfTracer.log(perf);
        }
    }
}
