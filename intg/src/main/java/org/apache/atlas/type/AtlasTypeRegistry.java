/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas.type;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.typedef.BaseAtlasBaseTypeDef;
import org.apache.atlas.model.typedef.AtlasClassificationDef;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.model.typedef.AtlasEnumDef;
import org.apache.atlas.model.typedef.AtlasRelationshipDef;
import org.apache.atlas.model.typedef.AtlasStructDef;
import org.apache.atlas.model.typedef.AtlasTypesDef;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.atlas.model.typedef.BaseAtlasBaseTypeDef.*;
/**
 * registry for all types defined in Atlas.
 */
@Singleton
@Component
public class AtlasTypeRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasTypeRegistry.class);
    private static final int    DEFAULT_LOCK_MAX_WAIT_TIME_IN_SECONDS = 15;

    protected       RegistryData                   registryData;
    private   final TypeRegistryUpdateSynchronizer updateSynchronizer;
    private   final Set<String>                    missingRelationshipDefs;


    public AtlasTypeRegistry() {
        registryData            = new RegistryData();
        updateSynchronizer      = new TypeRegistryUpdateSynchronizer(this);
        missingRelationshipDefs = new HashSet<>();
    }

    // used only by AtlasTransientTypeRegistry
    protected AtlasTypeRegistry(AtlasTypeRegistry other) {
        registryData            = new RegistryData();
        updateSynchronizer      = other.updateSynchronizer;
        missingRelationshipDefs = other.missingRelationshipDefs;
    }

    public Collection<String> getAllTypeNames() { return registryData.allTypes.getAllTypeNames(); }

    public Collection<BaseAtlasType> getAllTypes() { return registryData.allTypes.getAllTypes(); }

    public Set<String> getAllServiceTypes() { return registryData.allTypes.getAllServiceTypes(); }

    public boolean isRegisteredType(String typeName) {
        return registryData.allTypes.isKnownType(typeName);
    }

    public BaseAtlasType getType(String typeName) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AtlasTypeRegistry.getType({})", typeName);
        }

        BaseAtlasType ret = registryData.allTypes.getTypeByName(typeName);

        if (ret == null) {
            if (typeName.startsWith(ATLAS_TYPE_ARRAY_PREFIX) && typeName.endsWith(ATLAS_TYPE_ARRAY_SUFFIX)) {
                int    startIdx        = ATLAS_TYPE_ARRAY_PREFIX.length();
                int    endIdx          = typeName.length() - ATLAS_TYPE_ARRAY_SUFFIX.length();
                String elementTypeName = typeName.substring(startIdx, endIdx);

                ret = new AtlasArrayType(elementTypeName, this);
            } else if (typeName.startsWith(ATLAS_TYPE_MAP_PREFIX) && typeName.endsWith(ATLAS_TYPE_MAP_SUFFIX)) {
                int      startIdx      = ATLAS_TYPE_MAP_PREFIX.length();
                int      endIdx        = typeName.length() - ATLAS_TYPE_MAP_SUFFIX.length();
                String[] keyValueTypes = typeName.substring(startIdx, endIdx).split(ATLAS_TYPE_MAP_KEY_VAL_SEP, 2);
                String   keyTypeName   = keyValueTypes.length > 0 ? keyValueTypes[0] : null;
                String   valueTypeName = keyValueTypes.length > 1 ? keyValueTypes[1] : null;

                ret = new AtlasMapType(keyTypeName, valueTypeName, this);
            } else {
                LOG.warn("?????????typeName{}", typeName);
                throw new AtlasBaseException(AtlasErrorCode.TYPE_NAME_NOT_FOUND, typeName);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AtlasTypeRegistry.getType({}): {}", typeName, ret);
        }

        return ret;
    }

    public BaseAtlasType getTypeByGuid(String guid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> AtlasTypeRegistry.getTypeByGuid({})", guid);
        }

        BaseAtlasType ret = registryData.allTypes.getTypeByGuid(guid);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== AtlasTypeRegistry.getTypeByGuid({}): {}", guid, ret);
        }

        return ret;
    }

    public BaseAtlasBaseTypeDef getTypeDefByName(String name) { return registryData.getTypeDefByName(name); }

    public BaseAtlasBaseTypeDef getTypeDefByGuid(String guid) { return registryData.getTypeDefByGuid(guid); }


    public Collection<AtlasEnumDef> getAllEnumDefs() { return registryData.enumDefs.getAll(); }

    public AtlasEnumDef getEnumDefByGuid(String guid) {
        return registryData.enumDefs.getTypeDefByGuid(guid);
    }

    public AtlasEnumDef getEnumDefByName(String name) {
        return registryData.enumDefs.getTypeDefByName(name);
    }

    public Collection<String> getAllEnumDefNames() { return registryData.enumDefs.getAllNames(); }

    public Collection<AtlasEnumType> getAllEnumTypes() { return registryData.enumDefs.getAllTypes(); }

    public AtlasEnumType getEnumTypeByName(String name) { return registryData.enumDefs.getTypeByName(name); }


    public Collection<AtlasStructDef> getAllStructDefs() { return registryData.structDefs.getAll(); }

    public AtlasStructDef getStructDefByGuid(String guid) {
        return registryData.structDefs.getTypeDefByGuid(guid);
    }

    public AtlasStructDef getStructDefByName(String name) { return registryData.structDefs.getTypeDefByName(name); }

    public Collection<String> getAllStructDefNames() { return registryData.structDefs.getAllNames(); }

    public Collection<AtlasStructType> getAllStructTypes() { return registryData.structDefs.getAllTypes(); }

    public AtlasStructType getStructTypeByName(String name) { return registryData.structDefs.getTypeByName(name); }


    public Collection<AtlasClassificationDef> getAllClassificationDefs() {
        return registryData.classificationDefs.getAll();
    }

    public AtlasClassificationDef getClassificationDefByGuid(String guid) {
        return registryData.classificationDefs.getTypeDefByGuid(guid);
    }

    public AtlasClassificationDef getClassificationDefByName(String name) {
        return registryData.classificationDefs.getTypeDefByName(name);
    }

    public Collection<String> getAllClassificationDefNames() { return registryData.classificationDefs.getAllNames(); }

    public Collection<AtlasClassificationType> getAllClassificationTypes() {
        return registryData.classificationDefs.getAllTypes();
    }

    public AtlasClassificationType getClassificationTypeByName(String name) {
        return registryData.classificationDefs.getTypeByName(name);
    }

    public Collection<AtlasRelationshipDef> getAllRelationshipDefs() { return registryData.relationshipDefs.getAll(); }

    public Collection<AtlasEntityDef> getAllEntityDefs() { return registryData.entityDefs.getAll(); }
    public AtlasEntityDef getEntityDefByGuid(String guid) {
        return registryData.entityDefs.getTypeDefByGuid(guid);
    }

    public AtlasEntityDef getEntityDefByName(String name) {
        return registryData.entityDefs.getTypeDefByName(name);
    }

    public Collection<String> getAllEntityDefNames() { return registryData.entityDefs.getAllNames(); }
    public Collection<AtlasEntityType> getAllEntityTypes() { return registryData.entityDefs.getAllTypes(); }
    public AtlasEntityType getEntityTypeByName(String name) { return registryData.entityDefs.getTypeByName(name); }
    /**
     * @return relationshipTypes
     */
    public Collection<AtlasRelationshipType> getAllRelationshipTypes() { return registryData.relationshipDefs.getAllTypes(); }

    public AtlasRelationshipDef getRelationshipDefByGuid(String guid) {
        return registryData.relationshipDefs.getTypeDefByGuid(guid);
    }
    public AtlasRelationshipDef getRelationshipDefByName(String name) {
        return registryData.relationshipDefs.getTypeDefByName(name);
    }
    public AtlasRelationshipType getRelationshipTypeByName(String name) { return registryData.relationshipDefs.getTypeByName(name); }

    public AtlasTransientTypeRegistry lockTypeRegistryForUpdate() throws AtlasBaseException {
        return lockTypeRegistryForUpdate(DEFAULT_LOCK_MAX_WAIT_TIME_IN_SECONDS);
    }

    public AtlasTransientTypeRegistry lockTypeRegistryForUpdate(int lockMaxWaitTimeInSeconds) throws AtlasBaseException {
        return updateSynchronizer.lockTypeRegistryForUpdate(lockMaxWaitTimeInSeconds);
    }

    public void releaseTypeRegistryForUpdate(AtlasTransientTypeRegistry transientTypeRegistry, boolean commitUpdates) {
        updateSynchronizer.releaseTypeRegistryForUpdate(transientTypeRegistry, commitUpdates);
    }

    public void reportMissingRelationshipDef(String entityType1, String entityType2, String attributeName) {
        String key = entityType1 + "->" + entityType2 + ":" + attributeName;

        if (!missingRelationshipDefs.contains(key)) {
            LOG.warn("No RelationshipDef defined between {} and {} on attribute: {}.{}", entityType1, entityType2, entityType1, attributeName);

            missingRelationshipDefs.add(key);
        }
    }

    static class RegistryData {
        final TypeCache                                                       allTypes;
        final TypeDefCache<AtlasEnumDef, AtlasEnumType>                       enumDefs;
        final TypeDefCache<AtlasStructDef, AtlasStructType>                   structDefs;
        final TypeDefCache<AtlasClassificationDef, AtlasClassificationType>   classificationDefs;
        final TypeDefCache<AtlasEntityDef, AtlasEntityType>                   entityDefs;
        final TypeDefCache<AtlasRelationshipDef, AtlasRelationshipType>       relationshipDefs;
        final TypeDefCache<? extends BaseAtlasBaseTypeDef, ? extends BaseAtlasType>[] allDefCaches;

        RegistryData() {
            allTypes           = new TypeCache();
            enumDefs           = new TypeDefCache<>(allTypes);
            structDefs         = new TypeDefCache<>(allTypes);
            classificationDefs = new TypeDefCache<>(allTypes);
            entityDefs         = new TypeDefCache<>(allTypes);
            relationshipDefs   = new TypeDefCache<>(allTypes);
            allDefCaches       = new TypeDefCache[] { enumDefs, structDefs, classificationDefs, entityDefs, relationshipDefs };

            init();
        }

        void init() {
            allTypes.addType(new AtlasBuiltInTypes.AtlasBooleanType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasByteType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasShortType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasIntType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasLongType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasFloatType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasDoubleType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasBigIntegerType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasBigDecimalType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasDateType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasStringType());
            allTypes.addType(new AtlasBuiltInTypes.AtlasObjectIdType());
        }

        BaseAtlasBaseTypeDef getTypeDefByName(String name) {
            BaseAtlasBaseTypeDef ret = null;

            if (name != null) {
                for (TypeDefCache typeDefCache : allDefCaches) {
                    ret = typeDefCache.getTypeDefByName(name);

                    if (ret != null) {
                        break;
                    }
                }
            }

            return ret;
        }

        BaseAtlasBaseTypeDef getTypeDefByGuid(String guid) {
            BaseAtlasBaseTypeDef ret = null;

            if (guid != null) {
                for (TypeDefCache typeDefCache : allDefCaches) {
                    ret = typeDefCache.getTypeDefByGuid(guid);

                    if (ret != null) {
                        break;
                    }
                }
            }

            return ret;
        }

        void updateGuid(String typeName, String guid) {
            if (typeName != null) {
                enumDefs.updateGuid(typeName, guid);
                structDefs.updateGuid(typeName, guid);
                classificationDefs.updateGuid(typeName, guid);
                entityDefs.updateGuid(typeName, guid);
                relationshipDefs.updateGuid(typeName, guid);
            }
        }

        void removeByGuid(String guid) {
            if (guid != null) {
                enumDefs.removeTypeDefByGuid(guid);
                structDefs.removeTypeDefByGuid(guid);
                classificationDefs.removeTypeDefByGuid(guid);
                entityDefs.removeTypeDefByGuid(guid);
                relationshipDefs.removeTypeDefByGuid(guid);
            }
        }

        void removeByName(String typeName) {
            if (typeName != null) {
                enumDefs.removeTypeDefByName(typeName);
                structDefs.removeTypeDefByName(typeName);
                classificationDefs.removeTypeDefByName(typeName);
                entityDefs.removeTypeDefByName(typeName);
                relationshipDefs.removeTypeDefByName(typeName);
            }
        }

        void clear() {
            allTypes.clear();
            enumDefs.clear();
            structDefs.clear();
            classificationDefs.clear();
            entityDefs.clear();
            relationshipDefs.clear();

            init();
        }
    }

    public static class AtlasTransientTypeRegistry extends AtlasTypeRegistry {
        private List<BaseAtlasBaseTypeDef> addedTypes   = new ArrayList<>();
        private List<BaseAtlasBaseTypeDef> updatedTypes = new ArrayList<>();
        private List<BaseAtlasBaseTypeDef> deletedTypes = new ArrayList<>();


        private AtlasTransientTypeRegistry(AtlasTypeRegistry parent) throws AtlasBaseException {
            super(parent);

            addTypesWithNoRefResolve(parent.getAllEnumDefs());
            addTypesWithNoRefResolve(parent.getAllStructDefs());
            addTypesWithNoRefResolve(parent.getAllClassificationDefs());
            addTypesWithNoRefResolve(parent.getAllEntityDefs());
            addTypesWithNoRefResolve(parent.getAllRelationshipDefs());

            addedTypes.clear();
            updatedTypes.clear();
            deletedTypes.clear();
        }

        private void resolveReferences() throws AtlasBaseException {
            for (BaseAtlasType type : registryData.allTypes.getAllTypes()) {
                type.resolveReferences(this);
            }

            for (BaseAtlasType type : registryData.allTypes.getAllTypes()) {
                type.resolveReferencesPhase2(this);
            }

            for (BaseAtlasType type : registryData.allTypes.getAllTypes()) {
                type.resolveReferencesPhase3(this);
            }
        }

        public void clear() {
            registryData.clear();
        }

        public void addType(BaseAtlasBaseTypeDef typeDef) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.addType({})", typeDef);
            }

            if (typeDef != null) {
                addTypeWithNoRefResolve(typeDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.addType({})", typeDef);
            }
        }

        public void updateGuid(String typeName, String guid) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateGuid({}, {})", typeName, guid);
            }

            registryData.updateGuid(typeName, guid);

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateGuid({}, {})", typeName, guid);
            }
        }

        public void addTypes(Collection<? extends BaseAtlasBaseTypeDef> typeDefs) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.addTypes(length={})", (typeDefs == null ? 0 : typeDefs.size()));
            }

            if (CollectionUtils.isNotEmpty(typeDefs)) {
                addTypesWithNoRefResolve(typeDefs);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.addTypes(length={})", (typeDefs == null ? 0 : typeDefs.size()));
            }
        }

        public void addTypes(AtlasTypesDef typesDef) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.addTypes({})", typesDef);
            }

            if (typesDef != null) {
                addTypesWithNoRefResolve(typesDef.getEnumDefs());
                addTypesWithNoRefResolve(typesDef.getStructDefs());
                addTypesWithNoRefResolve(typesDef.getClassificationDefs());
                addTypesWithNoRefResolve(typesDef.getEntityDefs());
                addTypesWithNoRefResolve(typesDef.getRelationshipDefs());
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.addTypes({})", typesDef);
            }
        }

        public void updateType(BaseAtlasBaseTypeDef typeDef) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateType({})", typeDef);
            }

            if (typeDef != null) {
                updateTypeWithNoRefResolve(typeDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateType({})", typeDef);
            }
        }

        public void updateTypeByGuid(String guid, BaseAtlasBaseTypeDef typeDef) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateTypeByGuid({})", guid);
            }

            if (guid != null && typeDef != null) {
                updateTypeByGuidWithNoRefResolve(guid, typeDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateTypeByGuid({})", guid);
            }
        }

        public void updateTypeByName(String name, BaseAtlasBaseTypeDef typeDef) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateEnumDefByName({})", name);
            }

            if (name != null && typeDef != null) {
                updateTypeByNameWithNoRefResolve(name, typeDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateEnumDefByName({})", name);
            }
        }

        public void updateTypes(Collection<? extends BaseAtlasBaseTypeDef> typeDefs) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateTypes(length={})", (typeDefs == null ? 0 : typeDefs.size()));
            }

            if (CollectionUtils.isNotEmpty(typeDefs)) {
                updateTypesWithNoRefResolve(typeDefs);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateTypes(length={})", (typeDefs == null ? 0 : typeDefs.size()));
            }
        }

        public void updateTypes(AtlasTypesDef typesDef) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateTypes({})", typesDef);
            }

            if (typesDef != null) {
                updateTypesWithNoRefResolve(typesDef);
            }

            resolveReferences();

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateTypes({})", typesDef);
            }
        }

        public void updateTypesWithNoRefResolve(AtlasTypesDef typesDef) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateTypesWithNoRefResolve({})", typesDef);
            }

            if (typesDef != null) {
                updateTypesWithNoRefResolve(typesDef.getEnumDefs());
                updateTypesWithNoRefResolve(typesDef.getStructDefs());
                updateTypesWithNoRefResolve(typesDef.getClassificationDefs());
                updateTypesWithNoRefResolve(typesDef.getEntityDefs());
                updateTypesWithNoRefResolve(typesDef.getRelationshipDefs());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateTypesWithNoRefResolve({})", typesDef);
            }
        }

        public void removeTypesDef(AtlasTypesDef typesDef) throws AtlasBaseException {
            if (null != typesDef && !typesDef.isEmpty()) {
                removeTypesWithNoRefResolve(typesDef.getEnumDefs());
                removeTypesWithNoRefResolve(typesDef.getStructDefs());
                removeTypesWithNoRefResolve(typesDef.getClassificationDefs());
                removeTypesWithNoRefResolve(typesDef.getEntityDefs());
                removeTypesWithNoRefResolve(typesDef.getRelationshipDefs());
            }

            resolveReferences();
        }

        private void removeTypesWithNoRefResolve(Collection<? extends BaseAtlasBaseTypeDef> typeDefs) {
            if (CollectionUtils.isNotEmpty(typeDefs)) {
                for (BaseAtlasBaseTypeDef typeDef : typeDefs) {
                    if (StringUtils.isNotEmpty(typeDef.getGuid())) {
                        removeTypeByGuidWithNoRefResolve(typeDef);
                    } else {
                        removeTypeByNameWithNoRefResolve(typeDef);
                    }
                }
            }
        }

        private void removeTypeByNameWithNoRefResolve(BaseAtlasBaseTypeDef typeDef) {
            switch (typeDef.getCategory()) {
                case ENUM:
                    registryData.enumDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case STRUCT:
                    registryData.structDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case CLASSIFICATION:
                    registryData.classificationDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case ENTITY:
                    registryData.entityDefs.removeTypeDefByName(typeDef.getName());
                    break;
                case RELATIONSHIP:
                    registryData.relationshipDefs.removeTypeDefByName(typeDef.getName());
                    break;
                default:
                    break;
            }
            deletedTypes.add(typeDef);
        }

        private void removeTypeByGuidWithNoRefResolve(BaseAtlasBaseTypeDef typeDef) {
            switch (typeDef.getCategory()) {
                case ENUM:
                    registryData.enumDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case STRUCT:
                    registryData.structDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case CLASSIFICATION:
                    registryData.classificationDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case ENTITY:
                    registryData.entityDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                case RELATIONSHIP:
                    registryData.relationshipDefs.removeTypeDefByGuid(typeDef.getGuid());
                    break;
                default:
                    break;
            }
            deletedTypes.add(typeDef);
        }

        public void removeTypeByGuid(String guid) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.removeTypeByGuid({})", guid);
            }

            BaseAtlasBaseTypeDef typeDef = getTypeDefByGuid(guid);

            if (guid != null) {
                registryData.removeByGuid(guid);
            }

            resolveReferences();

            if (typeDef != null) {
                deletedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.removeTypeByGuid({})", guid);
            }
        }

        public void removeTypeByName(String name) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.removeTypeByName({})", name);
            }

            BaseAtlasBaseTypeDef typeDef = getTypeDefByName(name);

            if (name != null) {
                registryData.removeByName(name);
            }

            resolveReferences();

            if (typeDef != null) {
                deletedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.removeEnumDefByName({})", name);
            }
        }

        public List<BaseAtlasBaseTypeDef> getAddedTypes() { return addedTypes; }

        public List<BaseAtlasBaseTypeDef> getUpdatedTypes() { return updatedTypes; }

        public List<BaseAtlasBaseTypeDef> getDeleteedTypes() { return deletedTypes; }


        private void addTypeWithNoRefResolve(BaseAtlasBaseTypeDef typeDef) throws AtlasBaseException{
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.addTypeWithNoRefResolve({})", typeDef);
            }

            if (typeDef != null) {
                if (this.isRegisteredType(typeDef.getName())) {
                    throw new AtlasBaseException(AtlasErrorCode.TYPE_ALREADY_EXISTS, typeDef.getName());
                }

                if (typeDef.getClass().equals(AtlasEnumDef.class)) {
                    AtlasEnumDef enumDef = (AtlasEnumDef) typeDef;

                    registryData.enumDefs.addType(enumDef, new AtlasEnumType(enumDef));
                } else if (typeDef.getClass().equals(AtlasStructDef.class)) {
                    AtlasStructDef structDef = (AtlasStructDef) typeDef;

                    registryData.structDefs.addType(structDef, new AtlasStructType(structDef));
                } else if (typeDef.getClass().equals(AtlasClassificationDef.class)) {
                    AtlasClassificationDef classificationDef = (AtlasClassificationDef) typeDef;

                    registryData.classificationDefs.addType(classificationDef,
                            new AtlasClassificationType(classificationDef));
                } else if (typeDef.getClass().equals(AtlasEntityDef.class)) {
                    AtlasEntityDef entityDef = (AtlasEntityDef) typeDef;

                    registryData.entityDefs.addType(entityDef, new AtlasEntityType(entityDef));
                } else if (typeDef.getClass().equals(AtlasRelationshipDef.class)) {
                    AtlasRelationshipDef relationshipDef = (AtlasRelationshipDef) typeDef;

                    registryData.relationshipDefs.addType(relationshipDef, new AtlasRelationshipType(relationshipDef));
                }

                addedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.addTypeWithNoRefResolve({})", typeDef);
            }
        }

        private void addTypesWithNoRefResolve(Collection<? extends BaseAtlasBaseTypeDef> typeDefs) throws AtlasBaseException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.addTypesWithNoRefResolve(length={})",
                        (typeDefs == null ? 0 : typeDefs.size()));
            }

            if (CollectionUtils.isNotEmpty(typeDefs)) {
                for (BaseAtlasBaseTypeDef typeDef : typeDefs) {
                    addTypeWithNoRefResolve(typeDef);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.addTypesWithNoRefResolve(length={})",
                        (typeDefs == null ? 0 : typeDefs.size()));
            }
        }

        private void updateTypeWithNoRefResolve(BaseAtlasBaseTypeDef typeDef) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateType({})", typeDef);
            }

            if (typeDef != null) {
                if (StringUtils.isNotBlank(typeDef.getGuid())) {
                    updateTypeByGuidWithNoRefResolve(typeDef.getGuid(), typeDef);
                } else if (StringUtils.isNotBlank(typeDef.getName())) {
                    updateTypeByNameWithNoRefResolve(typeDef.getName(), typeDef);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateType({})", typeDef);
            }
        }

        private void updateTypeByGuidWithNoRefResolve(String guid, BaseAtlasBaseTypeDef typeDef) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateTypeByGuidWithNoRefResolve({})", guid);
            }

            if (guid != null && typeDef != null) {
                // ignore
                if (typeDef.getClass().equals(AtlasEnumDef.class)) {
                    AtlasEnumDef enumDef = (AtlasEnumDef) typeDef;

                    registryData.enumDefs.removeTypeDefByGuid(guid);
                    registryData.enumDefs.addType(enumDef, new AtlasEnumType(enumDef));
                } else if (typeDef.getClass().equals(AtlasStructDef.class)) {
                    AtlasStructDef structDef = (AtlasStructDef) typeDef;

                    registryData.structDefs.removeTypeDefByGuid(guid);
                    registryData.structDefs.addType(structDef, new AtlasStructType(structDef));
                } else if (typeDef.getClass().equals(AtlasClassificationDef.class)) {
                    AtlasClassificationDef classificationDef = (AtlasClassificationDef) typeDef;

                    registryData.classificationDefs.removeTypeDefByGuid(guid);
                    registryData.classificationDefs.addType(classificationDef,
                            new AtlasClassificationType(classificationDef));
                } else if (typeDef.getClass().equals(AtlasEntityDef.class)) {
                    AtlasEntityDef entityDef = (AtlasEntityDef) typeDef;

                    registryData.entityDefs.removeTypeDefByGuid(guid);
                    registryData.entityDefs.addType(entityDef, new AtlasEntityType(entityDef));
                } else if (typeDef.getClass().equals(AtlasRelationshipDef.class)) {
                    AtlasRelationshipDef relationshipDef = (AtlasRelationshipDef) typeDef;

                    registryData.relationshipDefs.removeTypeDefByGuid(guid);
                    registryData.relationshipDefs.addType(relationshipDef, new AtlasRelationshipType(relationshipDef));
                }

                updatedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateTypeByGuidWithNoRefResolve({})", guid);
            }
        }

        private void updateTypeByNameWithNoRefResolve(String name, BaseAtlasBaseTypeDef typeDef) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateTypeByNameWithNoRefResolve({})", name);
            }

            if (name != null && typeDef != null) {
                if (typeDef.getClass().equals(AtlasEnumDef.class)) {
                    AtlasEnumDef enumDef = (AtlasEnumDef) typeDef;

                    registryData.enumDefs.removeTypeDefByName(name);
                    registryData.enumDefs.addType(enumDef, new AtlasEnumType(enumDef));
                } else if (typeDef.getClass().equals(AtlasStructDef.class)) {
                    AtlasStructDef structDef = (AtlasStructDef) typeDef;

                    registryData.structDefs.removeTypeDefByName(name);
                    registryData.structDefs.addType(structDef, new AtlasStructType(structDef));
                } else if (typeDef.getClass().equals(AtlasClassificationDef.class)) {
                    AtlasClassificationDef classificationDef = (AtlasClassificationDef) typeDef;

                    registryData.classificationDefs.removeTypeDefByName(name);
                    registryData.classificationDefs.addType(classificationDef,
                            new AtlasClassificationType(classificationDef));
                } else if (typeDef.getClass().equals(AtlasEntityDef.class)) {
                    AtlasEntityDef entityDef = (AtlasEntityDef) typeDef;

                    registryData.entityDefs.removeTypeDefByName(name);
                    registryData.entityDefs.addType(entityDef, new AtlasEntityType(entityDef));
                } else if (typeDef.getClass().equals(AtlasRelationshipDef.class)) {
                    AtlasRelationshipDef relationshipDef = (AtlasRelationshipDef) typeDef;

                    registryData.relationshipDefs.removeTypeDefByName(name);
                    registryData.relationshipDefs.addType(relationshipDef, new AtlasRelationshipType(relationshipDef));
                }

                updatedTypes.add(typeDef);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateTypeByNameWithNoRefResolve({})", name);
            }
        }

        private void updateTypesWithNoRefResolve(Collection<? extends BaseAtlasBaseTypeDef> typeDefs) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("==> AtlasTypeRegistry.updateTypesWithNoRefResolve(length={})",
                        (typeDefs == null ? 0 : typeDefs.size()));
            }

            if (CollectionUtils.isNotEmpty(typeDefs)) {
                for (BaseAtlasBaseTypeDef typeDef : typeDefs) {
                    updateTypeWithNoRefResolve(typeDef);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("<== AtlasTypeRegistry.updateTypesWithNoRefResolve(length={})",
                        (typeDefs == null ? 0 : typeDefs.size()));
            }
        }
    }

    static class TypeRegistryUpdateSynchronizer {
        private final AtlasTypeRegistry typeRegistry;
        private final ReentrantLock     typeRegistryUpdateLock;
        private AtlasTransientTypeRegistry typeRegistryUnderUpdate = null;
        private String                     lockedByThread          = null;

        TypeRegistryUpdateSynchronizer(AtlasTypeRegistry typeRegistry) {
            this.typeRegistry           = typeRegistry;
            this.typeRegistryUpdateLock = new ReentrantLock();
        }

        AtlasTransientTypeRegistry lockTypeRegistryForUpdate(int lockMaxWaitTimeInSeconds) throws AtlasBaseException {
            LOG.debug("==> lockTypeRegistryForUpdate()");

            boolean alreadyLockedByCurrentThread = typeRegistryUpdateLock.isHeldByCurrentThread();

            if (!alreadyLockedByCurrentThread) {
                if (lockedByThread != null) {
                    LOG.info("lockTypeRegistryForUpdate(): waiting for lock to be released by thread {}", lockedByThread);
                }
            } else {
                LOG.warn("lockTypeRegistryForUpdate(): already locked. currentLockCount={}",
                        typeRegistryUpdateLock.getHoldCount());
            }

            try {
                boolean isLocked = typeRegistryUpdateLock.tryLock(lockMaxWaitTimeInSeconds, TimeUnit.SECONDS);

                if (!isLocked) {
                    throw new AtlasBaseException(AtlasErrorCode.FAILED_TO_OBTAIN_TYPE_UPDATE_LOCK);
                }
            } catch (InterruptedException excp) {
                throw new AtlasBaseException(AtlasErrorCode.FAILED_TO_OBTAIN_TYPE_UPDATE_LOCK, excp);
            }

            if (!alreadyLockedByCurrentThread) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("lockTypeRegistryForUpdate(): wait over..got the lock");
                }

                typeRegistryUnderUpdate = new AtlasTransientTypeRegistry(typeRegistry);
                lockedByThread          = Thread.currentThread().getName();
            }

            LOG.debug("<== lockTypeRegistryForUpdate()");

            return typeRegistryUnderUpdate;
        }

        void releaseTypeRegistryForUpdate(AtlasTransientTypeRegistry ttr, boolean commitUpdates) {
            LOG.debug("==> releaseTypeRegistryForUpdate()");

            if (typeRegistryUpdateLock.isHeldByCurrentThread()) {
                try {
                    if (typeRegistryUnderUpdate != ttr) {
                        LOG.error("releaseTypeRegistryForUpdate(): incorrect typeRegistry returned for release" +
                                        ": found=" + ttr + "; expected=" + typeRegistryUnderUpdate,
                                new Exception().fillInStackTrace());
                    } else if (typeRegistryUpdateLock.getHoldCount() == 1) {
                        if (ttr != null && commitUpdates) {
                            typeRegistry.registryData = ttr.registryData;
                        }
                    }

                    if (typeRegistryUpdateLock.getHoldCount() == 1) {
                        lockedByThread          = null;
                        typeRegistryUnderUpdate = null;
                    } else {
                        LOG.warn("releaseTypeRegistryForUpdate(): pendingReleaseCount={}", typeRegistryUpdateLock.getHoldCount() - 1);
                    }
                } finally {
                    typeRegistryUpdateLock.unlock();
                }
            } else {
                LOG.error("releaseTypeRegistryForUpdate(): current thread does not hold the lock",
                        new Exception().fillInStackTrace());
            }

            LOG.debug("<== releaseTypeRegistryForUpdate()");
        }

    }
}

