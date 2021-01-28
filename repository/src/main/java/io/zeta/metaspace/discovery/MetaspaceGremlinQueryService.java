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
import static org.apache.atlas.repository.graph.GraphHelper.string;


import io.zeta.metaspace.model.metadata.*;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.GraphTransaction;
import org.apache.atlas.authorize.AtlasAuthorizationUtils;
import org.apache.atlas.authorize.AtlasEntityAccessRequest;
import org.apache.atlas.authorize.AtlasPrivilege;
import org.apache.atlas.discovery.EntityLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.repository.graphdb.AtlasEdge;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.v2.AtlasGraphUtilsV2;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.AbstractMetaspaceGremlinQueryProvider;

import java.text.SimpleDateFormat;
import java.util.*;
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
    private final AbstractMetaspaceGremlinQueryProvider gremlinQueryProvider;
    private final EntityGraphRetriever entityRetriever;
    private final AtlasTypeRegistry atlasTypeRegistry;
    private String temporary = "temporary";

    @Inject
    MetaspaceGremlinQueryService(AtlasTypeRegistry typeRegistry, AtlasGraph atlasGraph) {
        this.graph = atlasGraph;
        this.gremlinQueryProvider = AbstractMetaspaceGremlinQueryProvider.INSTANCE;
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
        LOG.info("query:" + lineageQuery);
        if (Objects.nonNull(depthList) && depthList.size() > 0) {
            return Integer.parseInt(depthList.get(0).toString());
        } else
            return 0;
    }

    private String getLineageDepthQuery(String entityGuid, AtlasLineageInfo.LineageDirection direction) {
        String lineageQuery = null;

        if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.LINEAGE_DEPTH_V2);
            lineageQuery = String.format(query, entityGuid, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);

        } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.LINEAGE_DEPTH_V2);
            lineageQuery = String.format(query, entityGuid, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
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
    public PageResult<Database> getAllDBAndTable(String queryDb, int limit, int offset, String dbs) throws AtlasBaseException {
        try {
            AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQeury = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_FULL_DB_TABLE : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_DB_TABLE_BY_QUERY);
            String query = gremlinQueryProvider.getQuery(gremlinQeury);

            String dbQuery = String.format(query, queryDb, dbs, offset, offset + limit);
            List vertexMap = (List) graph.executeGremlinScript(dbQuery, false);
            Iterator<Map<String, AtlasVertex>> results = vertexMap.iterator();

            PageResult<Database> pageResult = new PageResult<>();
            List<Database> databases = toDbAndTable(results);
            pageResult.setLists(databases);
            pageResult.setCurrentSize(databases.size());
            String gremlinQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_DB_TOTAL_NUM_BY_QUERY);
            String numQuery = String.format(gremlinQuery, queryDb, dbs);
            List num = (List) graph.executeGremlinScript(numQuery, false);
            pageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误");
        }

    }

    @Override
    public PageResult<Database> getAllDBAndTable(String queryDb, int limit, int offset) throws AtlasBaseException {
        try {
            AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQeury = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_DB_TABLE : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_TABLE_BY_QUERY);
            String query = gremlinQueryProvider.getQuery(gremlinQeury);

            String dbQuery = String.format(query, queryDb, offset, offset + limit);
            List vertexMap = (List) graph.executeGremlinScript(dbQuery, false);
            Iterator<Map<String, AtlasVertex>> results = vertexMap.iterator();

            PageResult<Database> pageResult = new PageResult<>();
            List<Database> databases = toDbAndTable(results);
            pageResult.setLists(databases);
            pageResult.setCurrentSize(databases.size());
            String gremlinQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_TOTAL_NUM_BY_QUERY);
            String numQuery = String.format(gremlinQuery, queryDb);
            List num = (List) graph.executeGremlinScript(numQuery, false);
            pageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误");
        }

    }

    private List<Database> toDbAndTable(Iterator<Map<String, AtlasVertex>> results) throws AtlasBaseException {
        List<Database> databases = new ArrayList<>();
        List<TableHeader> tables = null;
        Boolean hasRecoredDB = null;
        Database db = null;

        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("comment");
        attributes.add("description");
        attributes.add("createTime");
        while (results.hasNext()) {
            hasRecoredDB = false;
            Map<String, AtlasVertex> map = results.next();
            AtlasVertex dbVertex = map.get("db");
            AtlasVertex tableVertex = map.get("table");

            AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dbVertex, attributes, null, true);
            AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
            String dbGuid = getGuid(dbVertex);

            TableHeader table = new TableHeader();
            if (Objects.nonNull(tableVertex)) {
                AtlasEntity.AtlasEntityWithExtInfo tableEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(tableVertex, attributes, null, true);
                AtlasEntity tableEntity = tableEntityWithExtInfo.getEntity();
                String tableGuid = getGuid(tableVertex);
                String tableName = tableEntity.getAttribute("name").toString();
                table.setTableId(tableGuid);
                table.setTableName(tableName);
                Date createTime = tableEntity.getCreateTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formatDateStr = sdf.format(createTime);
                table.setCreateTime(formatDateStr);
                table.setStatus(tableEntity.getStatus().name());


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
        return databases;
    }

    @Override
    public String getGuidByDBAndTableName(String dbName, String tableName) throws AtlasBaseException, InterruptedException {
        int[] sleepSeconds = new int[]{4, 3, 2};
        int tryCount = 3;
        String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_GUID_QUERY);
        String guidQuery = String.format(query, dbName, tableName);
        String guid = null;
        while (Objects.isNull(guid) && tryCount-- > 0) {
            TimeUnit.SECONDS.sleep(sleepSeconds[tryCount]);
            graph.commit();
            List guidList = (List) graph.executeGremlinScript(guidQuery, false);
            if (Objects.nonNull(guidList) && guidList.size() > 0) {
                guid = guidList.get(0).toString();
            }
        }
        return guid;
    }


    public PageResult<Table> getTableNameAndDbNameByQuery(String queryTable, Boolean active, int offset, int limit, String dbs) throws AtlasBaseException {
        PageResult<Table> tablePageResult = new PageResult<>();
        ArrayList<Table> tables = new ArrayList<>();
        try {
            AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = active ? ((limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_FULL_ACTIVE_TABLE_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_ACTIVE_TABLE_DB_BY_QUERY))
                    : ((limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_FULL_TABLE_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_TABLE_DB_BY_QUERY));
            String query = gremlinQueryProvider.getQuery(gremlinQuery);
            String tableQuery = String.format(query, dbs, queryTable, offset, offset + limit);
            List<Map<String, AtlasVertex>> tableDBs = (List) graph.executeGremlinScript(tableQuery, false);
            for (Map<String, AtlasVertex> tableDB : tableDBs) {
                AtlasVertex tableVertex = tableDB.get("table");
                AtlasVertex dbVertex = tableDB.get("db");
                Table table = getTableByVertex(tableVertex, dbVertex);
                tables.add(table);
            }
            tablePageResult.setLists(tables);
            tablePageResult.setCurrentSize(tables.size());
            String countQuery = gremlinQueryProvider.getQuery(active ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_ACTIVE_TABLE_COUNT_BY_QUEERY : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_TABLE_COUNT_BY_QUEERY);
            List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, dbs, queryTable), false);
            tablePageResult.setTotalSize(counts.get(0));
            return tablePageResult;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public PageResult<Table> getTableNameAndDbNameByQuery(String queryTable, Boolean active, int offset, int limit) throws AtlasBaseException {
        PageResult<Table> tablePageResult = new PageResult<>();
        ArrayList<Table> tables = new ArrayList<>();
        try {
            AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = active ? ((limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_ACTIVE_TABLE_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_TABLE_DB_BY_QUERY))
                    : ((limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_TABLE_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_DB_BY_QUERY));
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
            tablePageResult.setCurrentSize(tables.size());
            String countQuery = gremlinQueryProvider.getQuery(active ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_TABLE_COUNT_BY_QUEERY : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_COUNT_BY_QUEERY);
            List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, queryTable), false);
            tablePageResult.setTotalSize(counts.get(0));
            return tablePageResult;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public PageResult<RDBMSDatabase> getRDBMSDBNameAndSourceNameByQuery(String queryTable, int offset, int limit, String sourceType, Boolean active) throws AtlasBaseException {
        PageResult<RDBMSDatabase> dbPageResult = new PageResult<>();
        ArrayList<RDBMSDatabase> dbs = new ArrayList<>();
        AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = null;
        if (active) {
            gremlinQuery = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_FULL_RDBMS_DB_SOURCE : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_DB_SOURCE_BY_QUERY);
        } else {
            gremlinQuery = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_RDBMS_DB_SOURCE : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_SOURCE_BY_QUERY);
        }
        String query = gremlinQueryProvider.getQuery(gremlinQuery);
        String dbQuery = String.format(query, queryTable, sourceType.toLowerCase(), offset, offset + limit);
        List<Map<String, AtlasVertex>> sourceDBs = (List) graph.executeGremlinScript(dbQuery, false);
        for (Map<String, AtlasVertex> sourceDB : sourceDBs) {
            AtlasVertex dbVertex = sourceDB.get("db");
            AtlasVertex sourceVertex = sourceDB.get("instance");
            RDBMSDatabase db = getRDBMSDBByVertex(dbVertex, sourceVertex);
            dbs.add(db);
        }
        dbPageResult.setLists(dbs);
        dbPageResult.setCurrentSize(dbs.size());
        String countQuery = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_DB_COUNT_BY_QUERY) : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_COUNT_BY_QUERY);
        List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, queryTable, sourceType.toLowerCase()), false);
        dbPageResult.setTotalSize(counts.get(0));
        return dbPageResult;
    }

    private RDBMSDatabase getRDBMSDBByVertex(AtlasVertex dbVertex, AtlasVertex sourceVertex) throws AtlasBaseException {
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("comment");
        attributes.add("createTime");
        RDBMSDatabase db = new RDBMSDatabase();
        if (Objects.nonNull(dbVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dbVertex, attributes, null, true);
            AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
            db.setDatabaseId(dbEntity.getGuid());
            db.setDatabaseName(dbEntity.getAttribute("name").toString());
            db.setStatus(dbEntity.getStatus().name());
            db.setDatabaseDescription(dbEntity.getAttribute("comment") == null || dbEntity.getAttribute("comment").toString().length() == 0 ? "-" : dbEntity.getAttribute("comment").toString());
            Date createTime = dbEntity.getCreateTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatDateStr = sdf.format(createTime);
        }
        if (Objects.nonNull(dbVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo sourceEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(sourceVertex, attributes, null, true);
            AtlasEntity sourceEntity = sourceEntityWithExtInfo.getEntity();
            db.setSourceName(sourceEntity.getAttribute("name").toString());
            db.setSourceId(sourceEntity.getGuid());
            db.setSourceStatus(sourceEntity.getStatus().toString());
        }
        return db;
    }

    public PageResult<RDBMSTable> getRDBMSTableNameAndDBAndSourceNameByQuery(String queryTable, int offset, int limit, String sourceType, Boolean active) throws AtlasBaseException {
        PageResult<RDBMSTable> tablePageResult = new PageResult<>();
        ArrayList<RDBMSTable> tables = new ArrayList<>();
        AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = null;
        if (active) {
            gremlinQuery = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_FULL_RDBMS_TABLE_DB_SOURCE : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_TABLE_DB_SOURCE_BY_QUERY);
        } else {
            gremlinQuery = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_RDBMS_TABLE_DB_SOURCE : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_TABLE_DB_SOURCE_BY_QUERY);

        }
        String query = gremlinQueryProvider.getQuery(gremlinQuery);
        String tableQuery = String.format(query, sourceType.toLowerCase(), queryTable, offset, offset + limit);
        List<Map<String, AtlasVertex>> sourceDBTables = (List) graph.executeGremlinScript(tableQuery, false);
        for (Map<String, AtlasVertex> sourceDBTable : sourceDBTables) {
            AtlasVertex tableVertex = sourceDBTable.get("table");
            AtlasVertex dbVertex = sourceDBTable.get("db");
            AtlasVertex sourceVertex = sourceDBTable.get("instance");
            RDBMSTable table = getRDBMSTableByVertex(tableVertex, dbVertex, sourceVertex);
            tables.add(table);
        }
        tablePageResult.setLists(tables);
        tablePageResult.setCurrentSize(tables.size());
        String countQuery = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_TABLE_COUNT_BY_QUERY) : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_TABLE_COUNT_BY_QUERY);
        List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, sourceType.toLowerCase(), queryTable), false);
        tablePageResult.setTotalSize(counts.get(0));
        return tablePageResult;
    }

    private RDBMSTable getRDBMSTableByVertex(AtlasVertex tableVertex, AtlasVertex dbVertex, AtlasVertex sourceVertex) throws AtlasBaseException {
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("comment");
        attributes.add("description");
        attributes.add("createTime");
        attributes.add(temporary);
        RDBMSTable table = new RDBMSTable();
        if (Objects.nonNull(tableVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo tableEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(tableVertex, attributes, null, true);
            AtlasEntity tableEntity = tableEntityWithExtInfo.getEntity();
            table.setTableId(tableEntity.getGuid());
            table.setTableName(tableEntity.getAttribute("name").toString());
            //setVirtualTable(table);
            table.setStatus(tableEntity.getStatus().name());
            table.setTableDescription(tableEntity.getAttribute("comment") == null || tableEntity.getAttribute("comment").toString().length() == 0 ? "null" : tableEntity.getAttribute("comment").toString());
            Date createTime = tableEntity.getCreateTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatDateStr = sdf.format(createTime);
            table.setCreateTime(formatDateStr);
        }
        if (Objects.nonNull(dbVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dbVertex, attributes, null, true);
            AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
            table.setDatabaseName(dbEntity.getAttribute("name").toString());
            table.setDatabaseId(dbEntity.getGuid());
            table.setDatabaseStatus(dbEntity.getStatus().name());
        }
        if (Objects.nonNull(sourceVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo sourceEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(sourceVertex, attributes, null, true);
            AtlasEntity sourceEntity = sourceEntityWithExtInfo.getEntity();
            table.setSourceName(sourceEntity.getAttribute("name").toString());
            table.setSourceId(sourceEntity.getGuid());
            table.setSourceStatus(sourceEntity.getStatus().name());
        }
        return table;
    }

    public PageResult<RDBMSColumn> getRDBMSColumnNameTableNameAndDBAndSourceNameByQuery(String queryTable, int offset, int limit, String sourceType) throws AtlasBaseException {
        PageResult<RDBMSColumn> columnPageResult = new PageResult<>();
        ArrayList<RDBMSColumn> columns = new ArrayList<>();
        AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = (limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_RDBMS_COLUMN_TABLE_DB_SOURCE : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_COLUMN_TABLE_DB_SOURCE_BY_QUERY);
        String query = gremlinQueryProvider.getQuery(gremlinQuery);
        String columnQuery = String.format(query, sourceType.toLowerCase(), queryTable, offset, offset + limit);
        List<Map<String, AtlasVertex>> sourceDBTableColumns = (List) graph.executeGremlinScript(columnQuery, false);
        for (Map<String, AtlasVertex> sourceDBTableColumn : sourceDBTableColumns) {
            AtlasVertex columnVertex = sourceDBTableColumn.get("column");
            AtlasVertex tableVertex = sourceDBTableColumn.get("table");
            AtlasVertex dbVertex = sourceDBTableColumn.get("db");
            AtlasVertex sourceVertex = sourceDBTableColumn.get("instance");
            RDBMSColumn column = getRDBMSColumnByVertex(columnVertex, tableVertex, dbVertex, sourceVertex);
            columns.add(column);
        }
        columnPageResult.setLists(columns);
        columnPageResult.setCurrentSize(columns.size());
        String countQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_COLUMN_COUNT_BY_QUERY);
        List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, sourceType.toLowerCase(), queryTable), false);

        columnPageResult.setTotalSize(counts.get(0));
        return columnPageResult;
    }

    private RDBMSColumn getRDBMSColumnByVertex(AtlasVertex columnVertex, AtlasVertex tableVertex, AtlasVertex dbVertex, AtlasVertex sourceVertex) throws AtlasBaseException {
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("comment");
        attributes.add("description");
        RDBMSColumn column = new RDBMSColumn();
        if (Objects.nonNull(columnVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo columnEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(columnVertex, attributes, null, true);
            AtlasEntity columnEntity = columnEntityWithExtInfo.getEntity();
            column.setColumnId(columnEntity.getGuid());
            column.setColumnName(columnEntity.getAttribute("name").toString());
            //setVirtualTable(table);
            column.setStatus(columnEntity.getStatus().name());
            column.setColumnDescription(columnEntity.getAttribute("comment") == null || columnEntity.getAttribute("comment").toString().length() == 0 ? "null" : columnEntity.getAttribute("comment").toString());
        }
        if (Objects.nonNull(tableVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo tableEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(tableVertex, attributes, null, true);
            AtlasEntity tableEntity = tableEntityWithExtInfo.getEntity();
            column.setTableId(tableEntity.getGuid());
            column.setTableName(tableEntity.getAttribute("name").toString());
            //setVirtualTable(table);
            column.setTableStatus(tableEntity.getStatus().name());
        }
        if (Objects.nonNull(dbVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dbVertex, attributes, null, true);
            AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
            column.setDatabaseName(dbEntity.getAttribute("name").toString());
            column.setDatabaseId(dbEntity.getGuid());
            column.setDatabaseStatus(dbEntity.getStatus().name());
        }
        if (Objects.nonNull(sourceVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo sourceEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(sourceVertex, attributes, null, true);
            AtlasEntity sourceEntity = sourceEntityWithExtInfo.getEntity();
            column.setSourceName(sourceEntity.getAttribute("name").toString());
            column.setSourceId(sourceEntity.getGuid());
            column.setSourceStatus(sourceEntity.getStatus().name());
        }
        return column;
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
        attributes.add("createTime");
        attributes.add(temporary);
        Table table = new Table();
        if (Objects.nonNull(tableVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo tableEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(tableVertex, attributes, null, true);
            AtlasEntity tableEntity = tableEntityWithExtInfo.getEntity();
            table.setTableId(tableEntity.getGuid());
            table.setTableName(tableEntity.getAttribute("name").toString());
            if (Boolean.parseBoolean(tableEntity.getAttribute(temporary).toString()) == true) {
                table.setVirtualTable(true);
            } else {
                table.setVirtualTable(false);
            }
            //setVirtualTable(table);
            table.setStatus(tableEntity.getStatus().name());
            table.setDescription(tableEntity.getAttribute("comment") == null ? "" : tableEntity.getAttribute("comment").toString());
            Date createTime = tableEntity.getCreateTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatDateStr = sdf.format(createTime);
            table.setCreateTime(formatDateStr);
        }
        if (Objects.nonNull(dbVertex)) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dbVertex, attributes, null, true);
            AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
            table.setDatabaseName(dbEntity.getAttribute("name").toString());
            table.setDatabaseId(dbEntity.getGuid());
            table.setDatabaseStatus(dbEntity.getStatus().name());
        }
        return table;
    }

    @Override
    public PageResult<Column> getColumnNameAndTableNameAndDbNameByQuery(String queryColumn, Boolean active, int offset, int limit) throws AtlasBaseException {
        PageResult<Column> columnPageResult = new PageResult<>();
        ArrayList<Column> columns = new ArrayList<>();
        AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = active ? ((limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_ACTIVE_COLUMN_TABLE_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_COLUMN_TABLE_DB_BY_QUERY))
                : ((limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_COLUMN_TABLE_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_TABLE_DB_BY_QUERY));
        String query = gremlinQueryProvider.getQuery(gremlinQuery);
        String columnQuery = String.format(query, queryColumn, offset, offset + limit);
        List<Map<String, AtlasVertex>> columnTableDBs = (List) graph.executeGremlinScript(columnQuery, false);
        for (Map<String, AtlasVertex> columnTableDB : columnTableDBs) {
            AtlasVertex columnVertex = columnTableDB.get("column");
            AtlasVertex tableVertex = columnTableDB.get("table");
            AtlasVertex dbVertex = columnTableDB.get("db");

            Column column = getColumnByVertex(columnVertex, tableVertex, dbVertex);
            columns.add(column);
        }
        columnPageResult.setLists(columns);
        columnPageResult.setCurrentSize(columns.size());
        String countQuery = gremlinQueryProvider.getQuery(active ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_COLUMN_COUNT_BY_QUERY : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_COUNT_BY_QUERY);
        List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, queryColumn), false);
        columnPageResult.setTotalSize(counts.get(0));
        return columnPageResult;
    }

    @Override
    public PageResult<Column> getColumnNameAndTableNameAndDbNameByQuery(String queryColumn, Boolean active, int offset, int limit, String dbs) throws AtlasBaseException {
        PageResult<Column> columnPageResult = new PageResult<>();
        ArrayList<Column> columns = new ArrayList<>();
        AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery = active ? ((limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_FULL_ACTIVE_COLUMN_TABLE_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_ACTIVE_COLUMN_TABLE_DB_BY_QUERY))
                : ((limit == -1 ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_FULL_COLUMN_TABLE_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_COLUMN_TABLE_DB_BY_QUERY));
        String query = gremlinQueryProvider.getQuery(gremlinQuery);
        String columnQuery = String.format(query, queryColumn, dbs, offset, offset + limit);
        List<Map<String, AtlasVertex>> columnTableDBs = (List) graph.executeGremlinScript(columnQuery, false);
        for (Map<String, AtlasVertex> columnTableDB : columnTableDBs) {
            AtlasVertex columnVertex = columnTableDB.get("column");
            AtlasVertex tableVertex = columnTableDB.get("table");
            AtlasVertex dbVertex = columnTableDB.get("db");

            Column column = getColumnByVertex(columnVertex, tableVertex, dbVertex);
            columns.add(column);
        }
        columnPageResult.setLists(columns);
        columnPageResult.setCurrentSize(columns.size());
        String countQuery = gremlinQueryProvider.getQuery(active ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_ACTIVE_COLUMN_COUNT_BY_QUERY : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_COLUMN_COUNT_BY_QUERY);
        List<Long> counts = (List) graph.executeGremlinScript(String.format(countQuery, queryColumn, dbs), false);
        columnPageResult.setTotalSize(counts.get(0));
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

    public PageResult<Database> getDatabaseByQuery(String queryDb, boolean active, long offset, long limit) throws AtlasBaseException {
        PageResult<Database> databasePageResult = new PageResult<>();
        List<Database> lists = new ArrayList<>();
        String queryStr = "";
        if ((offset == 0 && limit == -1)) {
            queryStr = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_ACTIVE_DATABASE)
                    : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_DATABASE);
        } else {
            queryStr = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_DATABASE_BY_QUERY)
                    : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DATABASE_BY_QUERY);

        }
        String format = (offset == 0 && limit == -1) ? String.format(queryStr, queryDb) : String.format(queryStr, queryDb, offset, offset + limit);
        List<AtlasVertex> databases = (List) graph.executeGremlinScript(format, false);
        for (AtlasVertex database : databases) {
            Database db = new Database();
            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            attributes.add("comment");
            if (Objects.nonNull(database)) {
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(database, attributes, null, true);
                AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
                db.setDatabaseName(dbEntity.getAttribute("name").toString());
                db.setDatabaseId(dbEntity.getGuid());
                db.setStatus(dbEntity.getStatus().name());
                db.setDatabaseDescription(dbEntity.getAttribute("comment") == null ? "-" : dbEntity.getAttribute("comment").toString());
            }
            lists.add(db);
        }
        databasePageResult.setCurrentSize(lists.size());
        databasePageResult.setLists(lists);
        String gremlinQuery = gremlinQueryProvider.getQuery(active ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_ACTIVE_TOTAL_NUM_BY_QUERY : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_TOTAL_NUM_BY_QUERY);
        String numQuery = String.format(gremlinQuery, queryDb);
        List num = (List) graph.executeGremlinScript(numQuery, false);
        databasePageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
        return databasePageResult;
    }

    private static final String clusterName = AtlasConfiguration.ATLAS_CLUSTER_NAME.getString();

    public String getInstanceQualifiedName(String instanceId) {
        return String.format("%s@%s", instanceId, clusterName);
    }

    public PageResult<Database> getSchemaList(String sourceId, boolean active, long offset, long limit, String query, String dbs, boolean queryTableCount) throws AtlasBaseException {
        PageResult<Database> databasePageResult = new PageResult<>();
        List<Database> lists = new ArrayList<>();

        String activeQuery = active ? ".has('__state','ACTIVE')" : "";
        String nameQuery = StringUtils.isEmpty(query) ? "" : ".has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*" + query + ".*'))";
        String pageQuery = (offset == 0 && limit == -1) ? "" : ".range(" + offset + "," + (offset + limit) + ")";

        String format = "";
        if (StringUtils.isEmpty(sourceId)) {
            //跨类型搜索
            format = "g.tx().commit();g.V().or(has('__typeName','rdbms_db'),has('__typeName','hive_db').has('Asset.name', within(" + dbs + "))).has('__guid')%s.order().by('__timestamp').dedup()%s.toList()";
        } else if ("hive".equalsIgnoreCase(sourceId)) {
            //hive 搜索
            format = "g.tx().commit();g.V().has('__typeName','hive_db').has('__guid')%s.has('Asset.name', within(" + dbs + ")).order().by('__timestamp').dedup()%s.toList()";
        } else {
            //关系型数据源搜索
            format = "g.tx().commit();g.V().has('__typeName','rdbms_instance').has('Referenceable.qualifiedName','"+getInstanceQualifiedName(sourceId)+"').inE().outV().has('__typeName','rdbms_db').has('__guid')%s.order().by('__timestamp').dedup()%s.toList()";
        }

        String queryStr =  String.format(format,
                activeQuery + nameQuery,
                pageQuery
        );

        String queryCountStr = String.format(format,
                activeQuery + nameQuery,
                ".count()"
        );

        List<AtlasVertex> databases = (List) graph.executeGremlinScript(queryStr, false);
        for (AtlasVertex database : databases) {
            Database db = new Database();
            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            attributes.add("comment");
            if (Objects.nonNull(database)) {
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(database, attributes, null, true);
                AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
                db.setDatabaseName(dbEntity.getAttribute("name").toString());
                db.setDatabaseId(dbEntity.getGuid());
                db.setStatus(dbEntity.getStatus().name());
                db.setDatabaseDescription(dbEntity.getAttribute("comment") == null ? "-" : dbEntity.getAttribute("comment").toString());

                if (queryTableCount) {
                    String queryTableCountStr = String.format("g.tx().commit();g.V().has('__typeName',within('rdbms_db','hive_db')).has('__guid','%s').inE().outV().has('__typeName', within('rdbms_table','hive_table'))%s.has('__guid').dedup().count().toList()",
                            dbEntity.getGuid(),
                            activeQuery);
                    List num = (List) graph.executeGremlinScript(queryTableCountStr, false);
                    db.setTableCount(Integer.parseInt(num.get(0).toString()));
                }
            }
            lists.add(db);
        }
        databasePageResult.setCurrentSize(lists.size());
        databasePageResult.setLists(lists);
        List num = (List) graph.executeGremlinScript(queryCountStr, false);
        databasePageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
        return databasePageResult;
    }

    public PageResult<TableEntity> getTableList(String schemaId, boolean active, long offset, long limit, String query, Boolean isView) throws AtlasBaseException {
        PageResult<TableEntity> tablePageResult = new PageResult<>();
        List<TableEntity> lists = new ArrayList<>();

        String schemaIdQuery = StringUtils.isEmpty(schemaId) ? "" : ".has('__guid','" + schemaId + "')";
        String activeQuery = active ? ".has('__state','ACTIVE')" : "";
        String pageQuery = (offset == 0 && limit == -1) ? "" : ".range(" + offset + "," + (offset + limit) + ")";
        String nameQuery = StringUtils.isEmpty(query) ? "" : ".has('Asset.name', org.janusgraph.core.attribute.Text.textRegex('.*" + query + ".*'))";
        String viewQuery = isView == null ? "" : (isView ? ".or(has('hive_table.tableType',org.janusgraph.core.attribute.Text.textRegex('.*(?i)VIEW.*')),has('rdbms_table.type',org.janusgraph.core.attribute.Text.textRegex('.*(?i)VIEW.*')))"
                : ".not(or(has('hive_table.tableType',org.janusgraph.core.attribute.Text.textRegex('.*(?i)VIEW.*')),has('rdbms_table.type',org.janusgraph.core.attribute.Text.textRegex('.*(?i)VIEW.*'))))");


        String queryStr = String.format("g.tx().commit();g.V().has('__typeName',within('rdbms_db','hive_db'))%s.inE().outV().has('__typeName', within('rdbms_table','hive_table')).has('__guid')%s.order().by('__timestamp').dedup()%s.toList()",
                schemaIdQuery,
                activeQuery + nameQuery + viewQuery,
                pageQuery
        );

        String queryCountStr = String.format("g.tx().commit();g.V().has('__typeName',within('rdbms_db','hive_db'))%s.inE().outV().has('__typeName', within('rdbms_table','hive_table')).has('__guid')%s.order().by('__timestamp').dedup()%s.toList()",
                schemaIdQuery,
                activeQuery + nameQuery + viewQuery,
                ".count()"
        );

        List<AtlasVertex> tables = (List) graph.executeGremlinScript(queryStr, false);
        for (AtlasVertex table : tables) {
            TableEntity tb = new TableEntity();
            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            attributes.add("comment");
            attributes.add(temporary);
            attributes.add("tableType");
            attributes.add("type");
            if (Objects.nonNull(table)) {
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(table, attributes, null, true);
                AtlasEntity entity = dbEntityWithExtInfo.getEntity();
                tb.setName(entity.getAttribute("name").toString());
                tb.setHiveTable(entity.getTypeName().toLowerCase().contains("hive"));
                tb.setTableType(String.valueOf(entity.getAttribute(tb.isHiveTable() ? "tableType":"type")));
                if (Boolean.parseBoolean(String.valueOf(entity.getAttribute(temporary))) == true) {
                    tb.setVirtualTable(true);
                } else {
                    tb.setVirtualTable(false);
                }
                tb.setId(entity.getGuid());
                tb.setStatus(entity.getStatus().name());
                tb.setDescription(entity.getAttribute("comment") == null ? "-" : entity.getAttribute("comment").toString());
            }
            lists.add(tb);
        }
        tablePageResult.setCurrentSize(lists.size());
        tablePageResult.setOffset(offset);
        tablePageResult.setLists(lists);
        List num = (List) graph.executeGremlinScript(queryCountStr, false);
        tablePageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
        return tablePageResult;
    }



    public PageResult<Database> getDatabaseByQuery(String queryDb, boolean active, long offset, long limit, String dbs) throws AtlasBaseException {
        return getDatabaseByQuery(queryDb, active, offset, limit, dbs, false);
    }

    public PageResult<Database> getDatabaseByQuery(String queryDb, boolean active, long offset, long limit, String dbs, boolean queryCount) throws AtlasBaseException {
        PageResult<Database> databasePageResult = new PageResult<>();
        List<Database> lists = new ArrayList<>();
        String queryStr = "";
        if ((offset == 0 && limit == -1)) {
            queryStr = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_FULL_ACTIVE_DATABASE)
                    : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_FULL_DATABASE);
        } else {
            queryStr = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_ACTIVE_DATABASE_BY_QUERY)
                    : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_DATABASE_BY_QUERY);

        }
        String format = (offset == 0 && limit == -1) ? String.format(queryStr, queryDb, dbs) : String.format(queryStr, queryDb, dbs, offset, offset + limit);
        List<AtlasVertex> databases = (List) graph.executeGremlinScript(format, false);
        for (AtlasVertex database : databases) {
            Database db = new Database();
            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            attributes.add("comment");
            if (Objects.nonNull(database)) {
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(database, attributes, null, true);
                AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
                db.setDatabaseName(dbEntity.getAttribute("name").toString());
                db.setDatabaseId(dbEntity.getGuid());
                db.setStatus(dbEntity.getStatus().name());
                db.setDatabaseDescription(dbEntity.getAttribute("comment") == null ? "-" : dbEntity.getAttribute("comment").toString());

                if (queryCount) {
                    String gremlinQuery = gremlinQueryProvider.getQuery(active ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_TABLE_TOTAL_BY_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_TOTAL_BY_DB);
                    String numQuery = String.format(gremlinQuery, database.getId());
                    List num = (List) graph.executeGremlinScript(numQuery, false);
                    db.setTableCount(Integer.parseInt(num.get(0).toString()));
                }
            }
            lists.add(db);
        }
        databasePageResult.setCurrentSize(lists.size());
        databasePageResult.setLists(lists);
        String gremlinQuery = gremlinQueryProvider.getQuery(active ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_DB_ACTIVE_TOTAL_NUM_BY_QUERY : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_DB_TOTAL_NUM_BY_QUERY);
        String numQuery = String.format(gremlinQuery, queryDb, dbs);
        List num = (List) graph.executeGremlinScript(numQuery, false);
        databasePageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
        return databasePageResult;
    }

    public PageResult<RDBMSDataSource> getRDBMSDataSourceByQuery(String queryDb, long offset, long limit, String sourceType, Boolean active) throws AtlasBaseException {
        PageResult<RDBMSDataSource> databasePageResult = new PageResult<>();
        List<RDBMSDataSource> lists = new ArrayList<>();
        String queryStr = "";
        if (active) {
            if ((offset == 0 && limit == -1)) {
                queryStr = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_FUll_RDBMS_SOURCE);
            } else {
                queryStr = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_SOURCE_BY_QUERY);
            }
        } else {
            if ((offset == 0 && limit == -1)) {
                queryStr = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FUll_RDBMS_SOURCE);
            } else {
                queryStr = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_SOURCE_BY_QUERY);
            }
        }

        String format = (offset == 0 && limit == -1) ? String.format(queryStr, sourceType.toLowerCase(), queryDb) : String.format(queryStr, sourceType.toLowerCase(), queryDb, offset, offset + limit);
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("comment");
        attributes.add("qualifiedName");
        attributes.add("platform");
        List<AtlasVertex> dataSources = (List) graph.executeGremlinScript(format, false);
        List<AtlasEntity> test = new ArrayList<>();
        for (AtlasVertex dataSource : dataSources) {
            RDBMSDataSource source = new RDBMSDataSource();
            if (Objects.nonNull(dataSource)) {
                AtlasEntity.AtlasEntityWithExtInfo sourceEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(dataSource, attributes, null, true);
                AtlasEntity sourceEntity = sourceEntityWithExtInfo.getEntity();
                test.add(sourceEntity);
                source.setSourceName(sourceEntity.getAttribute("name").toString());
                source.setSourceId(sourceEntity.getGuid());
                source.setStatus(sourceEntity.getStatus().name());
                source.setSourceDescription(sourceEntity.getAttribute("comment") == null || sourceEntity.getAttribute("comment").toString().length() == 0 ? "-" : sourceEntity.getAttribute("comment").toString());
            }
            lists.add(source);
        }
        databasePageResult.setCurrentSize(lists.size());
        databasePageResult.setLists(lists);
        String gremlinQuery = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_SOURCE_TOTAL_NUM_BY_QUERY) : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_SOURCE_TOTAL_NUM_BY_QUERY);
        String numQuery = String.format(gremlinQuery, sourceType.toLowerCase(), queryDb);
        List num = (List) graph.executeGremlinScript(numQuery, false);
        databasePageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
        return databasePageResult;
    }

    public PageResult<RDBMSDatabase> getRDBMSDBBySource(String sourceId, long offset, long limit, Boolean active) throws AtlasBaseException {
        return getRDBMSDBBySource(sourceId, offset, limit, active, false, null);
    }

    public PageResult<RDBMSDatabase> getRDBMSDBBySource(String sourceId, long offset, long limit, Boolean active, boolean queryCount, String query) throws AtlasBaseException {
        PageResult<RDBMSDatabase> databasePageResult = new PageResult<>();
        List<RDBMSDatabase> lists = new ArrayList<>();
        String format = null;
        query = StringUtils.isEmpty(query) ? "" : query;
        String sourceIdHasStr = StringUtils.isEmpty(sourceId) ? "" : ".has('__guid','" + sourceId + "')";
        if (active) {
            format = (offset == 0 && limit == -1) ? String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_FULL_RDBMS_DATABASE_DYNAMIC), sourceIdHasStr, query) : String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_DATABASE_BY_SOURCE_DYNAMIC), sourceIdHasStr, query, offset, offset + limit);
        } else {
            format = (offset == 0 && limit == -1) ? String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_RDBMS_DATABASE_DYNAMIC), sourceIdHasStr, query) : String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DATABASE_BY_SOURCE_DYNAMIC), sourceIdHasStr, query, offset, offset + limit);

        }
        List<AtlasVertex> databases = (List) graph.executeGremlinScript(format, false);
        for (AtlasVertex database : databases) {
            RDBMSDatabase db = new RDBMSDatabase();
            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            attributes.add("comment");
            List<String> relationAttributes = new ArrayList<>();
            relationAttributes.add("db");
            if (Objects.nonNull(database)) {
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(database, attributes, null, true);
                AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
                db.setDatabaseName(dbEntity.getAttribute("name").toString());
                //setVirtualTable(tb);
                db.setDatabaseId(dbEntity.getGuid());
                db.setStatus(dbEntity.getStatus().name());
                db.setDatabaseDescription(dbEntity.getAttribute("comment") == null || dbEntity.getAttribute("comment").toString().length() == 0 ? "-" : dbEntity.getAttribute("comment").toString());
                if (queryCount) {
                    String gremlinQuery = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_TABLE_TOTAL_BY_DB) : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_TABLE_TOTAL_BY_DB);
                    String numQuery = String.format(gremlinQuery, database.getId());
                    List num = (List) graph.executeGremlinScript(numQuery, false);
                    db.setTableCount(Integer.parseInt(num.get(0).toString()));
                }
            }
            lists.add(db);
        }
        databasePageResult.setCurrentSize(lists.size());
        databasePageResult.setOffset(offset);
        databasePageResult.setLists(lists);
        String gremlinQuery = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_DATABASE_TOTAL_BY_SOURCE) : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DATABASE_TOTAL_BY_SOURCE);
        String numQuery = String.format(gremlinQuery, sourceId);
        List num = (List) graph.executeGremlinScript(numQuery, false);
        databasePageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
        return databasePageResult;
    }

    public PageResult<RDBMSTable> getRDBMSTableByDB(String databaseId, long offset, long limit, Boolean active) throws AtlasBaseException {
        PageResult<RDBMSTable> tablePageResult = new PageResult<>();
        List<RDBMSTable> lists = new ArrayList<>();
        String format = null;
        if (active) {
            format = (offset == 0 && limit == -1) ? String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_FULL_RDBMS_TABLE), databaseId) : String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_TABLE_BY_DB), databaseId, offset, offset + limit);
        } else {
            format = (offset == 0 && limit == -1) ? String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_RDBMS_TABLE), databaseId) : String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_TABLE_BY_DB), databaseId, offset, offset + limit);
        }
        List<AtlasVertex> tables = (List) graph.executeGremlinScript(format, false);
        for (AtlasVertex table : tables) {
            RDBMSTable tb = new RDBMSTable();
            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            attributes.add("comment");
            List<String> relationAttributes = new ArrayList<>();
            relationAttributes.add("db");
            if (Objects.nonNull(table)) {
                AtlasEntity.AtlasEntityWithExtInfo tableEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(table, attributes, null, true);
                AtlasEntity tableEntity = tableEntityWithExtInfo.getEntity();
                tb.setTableName(tableEntity.getAttribute("name").toString());
                //setVirtualTable(tb);
                tb.setTableId(tableEntity.getGuid());
                tb.setStatus(tableEntity.getStatus().name());
                tb.setTableDescription(tableEntity.getAttribute("comment") == null || tableEntity.getAttribute("comment").toString().length() == 0 ? "-" : tableEntity.getAttribute("comment").toString());
            }
            lists.add(tb);
        }
        tablePageResult.setCurrentSize(lists.size());
        tablePageResult.setOffset(offset);
        tablePageResult.setLists(lists);
        String gremlinQuery = active ? gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_RDBMS_TABLE_TOTAL_BY_DB) : gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_TABLE_TOTAL_BY_DB);
        String numQuery = String.format(gremlinQuery, databaseId);
        List num = (List) graph.executeGremlinScript(numQuery, false);
        tablePageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
        return tablePageResult;
    }

    public PageResult<Table> getTableByDB(String databaseId, Boolean active, long offset, long limit) throws AtlasBaseException {
        PageResult<Table> tablePageResult = new PageResult<>();
        List<Table> lists = new ArrayList<>();
        String format = active ? ((offset == 0 && limit == -1) ? String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_ACTIVE_TABLE), databaseId) : String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_TABLE_BY_DB), databaseId, offset, offset + limit))
                : ((offset == 0 && limit == -1) ? String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_TABLE), databaseId) : String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_BY_DB), databaseId, offset, offset + limit));
        List<AtlasVertex> tables = (List) graph.executeGremlinScript(format, false);
        for (AtlasVertex table : tables) {
            Table tb = new Table();
            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            attributes.add("comment");
            attributes.add(temporary);
            List<String> relationAttributes = new ArrayList<>();
            relationAttributes.add("db");
            if (Objects.nonNull(table)) {
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(table, attributes, null, true);
                AtlasEntity entity = dbEntityWithExtInfo.getEntity();
                tb.setTableName(entity.getAttribute("name").toString());
                if (Boolean.parseBoolean(entity.getAttribute(temporary).toString()) == true) {
                    tb.setVirtualTable(true);
                } else {
                    tb.setVirtualTable(false);
                }
                //setVirtualTable(tb);
                tb.setTableId(entity.getGuid());
                tb.setStatus(entity.getStatus().name());
                tb.setDescription(entity.getAttribute("comment") == null ? "-" : entity.getAttribute("comment").toString());
            }
            lists.add(tb);
        }
        tablePageResult.setCurrentSize(lists.size());
        tablePageResult.setOffset(offset);
        tablePageResult.setLists(lists);
        String gremlinQuery = gremlinQueryProvider.getQuery(active ? MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.ACTIVE_TABLE_TOTAL_BY_DB : MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_TOTAL_BY_DB);
        String numQuery = String.format(gremlinQuery, databaseId);
        List num = (List) graph.executeGremlinScript(numQuery, false);
        tablePageResult.setTotalSize(Integer.parseInt(num.get(0).toString()));
        return tablePageResult;
    }

    public List<Long> getDBTotal() throws AtlasBaseException {
        String gremlinQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_TOTAL_NUM_BY_QUERY);
        return (List) graph.executeGremlinScript(String.format(gremlinQuery, ""), false);

    }

    public List<Long> getDBTotal(String dbs) throws AtlasBaseException {
        String gremlinQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_DB_TOTAL_NUM_BY_QUERY);
        return (List) graph.executeGremlinScript(String.format(gremlinQuery, "", dbs), false);

    }

    public List<Long> getTBTotal() throws AtlasBaseException {
        String countQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_COUNT_BY_QUEERY);
        return (List) graph.executeGremlinScript(String.format(countQuery, ""), false);
    }

    public List<Long> getTBTotal(String dbs) throws AtlasBaseException {
        String countQuery = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TENANT_TABLE_COUNT_BY_QUEERY);
        return (List) graph.executeGremlinScript(String.format(countQuery, dbs, ""), false);
    }

}

