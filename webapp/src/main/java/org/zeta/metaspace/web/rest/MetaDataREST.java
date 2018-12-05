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
package org.zeta.metaspace.web.rest;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.zeta.metaspace.model.result.BuildTableSql;
import org.zeta.metaspace.model.result.PageResult;
import org.zeta.metaspace.model.result.TableShow;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.zeta.metaspace.model.metadata.CategoryChildren;
import org.zeta.metaspace.model.metadata.Column;
import org.zeta.metaspace.model.metadata.ColumnEdit;
import org.zeta.metaspace.model.metadata.ColumnLineageInfo;
import org.zeta.metaspace.model.metadata.ColumnQuery;
import org.zeta.metaspace.model.metadata.Database;
import org.zeta.metaspace.model.metadata.GuidCount;
import org.zeta.metaspace.model.metadata.LineageDepthInfo;
import org.zeta.metaspace.model.metadata.Parameters;

import org.zeta.metaspace.model.metadata.RelationQuery;
import org.zeta.metaspace.model.metadata.Table;
import org.zeta.metaspace.model.metadata.TableEdit;
import org.zeta.metaspace.model.metadata.TableLineageInfo;
import org.zeta.metaspace.web.service.MetaDataService;
import org.zeta.metaspace.web.service.SearchService;
import org.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.atlas.web.service.DataManageService;