class TypeCache {
    private final Map<String, BaseAtlasType> typeGuidMap;
    private final Map<String, BaseAtlasType> typeNameMap;
    private final Set<String>            serviceTypes;

    public TypeCache() {
        typeGuidMap  = new ConcurrentHashMap<>();
        typeNameMap  = new ConcurrentHashMap<>();
        serviceTypes = new HashSet<>();
    }

    public TypeCache(TypeCache other) {
        typeGuidMap  = new ConcurrentHashMap<>(other.typeGuidMap);
        typeNameMap  = new ConcurrentHashMap<>(other.typeNameMap);
        serviceTypes = new HashSet<>(other.serviceTypes);
    }

    public void addType(BaseAtlasType type) {
        if (type != null) {
            if (StringUtils.isNotEmpty(type.getTypeName())) {
                typeNameMap.put(type.getTypeName(), type);
            }

            if (StringUtils.isNotEmpty(type.getServiceType())) {
                serviceTypes.add(type.getServiceType());
            }
        }
    }

    public void addType(BaseAtlasBaseTypeDef typeDef, BaseAtlasType type) {
        if (typeDef != null && type != null) {
            if (StringUtils.isNotEmpty(typeDef.getGuid())) {
                typeGuidMap.put(typeDef.getGuid(), type);
            }

            if (StringUtils.isNotEmpty(typeDef.getName())) {
                typeNameMap.put(typeDef.getName(), type);
            }

            if (StringUtils.isNotEmpty(type.getServiceType())) {
                serviceTypes.add(type.getServiceType());
            }
        }
    }

