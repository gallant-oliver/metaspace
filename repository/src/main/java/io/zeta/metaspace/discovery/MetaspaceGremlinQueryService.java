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
/**
 * @author sunhaoning@gridsum.com
 * @date 2018/12/4 19:35
 */
package io.zeta.metaspace.discovery;

import static org.apache.atlas.repository.Constants.RELATIONSHIP_GUID_PROPERTY_KEY;
import static org.apache.atlas.repository.graph.GraphHelper.getGuid;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.GraphTransaction;
import org.apache.atlas.authorize.AtlasAuthorizationUtils;
import org.apache.atlas.authorize.AtlasEntityAccessRequest;
import org.apache.atlas.authorize.AtlasPrivilege;
import org.apache.atlas.discovery.EntityLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.repository.graphdb.AtlasEdge;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.v2.AtlasGraphUtilsV2;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.MetaspaceGremlinQueryProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/*
 * @description
 * @author sunhaoning
 * @date 2018/12/4 19:35
 */

@Service
public class MetaspaceGremlinQueryService implements MetaspaceGremlinService {

    private static final Logger LOG = LoggerFactory.getLogger(EntityLineageService.class);

    private static final String PROCESS_INPUTS_EDGE = "__Process.inputs";
    private static final String PROCESS_OUTPUTS_EDGE = "__Process.outputs";

    private final AtlasGraph graph;
    private final MetaspaceGremlinQueryProvider gremlinQueryProvider;
    private final EntityGraphRetriever entityRetriever;
    private final AtlasTypeRegistry atlasTypeRegistry;

    @Inject
    MetaspaceGremlinQueryService(AtlasTypeRegistry typeRegistry, AtlasGraph atlasGraph) {
        this.graph = atlasGraph;
        this.gremlinQueryProvider = MetaspaceGremlinQueryProvider.INSTANCE;
        this.entityRetriever = new EntityGraphRetriever(typeRegistry);
        this.atlasTypeRegistry = typeRegistry;
    }


