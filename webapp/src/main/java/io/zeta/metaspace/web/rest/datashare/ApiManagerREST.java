// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.rest.datashare;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

import com.google.common.base.Joiner;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.apigroup.ApiVersion;
import io.zeta.metaspace.model.datasource.DataSourceType;
import io.zeta.metaspace.model.datasource.DataSourceTypeInfo;
import io.zeta.metaspace.model.desensitization.DesensitizationRule;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.Queue;
import io.zeta.metaspace.model.share.*;
import io.zeta.metaspace.web.service.*;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * @author lixiang03
 * @Data 2020/6/3 10:43N
 */
@Path("datashare/api")
@Singleton
@Service
public class ApiManagerREST {
    private static final Logger LOG = LoggerFactory.getLogger(ApiManagerREST.class);
    @Autowired
    private DataShareGroupService groupService;
    @Autowired
    private DataShareService shareService;
    @Autowired
    private SearchService searchService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private DesensitizationService desensitizationService;

    private static int CATEGORY_TYPE = 2;

    /**
     * 创建API
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("{submit}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result insertAPIInfo(ApiInfoV2 info, @HeaderParam("tenantId") String tenantId, @PathParam("submit") boolean submit) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "创建api:" + info.getName());
        try {
            shareService.insertAPIInfoV2(info, tenantId, submit);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "创建api失败");
        }
    }

    /**
     * 更新api
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("{submit}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateAPIInfo(ApiInfoV2 info, @HeaderParam("tenantId") String tenantId, @PathParam("submit") boolean submit) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "更新api:" + info.getName());

        try {
            shareService.updateAPIInfoV2(info, tenantId, submit);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新api失败");
        }
    }


    /**
     * 更新 api 策略
     *
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/poly/{apiId}/{version}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateAPIPloy(@PathParam("apiId") String apiId, @PathParam("version") String version, ApiPolyEntity apiPolyEntity, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "更新api策略:" + apiId + " " + version);
        try {
            shareService.updateAPIInfoV2ApiPolyEntity(apiId, version, apiPolyEntity, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新api策略");
        }
    }


    /**
     * 提交api
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/submit")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result submitApi(ApiInfoV2 info, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        ApiInfoV2 apiInfoByVersion = shareService.getApiInfoByVersion(info.getGuid(), info.getVersion());
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "提交api审核:" + apiInfoByVersion.getName());

        try {
            shareService.submitApi(info.getGuid(), info.getVersion(), tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "提交api失败");
        }
    }

    /**
     * 取消审核
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/revoke")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result revokeApiAudit(ApiInfoV2 info, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        ApiInfoV2 apiInfoByVersion = shareService.getApiInfoByVersion(info.getGuid(), info.getVersion());
        if (apiInfoByVersion == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Api 不存在");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "取消审核:" + apiInfoByVersion.getName());

        try {
            auditService.cancelApiAudit(tenantId, info.getGuid(), info.getVersion());
            shareService.addApiLog(ApiLogEnum.UNSUBMIT, info.getGuid(), AdminUtils.getUserData().getUserId());
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "取消审核失败");
        }
    }

    /**
     * 删除api
     *
     * @param ids
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteApi(List<String> ids, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        List<String> projectNames = shareService.getApiInfoByIds(ids).stream().map(apiInfoV2 -> apiInfoV2.getName()).collect(Collectors.toList());
        if (projectNames == null || projectNames.size() == 0) {
            return ReturnUtil.success();
        }
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "批量删除api:[" + Joiner.on("、").join(projectNames) + "]");
        try {
            shareService.deleteApi(ids, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除api失败");
        }
    }

    /**
     * 删除api版本
     *
     * @param api
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("version")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteApiVersion(ApiVersion api, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        ApiInfoV2 apiInfoByVersion = shareService.getApiInfoByVersion(api.getApiId(), api.getVersion());
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "删除api:" + apiInfoByVersion.getName());
        try {
            shareService.deleteApiVersion(api, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除api失败");
        }
    }

    /**
     * 获取api详情
     *
     * @param apiId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{apiId}/info")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApiInfo(@PathParam("apiId") String apiId) throws AtlasBaseException {
        try {
            ApiInfoV2 apiInfoMaxVersion = shareService.getApiInfoMaxVersion(apiId);
            return ReturnUtil.success(apiInfoMaxVersion);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取详情失败");
        }
    }

    /**
     * 根据版本获取api详情
     *
     * @param apiId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/apiinfo/{apiId}/{version}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApiInfoByVersion(@PathParam("apiId") String apiId, @PathParam("version") String version) throws AtlasBaseException {
        try {
            ApiInfoV2 apiInfo = shareService.getApiInfoByVersion(apiId, version);
            return ReturnUtil.success(apiInfo);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取详情失败");
        }
    }

    /**
     * 获取api版本
     *
     * @param apiId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/version/{apiId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApiVersion(@PathParam("apiId") String apiId) throws AtlasBaseException {
        try {
            List<ApiVersion> apiVersion = shareService.getApiVersion(apiId);
            return ReturnUtil.success(apiVersion);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取api版本失败");
        }
    }

    @PUT
    @Path("/status")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result updateApiStatus(ApiStatus apiInfo, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            shareService.updateApiStatus(apiInfo.getGuid(), apiInfo.isStatus());
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新api上下架状态异常");
        }

    }

    @GET
    @Path("/{apiId}/log")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApiLog(@PathParam("apiId") String apiId,
                            @DefaultValue("0") @QueryParam("offset") int offset,
                            @DefaultValue("-1") @QueryParam("limit") int limit,
                            @QueryParam("search") String search) throws AtlasBaseException {
        try {
            Parameters param = new Parameters();
            param.setOffset(offset);
            param.setLimit(limit);
            param.setQuery(search);
            PageResult<ApiLog> pageResult = shareService.getApiLog(param, apiId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取api日志失败");
        }
    }

    /**
     * 创建目录
     *
     * @param categoryInfo
     * @param tenantId
     * @param projectId
     * @return
     * @throws Exception
     */
    @POST
    @Path("/category/{projectId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result createCategory(CategoryInfoV2 categoryInfo, @HeaderParam("tenantId") String tenantId, @PathParam("projectId") String projectId) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "创建目录:" + categoryInfo.getName());
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "ApiREST.createCategory()");
            }
            CategoryPrivilege category = shareService.createCategory(categoryInfo, projectId, tenantId);
            return ReturnUtil.success(category);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "创建目录失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 更新目录
     *
     * @param categoryInfo
     * @param projectId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/category/{projectId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateCategory(CategoryInfoV2 categoryInfo, @PathParam("projectId") String projectId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "更新目录:" + categoryInfo.getName());
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "ApiREST.updateCategory()");
            }
            shareService.updateCategory(categoryInfo, projectId, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新目录失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除目录
     *
     * @param categoryDelete
     * @param projectId
     * @param tenantId
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("/category/{projectId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteCategory(CategoryDelete categoryDelete, @PathParam("projectId") String projectId, @HeaderParam("tenantId") String tenantId) throws Exception {
        categoryDelete.setProjectId(projectId);
        Servlets.validateQueryParamLength("categoryGuid", categoryDelete.getId());
        AtlasPerfTracer perf = null;
        CategoryEntityV2 category = shareService.getCategory(categoryDelete.getId(), tenantId);
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "删除目录:" + category.getName());
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "ApiREST.deleteCategory(" + categoryDelete.getId() + ")");
            }
            shareService.deleteCategory(categoryDelete, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除目录失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取全部目录
     *
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category/{projectId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getCategories(@PathParam("projectId") String projectId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(LOG, "ApiREST.getCategories()");
            }
            List<CategoryPrivilege> categoryByProject = shareService.getCategoryByProject(projectId, tenantId);
            return ReturnUtil.success(categoryByProject);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取目录失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 查询api
     *
     * @param categoryId
     * @param projectId
     * @param limit
     * @param offset
     * @param search
     * @param order
     * @param sort
     * @param status
     * @param approve
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("{projectId}/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result searchApi(@PathParam("categoryId") String categoryId, @PathParam("projectId") String projectId,
                            @DefaultValue("-1") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
                            @QueryParam("search") String search, @QueryParam("order") String order,
                            @QueryParam("sortBy") String sort, @QueryParam("status") String status,
                            @QueryParam("isApprove") String approve,
                            @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            Parameters parameters = new Parameters();
            parameters.setQuery(search);
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            parameters.setOrder(order);
            parameters.setSortby(sort);
            PageResult<ApiHead> pageResult = shareService.searchApi(parameters, projectId, categoryId, status, approve, tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询api失败");
        }
    }

    /**
     * 迁移目录
     *
     * @param moveApi
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("category/move/api")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result moveApi(MoveApi moveApi, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        CategoryEntityV2 category = shareService.getCategory(moveApi.getOldCategoryId(), tenantId);
        HttpRequestContext.get().auditLog(ModuleEnum.APIMANAGE.getAlias(), "迁移目录:" + category.getName());

        try {
            shareService.moveApi(moveApi);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "目录迁移失败");
        }
    }

    /**
     * 检查状态
     *
     * @param fields
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/check/datatype")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result checkDataType(List<QueryParameter.Field> fields) throws AtlasBaseException {
        shareService.checkDataType(fields);
        return ReturnUtil.success();
    }

    /**
     * 同名校验
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/same")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result querySameName(ApiInfoV2 info, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            String name = info.getName();
            boolean bool = shareService.querySameNameV2(name, tenantId, info.getProjectId());
            return ReturnUtil.success(bool);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询失败");
        }
    }

    /**
     * 同版本校验
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/same/version")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result querySameVersion(ApiInfoV2 info, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            boolean bool = shareService.isApiSameVersion(info.getGuid(), info.getVersion());
            return ReturnUtil.success(bool);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询失败");
        }
    }

    /**
     * 获取库列表
     *
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/databases")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDatabaseByQuery(Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            PageResult<Database> pageResult = searchService.getDatabasePageResultV2(parameters, tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取库列表失败");
        }
    }

    /**
     * 搜库
     *
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/search/databases")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDatabaseAndTableByQuery(Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            PageResult<Database> pageResult = searchService.getDatabasePageResultV2(parameters, tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "搜索库列表失败");
        }
    }

    /**
     * 根据库id返回表
     *
     * @return List<Database>
     */
    @POST
    @Path("/tables/{databaseId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getTableByDB(@PathParam("databaseId") String databaseId, Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            PageResult<TableInfo> pageResult = searchService.getTableByDBWithQueryWithoutTmp(databaseId, parameters, tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表列表失败");
        }
    }

    /**
     * 获取表字段
     *
     * @param tableGuid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/columns/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getTableColumns(@PathParam("tableGuid") String tableGuid, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            List<Column> tableColumnList = shareService.getTableColumnList(tableGuid, tenantId);
            return ReturnUtil.success(tableColumnList);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表字段失败");
        }
    }

    /**
     * 获取ORACLE数据源
     *
     * @param parameters
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{type}/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getDataSourceList(Parameters parameters, @HeaderParam("tenantId") String tenantId, @PathParam("type") String type) throws AtlasBaseException {
        try {
            PageResult oracleDataSourceList = shareService.getDataSourceList(parameters, type, tenantId);
            return oracleDataSourceList;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取数据源失败");
        }
    }

    /**
     * 获取schema列表失败
     *
     * @param sourceId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/oracle/{sourceId}/schemas")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getSchemaList(@PathParam("sourceId") String sourceId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult dataList = shareService.getDataList(DataShareService.SEARCH_TYPE.SCHEMA, parameters, sourceId);
            return ReturnUtil.success(dataList);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取schema列表失败");
        }
    }

    /**
     * 获取表列表
     *
     * @param sourceId
     * @param schemaName
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/oracle/{sourceId}/{schemaName}/tables")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getTableList(@PathParam("sourceId") String sourceId, @PathParam("schemaName") String schemaName, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult tableList = shareService.getDataList(DataShareService.SEARCH_TYPE.TABLE, parameters, sourceId, schemaName);
            return ReturnUtil.success(tableList);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表列表失败");
        }
    }

    /**
     * 获取表字段
     *
     * @param sourceId
     * @param schemaName
     * @param tableName
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/oracle/{sourceId}/{schemaName}/{tableName}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getColumnList(@PathParam("sourceId") String sourceId, @PathParam("schemaName") String schemaName, @PathParam("tableName") String tableName, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult dataList = shareService.getDataList(DataShareService.SEARCH_TYPE.COLUMN, parameters, sourceId, schemaName, tableName);
            return ReturnUtil.success(dataList);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表字段失败");
        }
    }

    /**
     * 获取资源池失败
     *
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/pools")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getPools(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            List<Queue> pools = shareService.getPools(tenantId);
            return ReturnUtil.success(pools);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取资源池失败");
        }
    }

    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/test/{randomName}")
    public Result testApi(@HeaderParam("tenantId") String tenantId, ApiInfoV2 apiInfoV2,
                          @DefaultValue("1") @QueryParam("page_num") Long pageNum,
                          @DefaultValue("10") @QueryParam("page_size") Long pageSize,
                          @PathParam("randomName") String randomName) throws Exception {
        try {
            long limit = 10;
            long offset = 0;
            if (pageSize != null) {
                limit = Long.valueOf(pageSize);
            }
            if (pageNum != null && Long.valueOf(pageNum) > 0) {
                offset = (Long.valueOf(pageNum) - 1) * limit;
            }
            Map resultMap = shareService.testAPIV2(randomName, apiInfoV2, limit, offset);
            List<LinkedHashMap<String, Object>> result = (List<LinkedHashMap<String, Object>>) resultMap.get("queryResult");
            return ReturnUtil.success(result);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "测试api失败");
        }
    }

    @PUT
    @Path("/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void stopTestAPI(@PathParam("randomName") String randomName) throws Exception {
        try {
            shareService.cancelAPIThread(randomName);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "任务取消失败");
        }
    }

    @GET
    @Path("/type")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataSourceType() {
        List<DataSourceTypeInfo> dataSourceType = shareService.getDataSourceType();
        return ReturnUtil.success(dataSourceType);
    }

    /**
     * 获取规则列表
     */
    @GET
    @Path("desensitization")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DesensitizationRule> getDesensitizationRuleList(@QueryParam("limit") int limit, @QueryParam("offset") int offset, @QueryParam("query") String query, @QueryParam("enable") String enableStr, @HeaderParam("tenantId") String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setQuery(query);
        parameters.setLimit(limit);
        parameters.setOffset(offset);

        Boolean enable = StringUtils.isNotEmpty(enableStr) ? Boolean.valueOf(enableStr) : null;

        return desensitizationService.getDesensitizationRuleList(parameters, enable, tenantId);

    }
}