import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

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

    @Autowired
    private DataManageService dataManageService;

    private final MetaDataService metadataService;

    @Inject
    public MetaDataREST(final MetaDataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * 返回全部的库
     *
     * @return List<Database>
     */
    @POST
    @Path("/search/database")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getAllDatabase(Parameters parameters, @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getAllDatabase(" + parameters + " )");
            }
            PageResult<Database> pageResult = searchService.getDatabasePageResult(parameters);
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
    public PageResult<Table> getTableByQuery(Parameters parameters) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableByQuery(" + parameters + " )");
            }
            PageResult<Table> pageResult = searchService.getTablePageResult(parameters);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回行
     *
     * @return List<Column>
     */
    @POST
    @Path("/search/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Column> getColumnByQuery(Parameters parameters) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnByQuery(" + parameters + " )");
            }
            PageResult<Column> pageResult = searchService.getColumnPageResult(parameters);
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
            TableShow tableShow = searchService.getTableShow(guidCount);
            return tableShow;
        } catch (AtlasBaseException e) {
            throw  e;
        } catch (IOException e) {
            throw  new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无权限访问");
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
        if(tableId==null|tableId.equals("")) {
            //表id为空

        }
        try {

            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableSQL(" + tableId + " )");
            }
            BuildTableSql buildTableSql = searchService.getBuildTableSql(tableId);
            return buildTableSql;
        } catch (AtlasBaseException e) {
            throw  e;
        }catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"hive查询异常");
        } catch (IOException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"图数据查询异常");
        } finally {
            AtlasPerfTracer.log(perf);
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
    @Path("/table/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table getTableInfoById(@PathParam("guid") String guid, @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableInfoById()");
            }
            return metadataService.getTableInfoById(guid,refreshCache);
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
                                            @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth,
                                            @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
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
     * 表血缘深度详情
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/lineage/depth/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public LineageDepthInfo getTableLineageDepthInfo(@PathParam("guid") String guid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getLineageInfo");
            }
            return metadataService.getTableLineageDepthInfo(guid);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取字段血缘
     * @param guid
     * @param direction
     * @param depth
     * @param refreshCache
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/column/lineage/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ColumnLineageInfo getColumnLineage(@PathParam("guid") String guid,
                                              @QueryParam("direction") @DefaultValue(DEFAULT_DIRECTION) AtlasLineageInfo.LineageDirection direction,
                                              @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth,
                                              @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
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
     * 字段血缘深度详情
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/column/lineage/depth/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public LineageDepthInfo getColumnLineageDepthInfo(@PathParam("guid") String guid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getLineageInfo");
            }
            return metadataService.getColumnLineageDepthInfo(guid);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /*@POST
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryEntity createMetadataCategory(CategoryEntity category) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.createMetadataCategory()");
            }
            return metadataService.createMetadataCategory(category);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }*/

    /**
     * 添加目录 V2
     * @param categoryInfo
     * @return
     * @throws Exception
     */
    @POST
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryEntityV2 createCategory(CategoryInfoV2 categoryInfo) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.createMetadataCategory()");
            }
            return dataManageService.createCategory(categoryInfo);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /*@POST
    @Path("/update/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryEntity updateMetadataCategory(CategoryEntity category) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.CategoryEntity()");
            }
            return metadataService.updateMetadataCategory(category);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }*/

    /**
     * 修改目录信息 V2
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/update/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryEntityV2 updateCategory(CategoryInfoV2 categoryInfo) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.CategoryEntity()");
            }
            return dataManageService.updateCategory(categoryInfo);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /*@DELETE
    @Path("/category/{categoryGuid}")
    public Response deleteGlossaryCategory(@PathParam("categoryGuid") String categoryGuid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.deleteGlossaryCategory(" + categoryGuid + ")");
            }
            metadataService.deleteGlossaryCategory(categoryGuid);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }*/

    /**
     * 删除目录 V2
     * @param categoryGuid
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("/category/{categoryGuid}")
    public Response deleteCategory(@PathParam("categoryGuid") String categoryGuid) throws Exception {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.deleteCategory(" + categoryGuid + ")");
            }
            dataManageService.deleteCategory(categoryGuid);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /*@POST
    @Path("/category/{categoryGuid}/assignedEntities")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<AtlasRelatedObjectId> assignTermToEntities(@PathParam("categoryGuid") String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.assignTermToEntities(" + categoryGuid + ")");
            }
            return metadataService.assignTermToEntities(categoryGuid, relatedObjectIds);
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加关联失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }*/

    /**
     * 添加关联
     * @param categoryGuid
     * @param relations
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/{categoryGuid}/assignedEntities")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response assignTableToCategory(@PathParam("categoryGuid") String categoryGuid, List<RelationEntityV2> relations) throws AtlasBaseException {
        //Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.assignTableToCategory(" + categoryGuid + ")");
            }
            dataManageService.assignTablesToCategory(categoryGuid, relations);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加关联失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /*@GET
    @Path("/category/relations/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public RelationEntity getCategoryRelations(@PathParam("categoryGuid") String categoryGuid,
                                               @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.getCategoryRelations(" + categoryGuid + ")");
            }
            return metadataService.getCategoryRelations(categoryGuid,refreshCache);
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }*/

    /**
     * 获取关联关系
     * @param categoryGuid
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/relations/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getCategoryRelations(@PathParam("categoryGuid") String categoryGuid,RelationQuery relationQuery) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.getCategoryRelations(" + categoryGuid + ")");
            }
            return dataManageService.getRelationsByCategoryGuid(categoryGuid, relationQuery);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取子目录
     * @param categoryGuid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category/children/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryChildren getCategoryChildren(@PathParam("categoryGuid") String categoryGuid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.getCategoryChildren(" + categoryGuid + ")");
            }
            return metadataService.getCategoryChildren(categoryGuid);
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取子目录失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /*@DELETE
    @Path("/category/{categoryGuid}/assignedEntities")
    public Response removeRelationAssignmentFromEntities(@PathParam("categoryGuid") String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.removeRelationAssignmentFromEntities(" + categoryGuid + ")");
            }
            metadataService.removeRelationAssignmentFromEntities(categoryGuid, relatedObjectIds);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }*/

    /**
     * 删除关联关系
     * @param relationshipList
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/category/relation")
    public Response removeRelationAssignmentFromTables(List<RelationEntityV2> relationshipList) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.removeRelationAssignmentFromEntities(" + relationshipList + ")");
            }
            dataManageService.removeRelationAssignmentFromTables(relationshipList);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /*@POST
    @Path("/table/relations/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<RelationEntity.RelationInfo> getQueryTables(RelationQuery relationQuery) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getQueryTables()");
            }
            String filterName = relationQuery.getFilterTableName();
            return metadataService.getQueryTables(filterName);
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }*/

    /**
     * 获取表关联
     * @param relationQuery
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/relations/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableName(relationQuery);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /*@GET
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryHeader> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort,
                                             @DefaultValue("false") @QueryParam("refreshCache") Boolean refreshCache) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getCategories()");
            }
            return metadataService.getCategories(sort, refreshCache);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }*/

    /**
     * 获取全部目录
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryEntityV2> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getCategories()");
            }
            return dataManageService.getAll();
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 更新表描述
     * @param tableEdit
     * @throws AtlasBaseException
     */
    @POST
    @Path("/update/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateTableDescription(TableEdit tableEdit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.updateTableDescription()");
            }
            metadataService.updateTableDescription(tableEdit);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 更新字段描述
     * @param columnEdits
     * @throws AtlasBaseException
     */
    @POST
    @Path("/update/table/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateColumnDescription(List<ColumnEdit> columnEdits) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.updateColumnDescription()");
            }
            metadataService.updateColumnDescription(columnEdits);
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }



    /**
     * 清除缓存
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

}