    public boolean isKnownType(String typeName) {
        return typeNameMap.containsKey(typeName);
    }

    public Collection<String> getAllTypeNames() {
        return Collections.unmodifiableCollection(typeNameMap.keySet());
    }

    public Collection<BaseAtlasType> getAllTypes() {
        return Collections.unmodifiableCollection(typeNameMap.values());
    }

    public Set<String> getAllServiceTypes() {
        return Collections.unmodifiableSet(serviceTypes);
    }

    public BaseAtlasType getTypeByGuid(String guid) {

        return guid != null ? typeGuidMap.get(guid) : null;
    }

    public BaseAtlasType getTypeByName(String name) {

        return name != null ? typeNameMap.get(name) : null;
    }

    public void updateGuid(String typeName, String currGuid, String newGuid) {
        if (currGuid != null) {
            typeGuidMap.remove(currGuid);
        }

        if (typeName != null && newGuid != null) {
            BaseAtlasType type = typeNameMap.get(typeName);

            if (type != null) {
                typeGuidMap.put(newGuid, type);
            }
        }
    }

    public void removeTypeByGuid(String guid) {
        if (guid != null) {
            typeGuidMap.remove(guid);
        }
    }

    public void removeTypeByName(String name) {
        if (name != null) {
            typeNameMap.remove(name);
        }
    }

