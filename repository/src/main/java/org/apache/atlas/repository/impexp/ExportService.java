/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas.repository.impexp;

import com.google.common.annotations.VisibleForTesting;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.TypeCategory;
import org.apache.atlas.model.impexp.AtlasExportRequest;
import org.apache.atlas.model.impexp.AtlasExportResult;
import org.apache.atlas.model.instance.AtlasClassification;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.typedef.BaseAtlasBaseTypeDef;
import org.apache.atlas.model.typedef.AtlasClassificationDef;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.model.typedef.AtlasEnumDef;
import org.apache.atlas.model.typedef.AtlasStructDef;
import org.apache.atlas.model.typedef.AtlasStructDef.AtlasAttributeDef;
import org.apache.atlas.model.typedef.AtlasTypesDef;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.type.AtlasArrayType;
import org.apache.atlas.type.AtlasClassificationType;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasEnumType;
import org.apache.atlas.type.AtlasMapType;
import org.apache.atlas.type.AtlasStructType;
import org.apache.atlas.type.AtlasStructType.AtlasAttribute;
import org.apache.atlas.type.BaseAtlasType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.type.AtlasTypeUtil;
import org.apache.atlas.util.BaseAtlasGremlinQueryProvider;
import org.apache.atlas.util.BaseAtlasGremlinQueryProvider.AtlasGremlinQuery;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.*;

import static org.apache.atlas.model.impexp.AtlasExportRequest.*;

@Component
public class ExportService {
    private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

    private final AtlasTypeRegistry         typeRegistry;
    private final AtlasGraph                atlasGraph;
    private final EntityGraphRetriever      entityGraphRetriever;
    private final BaseAtlasGremlinQueryProvider gremlinQueryProvider;

    @Inject
    public ExportService(final AtlasTypeRegistry typeRegistry, AtlasGraph atlasGraph) throws AtlasBaseException {
        this.typeRegistry         = typeRegistry;
        this.entityGraphRetriever = new EntityGraphRetriever(this.typeRegistry);
        this.atlasGraph           = atlasGraph;
        this.gremlinQueryProvider = BaseAtlasGremlinQueryProvider.INSTANCE;
    }

    public AtlasExportResult run(ZipSink exportSink, AtlasExportRequest request, String userName, String hostName,
                                 String requestingIP) throws AtlasBaseException {
        long              startTime = System.currentTimeMillis();
        AtlasExportResult result    = new AtlasExportResult(request, userName, requestingIP, hostName, startTime);
        ExportContext     context   = new ExportContext(result, exportSink);

        try {
            LOG.info("==> export(user={}, from={})", userName, requestingIP);

            AtlasExportResult.OperationStatus[] statuses = processItems(request, context);

            processTypesDef(context);
            updateSinkWithOperationMetrics(context, statuses, getOperationDuration(startTime));
        } catch(Exception ex) {
            LOG.error("Operation failed: ", ex);
        } finally {
            atlasGraph.releaseGremlinScriptEngine(context.scriptEngine);
            LOG.info("<== export(user={}, from={}): status {}", userName, requestingIP, context.result.getOperationStatus());
            context.clear();
            result.clear();
        }

        return context.result;
    }

    private void updateSinkWithOperationMetrics(ExportContext context, AtlasExportResult.OperationStatus[] statuses, int duration) throws AtlasBaseException {
        context.result.getData().getEntityCreationOrder().addAll(context.lineageProcessed);
        context.sink.setExportOrder(context.result.getData().getEntityCreationOrder());
        context.sink.setTypesDef(context.result.getData().getTypesDef());
        clearContextData(context);
        context.result.setOperationStatus(getOverallOperationStatus(statuses));
        context.result.incrementMeticsCounter("duration", duration);
        context.sink.setResult(context.result);
    }

    private void clearContextData(ExportContext context) {
        context.result.setData(null);
    }

    private int getOperationDuration(long startTime) {
        return (int) (System.currentTimeMillis() - startTime);
    }

