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
package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.bo.DatabaseInfoBO;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.datasource.DataSourceHead;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datastandard.DataStandAndTable;
import io.zeta.metaspace.model.datastandard.DataStandardHead;
import io.zeta.metaspace.model.global.UserPermissionPO;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.BuildTableSql;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableShow;
import io.zeta.metaspace.model.security.Tenant;
import io.zeta.metaspace.model.security.TenantExtInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveTableColumnVO;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.dao.UserPermissionDAO;
import io.zeta.metaspace.web.service.*;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDeriveTableInfoService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.EntityUtil;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.EntityMutationResponse;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Path("metadata")
@Singleton
@Service
public class MetaDataREST {
    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.MetaDataREST");
    @Autowired
    private SearchService searchService;
    @Context
    private HttpServletRequest httpServletRequest;
    private static final String DEFAULT_DIRECTION = "BOTH";
    private static final String DEFAULT_DEPTH = "-1";
    private AtomicBoolean importing = new AtomicBoolean(false);


    @Autowired
    private ColumnTagService columnTagService;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private TableDAO tableDAO;
    @Autowired
    private DataStandardService dataStandardService;

    private final MetaDataService metadataService;
    @Autowired
    private BusinessService businessService;
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    SourceInfoDeriveTableInfoService sourceInfoDeriveTableInfoService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private UserPermissionDAO userPermissionDAO;

    @Inject
    public MetaDataREST(final MetaDataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * 当前账户是否配置全局权限
     * @return true：已配置全局权限
     */
    private boolean isConfigGloble(){
        User user = AdminUtils.getUserData();
        UserPermissionPO userPermissionPO = userPermissionDAO.selectListByUsersId(user.getUserId());
        return userPermissionPO != null;
    }
    @GET
    @Path("/tenantId")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TenantExtInfo> getTenantIdList(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTenantIdList");
            }
            List<TenantExtInfo> tenants =  new ArrayList<>();
            //查看是否配置全局权限，配置则查看所有租户信息，否则只显示当前租户
            if( isConfigGloble() ){
                List<Tenant> tenantList = tenantService.getTenants();
                if(CollectionUtils.isNotEmpty(tenantList)){
                    TenantExtInfo tenant = null;
                    for(Tenant item : tenantList){
                        tenant = new TenantExtInfo();
                        tenant.setTenantId(item.getTenantId());
                        tenant.setProjectName(item.getProjectName());
                        tenant.setBizTreeId(EntityUtil.generateBusinessId(item.getTenantId(),"","",""));
                        tenants.add(tenant);
                    }
                }
            }else{
                String name = tenantService.getNameById(tenantId);
                TenantExtInfo tenant = new TenantExtInfo();
                tenant.setTenantId(tenantId);
                tenant.setProjectName(name);
                tenant.setBizTreeId(EntityUtil.generateBusinessId(tenantId,"","",""));
                tenants.add(tenant);
            }

            return tenants;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("/info/datasource/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceInfo getDataSourceInfo(@PathParam("sourceId") String sourceId, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        if ("hive".equalsIgnoreCase(sourceId)) {
            DataSourceInfo dataSourceInfo = new DataSourceInfo();
            dataSourceInfo.setSourceId("hive");
            dataSourceInfo.setSourceId("hive");
            dataSourceInfo.setSourceType("HIVE");
            return dataSourceInfo;
        }
        return dataSourceService.getDataSourceInfo(sourceId);
    }


    @GET
    @Path("/info/schema/{schemaId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Database getDatabase(@PathParam("schemaId") String schemaId, @HeaderParam("tenantId") String tenantId, @QueryParam("sourceId")@DefaultValue("") String sourceId) throws AtlasBaseException {
        return metadataService.getDatabase(schemaId, tenantId,sourceId);
    }


    @GET
    @Path("/info/table/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Map<String, Object> getTableTree(@PathParam("guid") String guid, @QueryParam("sourceId") @DefaultValue("") String sourceId) throws AtlasBaseException {

        Map<String, Object> tableType = metadataService.getTableType(guid);
        if(StringUtils.isNotBlank(sourceId)){
            tableType.put("sourceId", sourceId);
        }
        return tableType;
    }