    public void clear() {
        typeGuidMap.clear();
        typeNameMap.clear();
    }
}

class TypeDefCache<T1 extends BaseAtlasBaseTypeDef, T2 extends BaseAtlasType> {
    private static final Logger LOG = LoggerFactory.getLogger(TypeDefCache.class);

    private final TypeCache       typeCache;
    private final Map<String, T1> typeDefGuidMap;
    private final Map<String, T1> typeDefNameMap;
    private final Map<String, T2> typeNameMap;

    public TypeDefCache(TypeCache typeCache) {
        this.typeCache      = typeCache;
        this.typeDefGuidMap = new ConcurrentHashMap<>();
        this.typeDefNameMap = new ConcurrentHashMap<>();
        this.typeNameMap    = new ConcurrentHashMap<>();
    }

    public TypeDefCache(TypeDefCache other, TypeCache typeCache) {
        this.typeCache      = typeCache;
        this.typeDefGuidMap = new ConcurrentHashMap<>(other.typeDefGuidMap);
        this.typeDefNameMap = new ConcurrentHashMap<>(other.typeDefNameMap);
        this.typeNameMap    = new ConcurrentHashMap<>(other.typeNameMap);
    }

    public void addType(T1 typeDef, T2 type) {
        if (typeDef != null && type != null) {
            if (StringUtils.isNotEmpty(typeDef.getGuid())) {
                typeDefGuidMap.put(typeDef.getGuid(), typeDef);
            }

            if (StringUtils.isNotEmpty(typeDef.getName())) {
                typeDefNameMap.put(typeDef.getName(), typeDef);
                typeNameMap.put(typeDef.getName(), type);
            }

            typeCache.addType(typeDef, type);
        }
    }

