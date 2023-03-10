package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dataassets.*;
import io.zeta.metaspace.model.dataquality2.ExecutionRecordPage;
import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.metadata.RuleParameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableShow;
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
            PageResult result = dataAssetsRetrievalService.getBusinesses(themeId, tenantId, limit, offset);
            return ReturnUtil.success(result);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取业务对象列表失败:" + e.getMessage());
        }
    }

    /**
     * 查询业务对象下数据表
     *
     * @param belongTenantId 所属租户id
     * @param tenantId 当前租户id
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
                                               @QueryParam("belongTenantId") String belongTenantId,
                                               @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.getTableInfos(" + businessId + " )");
            }
            return dataAssetsRetrievalService.getTableInfoByBusinessId(businessId, belongTenantId, tenantId, limit, offset);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "表信息查询失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 查询数据表字段信息
     *
     * @param belongTenantId 所属租户id
     * @param tenantId 当前租户id
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/{tableId}/columnInfos")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<ColumnInfo> getColumnInfos(@PathParam("tableId") String tableId,
                                           @QueryParam("belongTenantId") String belongTenantId,
                                           @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.getColumnInfos(" + tableId + " )");
            }
            return dataAssetsRetrievalService.getColumnInfoByTableId(tableId, belongTenantId, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "字段信息查询失败");
        } finally {
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
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询预览数据失败");
        } finally {
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
                                         @QueryParam("query") String query,
                                         @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.search(" + type + " [搜索类型：0全部；1业务对象；2数据表] )");
            }
            return dataAssetsRetrievalService.search(type, offset, limit, tenantId, query);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据资产失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 数据资产查询
     *
     * @param id         数据资产id
     * @param type       搜索类型：1业务对象；2数据表；3主题；4、任务；5、标准
     * @param businessId 所属业务对象id（当type=2，即资产类型为数据表时需要传参）
     * @param belongTenantId 所属租户id
     * @param tenantId 当前租户id
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataAssetsById(@PathParam("id") String id,
                                        @QueryParam("type") int type,
                                        @QueryParam("businessId") String businessId,
                                        @QueryParam("belongTenantId") String belongTenantId,
                                        @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.search(" + type + " [类型：1业务对象；2数据表] )");
            }
            return dataAssetsRetrievalService.getDataAssetsById(id, type, belongTenantId, tenantId, businessId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据资产失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据（数据/命名）标准id查询对应的（数据/命名）标准关联的质量规则列表详情
     * @param id （数据/命名）标准id
     * @param offset 质量规则分页
     * @param limit 质量规则分页
     * @return （数据/命名）标准关联的质量规则列表详情
     */
    @GET
    @Path("/rule/list/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RuleTemplate> getRetrievalRuleList(@PathParam("id") String id,
                                                         @QueryParam("offset") int offset,
                                                         @QueryParam("limit") int limit) {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.getRetrievalRuleList( 使用数据标准id： " + id + " 查询数据标准详情 )");
            }
            RuleParameters parameters = new RuleParameters();
            parameters.setOffset(offset);
            parameters.setLimit(limit);
            return dataAssetsRetrievalService.getStandardDetailListByDataStandardId(id, parameters);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据资产失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据任务ID（taskId）查询对应的执行列表详情
     * @param taskId 任务ID（taskId）
     * @param offset 质量规则分页
     * @param limit 质量规则分页
     * @return 对应的执行列表详情
     */
    @GET
    @Path("/report/list/{taskId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<ExecutionRecordPage> getRetrievalReportList(@PathParam("taskId") String taskId,
                                                                  @QueryParam("offset") int offset,
                                                                  @QueryParam("limit") int limit,
                                                                  @QueryParam("tenantId") String tenantId) {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "DataAssetsRetrievalREST.getRetrievalReportList( 使用数据标准id： " + taskId + " 查询任务执行列表详情 )");
            }
            RuleParameters parameters = new RuleParameters();
            parameters.setOffset(offset);
            parameters.setLimit(limit);
            return dataAssetsRetrievalService.getDataAssetsTaskExecutionReport(taskId, parameters, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据资产失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
}
