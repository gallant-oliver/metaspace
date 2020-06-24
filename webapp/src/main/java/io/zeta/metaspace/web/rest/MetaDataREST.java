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

import com.google.gson.Gson;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.datastandard.DataStandAndTable;
import io.zeta.metaspace.model.datastandard.DataStandardHead;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.BuildTableSql;
import io.zeta.metaspace.model.result.DownloadUri;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableShow;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.tag.Tag2Table;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.model.Progress;
import io.zeta.metaspace.web.model.TableSchema;
import io.zeta.metaspace.web.service.*;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.EntityMutationResponse;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Path("metadata")
@Singleton
@Service
public class MetaDataREST {
    private static final Logger LOG = LoggerFactory.getLogger(MetaDataREST.class);
    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.MetaDataREST");
    @Autowired
    private SearchService searchService;
    @Context
    private HttpServletRequest httpServletRequest;
    private static final String DEFAULT_DIRECTION = "BOTH";
    private static final String DEFAULT_DEPTH = "-1";
    private AtomicBoolean importing = new AtomicBoolean(false);

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
    private UsersService usersService;
    @Autowired
    private UserGroupDAO userGroupDAO;

    @Inject
    public MetaDataREST(final MetaDataService metadataService) {
        this.metadataService = metadataService;
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
    public PageResult<Database> getDatabaseByQuery(@QueryParam("active")@DefaultValue("true")Boolean active,Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getDatabaseByQuery(" + parameters + " )");
            }
            PageResult<Database> pageResult = searchService.getDatabasePageResult(active, parameters,tenantId,AdminUtils.getUserData().getAccount());
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
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
    public PageResult<Table> getTableByDB(@QueryParam("active")@DefaultValue("true")Boolean active, @PathParam("databaseId") String databaseId, @PathParam("offset") long offset, @PathParam("limit") long limit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getDatabaseByQuery(" + databaseId + "," + limit + "," + offset + " )");
            }
            PageResult<Table> pageResult = searchService.getTableByDB(databaseId, active,  offset, limit);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回表
     *
     * @return List<Table>
     */
    @POST
    @Path("/search/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Table> getTableByQuery(@QueryParam("active")@DefaultValue("true")Boolean active, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableByQuery(" + parameters + " )");
            }
            PageResult<Table> pageResult = searchService.getTablePageResultV2(active, parameters,tenantId,AdminUtils.getUserData().getAccount());
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回列
     *
     * @return List<Column>
     */
    @POST
    @Path("/search/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Column> getColumnByQuery(@QueryParam("active")@DefaultValue("true")Boolean active, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnByQuery(" + parameters + " )");
            }
            PageResult<Column> pageResult = searchService.getColumnPageResultV2(active ,parameters,tenantId,AdminUtils.getUserData().getAccount());
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
            TableShow tableShow = searchService.getTableShow(guidCount,false);
            return tableShow;
        } catch (AtlasBaseException e) {
            throw e;
        }  catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"查询数据失败");
        }finally {
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
            BuildTableSql buildTableSql = searchService.getBuildTableSql(tableId);
            return buildTableSql;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "hive查询异常");
        } catch (IOException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "图数据查询异常");
        } catch (AtlasException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取配置文件异常"+e.getMessage());
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
    public List<Column> getColumnInfoById(ColumnQuery query, @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", query.getGuid());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnInfoById");
            }
            return metadataService.getColumnInfoById(query, refreshCache);
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
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableLineage");
            }
            return metadataService.getTableLineage(guid, direction, depth);
        } finally {
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
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnLineage");
            }
            return metadataService.getColumnLineageV2(guid, direction, depth);
        } finally {
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "刷新失败");
        }
        return Response.status(200).entity("success").build();
    }

    @GET
    @Path("/databases")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<String> databases() throws AtlasBaseException {
        return HiveJdbcUtils.databases();
    }

    @GET
    @Path("/tableExists")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean table(@QueryParam("database") String database, @QueryParam("tableName") String tableName) throws AtlasBaseException, SQLException, IOException {
        return HiveJdbcUtils.tableExists(database, tableName);
    }

    @Autowired
    TableTagService tableTagService;

    @Path("/tag")
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String addTag(Tag tag,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            String s = tableTagService.addTag(tag.getTagName(),tenantId);
            return s;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "创建标签失败");
        }

    }

    @Path("/tags")
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Tag> getTags(Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            List<Tag> tags = tableTagService.getTags(parameters.getQuery(), parameters.getOffset(), parameters.getLimit(),tenantId);
            return tags;
        } catch (Exception e) {
            PERF_LOG.error(e.getMessage(), e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取标签失败");
        }
    }

    @Path("/tag/table2tab")
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String addtable2tab(Tag2Table tag2Table) throws AtlasBaseException {
        try {
            tableTagService.addTable2Tag(tag2Table.getTable(), tag2Table.getTags());
            return "success";
        } catch (Exception e) {
            PERF_LOG.error(e.getMessage(), e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加标签失败");
        }
    }

    @Path("/tag/{tagId}")
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public String deleteTag(@PathParam("tagId") String tagId) throws AtlasBaseException {
        try {
            tableTagService.deleteTag(tagId);
            return "success";
        } catch (Exception e) {
            PERF_LOG.error(e.getMessage(), e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除标签失败");
        }
    }

    @Path("/tag/tag2table/{tableguId}/{tagId}")
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public String deletetag2table(@PathParam("tableguId") String tableguId, @PathParam("tagId") String tagId) throws AtlasBaseException {
        try {
            tableTagService.deleteTable2Tag(tableguId, tagId);
            return "success";
        } catch (Exception e) {
            PERF_LOG.error(e.getMessage(), e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除标签失败");
        }
    }

    @Path("/supplementTable")
    @GET
    public String supplementTable(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            dataManageService.supplementTable(tenantId);
            return "success";
        } catch (Exception e) {
            PERF_LOG.error(e.getMessage(), e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "补充贴源层失败");
        }
    }


    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/import/{databaseType}")
    public Response synchronizeMetaData(@PathParam("databaseType") String databaseType, TableSchema tableSchema) throws Exception {
        if (!importing.getAndSet(true)) {
            CompletableFuture.runAsync(() -> {
                metadataService.synchronizeMetaData(databaseType, tableSchema);
                importing.set(false);
                metadataService.refreshCache();
            });
        } else {
            return Response.status(400).entity(String.format("%s元数据正在同步中", databaseType)).build();
        }
        return Response.status(202).entity(String.format("%s元数据增量同步已开始", databaseType)).build();
    }

    @GET
    @Path("/import/progress/{databaseType}")
    public Response importProgress(@PathParam("databaseType") String databaseType) throws Exception {
        Progress progress = metadataService.importProgress(databaseType,"hive");
        return Response.status(200).entity(new Gson().toJson(progress)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/business/trust")
    public Response updateTrustTable(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            businessService.updateBusinessTrustTable(tenantId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
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
        }  catch (Exception e) {
            PERF_LOG.error(e.getMessage(), e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除标签失败");
        }
    }


    /**
     * Delete an entity identified by its GUID.
     * @param  guid GUID for the entity
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
            throw e;
        }
    }

    @POST
    @Path("/export")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DownloadUri downloadExcelTemplate(List<String> tableGuidList) throws AtlasBaseException {
        try {
            return ExportDataPathUtils.generateURL(httpServletRequest.getRequestURL().toString(), tableGuidList);
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "下载报告失败");
        }
    }

    @GET
    @Path("/export/{downloadId}")
    @Valid
    public void exportSelected(@PathParam("downloadId") String downloadId,@HeaderParam("tenantId")String tenantId) throws Exception {
        File xlsxFile = null;
        try {
            List<String> metadataGuidList = ExportDataPathUtils.getDataIdsByUrlId(downloadId);
            xlsxFile = metadataService.exportExcel(metadataGuidList,tenantId);
            httpServletResponse.setContentType("application/msexcel;charset=utf-8");
            httpServletResponse.setCharacterEncoding("utf-8");
            String fileName = new String( new String(xlsxFile.getName()).getBytes(), "ISO-8859-1");
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
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/table/{tableGuid}/{version}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TableMetadata getTableMetadata(@PathParam("tableGuid") String tableGuid, @PathParam("version") Integer version) throws AtlasBaseException {
        try {
            return metadataService.getTableMetadata(tableGuid, version);
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }


    @POST
    @Path("/column/{tableGuid}/{version}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<ColumnMetadata> getColumnHistoryList(@PathParam("tableGuid") String tableGuid, @PathParam("version") Integer version, ColumnQuery query) throws AtlasBaseException {
        try {
            return metadataService.getColumnHistoryInfo(tableGuid, version, query);
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/table/{tableGuid}/compare/{version}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ComparisonMetadata getComparisionTableMetadata(@PathParam("tableGuid") String tableGuid, @PathParam("version") Integer version) throws AtlasBaseException {
        try {
            return metadataService.getComparisionTableMetadata(tableGuid, version);
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/column/{tableGuid}/compare/{version}/history")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ComparisonColumnMetadata getComparisionColumnTableMetadata(@PathParam("tableGuid") String tableGuid, @PathParam("version") Integer version) throws AtlasBaseException {
        try {
            return metadataService.getComparisionColumnMetadata(tableGuid, version);
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 更新依赖标准
     * @param dataStandAndTable
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/dataStandard")
    public boolean assignTableToStandard(DataStandAndTable dataStandAndTable,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        String tableName = tableDAO.getTableNameByTableGuid(dataStandAndTable.getTableGuid());
        if (tableName==null){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "表不存在或已删除，请刷新或者检查元数据");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.METADATA.getAlias(), tableName);
        try {
            dataStandardService.assignTableToStandard(dataStandAndTable,tableName,tenantId);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 根据搜索条件返回关系型数据库的数据源
     *
     * @return List<RDBMSDataSource>
     */
    @POST
    @Path("/search/rdbms/datasource/{sourceType}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RDBMSDataSource> getDataSourceByQuery(@QueryParam("active")@DefaultValue("true")Boolean active,Parameters parameters,@PathParam("sourceType") String sourceType) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getDataSourceByQuery(" + parameters + " )");
            }
            PageResult<RDBMSDataSource> pageResult = searchService.getDataSourcePageResult(parameters,sourceType,active);
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
    public PageResult<RDBMSDatabase> getRDBMSDBBySource(@QueryParam("active")@DefaultValue("true")Boolean active,@PathParam("sourceId") String sourceId, @PathParam("offset") long offset, @PathParam("limit") long limit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSDBBySource(" + sourceId + "," + limit + "," + offset + " )");
            }
            PageResult<RDBMSDatabase> pageResult = searchService.getRDBMSDBBySource(sourceId, offset, limit,active);
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
    public PageResult<RDBMSTable> getRDBMSTableByDB(@QueryParam("active")@DefaultValue("true")Boolean active,@PathParam("databaseId") String databaseId, @PathParam("offset") long offset, @PathParam("limit") long limit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSTableByDB(" + databaseId + "," + limit + "," + offset + " )");
            }
            PageResult<RDBMSTable> pageResult = searchService.getRDBMSTableByDB(databaseId, offset, limit,active);
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
    public PageResult<RDBMSDatabase> getRDBMSDBByQuery(@QueryParam("active")@DefaultValue("true")Boolean active,Parameters parameters,@PathParam("sourceType") String sourceType) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSDBByQuery(" + parameters + " )");
            }
            PageResult<RDBMSDatabase> pageResult = searchService.getRDBMSDBPageResultV2(parameters,sourceType,active);
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
    public PageResult<RDBMSTable> getRDBMSTableByQuery(@QueryParam("active")@DefaultValue("true")Boolean active,Parameters parameters,@PathParam("sourceType") String sourceType) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSTableByQuery(" + parameters + " )");
            }
            PageResult<RDBMSTable> pageResult = searchService.getRDBMSTablePageResultV2(parameters,sourceType,active);
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
    public PageResult<RDBMSColumn> getRDBMSColumnByQuery(Parameters parameters,@PathParam("sourceType") String sourceType) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getRDBMSColumnByQuery(" + parameters + " )");
            }
            PageResult<RDBMSColumn> pageResult = searchService.getRDBMSColumnPageResultV2(parameters,sourceType);
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
    public BuildTableSql getRDBMSTableSQL(@PathParam("tableId") String tableId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableSQL(" + tableId + " )");
            }
            BuildTableSql buildTableSql = searchService.getBuildRDBMSTableSql(tableId);
            return buildTableSql;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "hive查询异常");
        } catch (IOException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "图数据查询异常");
        } finally {
            AtlasPerfTracer.log(perf);
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
    public RDBMSTable getTableInfoById(@PathParam("tableId") String tableId) throws AtlasBaseException {
        return metadataService.getRDBMSTableInfoById(tableId);
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
    public RDBMSColumnAndIndexAndForeignKey getRDBMSColumnInfoById(ColumnQuery query, @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", query.getGuid());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnInfoById");
            }
            return metadataService.getRDBMSColumnInfoById(query, refreshCache);
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
        } catch (AtlasBaseException e) {
            throw e;
        } catch (IOException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限访问");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
    /**
     * Delete an entity identified by its GUID.
     * @param  guid GUID for the entity
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
            EntityMutationResponse entityMutationResponse =metadataService.hardDeleteRDBMSInstanceByGuid(guid);
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "刷新失败");
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/influence/api/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getTableInfluenceWithAPI( @PathParam("tableGuid") String tableGuid, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return metadataService.getTableInfluenceWithAPI(tableGuid, parameters,tenantId);
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }
    @GET
    @Path("/influence/table/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<TableHeader> getTableInfluenceWithDbAndTable( @PathParam("tableGuid") String tableGuid) throws AtlasBaseException {
        try {
            return metadataService.getTableInfluenceWithDbAndTable(tableGuid);
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 获取表依赖标准
     * @param tableGuid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/dataStandard/{tableGuid}")
    public List<DataStandardHead> getDataStandard(@PathParam("tableGuid") String tableGuid,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
           return dataStandardService.getDataStandardByTable(tableGuid,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }
    @GET
    @Path("/check/table/{tableGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CheckingInfo getCheckingTableInfo( @PathParam("tableGuid") String tableGuid) throws AtlasBaseException {
        try {
            return metadataService.getCheckingTableInfo(tableGuid);
        }  catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

}
