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
package org.zeta.metaspace.discovery;

import static org.apache.atlas.repository.Constants.RELATIONSHIP_GUID_PROPERTY_KEY;

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
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.repository.graphdb.AtlasEdge;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.v2.AtlasGraphUtilsV2;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.type.AtlasTypeUtil;
import org.apache.atlas.util.AtlasGremlinQueryProvider;
import org.apache.atlas.v1.model.lineage.SchemaResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

/*
 * @description
 * @author sunhaoning
 * @date 2018/12/4 19:35
 */
public class MetaspaceEntityLineageService implements MetaspaceLineageService {

    private static final Logger LOG = LoggerFactory.getLogger(EntityLineageService.class);

    private static final String PROCESS_INPUTS_EDGE  = "__Process.inputs";
    private static final String PROCESS_OUTPUTS_EDGE = "__Process.outputs";
    private static final String COLUMNS              = "columns";

    private final AtlasGraph graph;
    private final AtlasGremlinQueryProvider gremlinQueryProvider;
    private final EntityGraphRetriever entityRetriever;

    @Inject
    MetaspaceEntityLineageService(AtlasTypeRegistry typeRegistry, AtlasGraph atlasGraph) {
        this.graph = atlasGraph;
        this.gremlinQueryProvider = AtlasGremlinQueryProvider.INSTANCE;
        this.entityRetriever = new EntityGraphRetriever(typeRegistry);
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
        List<String> inputRelatedTable  = new ArrayList<>(getColumnRelatedTableInfo(guid, AtlasLineageInfo.LineageDirection.INPUT, depth));
        List<String> outputRelatedTable = new ArrayList<>(getColumnRelatedTableInfo(guid, AtlasLineageInfo.LineageDirection.OUTPUT, depth));

        inputRelatedTable.addAll(outputRelatedTable);
        return inputRelatedTable;
    }