    @GET
    @Path("/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataSourceHead> getDataSourceList(@QueryParam("limit") int limit, @QueryParam("offset") int offset,
                                                        @QueryParam("query") String query, @QueryParam("sourceType") String sourceType,
                                                        @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        Long start = System.currentTimeMillis();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getDataSourceList");
            }
            DataSourceHead hive = new DataSourceHead();
            if (offset == 0 && (query == null || "hive".contains(query))) {
                hive.setSourceType("hive");
                hive.setSourceName("hive");
                hive.setSourceId("hive");
                hive.setBizTreeId(EntityUtil.generateBusinessId(tenantId,"hive","",""));
                if (limit != -1) {
                    limit--;
                }
            }
            PageResult<DataSourceHead> pageResult = dataSourceService.searchDataSources(limit, offset, null, null, query, sourceType, null, null, null, true, tenantId);
            if (offset == 0 && (query == null || "hive".contains(query))) {
                pageResult.getLists().add(0, hive);
                pageResult.setCurrentSize(pageResult.getCurrentSize() + 1);
                pageResult.setTotalSize(pageResult.getTotalSize() + 1);
            }
            return pageResult;
        } finally {
            if(StringUtils.isNotBlank(query)){
                dataSourceService.metadataSearchStatistics(start, System.currentTimeMillis(), "metadata");
            }
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 公共租户下 - 元数据展示的查询数据源列表
     */
    @GET
    @Path("/public/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataSourceHead> getPublicDataSourceList(@QueryParam("limit") int limit, @QueryParam("offset") int offset,
                                                        @QueryParam("query") String query, @QueryParam("sourceType") String sourceType,
                                                        @QueryParam("bizTreeId") String bizTreeId,
                                                        @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        Long start = System.currentTimeMillis();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getPublicDataSourceList");
            }
            String queryTenantIdParam = null;
            // bizTreeId 不为空则是查询当前租户下的
            if(StringUtils.isNotBlank(bizTreeId)){
                Map<String, String> map = EntityUtil.decodeBusinessId(bizTreeId);
                queryTenantIdParam = map.get("tenantId");
            }else{
                //查询当前用户是否拥有全局权限，是则显示所有租户下的，否则只展示当前租户下的
                if(!isConfigGloble()){
                    queryTenantIdParam = tenantId;
                }
            }

            DataSourceHead hive = new DataSourceHead();
            if (offset == 0 && (query == null || "hive".contains(query))) {
                hive.setSourceType("hive");
                hive.setSourceName("hive");
                hive.setSourceId("hive");
                hive.setBizTreeId(EntityUtil.generateBusinessId(tenantId,"hive","",""));
                if (limit != -1) {
                    limit--;
                }
            }
            PageResult<DataSourceHead> pageResult = dataSourceService.searchDataSources(limit, offset, null, null, query, sourceType, null, null, null, true, queryTenantIdParam);
            if (offset == 0 && (query == null || "hive".contains(query))) {
                pageResult.getLists().add(0, hive);
                pageResult.setCurrentSize(pageResult.getCurrentSize() + 1);
                pageResult.setTotalSize(pageResult.getTotalSize() + 1);
            }
            return pageResult;
        } finally {
            if(StringUtils.isNotBlank(query)){
                dataSourceService.metadataSearchStatistics(start, System.currentTimeMillis(), "metadata");
            }
            AtlasPerfTracer.log(perf);
        }
    }

    //获取数据源 schema 分页列表
    @GET
    @Path("/schema")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getDatabaseList(@QueryParam("active") @DefaultValue("true") Boolean active,
                                                @QueryParam("queryTableCount") @DefaultValue("false") boolean queryTableCount,
                                                @QueryParam("sourceId") String sourceId,
                                                @QueryParam("query") String query,
                                                @QueryParam("offset") long offset, @QueryParam("limit") long limit, @HeaderParam("tenantId") String tenantId) throws InterruptedException {
        AtlasPerfTracer perf = null;
        Long start = System.currentTimeMillis();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getDatabaseList(" + sourceId + " )");
            }
            PageResult<Database> result = searchService.getDatabases(sourceId, offset, limit, query, tenantId, queryTableCount);
            return result;
        } finally {
            if(StringUtils.isNotBlank(query)){
                dataSourceService.metadataSearchStatistics(start, System.currentTimeMillis(), "metadata");
            }
            AtlasPerfTracer.log(perf);
        }
    }

    //公共租户下 - 元数据展示的 获取数据源 schema 分页列表
    @GET
    @Path("/public/schema")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getPublicDatabaseList(@QueryParam("active") @DefaultValue("true") Boolean active,
                                                @QueryParam("queryTableCount") @DefaultValue("false") boolean queryTableCount,
                                                @QueryParam("sourceId") String sourceId,
                                                @QueryParam("query") String query,@QueryParam("bizTreeId") String bizTreeId,
                                                @QueryParam("offset") long offset, @QueryParam("limit") long limit, @HeaderParam("tenantId") String tenantId) throws InterruptedException {
        AtlasPerfTracer perf = null;
        Long start = System.currentTimeMillis();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getPublicDatabaseList(" + sourceId + " )");
            }
            String queryTenantIdParam = null;
            PageResult<Database> result = null;
            // bizTreeId 不为空则是查询当前租户下的
            if(StringUtils.isNotBlank(bizTreeId)){
                Map<String, String> map = EntityUtil.decodeBusinessId(bizTreeId);
                queryTenantIdParam = map.get("tenantId");
                result = searchService.getDatabases(sourceId, offset, limit, query, queryTenantIdParam, queryTableCount);
                return result;
            }
            //查询当前用户是否拥有全局权限，是则显示所有租户下的，否则只展示当前租户下的
            if(!isConfigGloble()){
                queryTenantIdParam = tenantId;
                result = searchService.getDatabases(sourceId, offset, limit, query, queryTenantIdParam, queryTableCount);
                return result;
            }

            result = searchService.getPublicDatabases(offset, limit, query, queryTableCount);
            return result;
        } finally {
            if(StringUtils.isNotBlank(query)){
                dataSourceService.metadataSearchStatistics(start, System.currentTimeMillis(), "metadata");
            }
            AtlasPerfTracer.log(perf);
        }
    }

    //公共租户下 - 元数据展示的 获取数据表 分页
    @GET
    @Path("/public/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TableEntity> getPublicTableList(@HeaderParam("tenantId") String tenantId,
                                                @QueryParam("active") @DefaultValue("true") Boolean active,
                                                @QueryParam("schemaId") String schemaId,
                                                @QueryParam("queryInfo") @DefaultValue("false") boolean queryInfo,
                                                @QueryParam("query") String query, @QueryParam("bizTreeId") String bizTreeId,
                                                @QueryParam("sourceId") @DefaultValue("")String sourceId,
                                                @QueryParam("offset") long offset, @QueryParam("limit") long limit,
                                                @QueryParam("isView") @DefaultValue("") String isViewStr) {
        AtlasPerfTracer perf = null;
        Long start = System.currentTimeMillis();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getPublicTableList(" + schemaId + "," + limit + "," + offset + " )");
            }
            Boolean isView = StringUtils.isEmpty(isViewStr) ? null : Boolean.parseBoolean(isViewStr);
            String queryTenantIdParam = null;
            PageResult<TableEntity> result = null;
            // bizTreeId 不为空则是查询当前租户下的
            if(StringUtils.isNotBlank(bizTreeId)){
                Map<String, String> map = EntityUtil.decodeBusinessId(bizTreeId);
                queryTenantIdParam = map.get("tenantId");
                result = searchService.getTable(schemaId, offset, limit, query, isView, queryInfo, queryTenantIdParam, sourceId);
                return result;
            }
            //查询当前用户是否拥有全局权限，是则显示所有租户下的，否则只展示当前租户下的
            if(!isConfigGloble()){
                queryTenantIdParam = tenantId;
                result = searchService.getTable(schemaId, offset, limit, query, isView, queryInfo, queryTenantIdParam, sourceId);
                return result;
            }
            //多租户下的数据查询
            return searchService.getPublicTable(schemaId,  offset,  limit,  query,  isView, queryInfo);
        } finally {
            if(StringUtils.isNotBlank(query)){
                dataSourceService.metadataSearchStatistics(start, System.currentTimeMillis(), "metadata");
            }
            AtlasPerfTracer.log(perf);
        }
    }

    //获取数据表 分页
    @GET
    @Path("/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TableEntity> getTableList(@HeaderParam("tenantId") String tenantId,
                                                @QueryParam("active") @DefaultValue("true") Boolean active,
                                                @QueryParam("schemaId") String schemaId,
                                                @QueryParam("queryInfo") @DefaultValue("false") boolean queryInfo,
                                                @QueryParam("query") String query,
                                                @QueryParam("sourceId") @DefaultValue("")String sourceId,
                                                @QueryParam("offset") long offset, @QueryParam("limit") long limit,
                                                @QueryParam("isView") @DefaultValue("") String isViewStr) {
        AtlasPerfTracer perf = null;
        Long start = System.currentTimeMillis();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableList(" + schemaId + "," + limit + "," + offset + " )");
            }
            Boolean isView = StringUtils.isEmpty(isViewStr) ? null : Boolean.parseBoolean(isViewStr);
            return searchService.getTable(schemaId, offset, limit, query, isView, queryInfo, tenantId, sourceId);
        } finally {
            if(StringUtils.isNotBlank(query)){
                dataSourceService.metadataSearchStatistics(start, System.currentTimeMillis(), "metadata");
            }
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 根据搜索条件返回库
     *
     * @return List<Database>
     */
    @POST
    @Path("/search/databases")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getDatabaseByQuery(@QueryParam("active") @DefaultValue("true") Boolean active, Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getDatabaseByQuery(" + parameters + " )");
            }
            PageResult<Database> pageResult = searchService.getDatabasePageResult(active, parameters, tenantId, AdminUtils.getUserData().getAccount());
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 根据数据源id返回库
     *
     * @return List<RDBMSDatabase>
     */
    @POST
    @Path("/rdbms/databases/{sourceId}/{offset}/{limit}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RDBMSDatabase> getRDBMSDBBySource(@QueryParam("active") @DefaultValue("true") Boolean active, @PathParam("sourceId") String sourceId, @PathParam("offset") long offset, @PathParam("limit") long limit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSDBBySource(" + sourceId + "," + limit + "," + offset + " )");
            }
            PageResult<RDBMSDatabase> pageResult = searchService.getRDBMSDBBySource(sourceId, offset, limit, active);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    @GET
    @Path("/databases")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<String> databases() throws AtlasBaseException {
        return metadataService.getHiveSchemaList();
    }

    /**
     * 根据库id返回表
     *
     * @return List<Database>
     */
    @POST
    @Path("/tables/{databaseId}/{offset}/{limit}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Table> getTableByDB(@QueryParam("active") @DefaultValue("true") Boolean active, @PathParam("databaseId") String databaseId, @PathParam("offset") long offset, @PathParam("limit") long limit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getDatabaseByQuery(" + databaseId + "," + limit + "," + offset + " )");
            }
            PageResult<Table> pageResult = searchService.getTableByDB(databaseId, active, offset, limit);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回表
     *
     * 疑似废弃 2021-09-15
     *
     * @return List<Table>
     */
    @POST
    @Path("/search/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Table> getTableByQuery(@QueryParam("active") @DefaultValue("true") Boolean active, Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableByQuery(" + parameters + " )");
            }
            PageResult<Table> pageResult = searchService.getTablePageResultV2(active, parameters, tenantId, AdminUtils.getUserData().getAccount());
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回列
     *
     * 疑似废弃 2021-09-15
     *
     * @return List<Column>
     */
    @POST
    @Path("/search/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Column> getColumnByQuery(@QueryParam("active") @DefaultValue("true") Boolean active, Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnByQuery(" + parameters + " )");
            }
            PageResult<Column> pageResult = searchService.getColumnPageResultV2(active, parameters, tenantId, AdminUtils.getUserData().getAccount());
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 数据预览
     *
     * @return TableShow
     */
    @POST
    @Path("/table/preview")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableShow selectData(GuidCount guidCount) throws AtlasBaseException, SQLException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.selectData(" + guidCount.getGuid() + ", " + guidCount.getCount() + " )");
            }
            TableShow tableShow = searchService.getTableShow(guidCount, false);
            return tableShow;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询数据失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * sql
     *
     * @return BuildTableSql
     */
    @GET
    @Path("/table/sql/{tableId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BuildTableSql getTableSQL(@PathParam("tableId") String tableId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableSQL(" + tableId + " )");
            }
            Configuration conf = ApplicationProperties.get();
            boolean secure = conf.getBoolean("metaspace.secureplus.enable", true);
            String user = !secure ? MetaspaceConfig.getHiveAdmin() : AdminUtils.getUserName();
            BuildTableSql buildTableSql = searchService.getBuildTableSql(tableId, user);
            return buildTableSql;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询建表语句失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 获取字段详情
     *
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/column/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Column> getColumnInfoById(@HeaderParam("tenantId")String tenantId, ColumnQuery query, @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", query.getGuid());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnInfoById");
            }
            List<Column> columns = metadataService.getColumnInfoById(query, refreshCache);
            List<Column> columnsWithTags=columnTagService.buildTagsInfoColumn(columns,tenantId);
            Map<String,List<ColumnTag>> map = columnsWithTags.stream().collect(Collectors.toMap(Column::getColumnId,Column::getTags,(key1,key2)->key1));
            columns.forEach(column -> {
                if (map.get(column.getColumnId())!=null){
                    column.setTags(map.get(column.getColumnId()));
                }
            });
            return columns;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 表血缘
     *
     * @param guid
     * @param direction
     * @param depth
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/lineage/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableLineageInfo getTableLineage(@PathParam("guid") String guid,
                                            @QueryParam("direction") @DefaultValue(DEFAULT_DIRECTION) AtlasLineageInfo.LineageDirection direction,
                                            @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);
        AtlasPerfTracer perf = null;
        Long start = System.currentTimeMillis();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableLineage");
            }
            return metadataService.getTableLineage(guid, direction, depth);
        } finally {
            dataSourceService.metadataSearchStatistics(start, System.currentTimeMillis(), "data_kinship");
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 获取字段血缘
     *
     * @param guid
     * @param direction
     * @param depth
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/column/lineage/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ColumnLineageInfo getColumnLineage(@PathParam("guid") String guid,
                                              @QueryParam("direction") @DefaultValue(DEFAULT_DIRECTION) AtlasLineageInfo.LineageDirection direction,
                                              @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);
        AtlasPerfTracer perf = null;
        Long start = System.currentTimeMillis();
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnLineage");
            }