    public Collection<T1> getAll() {
        return Collections.unmodifiableCollection(typeDefNameMap.values());
    }

    public Collection<String> getAllNames() { return Collections.unmodifiableCollection(typeDefNameMap.keySet()); }

    public T1 getTypeDefByGuid(String guid) {
        return guid != null ? typeDefGuidMap.get(guid) : null;
    }

    public T1 getTypeDefByName(String name) {
        return name != null ? typeDefNameMap.get(name) : null;
    }

    public Collection<T2> getAllTypes() {
        return Collections.unmodifiableCollection(typeNameMap.values());
    }

    public T2 getTypeByName(String name) {
        return name != null ? typeNameMap.get(name) : null;
    }

    public void updateGuid(String typeName, String newGuid) {
        if (typeName != null) {
            T1 typeDef = typeDefNameMap.get(typeName);

            if (typeDef != null) {
                String currGuid = typeDef.getGuid();
                if (!typeDefGuidMap.containsKey(newGuid) || !StringUtils.equals(currGuid, newGuid)) {
                    if(LOG.isDebugEnabled()) {
                        if (!typeDefGuidMap.containsKey(newGuid)) {
                            LOG.debug("TypeDefGuidMap doesn't contain entry for guid {}. Adding new entry", newGuid);
                        } else {
                            LOG.debug("Removing entry for guid {} and adding entry for guid {}", currGuid, newGuid);
                        }
                    }
                    if (currGuid != null) {
                        typeDefGuidMap.remove(currGuid);
                    }

                    typeDef.setGuid(newGuid);

                    if (newGuid != null) {
                        typeDefGuidMap.put(newGuid, typeDef);
                    }

                    typeCache.updateGuid(typeName, currGuid, newGuid);
                }
            }
        }
    }

    public void removeTypeDefByGuid(String guid) {
        if (guid != null) {
            T1 typeDef = typeDefGuidMap.remove(guid);

            typeCache.removeTypeByGuid(guid);

            String name = typeDef != null ? typeDef.getName() : null;

            if (name != null) {
                typeDefNameMap.remove(name);
                typeNameMap.remove(name);
                typeCache.removeTypeByName(name);
            }

        }
    }

    public void removeTypeDefByName(String name) {
        if (name != null) {
            T1 typeDef = typeDefNameMap.remove(name);

            typeNameMap.remove(name);
            typeCache.removeTypeByName(name);

            String guid = typeDef != null ? typeDef.getGuid() : null;

            if (guid != null) {
                typeDefGuidMap.remove(guid);
                typeCache.removeTypeByGuid(guid);
            }
        }
    }

    public void clear() {
        typeCache.clear();
        typeDefGuidMap.clear();
        typeDefNameMap.clear();
        typeNameMap.clear();
    }
}