    public List<String> getColumnRelatedTableInfo(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        String                         lineageQuery =  getColumnRelatedTableQuery(guid, direction, depth);

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
            String query = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.FULL_COLUMN_RELATED_TABLE);
            columnRelatedTableQuery = String.format(query, entityGuid, incomingFrom, outgoingTo);
        } else {
            String query = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.PARTIAL_COLUMN_RELATED_TABLE);
            columnRelatedTableQuery = String.format(query, entityGuid, incomingFrom, outgoingTo, depth);
        }
        return columnRelatedTableQuery;
    }


    private List<String> getColumnIds(AtlasEntity entity) {
        List<String> ret        = new ArrayList<>();
        Object       columnObjs = entity.getAttribute(COLUMNS);

        if (columnObjs instanceof List) {
            for (Object pkObj : (List) columnObjs) {
                if (pkObj instanceof AtlasObjectId) {
                    ret.add(((AtlasObjectId) pkObj).getGuid());
                }
            }
        }

        return ret;
    }

    private boolean isColumn(List<String> columnIds, Map.Entry<String, AtlasEntity> e) {
        return columnIds.contains(e.getValue().getGuid());
    }


    private AtlasLineageInfo getLineageInfo(String guid, AtlasLineageInfo.LineageDirection direction, int depth, boolean isTable) throws AtlasBaseException {
        String                         lineageQuery =  getLineageQuery(guid, direction, depth, isTable);

        List edgeMapList = (List) graph.executeGremlinScript(lineageQuery, false);

        return formattedGraphData(edgeMapList, guid, direction, depth);
    }

    public AtlasLineageInfo formattedGraphData(List edgeMapList, String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        Map<String, AtlasEntityHeader> entities     = new HashMap<>();
        Set<AtlasLineageInfo.LineageRelation> relations    = new HashSet<>();
        if (CollectionUtils.isNotEmpty(edgeMapList)) {
            for (Object edgeMap : edgeMapList) {
                if (edgeMap instanceof Map) {
                    for (final Object o : ((Map) edgeMap).entrySet()) {
                        final Map.Entry entry = (Map.Entry) o;
                        Object          value = entry.getValue();

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
        AtlasVertex inVertex     = edge.getInVertex();
        AtlasVertex outVertex    = edge.getOutVertex();
        String      inGuid       = AtlasGraphUtilsV2.getIdFromVertex(inVertex);
        String      outGuid      = AtlasGraphUtilsV2.getIdFromVertex(outVertex);
        String      relationGuid = AtlasGraphUtilsV2.getEncodedProperty(edge, RELATIONSHIP_GUID_PROPERTY_KEY, String.class);
        boolean     isInputEdge  = edge.getLabel().equalsIgnoreCase(PROCESS_INPUTS_EDGE);

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

    private AtlasLineageInfo getBothLineageInfo(String guid, int depth, boolean isTable) throws AtlasBaseException {
        AtlasLineageInfo inputLineage  = getLineageInfo(guid, AtlasLineageInfo.LineageDirection.INPUT, depth, isTable);
        AtlasLineageInfo outputLineage = getLineageInfo(guid, AtlasLineageInfo.LineageDirection.OUTPUT, depth, isTable);
        AtlasLineageInfo ret           = inputLineage;

        ret.getRelations().addAll(outputLineage.getRelations());
        ret.getGuidEntityMap().putAll(outputLineage.getGuidEntityMap());
        ret.setLineageDirection(AtlasLineageInfo.LineageDirection.BOTH);

        return ret;
    }

    private String getLineageQuery(String entityGuid, AtlasLineageInfo.LineageDirection direction, int depth, boolean isTable) {
        String lineageQuery = null;

        if(isTable) {
            if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
                lineageQuery = generateLineageQuery(entityGuid, depth, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);

            } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
                lineageQuery = generateLineageQuery(entityGuid, depth, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
            }
        } else {
            if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
                lineageQuery = generateColumnLineageQuery(entityGuid, depth, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);

            } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
                lineageQuery = generateColumnLineageQuery(entityGuid, depth, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
            }
        }

        return lineageQuery;
    }

    private String generateLineageQuery(String entityGuid, int depth, String incomingFrom, String outgoingTo) {
        String lineageQuery;
        if (depth < 1) {
            String query = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.FULL_LINEAGE);
            lineageQuery = String.format(query, entityGuid, incomingFrom, outgoingTo);
        } else {
            String query = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.PARTIAL_LINEAGE);
            lineageQuery = String.format(query, entityGuid, incomingFrom, outgoingTo, depth);
        }
        return lineageQuery;
    }

    @Override
    @GraphTransaction
    public Integer getLineageDepth(String guid, AtlasLineageInfo.LineageDirection direction) throws AtlasBaseException {
        String lineageQuery = getLineageDepthQuery(guid, direction);
        List depthList = (List) graph.executeGremlinScript(lineageQuery, false);
        if(Objects.nonNull(depthList) && depthList.size() > 0)
            return Integer.parseInt(depthList.get(0).toString());
        else
            return 0;
    }
    private String getLineageDepthQuery(String entityGuid, AtlasLineageInfo.LineageDirection direction) {
        String lineageQuery = null;

        if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {
            String query  = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.LINEAGE_DEPTH);
            lineageQuery = String.format(query, entityGuid, PROCESS_OUTPUTS_EDGE, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);

        } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
            String query  = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.LINEAGE_DEPTH);
            lineageQuery = String.format(query, entityGuid, PROCESS_INPUTS_EDGE, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
        }
        return lineageQuery;
    }

    @Override
    @GraphTransaction
    public Integer getEntityDirectNum(String guid, AtlasLineageInfo.LineageDirection direction) throws AtlasBaseException {
        String lineageQuery = getEntityDirectNumQuery(guid, direction);
        List depthList = (List) graph.executeGremlinScript(lineageQuery, false);
        if(Objects.nonNull(depthList) && depthList.size()>0)
            return Integer.parseInt(depthList.get(0).toString());
        else
            return 0;
    }
    private String getEntityDirectNumQuery(String entityGuid, AtlasLineageInfo.LineageDirection direction) {
        String lineageQuery = null;
        String query  = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.DIRECT_ENTITY_NUM);
        if (direction.equals(AtlasLineageInfo.LineageDirection.INPUT)) {

            lineageQuery = String.format(query, entityGuid, PROCESS_OUTPUTS_EDGE, PROCESS_INPUTS_EDGE);

        } else if (direction.equals(AtlasLineageInfo.LineageDirection.OUTPUT)) {
            lineageQuery = String.format(query, entityGuid, PROCESS_INPUTS_EDGE, PROCESS_OUTPUTS_EDGE);
        }
        return lineageQuery;
    }

    private String generateColumnLineageQuery(String entityGuid, int depth, String incomingFrom, String outgoingTo) {
        String lineageQuery;
        if (depth < 1) {
            String query = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.FULL_COLUMN_LINEAGE);
            lineageQuery = String.format(query, entityGuid, incomingFrom, outgoingTo);
        } else {
            String query = gremlinQueryProvider.getQuery(AtlasGremlinQueryProvider.AtlasGremlinQuery.PARTIAL_COLUMN_LINEAGE);
            lineageQuery = String.format(query, entityGuid, incomingFrom, outgoingTo, depth);
        }
        return lineageQuery;
    }
}
