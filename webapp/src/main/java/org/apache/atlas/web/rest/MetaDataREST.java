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
import org.apache.atlas.SortOrder;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.glossary.GlossaryService;
import org.apache.atlas.model.discovery.SearchParameters;
import org.apache.atlas.model.glossary.AtlasGlossary;
import org.apache.atlas.model.glossary.AtlasGlossaryCategory;
import org.apache.atlas.model.glossary.AtlasGlossaryTerm;
import org.apache.atlas.model.glossary.relations.AtlasGlossaryHeader;
import org.apache.atlas.model.glossary.relations.AtlasRelatedCategoryHeader;
import org.apache.atlas.model.glossary.relations.AtlasRelatedTermHeader;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.metadata.*;
import org.apache.atlas.model.result.PageResult;
import org.apache.atlas.repository.audit.EntityAuditRepository;
import org.apache.atlas.repository.converters.AtlasInstanceConverter;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.HiveJdbcUtils;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
    private final String rootDir = "gridsum";
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


    /**
     * 获取字段详情
     * @param minExtInfo
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/column/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Column> getColumnInfoById(@QueryParam("minExtInfo") @DefaultValue("false") boolean minExtInfo,
                                          ColumnQuery query) throws AtlasBaseException {
        String guid = query.getGuid();
        Servlets.validateQueryParamLength("guid", guid);
        List<Column> columns = null;
        AtlasPerfTracer perf = null;

        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "EntityREST.getById(" + guid + ", " + minExtInfo + " )");
            }
            columns = new ArrayList<>();
            Column column = null;

            //获取entity
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
            AtlasEntity entity = info.getEntity();

            //获取PartitionKey的guid
            List<AtlasObjectId> partitionKeys = null;
            if(null != entity.getAttribute("partitionKeys")) {
                Object partitionObjects = entity.getAttribute("partitionKeys");
                if(partitionObjects instanceof ArrayList<?>) {
                    partitionKeys = (ArrayList<AtlasObjectId>)partitionObjects;
                }
            }
            Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
            for(String key: referredEntities.keySet()) {
                column = new Column();
                //tableId
                column.setTableId(guid);
                //tableName
                if(entity.hasAttribute("name") && entity.getAttribute("name") != null)
                    column.setTableName(entity.getAttribute("name").toString());

                //databaseId && dataBaseName
                if(entity.hasRelationshipAttribute("db") && entity.getRelationshipAttribute("db") != null) {
                    Object relAttribute = entity.getRelationshipAttribute("db");
                    if(relAttribute instanceof AtlasRelatedObjectId) {
                        AtlasRelatedObjectId relObject = (AtlasRelatedObjectId)relAttribute;
                        column.setDatabaseId(relObject.getGuid());
                        column.setDatabaseName(relObject.getDisplayText());
                    }
                }
                AtlasEntity referredEntity = referredEntities.get(key);
                if(referredEntity.getTypeName().contains("column")) {
                    column.setColumnId(referredEntity.getGuid());
                    column.setPartitionKey(false);
                    if(partitionKeys != null) {
                        for (int i = 0; i < partitionKeys.size(); i++) {
                            if (partitionKeys.get(i).getGuid().equals(column.getColumnId())) {
                                column.setPartitionKey(true);
                            }
                        }
                    }
                    Map<String,Object> attributes = referredEntity.getAttributes();
                    if(attributes.containsKey("name") && attributes.get("name") != null) {
                        column.setColumnName(attributes.get("name").toString());
                    } else {
                        column.setColumnName("");
                    }
                    if(attributes.containsKey("type") && attributes.get("type") != null) {
                        column.setType(attributes.get("type").toString());
                    } else {
                        column.setType("");
                    }
                    if(attributes.containsKey("comment") && attributes.get("comment") != null) {
                        column.setDescription(attributes.get("comment").toString());
                    } else {
                        column.setDescription("");
                    }
                    columns.add(column);
                }
            }
            if(query.getColumnFilter() != null) {
                ColumnQuery.ColumnFilter filter = query.getColumnFilter();
                String columnName = filter.getColumnName();
                String type = filter.getType();
                String description = filter.getDescription();
                if(columnName!=null && !columnName.equals("")) {
                    columns = columns.stream().filter(col -> col.getColumnName().equals(filter.getColumnName())).collect(Collectors.toList());
                }
                if(type!=null && !type.equals("")) {
                    columns = columns.stream().filter(col -> col.getType().equals(type)).collect(Collectors.toList());
                }
                if(description!=null && description.equals("")) {
                    columns = columns.stream().filter(col -> col.getDescription().equals(description)).collect(Collectors.toList());
                }
            }
            return columns;

        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取表详情
     *
     * @param guid
     * @param minExtInfo
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/guid/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table getTableInfoById(@PathParam("guid") String guid, @QueryParam("minExtInfo") @DefaultValue("false") boolean minExtInfo) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);
        Table table = null;
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "EntityREST.getById(" + guid + ", " + minExtInfo + " )");
            }
            table = new Table();
            table.setTableId(guid);

            //获取entity
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
            AtlasEntity entity = info.getEntity();

            if(entity.getTypeName().contains("table")) {
                //表名称
                if(entity.hasAttribute("name") && entity.getAttribute("name") != null) {
                    table.setTableName(entity.getAttribute("name").toString());
                } else {
                    table.setTableName("");
                }
                //创建人
                if(entity.hasAttribute("owner") && entity.getAttribute("owner") != null) {
                    table.setOwner(entity.getAttribute("owner").toString());
                } else {
                    table.setOwner("");
                }
                //创建时间
                if(entity.hasAttribute("createTime") && entity.getAttribute("createTime") != null) {
                    table.setCreateTime(entity.getAttribute("createTime").toString());
                }
                //描述
                if(entity.hasAttribute("description") && entity.getAttribute("description") != null) {
                    table.setDescription(entity.getAttribute("description").toString());
                }

                if(entity.hasAttribute("sd") && entity.getAttribute("sd") != null) {
                    Object obj = entity.getAttribute("sd");
                    if(obj instanceof AtlasObjectId) {
                        AtlasObjectId atlasObject = (AtlasObjectId)obj;
                        String sdGuid = atlasObject.getGuid();
                        AtlasEntity sdEntity = entitiesStore.getById(sdGuid).getEntity();
                        //位置
                        if(sdEntity.hasAttribute("location") && sdEntity.getAttribute("location") != null) {
                            table.setLocation(sdEntity.getAttribute("location").toString());
                        }
                        //格式
                        if(sdEntity.hasAttribute("inputFormat") && sdEntity.getAttribute("inputFormat") != null) {
                            String inputFormat = sdEntity.getAttribute("inputFormat").toString();
                            if(inputFormat.contains("TextInputFormat")) {
                                table.setFormat("TextFile");
                            } else if(inputFormat.contains("SequenceFileInputFormat")) {
                                table.setFormat("SequenceFile");
                            } else if(inputFormat.contains("RCFileInputFormat")) {
                                table.setFormat("RCFile");
                            } else if(inputFormat.contains("OrcInputFormat")) {
                                table.setFormat("ORCFile");
                            }
                        }
                    }
                }
                //类型
                if(entity.hasAttribute("tableType") && entity.getAttribute("tableType") != null) {
                    if(entity.getAttribute("tableType").toString().contains("EXTERNAL")) {
                        table.setType("EXTERNAL_TABLE");
                    } else {
                        table.setType("INTERNAL_TABLE");
                    }
                }
                //是否为分区表
                if(entity.hasAttribute("partitionKeys") && entity.getAttribute("partitionKeys") != null) {
                    table.setPartitionTable(true);
                } else {
                    table.setPartitionTable(false);
                }
                //数据库名
                if(entity.hasRelationshipAttribute("db") && entity.getRelationshipAttribute("db") != null) {
                    Object obj = entity.getRelationshipAttribute("db");
                    if(obj instanceof AtlasRelatedObjectId) {
                        AtlasRelatedObjectId relatedObject = (AtlasRelatedObjectId)obj;
                        table.setDatabaseId(relatedObject.getGuid());
                        table.setDatabaseName(relatedObject.getDisplayText());
                    }
                }

                //所属业务
                table.setBusiness("");
                //表关联信息
                List<String> relations = new ArrayList<>();
                table.setRelations(relations);
                //类别
                table.setCategory("");
                //表生命周期
                table.setTableLife("");
                //分区生命周期
                table.setPartitionLife("");
                //分类信息
                table.setTopic("");
                //权限
                TablePermission permission = new TablePermission();

                table.setTablePermission(permission);
            }
            List<Column> columns = new ArrayList<>();
            table.setColumns(columns);
            return table;
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
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, direction, depth);
            LineageInfo info = new LineageInfo();
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
            String lineageGuid = lineageInfo.getBaseEntityGuid();
            Set<AtlasLineageInfo.LineageRelation> relations = lineageInfo.getRelations();
            //guid
            info.setGuid(lineageGuid);
            //depth
            info.setLineageDepth(depth);
            //relations
            Iterator<AtlasLineageInfo.LineageRelation> it = relations.iterator();
            Set<LineageInfo.LineageRelation> lineageRelations = new HashSet<>();
            LineageInfo.LineageRelation  relation = null;
            while(it.hasNext()) {
                AtlasLineageInfo.LineageRelation atlasRelation = it.next();
                relation = new LineageInfo.LineageRelation();
                relation.setFromEntityId(atlasRelation.getFromEntityId());
                relation.setToEntityId(atlasRelation.getToEntityId());
                relation.setRelationshipId(atlasRelation.getRelationshipId());
                lineageRelations.add(relation);
            }
            //entities
            List<LineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
            LineageInfo.LineageEntity lineageEntity = null;
            for(String key: entities.keySet()) {
                lineageEntity = new LineageInfo.LineageEntity();
                AtlasEntityHeader atlasEntity = entities.get(key);
                getEntityInfo(key, lineageEntity, entities, atlasEntity);
                lineageEntity.setDirectUpStreamNum(0);
                lineageEntity.setDirectDownStreamNum(0);
                lineageEntity.setUpStreamLevelNum(0);
                lineageEntity.setDownStreamLevelNum(0);
                lineageEntities.add(lineageEntity);
            }
            info.setEntities(lineageEntities);
            info.setRelations(lineageRelations);
            System.out.println();

            return info;
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
        LineageInfo.LineageEntity lineageEntity = new LineageInfo.LineageEntity();

        AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, 1);
        Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
        AtlasEntityHeader atlasEntity = entities.get(guid);
        getEntityInfo(guid, lineageEntity, entities, atlasEntity);
        if(atlasEntity.getTypeName().contains("table")) {
            AtlasLineageInfo fullLineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, -1);
            Set<AtlasLineageInfo.LineageRelation> fullRelations = fullLineageInfo.getRelations();
            //直接上游表数量
            long directUpStreamNum = getInDirectRelationNode(guid, fullRelations).size();
            lineageEntity.setDirectUpStreamNum(directUpStreamNum);
            //直接下游表数量
            long directDownStreamNum = getOutDirectRelationNode(lineageEntity.getGuid(), fullRelations).size();
            lineageEntity.setDirectDownStreamNum(directDownStreamNum);
            //上游表层数
            long upStreamLevelNum = getMaxDepth("in", lineageEntity.getGuid(), fullRelations);
            lineageEntity.setUpStreamLevelNum((upStreamLevelNum - 1) / 2);
            //下游表层数
            long downStreamLevelNum = getMaxDepth("out", lineageEntity.getGuid(), fullRelations);
            lineageEntity.setDownStreamLevelNum((downStreamLevelNum - 1) / 2);
        }
        return lineageEntity;
    }

    public LineageInfo.LineageEntity getEntityInfo(String guid, LineageInfo.LineageEntity lineageEntity, Map<String, AtlasEntityHeader> entities, AtlasEntityHeader atlasEntity) throws AtlasBaseException{
        //guid
        if(atlasEntity.getGuid() != null)
            lineageEntity.setGuid(atlasEntity.getGuid());
        //typeName
        if(atlasEntity.getTypeName() != null)
            lineageEntity.setTypeName(atlasEntity.getTypeName());
        //tableName
        if(atlasEntity.hasAttribute("name") && atlasEntity.getAttribute("name") != null)
            lineageEntity.setTableName(atlasEntity.getAttribute("name").toString());
        //displayName
        if(atlasEntity.getDisplayText() != null) {
            lineageEntity.setDisplayText(atlasEntity.getDisplayText());
        }
        AtlasEntity atlasTableEntity = entitiesStore.getById(guid).getEntity();
        //updateTime
        lineageEntity.setTableUpdateTime(atlasTableEntity.getUpdateTime().toString());
        //dbName
        if(atlasTableEntity.hasRelationshipAttribute("db") && atlasTableEntity.getRelationshipAttribute("db") != null) {
            Object obj = atlasTableEntity.getRelationshipAttribute("db");
            if(obj instanceof AtlasRelatedObjectId) {
                AtlasRelatedObjectId relatedObject = (AtlasRelatedObjectId)obj;
                lineageEntity.setDbName(relatedObject.getDisplayText());
            }
        }
        return lineageEntity;
    }

    public Set<AtlasLineageInfo.LineageRelation> getOutDirectRelationNode(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = new HashSet<>();
        for(Iterator it = relations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            if(relation.getFromEntityId().equals(guid)) {
                directRelations.add(relation);
            }
        }
        return directRelations;
    }

    public Set<AtlasLineageInfo.LineageRelation> getInDirectRelationNode(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = new HashSet<>();
        for(Iterator it = relations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            if(relation.getToEntityId().equals(guid)) {
                directRelations.add(relation);
            }
        }
        return directRelations;
    }

    public Long getOutMaxDepth(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = getOutDirectRelationNode(guid, relations);
        long max = 0;
        for(Iterator it = directRelations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            max = Math.max(max, getOutMaxDepth(relation.getToEntityId(), relations));
        }
        return max + 1;
    }

    public Long getInMaxDepth(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = getInDirectRelationNode(guid, relations);
        long max = 0;
        for(Iterator it = directRelations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            max = Math.max(max, getInMaxDepth(relation.getFromEntityId(), relations));
        }
        return max + 1;
    }

    public Long getMaxDepth(String direction, String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        long max = 0;
        if(direction.equals("out"))
            max = getOutMaxDepth(guid, relations);
        else if(direction.equals("in"))
            max = getInMaxDepth(guid, relations);
        return max;
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

            List<AtlasGlossary> glossaries = glossaryService.getGlossaries(-1, 0, SortOrder.ASCENDING);
            AtlasGlossary baseGlosary = null;
            //如果Glossary为空，此时没有数据，则需要创建根Glossary
            if(glossaries == null || glossaries.size() ==0) {
                baseGlosary = new AtlasGlossary();
                baseGlosary.setName("BaseGlosary");
                baseGlosary.setQualifiedName("BaseGlosary");
                baseGlosary = glossaryService.createGlossary(baseGlosary);
            } else {
                baseGlosary = glossaries.get(0);
            }
            AtlasGlossaryHeader baseGlossaryHeader = new AtlasGlossaryHeader();
            baseGlossaryHeader.setGlossaryGuid(baseGlosary.getGuid());
            baseGlossaryHeader.setDisplayText(baseGlosary.getName());

            AtlasGlossaryCategory tmpCategory = new AtlasGlossaryCategory();
            tmpCategory.setAnchor(baseGlossaryHeader);
            tmpCategory.setName(category.getName());
            tmpCategory.setShortDescription(category.getDescription());
            tmpCategory.setLongDescription(category.getDescription());
            tmpCategory = glossaryService.createCategory(tmpCategory);
            category.setQualifiedName(tmpCategory.getQualifiedName());
            category.setGuid(tmpCategory.getGuid());
            category.setAnchor(tmpCategory.getAnchor());
            return category;
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
        String guid = category.getGuid();
        AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(guid);
        String historyName = glossaryCategory.getName();
        glossaryCategory.setName(category.getName());
        glossaryCategory.setLongDescription(category.getDescription());
        glossaryCategory.setShortDescription(category.getDescription());
        glossaryCategory = glossaryService.updateCategory(glossaryCategory);

        if(glossaryCategory.getAnchor() != null)
            category.setAnchor(glossaryCategory.getAnchor());
        if(glossaryCategory.getParentCategory() != null)
            category.setParentCategory(glossaryCategory.getParentCategory());
        if(glossaryCategory.getChildrenCategories() != null)
            category.setChildrenCategories(glossaryCategory.getChildrenCategories());
        String qualfiiedName = glossaryCategory.getQualifiedName().replaceFirst(historyName, category.getName());
        glossaryCategory.setQualifiedName(qualfiiedName);
        category.setQualifiedName(qualfiiedName);
        category.setQualifiedName(glossaryCategory.getQualifiedName());
        return category;
    }

    /**
     * 获取全部目录层级
     *
     * @param limit
     * @param offset
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryHeader> getCategories(@DefaultValue("-1") @QueryParam("limit") final String limit,
                                             @DefaultValue("0") @QueryParam("offset") final String offset,
                                             @DefaultValue("ASC") @QueryParam("sort") final String sort) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getCategories()");
            }
            Set<CategoryHeader> categoryHeaders = new HashSet<CategoryHeader>();
            List<AtlasGlossary> glossaries = glossaryService.getGlossaries(Integer.parseInt(limit), Integer.parseInt(offset), SortOrder.ASCENDING);
            if(glossaries!=null && glossaries.size()!=0) {
                AtlasGlossary baseGlosary = glossaries.get(0);
                Set<AtlasRelatedCategoryHeader> categories = baseGlosary.getCategories();
                Iterator<AtlasRelatedCategoryHeader> iterator = categories.iterator();
                while(iterator.hasNext()) {
                    AtlasRelatedCategoryHeader header = iterator.next();
                    CategoryHeader categoryHeader = new CategoryHeader();
                    categoryHeader.setCategoryGuid(header.getCategoryGuid());
                    categoryHeader.setName(header.getDisplayText());
                    categoryHeader.setRelationGuid(header.getRelationGuid());
                    if(header.getParentCategoryGuid() != null)
                        categoryHeader.setParentCategoryGuid(header.getParentCategoryGuid());
                    AtlasGlossaryCategory category = glossaryService.getCategory(categoryHeader.getCategoryGuid());
                    if(category.getLongDescription() != null)
                        categoryHeader.setDescription(category.getLongDescription());
                    categoryHeaders.add(categoryHeader);
                }
            }
            return categoryHeaders;
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
            glossaryService.deleteCategory(categoryGuid);
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
    public void assignTermToEntities(@PathParam("categoryGuid") String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        //根据categoryGuid获取GlossaryGuid
        AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
        AtlasGlossaryHeader glossaryHeader = glossaryCategory.getAnchor();
        /*String glossaryGuid = glossaryCategory.getAnchor().getGlossaryGuid();
        String glossaryShowText = glossaryCategory.getAnchor().getDisplayText();*/
        String categoryName = glossaryCategory.getName();

        AtlasGlossary glossary = glossaryService.getGlossary(glossaryHeader.getGlossaryGuid());
        glossaryHeader.setDisplayText(glossary.getName());


        for(int i=0; i<relatedObjectIds.size(); i++) {
            AtlasRelatedObjectId relatedObjectId = relatedObjectIds.get(i);
            AtlasGlossaryTerm glossaryTerm = new AtlasGlossaryTerm();
            glossaryTerm.setName(categoryName);

        }

        //创建Term
        /*AtlasGlossaryHeader glossaryHeader = new AtlasGlossaryHeader();
        glossaryHeader.setDisplayText(glossaryShowText);
        glossaryHeader.setGlossaryGuid(glossaryGuid);*/
        AtlasGlossaryTerm glossaryTerm = new AtlasGlossaryTerm();

        glossaryTerm.setAnchor(glossaryHeader);
        glossaryTerm = glossaryService.createTerm(glossaryTerm);
        //创建关联关系
        glossaryService.assignTermToEntities(glossaryTerm.getGuid(), relatedObjectIds);
    }

    @GET
    @Path("/category/relations/{categoryGuid}")
    public RelationEntity getGlossaryTerm(@PathParam("categoryGuid") String categoryGuid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        RelationEntity relationEntity = new RelationEntity();
        //获取Category信息
        AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
        relationEntity.setCategoryGuid(glossaryCategory.getGuid());
        relationEntity.setCategoryName(glossaryCategory.getName());
        //获取当前Category的子Category
        Set<AtlasRelatedCategoryHeader> childrenCategories =glossaryCategory.getChildrenCategories();
        Set<RelationEntity.ChildCatetory> childs = new HashSet<>();
        if(childrenCategories != null) {
            Iterator<AtlasRelatedCategoryHeader> it = childrenCategories.iterator();
            while(it.hasNext()) {
                AtlasRelatedCategoryHeader atlasRelatedCategory = it.next();
                RelationEntity.ChildCatetory childCatetory = new RelationEntity.ChildCatetory();
                childCatetory.setGuid(atlasRelatedCategory.getCategoryGuid());
                childCatetory.setName(atlasRelatedCategory.getDisplayText());
                childs.add(childCatetory);
            }
        }

        //添加子目录信息
        relationEntity.setChildCategory(childs);
        //获取与Category关联Term
        Set<AtlasRelatedTermHeader> relatedTerms = glossaryCategory.getTerms();
        //迭代获取父级目录信息
        AtlasRelatedCategoryHeader parent = glossaryCategory.getParentCategory();
        List<String> pathList = new ArrayList<>();
        pathList.add(glossaryCategory.getName());
        while(parent != null) {
            AtlasGlossaryCategory parentCategory = glossaryService.getCategory(parent.getCategoryGuid());
            parent = parentCategory.getParentCategory();
            pathList.add(parentCategory.getName());
        }
        //获取Glossary目录信息
        String parentGlossaryGuid = glossaryCategory.getAnchor().getGlossaryGuid();
        AtlasGlossary parentGlossary = glossaryService.getGlossary(parentGlossaryGuid);
        String parentGlossaryName = parentGlossary.getName();
        pathList.add(parentGlossaryName);
        //拼接路径
        String pathStr = "";
        pathStr += rootDir + "/";
        for(int i=pathList.size()-1; i>=0; i--) {
            pathStr += pathList.get(i) + "/";
        }
        Iterator<AtlasRelatedTermHeader> iterator = relatedTerms.iterator();
        if(iterator.hasNext()) {
            AtlasRelatedTermHeader term = iterator.next();
            String termGuid = term.getTermGuid();
            AtlasGlossaryTerm ret = glossaryService.getTerm(termGuid);
            //获取Term下的关联
            Set<AtlasRelatedObjectId> relatedObjectIds = ret.getAssignedEntities();
            Iterator<AtlasRelatedObjectId> relatedIterator = relatedObjectIds.iterator();
            Set<RelationEntity.RelationInfo> relationInfos = new HashSet<>();
            while(relatedIterator.hasNext()) {
                AtlasRelatedObjectId relatedObject = relatedIterator.next();
                RelationEntity.RelationInfo relationInfo = new RelationEntity.RelationInfo();
                String relatedObjectGuid = relatedObject.getGuid();
                Table table = getTableInfoById(relatedObjectGuid, false);
                String tableName = table.getTableName();
                String dbName = table.getDatabaseName();
                relationInfo.setGuid(relatedObjectGuid);
                relationInfo.setTableName(tableName);
                relationInfo.setDbName(dbName);
                relationInfo.setPath(pathStr + tableName);
                relationInfo.setRealationGuid(relatedObject.getRelationshipGuid());
                relationInfos.add(relationInfo);
            }

            relationEntity.setRelations(relationInfos);
        }
        return relationEntity;



        /*try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.getGlossaryTerm(" + categoryGuid + ")");
            }
            AtlasGlossaryTerm ret = glossaryService.getTerm(categoryGuid);
            if (ret == null) {
                throw new AtlasBaseException(AtlasErrorCode.INSTANCE_GUID_NOT_FOUND);
            }

            return ret;
        } finally {
            AtlasPerfTracer.log(perf);
        }*/
    }


    @DELETE
    @Path("/category/relations/{categoryGuid}/assignedEntities")
    public void removeRelationAssignmentFromEntities(@PathParam("categoryGuid") String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);

        AtlasPerfTracer perf = null;
        try {
            //获取Category信息
            AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);

            for(int i=0; i<relatedObjectIds.size(); i++) {
                String tableGuid = relatedObjectIds.get(i).getGuid();
                AtlasEntity.AtlasEntityWithExtInfo tableInfo = entitiesStore.getById(tableGuid);
                AtlasEntity entity = tableInfo.getEntity(tableGuid);
                relatedObjectIds = (List) entity.getRelationshipAttribute("meanings");
            }

            Set<AtlasRelatedTermHeader> relatedTerms = glossaryCategory.getTerms();
            Iterator<AtlasRelatedTermHeader> iterator = relatedTerms.iterator();
            if(iterator.hasNext()) {
                String termGuid = iterator.next().getTermGuid();
                glossaryService.removeTermFromEntities(termGuid, relatedObjectIds);
            }
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
    public TableShow getAllDatabase(GuidCount guidCount) throws AtlasBaseException, SQLException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "EntityREST.getById(" + guidCount.getGuid() + ", " + guidCount.getCount() + " )");
            }
            TableShow tableShow = new TableShow();
            //获取entity
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guidCount.getGuid());
            AtlasEntity entity = info.getEntity();
            String name = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
            if (name == "") {
                System.out.println("该id不存在");
            }
            String sql = "select * from " + name + " limit " + guidCount.getCount();
            ResultSet resultSet = HiveJdbcUtils.selectBySQL(sql);
            List<String> columns = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<Map<String, String>> resultList = new ArrayList<>();
            for (int i = 0; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                columns.add(columnName);
                Map<String, String> map = new HashMap<>();
                resultSet.next();
                String s = resultSet.getObject(columnName).toString();
                map.put(columnName, s);
                resultList.add(map);
            }
            tableShow.setTableId(guidCount.getGuid());
            tableShow.setColumnNames(columns);
            tableShow.setLines(resultList);
            return tableShow;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
}