    private void processTypesDef(ExportContext context) {
        AtlasTypesDef typesDef = context.result.getData().getTypesDef();

        for (String entityType : context.entityTypes) {
            AtlasEntityDef entityDef = typeRegistry.getEntityDefByName(entityType);

            typesDef.getEntityDefs().add(entityDef);
        }

        for (String classificationType : context.classificationTypes) {
            AtlasClassificationDef classificationDef = typeRegistry.getClassificationDefByName(classificationType);

            typesDef.getClassificationDefs().add(classificationDef);
        }

        for (String structType : context.structTypes) {
            AtlasStructDef structDef = typeRegistry.getStructDefByName(structType);

            typesDef.getStructDefs().add(structDef);
        }

        for (String enumType : context.enumTypes) {
            AtlasEnumDef enumDef = typeRegistry.getEnumDefByName(enumType);

            typesDef.getEnumDefs().add(enumDef);
        }
    }

    private AtlasExportResult.OperationStatus[] processItems(AtlasExportRequest request, ExportContext context) throws AtlasServiceException, AtlasException, AtlasBaseException {
        AtlasExportResult.OperationStatus[] statuses = new AtlasExportResult.OperationStatus[request.getItemsToExport().size()];
        List<AtlasObjectId> itemsToExport = request.getItemsToExport();
        for (int i = 0; i < itemsToExport.size(); i++) {
            AtlasObjectId item = itemsToExport.get(i);
            statuses[i] = processObjectId(item, context);
        }
        return statuses;
    }

    @VisibleForTesting
    AtlasExportResult.OperationStatus getOverallOperationStatus(AtlasExportResult.OperationStatus... statuses) {
        AtlasExportResult.OperationStatus overall = (statuses.length == 0) ?
                AtlasExportResult.OperationStatus.FAIL : statuses[0];

        for (AtlasExportResult.OperationStatus s : statuses) {
            if (overall != s) {
                overall = AtlasExportResult.OperationStatus.PARTIAL_SUCCESS;
            }
        }

        return overall;
    }

    private AtlasExportResult.OperationStatus processObjectId(AtlasObjectId item, ExportContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> processObjectId({})", item);
        }

        try {
            List<String> entityGuids = getStartingEntity(item, context);
            if(entityGuids.size() == 0) {
                return AtlasExportResult.OperationStatus.FAIL;
            }

            for (String guid : entityGuids) {
                processEntity(guid, context);
            }

            while (!context.guidsToProcess.isEmpty()) {
                while (!context.guidsToProcess.isEmpty()) {
                    String guid = context.guidsToProcess.remove(0);
                    processEntity(guid, context);
                }

                if (!context.lineageToProcess.isEmpty()) {
                    context.guidsToProcess.addAll(context.lineageToProcess);
                    context.lineageProcessed.addAll(context.lineageToProcess.getList());
                    context.lineageToProcess.clear();
                }
            }
        } catch (AtlasBaseException excp) {
            LOG.error("Fetching entity failed for: {}", item, excp);
            return AtlasExportResult.OperationStatus.FAIL;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== processObjectId({})", item);
        }

