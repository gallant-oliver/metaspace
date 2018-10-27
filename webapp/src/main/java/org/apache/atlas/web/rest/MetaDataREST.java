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
package org.apache.atlas.web.rest;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.metadata.*;
import org.apache.atlas.model.result.BuildTableSql;
import org.apache.atlas.model.result.PageResult;
import org.apache.atlas.model.result.TableShow;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.service.MetaDataService;
import org.apache.atlas.web.service.SearchService;
import org.apache.atlas.web.util.Servlets;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Path("metadata")
@Singleton
@Service
public class MetaDataREST {
    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.DiscoveryREST");
    @Autowired
    private SearchService searchService;
    @Context
    private HttpServletRequest httpServletRequest;
    private static final String DEFAULT_DIRECTION = "BOTH";
    private static final String DEFAULT_DEPTH = "3";


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
    public PageResult<Database> getAllDatabase(Parameters parameters) throws AtlasBaseException {
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
    @Path("/search/table/preview")
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
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * sql
     *
     * @return List<Table>
     */
    @GET
    @Path("/search/table/sql")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BuildTableSql getTableSQL(@QueryParam("tableId") String tableId) throws AtlasBaseException, TException, SQLException {
        AtlasPerfTracer perf = null;
        try {

            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableSQL(" + tableId + " )");
            }
            BuildTableSql buildTableSql = searchService.getBuildTableSql(tableId);
            return buildTableSql;
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
    @Path("/table/guid/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table getTableInfoById(@PathParam("guid") String guid) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableInfoById()");
            }
            return metadataService.getTableInfoById(guid);
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
    public List<Column> getColumnInfoById(ColumnQuery query) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", query.getGuid());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnInfoById");
            }
            return metadataService.getColumnInfoById(query);
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
    public LineageInfo getTableLineage(@PathParam("guid") String guid,
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
    public LineageInfo.LineageEntity getLineageInfo(@PathParam("guid") String guid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getLineageInfo");
            }
            return metadataService.getLineageInfo(guid);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加目录
     *
     * @param category
     * @return
     * @throws AtlasBaseException
     */
    @POST
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
    }

    /**
     * 修改目录信息
     *
     * @param category
     * @return
     * @throws AtlasBaseException
     */
    @POST
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
    }

    /**
     * 删除目录
     *
     * @param categoryGuid
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/category/{categoryGuid}")
    public void deleteGlossaryCategory(@PathParam("categoryGuid") String categoryGuid) throws AtlasBaseException {
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
    }

    /**
     * 添加关联
     *
     * @param categoryGuid
     * @param relatedObjectIds
     * @throws AtlasBaseException
     */
    @POST
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
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @GET
    @Path("/category/relations/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public RelationEntity getCategoryRelations(@PathParam("categoryGuid") String categoryGuid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.getCategoryRelations(" + categoryGuid + ")");
            }
            return metadataService.getCategoryRelations(categoryGuid);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @DELETE
    @Path("/category/{categoryGuid}/assignedEntities")
    public void removeRelationAssignmentFromEntities(@PathParam("categoryGuid") String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
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


    }

    /**
     * 获取全部目录层级
     *
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryHeader> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getCategories()");
            }
            return metadataService.getCategories(sort);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/update/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void updateTableDescription(TableEdit tableEdit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.updateTableDescription()");
            }
            metadataService.updateTableDescription(tableEdit);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @POST
    @Path("/update/table/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void updateColumnDescription(ColumnEdit columnEdit) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.updateColumnDescription()");
            }
            metadataService.updateColumnDescription(columnEdit);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
}
