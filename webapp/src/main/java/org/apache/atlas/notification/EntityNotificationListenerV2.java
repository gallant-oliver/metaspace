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
package org.apache.atlas.notification;

import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.service.MetadataHistoryService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.RequestContext;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.listener.EntityChangeListenerV2;
import org.apache.atlas.model.glossary.AtlasGlossaryTerm;
import org.apache.atlas.model.instance.AtlasClassification;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.notification.EntityNotification.EntityNotificationV2;
import org.apache.atlas.model.notification.EntityNotification.EntityNotificationV2.OperationType;
import org.apache.atlas.type.AtlasClassificationType;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasStructType.AtlasAttribute;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import io.zeta.metaspace.web.service.DataManageService;

import javax.inject.Inject;
import java.util.*;

import static org.apache.atlas.repository.graph.GraphHelper.isInternalType;
import static org.apache.atlas.model.notification.EntityNotification.EntityNotificationV2.OperationType.*;
import static org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever.CREATE_TIME;
import static org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever.DESCRIPTION;
import static org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever.NAME;
import static org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever.OWNER;
import static org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever.QUALIFIED_NAME;

@Component
public class EntityNotificationListenerV2 implements EntityChangeListenerV2 {
    private static final Logger LOG = LoggerFactory.getLogger(EntityNotificationListenerV2.class);

    private final AtlasTypeRegistry                              typeRegistry;
    private final EntityNotificationSender<EntityNotificationV2> notificationSender;

    @Autowired
    DataManageService dataManageService;

    @Lazy
    @Autowired(required = false)
    MetadataHistoryService metadataHistoryService;

    @Inject
    public EntityNotificationListenerV2(AtlasTypeRegistry typeRegistry,
                                        NotificationInterface notificationInterface,
                                        Configuration configuration) {
        this.typeRegistry       = typeRegistry;
        this.notificationSender = new EntityNotificationSender<>(notificationInterface, configuration);
    }

    @Override
    public void onEntitiesAdded(List<AtlasEntity> entities, boolean isImport, SyncTaskDefinition definition, KafkaConnector.Config config) throws AtlasBaseException {
        //notifyEntityEvents(entities, ENTITY_CREATE);
        dataManageService.addEntity(entities, definition, config);
        metadataHistoryService.storeHistoryMetadata(entities);
    }

    @Override
    public void onEntitiesUpdated(List<AtlasEntity> entities, boolean isImport, SyncTaskDefinition definition, KafkaConnector.Config config) throws AtlasBaseException {
        //notifyEntityEvents(entities, ENTITY_UPDATE);
        dataManageService.updateEntityInfo(entities, definition, config);
        metadataHistoryService.storeHistoryMetadata(entities);
    }

    @Override
    public void onEntitiesDeleted(List<AtlasEntity> entities, boolean isImport, SyncTaskDefinition definition, KafkaConnector.Config config) throws AtlasBaseException {
        //notifyEntityEvents(entities, ENTITY_DELETE);
        dataManageService.updateStatus(entities);
        metadataHistoryService.storeHistoryMetadata(entities);
    }

    @Override
    public void onClassificationsAdded(AtlasEntity entity, List<AtlasClassification> classifications) throws AtlasBaseException {
        notifyEntityEvents(Collections.singletonList(entity), CLASSIFICATION_ADD);
    }

    @Override
    public void onClassificationsUpdated(AtlasEntity entity, List<AtlasClassification> classifications) throws AtlasBaseException {
        Map<String, List<AtlasClassification>> addedPropagations   = RequestContext.get().getAddedPropagations();
        Map<String, List<AtlasClassification>> removedPropagations = RequestContext.get().getRemovedPropagations();

        if (addedPropagations.containsKey(entity.getGuid())) {
            notifyEntityEvents(Collections.singletonList(entity), CLASSIFICATION_ADD);
        } else if (!removedPropagations.containsKey(entity.getGuid())) {
            notifyEntityEvents(Collections.singletonList(entity), CLASSIFICATION_UPDATE);
        }
    }

    @Override
    public void onClassificationsDeleted(AtlasEntity entity, List<AtlasClassification> classifications) throws AtlasBaseException {
        notifyEntityEvents(Collections.singletonList(entity), CLASSIFICATION_DELETE);
    }

    @Override
    public void onTermAdded(AtlasGlossaryTerm term, List<AtlasRelatedObjectId> entities) {
        // do nothing -> notification not sent out for term assignment to entities
    }