//            return metadataService.getColumnLineageV2(guid, direction, depth);
            return metadataService.getColumnLineages(guid, direction, depth);
        } finally {
            dataSourceService.metadataSearchStatistics(start, System.currentTimeMillis(), "data_kinship");
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 清除缓存
     *
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/refreshcache")
    public Response refreshCache() throws AtlasBaseException {
        try {
            metadataService.refreshCache();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "刷新失败");
        }
        return Response.status(200).entity("success").build();
    }


    @GET
    @Path("/tableExists")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean table(@QueryParam("database") String database, @QueryParam("tableName") String tableName) throws AtlasBaseException, SQLException, IOException {
        AdapterSource adapterSource = AdapterUtils.getHiveAdapterSource();
        return adapterSource.getNewAdapterExecutor().tableExists(AdminUtils.getUserName(), database, tableName);
    }


    @Autowired
    TableTagService tableTagService;


    @GET
    @Path("/tag/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Tag> getTag(@PathParam("guid") String guid, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return tableTagService.getTags(guid, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取标签失败");
        }
    }

    @POST
    @Path("/tag/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String addTag(@PathParam("guid") String guid, Tag tag, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            tableTagService.addTag(guid, tag.getTagName(), tenantId);
            return "success";
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "创建标签失败");
        }
    }


    @DELETE
    @Path("/tag/{guid}/{tagId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public String deleteTag(@PathParam("guid") String guid, @PathParam("tagId") String tagId) throws AtlasBaseException {
        try {
            tableTagService.deleteTag(guid, tagId);
            return "success";
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除标签失败");
        }
    }


    @Path("/supplementTable")
    @GET
    public String supplementTable(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            dataManageService.supplementTable(tenantId);
            return "success";
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "补充贴源层失败");
        }
    }


    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/business/trust")
    public Response updateTrustTable(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            businessService.updateBusinessTrustTable(tenantId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新唯一信任数据失败");
        }
    }

    @Path("/owner/{tableGuid}")
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String deleteOwner(@PathParam("tableGuid") String tableGuid, List<String> ownerList) throws AtlasBaseException {
        try {
            metadataService.deleteTableOwner(tableGuid, ownerList);
            return "success";
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除标签失败");
        }
    }


    /**
     * Delete an entity identified by its GUID.
     *
     * @param guid GUID for the entity
     * @return EntityMutationResponse
     */
    @DELETE
    @Path("/guid/{guid}")
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_JSON})
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public EntityMutationResponse hardDeleteByGuid(@PathParam("guid") final String guid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);

        AtlasPerfTracer perf = null;

        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.hardDeleteByGuid(" + guid + ")");
            }
            EntityMutationResponse entityMutationResponse = metadataService.hardDeleteByGuid(guid);
            refreshCache();
            return entityMutationResponse;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/table/{guid}")
    @OperateType(UPDATE)
    public Response updateTableInfo(@PathParam("guid") final String guid, Table tableInfo) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.METADATA.getAlias(), tableInfo.getTableName());
        try {
            metadataService.updateTableInfo(guid, tableInfo);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新表信息失败");
        }
    }

    @POST
    @Path("/export")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DownloadUri downloadExcelTemplate(List<String> tableGuidList) throws AtlasBaseException {
        try {
            String url = MetaspaceConfig.getMetaspaceUrl() + "/api/metaspace/metadata/export";
            return ExportDataPathUtils.generateURL(url, tableGuidList);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "下载报告失败");
        }
    }

    @GET
    @Path("/export/{downloadId}")
    @Valid
    public void exportSelected(@PathParam("downloadId") String downloadId, @HeaderParam("tenantId") String tenantId) throws Exception {
        File xlsxFile = null;
        try {
            List<String> metadataGuidList = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
            xlsxFile = metadataService.exportExcel(metadataGuidList, tenantId);
            httpServletResponse.setContentType("application/msexcel;charset=utf-8");
            httpServletResponse.setCharacterEncoding("utf-8");
            String fileName = new String(new String(xlsxFile.getName()).getBytes(), "ISO-8859-1");
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            OutputStream os = httpServletResponse.getOutputStream();
            os.write(FileUtils.readFileToByteArray(xlsxFile));
            os.close();
            xlsxFile.delete();
        } finally {
            xlsxFile.deleteOnExit();
        }
    }

    @POST
    @Path("/{tableGuid}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getHistoryList(@PathParam("tableGuid") String tableGuid, Parameters parameters) throws AtlasBaseException {
        try {
            return metadataService.getTableHistoryList(tableGuid, parameters);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表历史信息失败");
        }
    }

    @GET
    @Path("/table/{tableGuid}/{version}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableMetadata getTableMetadata(@PathParam("tableGuid") String tableGuid, @PathParam("version") Integer version) throws AtlasBaseException {
        try {
            return metadataService.getTableMetadata(tableGuid, version);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表信息失败");
        }
    }


    @POST
    @Path("/column/{tableGuid}/{version}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<ColumnMetadata> getColumnHistoryList(@PathParam("tableGuid") String tableGuid, @PathParam("version") Integer version, ColumnQuery query) throws AtlasBaseException {
        try {
            return metadataService.getColumnHistoryInfo(tableGuid, version, query);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取列历史信息失败");
        }
    }

    @GET
    @Path("/table/{tableGuid}/compare/{version}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ComparisonMetadata getComparisionTableMetadata(@PathParam("tableGuid") String tableGuid, @PathParam("version") Integer version) throws AtlasBaseException {
        try {
            return metadataService.getComparisionTableMetadata(tableGuid, version);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取对比信息失败");
        }
    }

    @GET
    @Path("/column/{tableGuid}/compare/{version}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ComparisonColumnMetadata getComparisionColumnTableMetadata(@PathParam("tableGuid") String tableGuid, @PathParam("version") Integer version) throws AtlasBaseException {
        try {
            return metadataService.getComparisionColumnMetadata(tableGuid, version);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取对比信息失败");
        }
    }

    /**
     * 更新依赖标准
     *
     * @param dataStandAndTable
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/dataStandard")
    public boolean assignTableToStandard(DataStandAndTable dataStandAndTable, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        String tableName = tableDAO.getTableNameByTableGuid(dataStandAndTable.getTableGuid());
        if (tableName == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "表不存在或已删除，请刷新或者检查元数据");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.METADATA.getAlias(), tableName);
        try {
            dataStandardService.assignTableToStandard(dataStandAndTable, tableName, tenantId);
            return true;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新依赖标准失败");
        }
    }

    /**
     * 根据搜索条件返回关系型数据库的数据源
     *
     * @return List<RDBMSDataSource>
     */
    @POST
    @Path("/datasource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RDBMSDataSource> getDataSourceByQuery(@QueryParam("active") @DefaultValue("true") Boolean active, Parameters parameters, @QueryParam("sourceType") String sourceType) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getDataSourceByQuery(" + parameters + " )");
            }
            PageResult<RDBMSDataSource> pageResult = searchService.getDataSourcePageResult(parameters, sourceType, active);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 根据库id返回表
     *
     * @return List<RDBMSTable>
     */
    @POST
    @Path("/rdbms/tables/{databaseId}/{offset}/{limit}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RDBMSTable> getRDBMSTableByDB(@QueryParam("active") @DefaultValue("true") Boolean active, @PathParam("databaseId") String databaseId, @PathParam("offset") long offset, @PathParam("limit") long limit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSTableByDB(" + databaseId + "," + limit + "," + offset + " )");
            }
            PageResult<RDBMSTable> pageResult = searchService.getRDBMSTableByDB(databaseId, offset, limit, active);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回库
     *
     * @return List<RDBMSDatabase>
     */
    @POST
    @Path("/search/rdbms/db/{sourceType}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RDBMSDatabase> getRDBMSDBByQuery(@QueryParam("active") @DefaultValue("true") Boolean active, Parameters parameters, @PathParam("sourceType") String sourceType) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSDBByQuery(" + parameters + " )");
            }
            PageResult<RDBMSDatabase> pageResult = searchService.getRDBMSDBPageResultV2(parameters, sourceType, active);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回表
     *
     * @return List<RDBMSTable>
     */
    @POST
    @Path("/search/rdbms/table/{sourceType}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RDBMSTable> getRDBMSTableByQuery(@QueryParam("active") @DefaultValue("true") Boolean active, Parameters parameters, @PathParam("sourceType") String sourceType) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSTableByQuery(" + parameters + " )");
            }
            PageResult<RDBMSTable> pageResult = searchService.getRDBMSTablePageResultV2(parameters, sourceType, active);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回列
     *
     * @return List<RDBMSTable>
     */
    @POST
    @Path("/search/rdbms/column/{sourceType}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RDBMSColumn> getRDBMSColumnByQuery(Parameters parameters, @PathParam("sourceType") String sourceType) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSColumnByQuery(" + parameters + " )");
            }
            PageResult<RDBMSColumn> pageResult = searchService.getRDBMSColumnPageResultV2(parameters, sourceType);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * sql
     *
     * @return BuildTableSql
     */
    @GET
    @Path("/rdbms/table/sql/{tableId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BuildTableSql getRDBMSTableSQL(@PathParam("tableId") String tableId, @QueryParam("sourceId") @DefaultValue("") String sourceId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableSQL(" + tableId + " )");
            }
            BuildTableSql buildTableSql = searchService.getBuildRDBMSTableSql(tableId, sourceId);
            return buildTableSql;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取建表语句失败");
        }
    }

    /**
     * 获取表详情
     *
     * @param tableId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/rdbms/table/{tableId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public RDBMSTable getTableInfoById(@PathParam("tableId") String tableId, @HeaderParam("tenantId") String tenantId, @QueryParam("sourceId") @DefaultValue("") String sourceId) throws AtlasBaseException {
        RDBMSTable table = metadataService.getRDBMSTableInfoById(tableId, tenantId,sourceId);
        if(StringUtils.isNotBlank(sourceId)){
            if("hive".equalsIgnoreCase(sourceId)){
                table.setSourceType("HIVE");
            }else{
                DataSourceInfo dataSourceInfo = dataSourceService.getDataSourceInfo(sourceId);
                if(null != dataSourceInfo){
                    table.setSourceType(dataSourceInfo.getSourceType().toUpperCase());
                    table.setSourceName(dataSourceInfo.getSourceName());
                }
            }
        }
        table.setSourceId(sourceId);
        return table;
    }

    /**
     * 获取字段详情
     *
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/rdbms/table/column/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public RDBMSColumnAndIndexAndForeignKey getRDBMSColumnInfoById(@HeaderParam("tenantId")String tenantId, ColumnQuery query, @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", query.getGuid());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnInfoById");
            }
            RDBMSColumnAndIndexAndForeignKey key= metadataService.getRDBMSColumnInfoById(query, refreshCache);
            List<RDBMSColumn> columnsWithTags=columnTagService.buildTagsInfoRDBMSColumn(key.getColumns(),tenantId);
            Map<String,List<ColumnTag>> map = columnsWithTags.stream().collect(Collectors.toMap(RDBMSColumn::getColumnId,RDBMSColumn::getTags,(key1,key2)->key1));
            key.getColumns().forEach(column -> {
                if (map.get(column.getColumnId())!=null){
                    column.setTags(map.get(column.getColumnId()));
                }
            });
            return key;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 数据预览
     *
     * @return TableShow
     */
    @POST
    @Path("/rdbms/table/preview")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableShow selectRDBMSData(GuidCount guidCount) throws AtlasBaseException, SQLException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.selectRDBMSData(" + guidCount.getGuid() + ", " + guidCount.getCount() + " )");
            }
            TableShow tableShow = searchService.getRDBMSTableShow(guidCount);
            return tableShow;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "数据预览失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * Delete an entity identified by its GUID.
     *
     * @param guid GUID for the entity
     * @return EntityMutationResponse
     */
    @DELETE
    @Path("/rdbms/guid/{guid}")
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_JSON})
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public EntityMutationResponse hardDeleteRDBMSByGuid(@PathParam("guid") final String guid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);

        AtlasPerfTracer perf = null;

        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.hardDeleteRDBMSByGuid(" + guid + ")");
            }
            EntityMutationResponse entityMutationResponse = metadataService.hardDeleteRDBMSByGuid(guid);
            refreshRDBMSCache();
            return entityMutationResponse;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @DELETE
    @Path("/rdbms/instance/guid/{guid}")
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_JSON})
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public EntityMutationResponse hardDeleteRDBMSInstanceByGuid(@PathParam("guid") final String guid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);

        AtlasPerfTracer perf = null;

        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.hardDeleteRDBMSInstanceByGuid(" + guid + ")");
            }
            EntityMutationResponse entityMutationResponse = metadataService.hardDeleteRDBMSInstanceByGuid(guid);
            refreshRDBMSCache();
            return entityMutationResponse;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 清除关系型缓存
     *
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/refreshRdbmsCache")
    public Response refreshRDBMSCache() throws AtlasBaseException {
        try {
            metadataService.refreshRDBMSCache();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "刷新失败");
        }
        return Response.status(200).entity("success").build();
    }

    @PUT
    @Path("/subscribe/table/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response subscribeTableMetadata(@PathParam("tableGuid") String tableGuid) throws AtlasBaseException {
        try {
            metadataService.addMetadataSubscription(tableGuid);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加订阅元数据变更失败");
        }
    }

    @POST
    @Path("/influence/api/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getTableInfluenceWithAPI(@PathParam("tableGuid") String tableGuid, Parameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return metadataService.getTableInfluenceWithAPI(tableGuid, parameters, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表关联api失败");
        }
    }

    @PUT
    @Path("/unsubscribe/table/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response unsubscribeTableMetadata(@PathParam("tableGuid") String tableGuid) throws AtlasBaseException {
        try {
            metadataService.removeMetadataSubscription(tableGuid);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加订阅元数据变更失败");
        }
    }

    @GET
    @Path("/influence/table/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TableHeader> getTableInfluenceWithDbAndTable(@PathParam("tableGuid") String tableGuid) throws AtlasBaseException {
        try {
            return metadataService.getTableInfluenceWithDbAndTable(tableGuid);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取库表影响失败");
        }
    }

    /**
     * 获取表依赖标准
     *
     * @param tableGuid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/dataStandard/{tableGuid}")
    public List<DataStandardHead> getDataStandard(@PathParam("tableGuid") String tableGuid, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return dataStandardService.getDataStandardByTable(tableGuid, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表的依赖数据标准失败");
        }
    }

    @GET
    @Path("/check/table/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CheckingInfo getCheckingTableInfo(@PathParam("tableGuid") String tableGuid) throws AtlasBaseException {
        try {
            return metadataService.getCheckingTableInfo(tableGuid);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "元数据稽核失败");
        }
    }

    @Path("update/supplementTable")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result updateTable() throws AtlasBaseException {
        try {
            dataManageService.updateTable();
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新表信息失败");
        }
    }

    /**
     * 获取源信息登记信息
     * @param tenantId
     * @param sourceId
     * @param schemaId
     * @return
     */
    @Path("info/sourceInfo")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getSourceInfo(@HeaderParam("tenantId") String tenantId, @QueryParam("sourceId") String sourceId,
                                @QueryParam("schemaId") String schemaId){
        DatabaseInfoBO resultBO = metadataService.querySourceInfo(tenantId,sourceId,schemaId);
        if(resultBO == null){
            return new Result("-1","没有找到对应的数据库登记信息");
        }
        return ReturnUtil.success(resultBO);
    }
    /**
     * 获取衍生表登记信息
     * @param tenantId
     * @param sourceId
     * @param schemaId
     * @return
     */
    @Path("info/deriveTable/{tableGuid}")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDeriveTableInfo(@HeaderParam("tenantId") String tenantId,@PathParam("tableGuid") String tableGuid,
                                     @QueryParam("sourceId") String sourceId,@QueryParam("schemaId") String schemaId){
        SourceInfoDeriveTableColumnVO metadataDeriveTableInfo = sourceInfoDeriveTableInfoService.queryDeriveTableInfo(tenantId,sourceId,schemaId,tableGuid);
        if(metadataDeriveTableInfo == null){
            return new Result("-1","没有找到对应的衍生表登记信息");
        }
        return ReturnUtil.success(metadataDeriveTableInfo);
    }

    /**
     * 删除图数据库所有数据
     * @throws Exception
     */
    @Path("delete/janusgraph/all")
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void deleteAllEntity() throws Exception {
        searchService.deleteAllEntity();
    }
}
