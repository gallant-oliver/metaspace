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

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.glossary.GlossaryService;
import org.apache.atlas.model.discovery.AtlasSearchResult;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.metadata.*;
import org.apache.atlas.model.result.BuildTableSql;
import org.apache.atlas.model.result.PageResult;
import org.apache.atlas.model.result.TableShow;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.service.MetaDataService;
import org.apache.atlas.web.util.HiveJdbcUtils;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Path("metadata")
@Singleton
@Service
public class MetaDataREST {
    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.DiscoveryREST");
    @Autowired
    private DiscoveryREST discoveryREST;
    @Autowired
    private EntityREST entityREST;
    @Autowired
    private  AtlasEntityStore entitiesStore;
    @Autowired
    private  AtlasLineageService atlasLineageService;
    @Autowired
    private  GlossaryService glossaryService;
    @Context
    private HttpServletRequest httpServletRequest;
    private static final String DEFAULT_DIRECTION = "BOTH";
    private static final String DEFAULT_DEPTH     = "3";



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
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getAllDatabase(" + parameters+ " )");
            }
            PageResult<Database> pageResult = new PageResult<>();
            List<Database> databases = new ArrayList<>();
            List<List<Object>> hiveDbs = discoveryREST.searchUsingDSL("name like '*" + parameters.getQuery() + "*' where __state = 'ACTIVE' select name,__guid orderby __timestamp", "hive_db", "", parameters.getLimit(), parameters.getOffset()).getAttributes().getValues();
            if (hiveDbs == null) {
                throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
            }
            for (List<Object> db : hiveDbs) {
                Database database = new Database();
                ArrayList<Table> tables = new ArrayList<>();
                String databaseGuid = db.get(1).toString();
                String databaseName = db.get(0).toString();
                AtlasEntity.AtlasEntityWithExtInfo databaseInfo = entityREST.getById(databaseGuid, true);
                Map<String, Object> databaseAttr = databaseInfo.getEntity().getAttributes();
                String databaseDescription = databaseAttr.get("description") == null ? "null" : databaseAttr.get("description").toString();
                database.setDatabaseId(databaseGuid);
                database.setDatabaseName(databaseName);
                database.setDatabaseDescription(databaseDescription);
                Map<String, Object> databaseRelationshipAttribute = databaseInfo.getEntity().getRelationshipAttributes();
                List<AtlasRelatedObjectId> databaseAtlasRelatedObjectIds = (List) databaseRelationshipAttribute.get("tables");
                for (AtlasRelatedObjectId databaseAtlasRelatedObjectId : databaseAtlasRelatedObjectIds) {
                    Table table = new Table();
                    table.setDatabaseId(databaseGuid);
                    table.setDatabaseName(databaseName);
                    table.setTableId(databaseAtlasRelatedObjectId.getGuid());
                    table.setTableName(databaseAtlasRelatedObjectId.getDisplayText());
                    AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(table.getTableId(), true);
                    Map<String, Object> tableAttributes = tableInfo.getEntity().getAttributes();
                    String tableDescription = tableAttributes.get("description") == null ? "null" : tableAttributes.get("description").toString();
                    table.setDescription(tableDescription);
                    tables.add(table);
                }
                databases.add(database);

            }
            AtlasSearchResult numResult = discoveryREST.searchUsingDSL("name like '*" + parameters.getQuery() + "*' where __state = 'ACTIVE' select count()","hive_db","",1,0);
            Object values = numResult.getAttributes().getValues().get(0).get(0);
            pageResult.setOffset(parameters.getOffset());
            pageResult.setCount(databases.size());
            pageResult.setLists(databases);
            pageResult.setSum(Integer.valueOf(values.toString()));
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
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableByQuery(" + parameters+ " )");
            }
            PageResult<Table> pageResult = new PageResult<>();
            List<List<Object>> hiveTables = discoveryREST.searchUsingDSL("name like '*" + parameters.getQuery() + "*' where __state = 'ACTIVE' select name,__guid orderby __timestamp", "hive_table", "", parameters.getLimit(), parameters.getOffset()).getAttributes().getValues();
            if (hiveTables == null) {
                throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
            }
            List<Table> tables = new ArrayList<>();
            for (List<Object> nameId : hiveTables) {
                Table table = new Table();
                table.setTableId(nameId.get(1).toString());
                table.setTableName(nameId.get(0).toString());
                AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(table.getTableId(), true);
                AtlasEntity tableEntity = tableInfo.getEntity();
                Map<String, Object> tableAttributes = tableEntity.getAttributes();
                String tableDescription = tableAttributes.get("description") == null ? "null" : tableAttributes.get("description").toString();
                table.setDescription(tableDescription);
                Map<String, Object> relationshipAttributes = tableEntity.getRelationshipAttributes();
                AtlasRelatedObjectId db = (AtlasRelatedObjectId) relationshipAttributes.get("db");
                table.setDatabaseId(db.getGuid());
                table.setDatabaseName(db.getDisplayText());
                tables.add(table);
            }
            AtlasSearchResult numResult = discoveryREST.searchUsingDSL("name like '*" + parameters.getQuery() + "*' where __state = 'ACTIVE' select count()","hive_table","",1,0);
            Object values = numResult.getAttributes().getValues().get(0).get(0);
            pageResult.setOffset(parameters.getOffset());
            pageResult.setCount(tables.size());
            pageResult.setLists(tables);
            pageResult.setSum(Integer.valueOf(values.toString()));
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
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getColumnByQuery(" + parameters+ " )");
            }
            PageResult<Column> pageResult = new PageResult<>();
            List<List<Object>> hiveColumns = discoveryREST.searchUsingDSL("name like '*" + parameters.getQuery() + "*' where __state = 'ACTIVE' select name,__guid orderby __timestamp", "hive_column", "", parameters.getLimit(), parameters.getOffset()).getAttributes().getValues();
            if (hiveColumns == null) {
                throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
            }
            List<Column> columns = new ArrayList<>();
            for (List<Object> nameId : hiveColumns) {
                Column column = new Column();
                column.setColumnId(nameId.get(1).toString());
                column.setColumnName(nameId.get(0).toString());
                AtlasEntity.AtlasEntityWithExtInfo columnInfo = entityREST.getById(column.getColumnId(), true);
                AtlasEntity columnEntity = columnInfo.getEntity();
                Map<String, Object> relationshipAttributes = columnEntity.getRelationshipAttributes();
                AtlasRelatedObjectId table = (AtlasRelatedObjectId) relationshipAttributes.get("table");
                column.setTableId(table.getGuid());
                column.setTableName(table.getDisplayText());
                AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(table.getGuid(), true);
                AtlasEntity tableEntity = tableInfo.getEntity();
                Map<String, Object> dbRelationshipAttributes = tableEntity.getRelationshipAttributes();
                AtlasRelatedObjectId db = (AtlasRelatedObjectId) dbRelationshipAttributes.get("db");
                column.setDatabaseId(db.getGuid());
                column.setDatabaseName(db.getDisplayText());
                columns.add(column);
            }
            AtlasSearchResult numResult = discoveryREST.searchUsingDSL("name like '*" + parameters.getQuery() + "*' where __state = 'ACTIVE' select count()","hive_column","",1,0);
            Object values = numResult.getAttributes().getValues().get(0).get(0);
            pageResult.setOffset(parameters.getOffset());
            pageResult.setCount(columns.size());
            pageResult.setLists(columns);
            pageResult.setSum(Integer.valueOf(values.toString()));
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
            TableShow tableShow = new TableShow();
            //获取entity
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guidCount.getGuid());
            AtlasEntity entity = info.getEntity();
            String name = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
            if (name == "") {
                System.out.println("该id不存在");
            }
            AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(guidCount.getGuid(), true);
            AtlasEntity tableEntity = tableInfo.getEntity();
            Map<String, Object> dbRelationshipAttributes = tableEntity.getRelationshipAttributes();
            AtlasRelatedObjectId db = (AtlasRelatedObjectId) dbRelationshipAttributes.get("db");
            String dbDisplayText = db.getDisplayText();
            String sql = "select * from " + name + " limit " + guidCount.getCount();
            ResultSet resultSet = HiveJdbcUtils.selectBySQL(sql,dbDisplayText);
            List<String> columns = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<Map<String, String>> resultList = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                columns.add(columnName);
            }
            while (resultSet.next()){
                Map<String, String> map = new HashMap<>();
                for (String column : columns) {
                    String s = resultSet.getObject(column).toString();
                    map.put(column, s);
                }
                resultList.add(map);
            }
            resultSet.close();
            tableShow.setTableId(guidCount.getGuid());
            tableShow.setColumnNames(columns);
            tableShow.setLines(resultList);
            return tableShow;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
    /**
     * 根据搜索条件返回表
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
            BuildTableSql buildTableSql = new BuildTableSql();
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getTableSQL(" +tableId+ " )" );
            }
            //获取entity
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(tableId);
            AtlasEntity entity = info.getEntity();
            String name = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
            if (name == "") {
                System.out.println("该id不存在");
            }
            AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(tableId, true);
            AtlasEntity tableEntity = tableInfo.getEntity();
            Map<String, Object> dbRelationshipAttributes = tableEntity.getRelationshipAttributes();
            AtlasRelatedObjectId db = (AtlasRelatedObjectId) dbRelationshipAttributes.get("db");
            String dbDisplayText = db.getDisplayText();
            String sql = "show create table " + name ;
            ResultSet resultSet = HiveJdbcUtils.selectBySQL(sql,dbDisplayText);
            StringBuffer stringBuffer = new StringBuffer();
            while(resultSet.next()){
                Object object = resultSet.getObject(1);
                stringBuffer.append(object.toString());
            }
            buildTableSql.setSql(stringBuffer.toString());
            buildTableSql.setTableId(tableId);
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
    @Path("/category/relations/{categoryGuid}/assignedEntities")
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
}
