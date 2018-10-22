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
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.discovery.SearchParameters;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.metadata.Column;
import org.apache.atlas.model.metadata.Database;
import org.apache.atlas.model.metadata.Parameters;
import org.apache.atlas.model.metadata.Table;
import org.apache.atlas.model.result.PageResult;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private RelationshipREST relationshipREST;
    @Context
    private HttpServletRequest httpServletRequest;

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
            PageResult<Database> pageResult = new PageResult<>();
            List<Database> databases = new ArrayList<>();
            SearchParameters searchParameters = new SearchParameters();
            searchParameters.setOffset(parameters.getOffset());
            searchParameters.setLimit(parameters.getLimit());
            searchParameters.setExcludeDeletedEntities(true);
            searchParameters.setIncludeClassificationAttributes(true);
            searchParameters.setIncludeSubClassifications(true);
            searchParameters.setIncludeSubTypes(true);
            searchParameters.setTypeName("hive_db");
            List<AtlasEntityHeader> entities = discoveryREST.searchWithParameters(searchParameters).getEntities();
            if (entities == null) {
                throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
            }
            for (AtlasEntityHeader entity : entities) {
                Database database = new Database();
                ArrayList<Table> tables = new ArrayList<>();

                String databaseGuid = entity.getGuid();
                String databaseName = entity.getDisplayText();
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
            pageResult.setOffset(parameters.getOffset());
            pageResult.setCount(databases.size());
            pageResult.setLists(databases);
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
            PageResult<Table> pageResult = new PageResult<>();
            List<Table> tables = new ArrayList<>();
            SearchParameters searchParameters = new SearchParameters();
            searchParameters.setOffset(parameters.getOffset());
            searchParameters.setLimit(parameters.getLimit());
            searchParameters.setExcludeDeletedEntities(true);
            searchParameters.setIncludeClassificationAttributes(true);
            searchParameters.setIncludeSubClassifications(true);
            searchParameters.setIncludeSubTypes(true);
            searchParameters.setTypeName("hive_table");
            if(parameters.getQuery()!=null){
                SearchParameters.FilterCriteria entityFilters = new SearchParameters.FilterCriteria();
                SearchParameters.FilterCriteria criteria1 = new SearchParameters.FilterCriteria();
                ArrayList<SearchParameters.FilterCriteria> criterias = new ArrayList<>();
                criteria1.setAttributeName("name");
                criteria1.setOperator(SearchParameters.Operator.CONTAINS);
                criteria1.setAttributeValue(parameters.getQuery());
                criterias.add(criteria1);
                entityFilters.setCondition(SearchParameters.FilterCriteria.Condition.AND);
                entityFilters.setCriterion(criterias);
                searchParameters.setEntityFilters(entityFilters);
            }
            List<AtlasEntityHeader> entities = discoveryREST.searchWithParameters(searchParameters).getEntities();
            if (entities == null) {
                throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
            }
            for (AtlasEntityHeader entitie : entities) {
                Table table = new Table();
                table.setTableId(entitie.getGuid());
                table.setTableName(entitie.getDisplayText());
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
            pageResult.setOffset(parameters.getOffset());
            pageResult.setCount(tables.size());
            pageResult.setLists(tables);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 根据搜索条件返回表
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
            PageResult<Column> pageResult = new PageResult<>();
            List<Column> columns = new ArrayList<>();
            SearchParameters searchParameters = new SearchParameters();
            searchParameters.setOffset(parameters.getOffset());
            searchParameters.setLimit(parameters.getLimit());
            searchParameters.setExcludeDeletedEntities(true);
            searchParameters.setIncludeClassificationAttributes(true);
            searchParameters.setIncludeSubClassifications(true);
            searchParameters.setIncludeSubTypes(true);
            searchParameters.setTypeName("hive_column");
            if(parameters.getQuery()!=null) {
                SearchParameters.FilterCriteria entityFilters = new SearchParameters.FilterCriteria();
                SearchParameters.FilterCriteria criteria1 = new SearchParameters.FilterCriteria();
                ArrayList<SearchParameters.FilterCriteria> criterias = new ArrayList<>();
                criteria1.setAttributeName("name");
                criteria1.setOperator(SearchParameters.Operator.CONTAINS);
                criteria1.setAttributeValue(parameters.getQuery());
                criterias.add(criteria1);
                entityFilters.setCondition(SearchParameters.FilterCriteria.Condition.AND);
                entityFilters.setCriterion(criterias);
                searchParameters.setEntityFilters(entityFilters);
            }
            List<AtlasEntityHeader> entities = discoveryREST.searchWithParameters(searchParameters).getEntities();
            if (entities == null) {
                throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
            }
            for (AtlasEntityHeader entitie : entities) {
                Column column = new Column();
                column.setColumnId(entitie.getGuid());
                column.setColumnName(entitie.getDisplayText());
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
            pageResult.setOffset(parameters.getOffset());
            pageResult.setCount(columns.size());
            pageResult.setLists(columns);
            return pageResult;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
}