    @Override
    @GraphTransaction
    public AtlasLineageInfo getColumnLineageInfo(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        AtlasLineageInfo lineageInfo;

        AtlasEntityHeader entity = entityRetriever.toAtlasEntityHeaderWithClassifications(guid);

        AtlasAuthorizationUtils.verifyAccess(new AtlasEntityAccessRequest(atlasTypeRegistry, AtlasPrivilege.ENTITY_READ, entity), "read entity lineage: guid=", guid);

        AtlasEntityType entityType = atlasTypeRegistry.getEntityTypeByName(entity.getTypeName());

        if (entityType == null || !entityType.getTypeAndAllSuperTypes().contains(AtlasClient.DATA_SET_SUPER_TYPE)) {
            throw new AtlasBaseException(AtlasErrorCode.INSTANCE_GUID_NOT_DATASET, guid);
        }

        if (direction != null) {
            if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
                lineageInfo = getLineageInfo(guid, AtlasLineageInfo.LineageDirection.INPUT, depth);
            } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
                lineageInfo = getLineageInfo(guid, AtlasLineageInfo.LineageDirection.OUTPUT, depth);
            } else if (direction.equals(AtlasLineageInfo.LineageDirection.BOTH)) {
                lineageInfo = getBothColumnLineageInfo(guid, depth);
            } else {
                throw new AtlasBaseException(AtlasErrorCode.INSTANCE_LINEAGE_INVALID_PARAMS, "direction", direction.toString());
            }
        } else {
            throw new AtlasBaseException(AtlasErrorCode.INSTANCE_LINEAGE_INVALID_PARAMS, "direction", null);
        }

        return lineageInfo;
    }

    @Override
    @GraphTransaction
    public List<String> getColumnRelatedTable(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        List<String> columnRelatedTable = null;
        if (direction != null) {
            if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
                columnRelatedTable = getColumnRelatedTableInfo(guid, AtlasLineageInfo.LineageDirection.INPUT, depth);
            } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
                columnRelatedTable = getColumnRelatedTableInfo(guid, AtlasLineageInfo.LineageDirection.OUTPUT, depth);
            } else if (direction.equals(AtlasLineageInfo.LineageDirection.BOTH)) {
                columnRelatedTable = getBothColumnRelatedTable(guid, depth);
            } else {
                throw new AtlasBaseException(AtlasErrorCode.INSTANCE_LINEAGE_INVALID_PARAMS, "direction", direction.toString());
            }
        } else {
            throw new AtlasBaseException(AtlasErrorCode.INSTANCE_LINEAGE_INVALID_PARAMS, "direction", null);
        }
        return columnRelatedTable;
    }

    private List<String> getBothColumnRelatedTable(String guid, int depth) throws AtlasBaseException {
        List<String> inputRelatedTable = new ArrayList<>(getColumnRelatedTableInfo(guid, AtlasLineageInfo.LineageDirection.INPUT, depth));
        List<String> outputRelatedTable = new ArrayList<>(getColumnRelatedTableInfo(guid, AtlasLineageInfo.LineageDirection.OUTPUT, depth));

        inputRelatedTable.addAll(outputRelatedTable);
        return inputRelatedTable;
    }

    public List<String> getColumnRelatedTableInfo(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        String lineageQuery = getColumnRelatedTableQuery(guid, direction, depth);

        List vertexSet = (List) graph.executeGremlinScript(lineageQuery, false);
        return vertexSet;
    }

    public String getColumnRelatedTableQuery(String entityGuid, AtlasLineageInfo.LineageDirection direction, int depth) {
        String columnRelatedTableQuery = null;
        if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
            columnRelatedTableQuery = generateColumnRelatedTableQuery(entityGuid, depth, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);

        } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
            columnRelatedTableQuery = generateColumnRelatedTableQuery(entityGuid, depth, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
        }
        return columnRelatedTableQuery;
    }

    private String generateColumnRelatedTableQuery(String entityGuid, int depth, String incomingFrom, String outgoingTo) {
        String columnRelatedTableQuery;
        if (depth < 1) {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_COLUMN_RELATED_TABLE);
            columnRelatedTableQuery = String.format(query, entityGuid, incomingFrom, outgoingTo);
        } else {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.PARTIAL_COLUMN_RELATED_TABLE);
            columnRelatedTableQuery = String.format(query, entityGuid, incomingFrom, outgoingTo, depth);
        }
        return columnRelatedTableQuery;
    }

    public AtlasLineageInfo getLineageInfo(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        String lineageQuery = getColumnLineageQuery(guid, direction, depth);

        List edgeMapList = (List) graph.executeGremlinScript(lineageQuery, false);

        return formattedGraphData(edgeMapList, guid, direction, depth);
    }

    public AtlasLineageInfo formattedGraphData(List edgeMapList, String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        Map<String, AtlasEntityHeader> entities = new HashMap<>();
        Set<AtlasLineageInfo.LineageRelation> relations = new HashSet<>();
        if (CollectionUtils.isNotEmpty(edgeMapList)) {
            for (Object edgeMap : edgeMapList) {
                if (edgeMap instanceof Map) {
                    for (final Object o : ((Map) edgeMap).entrySet()) {
                        final Map.Entry entry = (Map.Entry) o;
                        Object value = entry.getValue();

                        if (value instanceof List) {
                            for (Object elem : (List) value) {
                                if (elem instanceof AtlasEdge) {
                                    processEdge((AtlasEdge) elem, entities, relations);
                                } else {
                                    LOG.warn("Invalid value of type {} found, ignoring", (elem != null ? elem.getClass().getSimpleName() : "null"));
                                }
                            }
                        } else if (value instanceof AtlasEdge) {
                            processEdge((AtlasEdge) value, entities, relations);
                        } else {
                            LOG.warn("Invalid value of type {} found, ignoring", (value != null ? value.getClass().getSimpleName() : "null"));
                        }
                    }
                }
            }
        }
        return new AtlasLineageInfo(guid, entities, relations, direction, depth);
    }

    private void processEdge(final AtlasEdge edge, final Map<String, AtlasEntityHeader> entities, final Set<AtlasLineageInfo.LineageRelation> relations) throws AtlasBaseException {
        AtlasVertex inVertex = edge.getInVertex();
        AtlasVertex outVertex = edge.getOutVertex();
        String inGuid = AtlasGraphUtilsV2.getIdFromVertex(inVertex);
        String outGuid = AtlasGraphUtilsV2.getIdFromVertex(outVertex);
        String relationGuid = AtlasGraphUtilsV2.getEncodedProperty(edge, RELATIONSHIP_GUID_PROPERTY_KEY, String.class);
        boolean isInputEdge = edge.getLabel().equalsIgnoreCase(PROCESS_INPUTS_EDGE);

        if (!entities.containsKey(inGuid)) {
            AtlasEntityHeader entityHeader = entityRetriever.toAtlasEntityHeader(inVertex);
            entities.put(inGuid, entityHeader);
        }

        if (!entities.containsKey(outGuid)) {
            AtlasEntityHeader entityHeader = entityRetriever.toAtlasEntityHeader(outVertex);
            entities.put(outGuid, entityHeader);
        }

        if (isInputEdge) {
            relations.add(new AtlasLineageInfo.LineageRelation(inGuid, outGuid, relationGuid));
        } else {
            relations.add(new AtlasLineageInfo.LineageRelation(outGuid, inGuid, relationGuid));
        }
    }

    private AtlasLineageInfo getBothColumnLineageInfo(String guid, int depth) throws AtlasBaseException {
        AtlasLineageInfo inputLineage = getColumnLineageInfo(guid, AtlasLineageInfo.LineageDirection.INPUT, depth);
        AtlasLineageInfo outputLineage = getColumnLineageInfo(guid, AtlasLineageInfo.LineageDirection.OUTPUT, depth);
        AtlasLineageInfo ret = inputLineage;

        ret.getRelations().addAll(outputLineage.getRelations());
        ret.getGuidEntityMap().putAll(outputLineage.getGuidEntityMap());
        ret.setLineageDirection(AtlasLineageInfo.LineageDirection.BOTH);

        return ret;
    }

    private String getColumnLineageQuery(String entityGuid, AtlasLineageInfo.LineageDirection direction, int depth) {
        String lineageQuery = null;
        if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
            lineageQuery = generateColumnLineageQuery(entityGuid, depth, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);
        } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
            lineageQuery = generateColumnLineageQuery(entityGuid, depth, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
        }
        return lineageQuery;
    }

    private String generateColumnLineageQuery(String entityGuid, int depth, String incomingFrom, String outgoingTo) {
        String lineageQuery;
        if (depth < 1) {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_COLUMN_LINEAGE);
            lineageQuery = String.format(query, entityGuid, incomingFrom, outgoingTo);
        } else {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.PARTIAL_COLUMN_LINEAGE);
            lineageQuery = String.format(query, entityGuid, incomingFrom, outgoingTo, depth);
        }
        return lineageQuery;
    }

    private String generateLineageQuery(String entityGuid, int depth, String incomingFrom, String outgoingTo) {
        String lineageQuery;
        if (depth < 1) {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_LINEAGE);
            lineageQuery = String.format(query, entityGuid, incomingFrom, outgoingTo);
        } else {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.PARTIAL_LINEAGE);
            lineageQuery = String.format(query, entityGuid, incomingFrom, outgoingTo, depth);
        }
        return lineageQuery;
    }

    @Override
    @GraphTransaction
    public Integer getLineageDepth(String guid, AtlasLineageInfo.LineageDirection direction) throws AtlasBaseException {
        String lineageQuery = getLineageDepthQuery(guid, direction);
        List depthList = (List) graph.executeGremlinScript(lineageQuery, false);
        if (Objects.nonNull(depthList) && depthList.size() > 0)
            return Integer.parseInt(depthList.get(0).toString());
        else
            return 0;
    }

    private String getLineageDepthQuery(String entityGuid, AtlasLineageInfo.LineageDirection direction) {
        String lineageQuery = null;

        if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.LINEAGE_DEPTH);
            lineageQuery = String.format(query, entityGuid, PROCESS_OUTPUTS_EDGE, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);

        } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.LINEAGE_DEPTH);
            lineageQuery = String.format(query, entityGuid, PROCESS_INPUTS_EDGE, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
        }
        return lineageQuery;
    }

    @Override
    @GraphTransaction
    public Integer getEntityDirectNum(String guid, AtlasLineageInfo.LineageDirection direction) throws AtlasBaseException {
        String lineageQuery = getEntityDirectNumQuery(guid, direction);
        List depthList = (List) graph.executeGremlinScript(lineageQuery, false);
        if (Objects.nonNull(depthList) && depthList.size() > 0)
            return Integer.parseInt(depthList.get(0).toString());
        else
            return 0;
    }

    private String getEntityDirectNumQuery(String entityGuid, AtlasLineageInfo.LineageDirection direction) {
        String lineageQuery = null;
        String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DIRECT_ENTITY_NUM);
        if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {

            lineageQuery = String.format(query, entityGuid, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);

        } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
            lineageQuery = String.format(query, entityGuid, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
        }
        return lineageQuery;
    }

    @Override
    public PageResult<Database> getAllDBAndTable(String queryDb, int limit, int offset) throws AtlasBaseException {
        try {
            MetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQeury = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_DB_TABLE : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_TABLE_BY_QUERY);
            String query = gremlinQueryProvider.getQuery(gremlinQeury);

            String dbQuery = String.format(query, queryDb, offset, offset + limit);
            graph.commit();
            graph.wait();
            List vertexMap = (List) graph.executeGremlinScript(dbQuery, false);
            Iterator<Map<String, AtlasVertex>> results = vertexMap.iterator();

            PageResult<Database> pageResult = new PageResult<>();
            List<Database> databases = new ArrayList<>();
            List<Table> tables = null;
            Boolean hasRecoredDB = null;
            Database db = null;

            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            attributes.add("comment");
            attributes.add("description");
            while (results.hasNext()) {
                hasRecoredDB = false;
                Map<String, AtlasVertex> map = results.next();
                AtlasVertex dbVertex = map.get("db");
                AtlasVertex tableVertex = map.get("table");

                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dbVertex, attributes, null, true);
                AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
                String dbGuid = getGuid(dbVertex);

                Table table = new Table();
                if (Objects.nonNull(tableVertex)) {
                    AtlasEntity.AtlasEntityWithExtInfo tableEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(tableVertex, attributes, null, true);
                    AtlasEntity tableEntity = tableEntityWithExtInfo.getEntity();
                    String tableGuid = getGuid(tableVertex);
                    String tableName = tableEntity.getAttribute("name").toString();
                    String tableStatus = tableEntity.getStatus().name();
                    String tableDescription = tableEntity.getAttribute("comment") == null ? "-" : tableEntity.getAttribute("comment").toString();
                    table.setTableId(tableGuid);
                    table.setTableName(tableName);
                    table.setStatus(tableStatus);
                    table.setDescription(tableDescription);

                    for (Database database : databases) {
                        String dbName = database.getDatabaseName();
                        if (dbGuid.equals(database.getDatabaseId())) {
                            hasRecoredDB = true;
                            table.setDatabaseId(dbGuid);
                            table.setDatabaseName(dbName);
                            tables = database.getTableList();
                            tables.add(table);
                            break;
                        }
                    }
                }
                //没有记录当前DB信息
                if (!hasRecoredDB) {
                    db = new Database();
                    String dbName = dbEntity.getAttribute("name").toString();
                    String dbStatus = dbEntity.getStatus().name();
                    String dbDescription = dbEntity.getAttribute("description") == null ? "-" : dbEntity.getAttribute("description").toString();
                    db.setDatabaseId(dbGuid);
                    db.setDatabaseName(dbName);
                    db.setStatus(dbStatus);
                    db.setDatabaseDescription(dbDescription);
                    tables = new ArrayList<>();
                    if (Objects.nonNull(tableVertex)) {
                        table.setDatabaseId(dbGuid);
                        table.setDatabaseName(dbName);
                        tables.add(table);
                    }
                    db.setTableList(tables);
                    databases.add(db);
                }
            }
            pageResult.setLists(databases);
            pageResult.setOffset(offset);
            pageResult.setCount(databases.size());
            String gremlinQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_TOTAL_NUM_BY_QUERY);
            String numQuery = String.format(gremlinQuery, queryDb);
            List num = (List) graph.executeGremlinScript(numQuery, false);
            pageResult.setSum(Integer.parseInt(num.get(0).toString()));
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误");
        }

    }

    @Override
    public String getGuidByDBAndTableName(String dbName, String tableName) throws AtlasBaseException, InterruptedException {
        int[] sleepSeconds = new int[]{4,3,2};
        int tryCount = 3;
        String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_GUID_QUERY);
        String guidQuery = String.format(query, dbName, tableName);
        String guid = null;
        while(Objects.isNull(guid) && tryCount-- > 0) {
            TimeUnit.SECONDS.sleep(sleepSeconds[tryCount]);
            graph.commit();
            List guidList = (List) graph.executeGremlinScript(guidQuery, false);
            if (Objects.nonNull(guidList) && guidList.size() > 0) {
                guid = guidList.get(0).toString();
            }
        }
        return guid;
    }

    public PageResult<Table> getTableNameAndDbNameByQuery(String queryTable, int offset, int limit) throws AtlasBaseException {
        PageResult<Table> tablePageResult = new PageResult<>();
        ArrayList<Table> tables = new ArrayList<>();
        MetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_TABLE_DB: MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_DB_BY_QUERY);
        String query = gremlinQueryProvider.getQuery(gremlinQuery);
        String tableQuery = String.format(query, queryTable, offset, offset + limit);
        List<Map<String, AtlasVertex>> tableDBs = (List) graph.executeGremlinScript(tableQuery, false);
        for (Map<String, AtlasVertex> tableDB : tableDBs) {
            AtlasVertex tableVertex = tableDB.get("table");
            AtlasVertex dbVertex = tableDB.get("db");
            Table table = getTableByVertex(tableVertex, dbVertex);
            tables.add(table);
        }
        tablePageResult.setLists(tables);
        tablePageResult.setCount(tables.size());
        tablePageResult.setOffset(offset);
        String countQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_COUNT_BY_QUEERY);
        List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, queryTable), false);
        tablePageResult.setSum(counts.get(0));
        return tablePageResult;
    }

    public List<AtlasEntityHeader> getAllTables() throws AtlasBaseException {
        String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ALL_TABLE);
        List<AtlasVertex> vertices = (List<AtlasVertex>) graph.executeGremlinScript(query, false);
        List<AtlasEntityHeader> resultList = new ArrayList<>(vertices.size());
        for (AtlasVertex vertex : vertices) {
            resultList.add(entityRetriever.toAtlasEntityHeader(vertex));
        }
        return resultList;
    }

    private Table getTableByVertex(AtlasVertex tableVertex, AtlasVertex dbVertex) throws AtlasBaseException {
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("comment");
        attributes.add("description");
        Table table = new Table();
        if (Objects.nonNull(tableVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo tableEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(tableVertex, attributes, null, true);
            AtlasEntity tableEntity = tableEntityWithExtInfo.getEntity();
            table.setTableId(tableEntity.getGuid());
            table.setTableName(tableEntity.getAttribute("name").toString());
            table.setStatus(tableEntity.getStatus().name());
            table.setDescription(tableEntity.getAttribute("comment") == null ? "null" : tableEntity.getAttribute("comment").toString());
        }
        if (Objects.nonNull(dbVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dbVertex, attributes, null, true);
            AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
            table.setDatabaseName(dbEntity.getAttribute("name").toString());
            table.setDatabaseId(dbEntity.getGuid());
        }
        return table;
    }

    @Override
    public PageResult<Column> getColumnNameAndTableNameAndDbNameByQuery(String queryColumn, int offset, int limit) throws AtlasBaseException {
        PageResult<Column> columnPageResult = new PageResult<>();
        ArrayList<Column> columns = new ArrayList<>();
        MetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_COLUMN_TABLE_DB: MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_TABLE_DB_BY_QUERY);
        String query = gremlinQueryProvider.getQuery(gremlinQuery);
        String columnQuery = String.format(query, queryColumn, offset, offset + limit);
        List<Map<String, AtlasVertex>> columnTableDBs = (List) graph.executeGremlinScript(columnQuery, false);
        for (Map<String, AtlasVertex> columnTableDB : columnTableDBs) {
            AtlasVertex columnVertex = columnTableDB.get("column");
            AtlasVertex tableVertex = columnTableDB.get("table");
            AtlasVertex dbVertex = columnTableDB.get("db");

            Column column = getColumnByVertex(columnVertex,tableVertex, dbVertex);
            columns.add(column);
        }
        columnPageResult.setLists(columns);
        columnPageResult.setCount(columns.size());
        columnPageResult.setOffset(offset);
        String countQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_COUNT_BY_QUERY);
        List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, queryColumn), false);
        columnPageResult.setSum(counts.get(0));
        return columnPageResult;
    }

    private Column getColumnByVertex(AtlasVertex columnVertex, AtlasVertex tableVertex, AtlasVertex dbVertex) throws AtlasBaseException {
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("comment");
        attributes.add("description");
        Column column = new Column();
        if (Objects.nonNull(columnVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo columnEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(columnVertex, attributes, null, true);
            AtlasEntity columnEntity = columnEntityWithExtInfo.getEntity();
            column.setColumnId(columnEntity.getGuid());
            column.setColumnName(columnEntity.getAttribute("name").toString());
            column.setStatus(columnEntity.getStatus().name());
            column.setDescription(columnEntity.getAttribute("comment") == null ? "null" : columnEntity.getAttribute("comment").toString());
            ;
        }
        if (Objects.nonNull(tableVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(tableVertex, attributes, null, true);
            AtlasEntity tableEntity = dbEntityWithExtInfo.getEntity();
            column.setTableName(tableEntity.getAttribute("name").toString());
            column.setTableId(tableEntity.getGuid());
        }
        if (Objects.nonNull(dbVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dbVertex, attributes, null, true);
            AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
            column.setDatabaseName(dbEntity.getAttribute("name").toString());
            column.setDatabaseId(dbEntity.getGuid());
        }
        return column;
    }
}