        return AtlasExportResult.OperationStatus.SUCCESS;
    }

    private List<String> getStartingEntity(AtlasObjectId item, ExportContext context) throws AtlasBaseException {
        List<String> ret = null;

        if (StringUtils.isNotEmpty(item.getGuid())) {
            ret = Collections.singletonList(item.getGuid());
        } else if (StringUtils.equalsIgnoreCase(context.matchType, MATCH_TYPE_FOR_TYPE) && StringUtils.isNotEmpty(item.getTypeName())) {
            final String queryTemplate = getQueryTemplateForMatchType(context);

            setupBindingsForTypeName(context, item.getTypeName());

            ret = executeGremlinQueryForGuids(queryTemplate, context);
        } else if (StringUtils.isNotEmpty(item.getTypeName()) && MapUtils.isNotEmpty(item.getUniqueAttributes())) {
            final String          queryTemplate = getQueryTemplateForMatchType(context);
            final String          typeName      = item.getTypeName();
            final AtlasEntityType entityType    = typeRegistry.getEntityTypeByName(typeName);

            if (entityType == null) {
                throw new AtlasBaseException(AtlasErrorCode.UNKNOWN_TYPENAME, typeName);
            }

            for (Map.Entry<String, Object> e : item.getUniqueAttributes().entrySet()) {
                String attrName  = e.getKey();
                Object attrValue = e.getValue();

                AtlasAttribute attribute = entityType.getAttribute(attrName);
                if (attribute == null || attrValue == null) {
                    continue;
                }

                setupBindingsForTypeNameAttrNameAttrValue(context, typeName, attrValue, attribute);

                List<String> guids = executeGremlinQueryForGuids(queryTemplate, context);

                if (CollectionUtils.isNotEmpty(guids)) {
                    if (ret == null) {
                        ret = new ArrayList<>();
                    }

                    for (String guid : guids) {
                        if (!ret.contains(guid)) {
                            ret.add(guid);
                        }
                    }
                }
            }
        }

        if (ret == null) {
            ret = Collections.emptyList();
        }

        logInfoStartingEntitiesFound(item, context, ret);
        return ret;
    }

    private void logInfoStartingEntitiesFound(AtlasObjectId item, ExportContext context, List<String> ret) {
        LOG.info("export(item={}; matchType={}, fetchType={}): found {} entities", item, context.matchType, context.fetchType, ret.size());
    }

    private void setupBindingsForTypeName(ExportContext context, String typeName) {
        context.bindings.clear();
        context.bindings.put("typeName", new HashSet<String>(Arrays.asList(StringUtils.split(typeName,","))));
    }

    private void setupBindingsForTypeNameAttrNameAttrValue(ExportContext context,
                                                           String typeName, Object attrValue, AtlasAttribute attribute) {
        context.bindings.clear();
        context.bindings.put("typeName", typeName);
        context.bindings.put("attrName", attribute.getQualifiedName());
        context.bindings.put("attrValue", attrValue);
    }

    private String getQueryTemplateForMatchType(ExportContext context) {
        if (StringUtils.equalsIgnoreCase(context.matchType, MATCH_TYPE_STARTS_WITH)) {
            return gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_TYPE_STARTS_WITH);
        }

        if (StringUtils.equalsIgnoreCase(context.matchType, MATCH_TYPE_ENDS_WITH)) {
            return gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_TYPE_ENDS_WITH);
        }

        if (StringUtils.equalsIgnoreCase(context.matchType, MATCH_TYPE_CONTAINS)) {
            return gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_TYPE_CONTAINS);
        }

        if (StringUtils.equalsIgnoreCase(context.matchType, MATCH_TYPE_MATCHES)) {
            return gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_TYPE_MATCHES);
        }

        if (StringUtils.equalsIgnoreCase(context.matchType, MATCH_TYPE_FOR_TYPE)) {
            return gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_TYPE_ALL_FOR_TYPE);
        }

        return gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_TYPE_DEFAULT);
    }

    private void processEntity(String guid, ExportContext context) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> processEntity({})", guid);
        }

        if (!context.guidsProcessed.contains(guid)) {
            TraversalDirection      direction         = context.guidDirection.get(guid);
            AtlasEntityWithExtInfo  entityWithExtInfo = entityGraphRetriever.toAtlasEntityWithExtInfo(guid);

            if(!context.lineageProcessed.contains(guid)) {
                context.result.getData().getEntityCreationOrder().add(entityWithExtInfo.getEntity().getGuid());
            }

            addEntity(entityWithExtInfo, context);
            addTypes(entityWithExtInfo.getEntity(), context);

            context.guidsProcessed.add(entityWithExtInfo.getEntity().getGuid());
            getConntedEntitiesBasedOnOption(entityWithExtInfo.getEntity(), context, direction);

            if(entityWithExtInfo.getReferredEntities() != null) {
                for (AtlasEntity e : entityWithExtInfo.getReferredEntities().values()) {
                    addTypes(e, context);
                    getConntedEntitiesBasedOnOption(e, context, direction);
                }

                context.guidsProcessed.addAll(entityWithExtInfo.getReferredEntities().keySet());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== processEntity({})", guid);
        }
    }

    private void getConntedEntitiesBasedOnOption(AtlasEntity entity, ExportContext context, TraversalDirection direction) throws AtlasBaseException {
        switch (context.fetchType) {
            case CONNECTED:
                getEntityGuidsForConnectedFetch(entity, context, direction);
                break;

            case FULL:
            default:
                getEntityGuidsForFullFetch(entity, context);
        }
    }

    private void getEntityGuidsForConnectedFetch(AtlasEntity entity, ExportContext context, TraversalDirection direction) throws AtlasBaseException {
        if (direction == null || direction == TraversalDirection.UNKNOWN) {
            getConnectedEntityGuids(entity, context, TraversalDirection.OUTWARD, TraversalDirection.INWARD);
        } else {
            if (isProcessEntity(entity)) {
                direction = TraversalDirection.OUTWARD;
            }

            getConnectedEntityGuids(entity, context, direction);
        }
    }

    private boolean isProcessEntity(AtlasEntity entity) throws AtlasBaseException {
        String          typeName   = entity.getTypeName();
        AtlasEntityType entityType = typeRegistry.getEntityTypeByName(typeName);

        return entityType.isSubTypeOf(BaseAtlasBaseTypeDef.ATLAS_TYPE_PROCESS);
    }

    private void getConnectedEntityGuids(AtlasEntity entity, ExportContext context, TraversalDirection... directions) {
        if(directions == null) {
            return;
        }

        for (TraversalDirection direction : directions) {
            String query = getQueryForTraversalDirection(direction);

            if (LOG.isDebugEnabled()) {
                LOG.debug("==> getConnectedEntityGuids({}): guidsToProcess {} query {}", AtlasTypeUtil.getAtlasObjectId(entity), context.guidsToProcess.size(), query);
            }

            context.bindings.clear();
            context.bindings.put("startGuid", entity.getGuid());

            List<Map<String, Object>> result = executeGremlinQuery(query, context);

            if (CollectionUtils.isEmpty(result)) {
                continue;
            }

            for (Map<String, Object> map : result) {
                String             guid             = (String) map.get("__guid");
                TraversalDirection currentDirection = context.guidDirection.get(guid);
                boolean            isLineage        = (boolean) map.get("isProcess");

                if (currentDirection == null) {
                    context.addToBeProcessed(isLineage, guid, direction);

                } else if (currentDirection == TraversalDirection.OUTWARD && direction == TraversalDirection.INWARD) {
                    // the entity should be reprocessed to get inward entities
                    context.guidsProcessed.remove(guid);
                    context.addToBeProcessed(isLineage, guid, direction);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== getConnectedEntityGuids({}): found {} guids; guidsToProcess {}", entity.getGuid(), result.size(), context.guidsToProcess.size());
            }
        }
    }

    private String getQueryForTraversalDirection(TraversalDirection direction) {
        switch (direction) {
            case INWARD:
                return this.gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_BY_GUID_CONNECTED_IN_EDGE);

            default:
            case OUTWARD:
                return this.gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_BY_GUID_CONNECTED_OUT_EDGE);
        }
    }

    private void getEntityGuidsForFullFetch(AtlasEntity entity, ExportContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getEntityGuidsForFullFetch({}): guidsToProcess {}", AtlasTypeUtil.getAtlasObjectId(entity), context.guidsToProcess.size());
        }

        String query = this.gremlinQueryProvider.getQuery(AtlasGremlinQuery.EXPORT_BY_GUID_FULL);

        context.bindings.clear();
        context.bindings.put("startGuid", entity.getGuid());

        List<Map<String, Object>> result = executeGremlinQuery(query, context);

        if (CollectionUtils.isEmpty(result)) {
            return;
        }

        for (Map<String, Object> map : result) {
            String  guid      = (String) map.get("__guid");
            boolean isLineage = (boolean) map.get("isProcess");

            if (!context.guidsProcessed.contains(guid)) {
                context.addToBeProcessed(isLineage, guid, TraversalDirection.BOTH);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getEntityGuidsForFullFetch({}): found {} guids; guidsToProcess {}", entity.getGuid(), result.size(), context.guidsToProcess.size());
        }
    }

    private void addEntity(AtlasEntityWithExtInfo entity, ExportContext context) throws AtlasBaseException {
        if(context.sink.hasEntity(entity.getEntity().getGuid())) {
            return;
        }

        context.sink.add(entity);

        context.result.incrementMeticsCounter(String.format("entity:%s", entity.getEntity().getTypeName()));
        if(entity.getReferredEntities() != null) {
            for (AtlasEntity e: entity.getReferredEntities().values()) {
                context.result.incrementMeticsCounter(String.format("entity:%s", e.getTypeName()));
            }
        }

        context.result.incrementMeticsCounter("entity:withExtInfo");
        context.reportProgress();
    }

    private void addTypes(AtlasEntity entity, ExportContext context) {
        addEntityType(entity.getTypeName(), context);

        if(CollectionUtils.isNotEmpty(entity.getClassifications())) {
            for (AtlasClassification c : entity.getClassifications()) {
                addClassificationType(c.getTypeName(), context);
            }
        }
    }

    private void addType(String typeName, ExportContext context) {
        BaseAtlasType type = null;

        try {
            type = typeRegistry.getType(typeName);

            addType(type, context);
        } catch (AtlasBaseException excp) {
            LOG.error("unknown type {}", typeName);
        }
    }

    private void addEntityType(String typeName, ExportContext context) {
        if (!context.entityTypes.contains(typeName)) {
            AtlasEntityType entityType = typeRegistry.getEntityTypeByName(typeName);

            addEntityType(entityType, context);
        }
    }

    private void addClassificationType(String typeName, ExportContext context) {
        if (!context.classificationTypes.contains(typeName)) {
            AtlasClassificationType classificationType = typeRegistry.getClassificationTypeByName(typeName);

            addClassificationType(classificationType, context);
        }
    }

    private void addType(BaseAtlasType type, ExportContext context) {
        if (type.getTypeCategory() == TypeCategory.PRIMITIVE) {
            return;
        }

        if (type instanceof AtlasArrayType) {
            AtlasArrayType arrayType = (AtlasArrayType)type;

            addType(arrayType.getElementType(), context);
        } else if (type instanceof AtlasMapType) {
            AtlasMapType mapType = (AtlasMapType)type;

            addType(mapType.getKeyType(), context);
            addType(mapType.getValueType(), context);
        } else if (type instanceof AtlasEntityType) {
            addEntityType((AtlasEntityType)type, context);
        } else if (type instanceof AtlasClassificationType) {
            addClassificationType((AtlasClassificationType)type, context);
        } else if (type instanceof AtlasStructType) {
            addStructType((AtlasStructType)type, context);
        } else if (type instanceof AtlasEnumType) {
            addEnumType((AtlasEnumType)type, context);
        }
    }

    private void addEntityType(AtlasEntityType entityType, ExportContext context) {
        if (!context.entityTypes.contains(entityType.getTypeName())) {
            context.entityTypes.add(entityType.getTypeName());

            addAttributeTypes(entityType, context);

            if (CollectionUtils.isNotEmpty(entityType.getAllSuperTypes())) {
                for (String superType : entityType.getAllSuperTypes()) {
                    addEntityType(superType, context);
                }
            }
        }
    }

    private void addClassificationType(AtlasClassificationType classificationType, ExportContext context) {
        if (!context.classificationTypes.contains(classificationType.getTypeName())) {
            context.classificationTypes.add(classificationType.getTypeName());

            addAttributeTypes(classificationType, context);

            if (CollectionUtils.isNotEmpty(classificationType.getAllSuperTypes())) {
                for (String superType : classificationType.getAllSuperTypes()) {
                    addClassificationType(superType, context);
                }
            }
        }
    }

    private void addStructType(AtlasStructType structType, ExportContext context) {
        if (!context.structTypes.contains(structType.getTypeName())) {
            context.structTypes.add(structType.getTypeName());

            addAttributeTypes(structType, context);
        }
    }

    private void addEnumType(AtlasEnumType enumType, ExportContext context) {
        if (!context.enumTypes.contains(enumType.getTypeName())) {
            context.enumTypes.add(enumType.getTypeName());
        }
    }

    private void addAttributeTypes(AtlasStructType structType, ExportContext context) {
        for (AtlasAttributeDef attributeDef : structType.getStructDef().getAttributeDefs()) {
            addType(attributeDef.getTypeName(), context);
        }
    }

    private List<Map<String, Object>> executeGremlinQuery(String query, ExportContext context) {
        try {
            return (List<Map<String, Object>>) atlasGraph.executeGremlinScript(context.scriptEngine, context.bindings, query, false);
        } catch (ScriptException e) {
            LOG.error("Script execution failed for query: ", query, e);
            return null;
        }
    }

    private List<String> executeGremlinQueryForGuids(String query, ExportContext context) {
        try {
            return (List<String>) atlasGraph.executeGremlinScript(context.scriptEngine, context.bindings, query, false);
        } catch (ScriptException e) {
            LOG.error("Script execution failed for query: ", query, e);
            return null;
        }
    }

    private enum TraversalDirection {
        UNKNOWN,
        INWARD,
        OUTWARD,
        BOTH;
    }


    public enum ExportFetchType {
        FULL(FETCH_TYPE_FULL),
        CONNECTED(FETCH_TYPE_CONNECTED);

        final String str;
        ExportFetchType(String s) {
            this.str = s;
        }

        public static final ExportFetchType from(String s) {
            for (ExportFetchType b : ExportFetchType.values()) {
                if (b.str.equalsIgnoreCase(s)) {
                    return b;
                }
            }

            return FULL;
        }
    }

    public static class UniqueList<T> {
        private final List<T>   list = new ArrayList<>();
        private final Set<T>    set = new HashSet<>();

        public void add(T e) {
            if(set.contains(e)) {
                return;
            }

            list.add(e);
            set.add(e);
        }

        public void addAll(UniqueList<T> uniqueList) {
            for (T item : uniqueList.list) {
                if(set.contains(item)) continue;

                set.add(item);
                list.add(item);
            }
        }

        public T remove(int index) {
            T e = list.remove(index);
            set.remove(e);
            return e;
        }

        public boolean contains(T e) {
            return set.contains(e);
        }

        public int size() {
            return list.size();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public void clear() {
            list.clear();
            set.clear();
        }

        public List<T> getList() {
            return list;
        }
    }


    private class ExportContext {
        final Set<String>                     guidsProcessed = new HashSet<>();
        final UniqueList<String>              guidsToProcess = new UniqueList<>();
        final UniqueList<String>              lineageToProcess = new UniqueList<>();
        final Set<String>                     lineageProcessed = new HashSet<>();
        final Map<String, TraversalDirection> guidDirection  = new HashMap<>();
        final Set<String>                     entityTypes         = new HashSet<>();
        final Set<String>                     classificationTypes = new HashSet<>();
        final Set<String>                     structTypes         = new HashSet<>();
        final Set<String>                     enumTypes           = new HashSet<>();
        final AtlasExportResult               result;
        final ZipSink                         sink;

        private final ScriptEngine        scriptEngine;
        private final Map<String, Object> bindings;
        private final ExportFetchType     fetchType;
        private final String              matchType;

        private       int                 progressReportCount = 0;

        ExportContext(AtlasExportResult result, ZipSink sink) throws AtlasBaseException {
            this.result = result;
            this.sink   = sink;

            scriptEngine = atlasGraph.getGremlinScriptEngine();
            bindings     = new HashMap<>();
            fetchType    = getFetchType(result.getRequest());
            matchType    = getMatchType(result.getRequest());
        }

        private ExportFetchType getFetchType(AtlasExportRequest request) {
            Object fetchOption = request.getOptions() != null ? request.getOptions().get(OPTION_FETCH_TYPE) : null;

            if (fetchOption instanceof String) {
                return ExportFetchType.from((String) fetchOption);
            } else if (fetchOption instanceof ExportFetchType) {
                return (ExportFetchType) fetchOption;
            }

            return ExportFetchType.FULL;
        }

        private String getMatchType(AtlasExportRequest request) {
            String matchType = null;

            if (MapUtils.isNotEmpty(request.getOptions())) {
                if (request.getOptions().get(OPTION_ATTR_MATCH_TYPE) != null) {
                    matchType = request.getOptions().get(OPTION_ATTR_MATCH_TYPE).toString();
                }
            }

            return matchType;
        }

        public void clear() {
            guidsToProcess.clear();
            guidsProcessed.clear();
            guidDirection.clear();
        }

        public void addToBeProcessed(boolean isSuperTypeProcess, String guid, TraversalDirection direction) {
            if(!isSuperTypeProcess) {
                guidsToProcess.add(guid);
            }

            if(isSuperTypeProcess) {
                lineageToProcess.add(guid);
            }

            guidDirection.put(guid, direction);
        }

        public void reportProgress() {

            int count = 1000;
            if ((guidsProcessed.size() - progressReportCount) > count) {
                progressReportCount = guidsProcessed.size();

                LOG.info("export(): in progress.. number of entities exported: {}", this.guidsProcessed.size());
            }
        }
    }
}