    @Override
    public void onTermDeleted(AtlasGlossaryTerm term, List<AtlasRelatedObjectId> entities) {
        // do nothing -> notification not sent out for term removal from entities
    }

    private void notifyEntityEvents(List<AtlasEntity> entities, OperationType operationType) throws AtlasBaseException {
        List<EntityNotificationV2> messages = new ArrayList<>();

        for (AtlasEntity entity : entities) {
            if (isInternalType(entity.getTypeName())) {
                continue;
            }

            messages.add(new EntityNotificationV2(toNotificationHeader(entity), operationType, RequestContext.get().getRequestTime()));
        }

        if (!messages.isEmpty()) {
            try {
                notificationSender.send(messages);
            } catch (NotificationException e) {
                throw new AtlasBaseException(AtlasErrorCode.ENTITY_NOTIFICATION_FAILED, e, operationType.name());
            }
        }
    }

    private AtlasEntityHeader toNotificationHeader(AtlasEntity entity) {
        AtlasEntityHeader ret         = new AtlasEntityHeader(entity.getTypeName(), entity.getGuid(), new HashMap<>());
        Object            name        = entity.getAttribute(NAME);
        Object            displayText = name != null ? name : entity.getAttribute(QUALIFIED_NAME);

        ret.setGuid(entity.getGuid());
        ret.setStatus(entity.getStatus());
        setAttribute(ret, NAME, name);
        setAttribute(ret, DESCRIPTION, entity.getAttribute(DESCRIPTION));
        setAttribute(ret, OWNER, entity.getAttribute(OWNER));
        setAttribute(ret, CREATE_TIME, entity.getAttribute(CREATE_TIME));

        if (displayText != null) {
            ret.setDisplayText(displayText.toString());
        }

        AtlasEntityType entityType = typeRegistry.getEntityTypeByName(entity.getTypeName());

        if (entityType != null) {
            for (AtlasAttribute attribute : entityType.getAllAttributes().values()) {
                if (attribute.getAttributeDef().getIsUnique() || attribute.getAttributeDef().getIncludeInNotification()) {
                    Object attrValue = entity.getAttribute(attribute.getName());

                    if (attrValue != null) {
                        ret.setAttribute(attribute.getName(), attrValue);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(entity.getClassifications())) {
                List<AtlasClassification> classifications     = new ArrayList<>(entity.getClassifications().size());
                List<String>              classificationNames = new ArrayList<>(entity.getClassifications().size());

                for (AtlasClassification classification : getAllClassifications(entity.getClassifications())) {
                    classifications.add(classification);
                    classificationNames.add(classification.getTypeName());
                }

                ret.setClassifications(classifications);
                ret.setClassificationNames(classificationNames);
            }
        }

        return ret;
    }

    private void setAttribute(AtlasEntityHeader entity, String attrName, Object attrValue) {
        if (attrValue != null) {
            entity.setAttribute(attrName, attrValue);
        }
    }

    private List<AtlasClassification> getAllClassifications(List<AtlasClassification> classifications) {
        List<AtlasClassification> ret = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(classifications)) {
            for (AtlasClassification classification : classifications) {
                AtlasClassificationType classificationType = typeRegistry.getClassificationTypeByName(classification.getTypeName());
                Set<String>             superTypeNames     = classificationType != null ? classificationType.getAllSuperTypes() : null;

                ret.add(classification);

                if (CollectionUtils.isNotEmpty(superTypeNames)) {
                    for (String superTypeName : superTypeNames) {
                        AtlasClassification superTypeClassification = new AtlasClassification(superTypeName);

                        superTypeClassification.setEntityGuid(classification.getEntityGuid());
                        superTypeClassification.setPropagate(classification.isPropagate());

                        if (MapUtils.isNotEmpty(classification.getAttributes())) {
                            AtlasClassificationType superType = typeRegistry.getClassificationTypeByName(superTypeName);

                            if (superType != null && MapUtils.isNotEmpty(superType.getAllAttributes())) {
                                Map<String, Object> superTypeClassificationAttributes = new HashMap<>();

                                for (Map.Entry<String, Object> attrEntry : classification.getAttributes().entrySet()) {
                                    String attrName = attrEntry.getKey();

                                    if (superType.getAllAttributes().containsKey(attrName)) {
                                        superTypeClassificationAttributes.put(attrName, attrEntry.getValue());
                                    }
                                }

                                superTypeClassification.setAttributes(superTypeClassificationAttributes);
                            }
                        }

                        ret.add(superTypeClassification);
                    }
                }
            }
        }

        return ret;
    }
}