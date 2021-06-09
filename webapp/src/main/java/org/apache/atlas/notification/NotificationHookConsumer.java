/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas.notification;

import com.google.common.annotations.VisibleForTesting;

import org.apache.atlas.*;

import org.apache.atlas.listener.ActiveStateChangeHandler;
import org.apache.atlas.model.instance.AtlasEntity;

import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.notification.HookNotification;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.v1.model.instance.Referenceable;
import org.apache.atlas.v1.model.notification.HookNotificationV1;
import org.apache.atlas.repository.converters.AtlasInstanceConverter;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStream;
import org.apache.atlas.repository.store.graph.v2.AtlasGraphUtilsV2;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.web.filters.AuditLog;
import org.apache.atlas.web.service.ServiceState;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

/**
 * Consumer of notifications from hooks e.g., hive hook etc.
 */
@Component
@Order(4)
@DependsOn(value = {"atlasTypeDefStoreInitializer", "atlasTypeDefGraphStoreV2"})
public class NotificationHookConsumer extends AbstractKafkaNotificationConsumer {
    private static final Logger LOG        = LoggerFactory.getLogger(NotificationHookConsumer.class);
    private static final String THREAD_NAME_PREFIX = NotificationHookConsumer.class.getSimpleName();


    @Inject
    public NotificationHookConsumer(AtlasEntityStore atlasEntityStore,
                                    ServiceState serviceState, AtlasInstanceConverter instanceConverter,
                                    AtlasTypeRegistry typeRegistry) throws AtlasException {
        super(serviceState, NotificationInterface.NotificationType.HOOK,
                atlasEntityStore, instanceConverter, typeRegistry);
        this.applicationProperties = ApplicationProperties.get();
    }

    @Override
    public AbstractKafkaConsumerRunnable getConsumerRunnable(NotificationConsumer<Notification> consumer) throws AtlasException{
        return new HookConsumer(consumer,serviceState);
    }

    @Override
    public int getHandlerOrder() {
        return ActiveStateChangeHandler.HandlerOrder.NOTIFICATION_HOOK_CONSUMER.getOrder();
    }


    @VisibleForTesting
    class HookConsumer extends AbstractKafkaConsumerRunnable {

        public HookConsumer(NotificationConsumer<Notification> consumer, ServiceState serviceState) throws AtlasException {
            super(consumer, serviceState, "atlas-hook-consumer-thread");
        }

        @Override
        protected AuditLog dealMessage(Notification message) {
            RequestContext requestContext = RequestContext.get();
            HookNotification hookMessage = (HookNotification)message;
            String messageUser = hookMessage.getUser();
            requestContext.setUser(messageUser, null);
            AuditLog auditLog = null;
            switch (hookMessage.getType()) {
                case ENTITY_CREATE: {
                    final HookNotificationV1.EntityCreateRequest createRequest = (HookNotificationV1.EntityCreateRequest) hookMessage;
                    final AtlasEntity.AtlasEntitiesWithExtInfo entities = instanceConverter.toAtlasEntities(createRequest.getEntities());

                    if (auditLog == null) {
                        auditLog = new AuditLog(messageUser, THREAD_NAME_PREFIX,
                                AtlasClient.API_V1.CREATE_ENTITY.getMethod(),
                                AtlasClient.API_V1.CREATE_ENTITY.getNormalizedPath());
                    }

                    atlasEntityStore.createOrUpdate(new AtlasEntityStream(entities), false);
                }
                break;

                case ENTITY_PARTIAL_UPDATE: {
                    final HookNotificationV1.EntityPartialUpdateRequest partialUpdateRequest = (HookNotificationV1.EntityPartialUpdateRequest) hookMessage;
                    final Referenceable referenceable = partialUpdateRequest.getEntity();
                    final AtlasEntity.AtlasEntitiesWithExtInfo entities = instanceConverter.toAtlasEntity(referenceable);

                    if (auditLog == null) {
                        auditLog = new AuditLog(messageUser, THREAD_NAME_PREFIX,
                                AtlasClientV2.API_V2.UPDATE_ENTITY_BY_ATTRIBUTE.getMethod(),
                                String.format(AtlasClientV2.API_V2.UPDATE_ENTITY_BY_ATTRIBUTE.getNormalizedPath(), partialUpdateRequest.getTypeName()));
                    }

                    AtlasEntityType entityType = typeRegistry.getEntityTypeByName(partialUpdateRequest.getTypeName());
                    String guid = AtlasGraphUtilsV2.getGuidByUniqueAttributes(entityType, Collections.singletonMap(partialUpdateRequest.getAttribute(), (Object) partialUpdateRequest.getAttributeValue()));

                    // There should only be one root entity
                    entities.getEntities().get(0).setGuid(guid);

                    atlasEntityStore.createOrUpdate(new AtlasEntityStream(entities), true);
                }
                break;

                case ENTITY_DELETE: {
                    final HookNotificationV1.EntityDeleteRequest deleteRequest = (HookNotificationV1.EntityDeleteRequest) hookMessage;

                    if (auditLog == null) {
                        auditLog = new AuditLog(messageUser, THREAD_NAME_PREFIX,
                                AtlasClientV2.API_V2.DELETE_ENTITY_BY_ATTRIBUTE.getMethod(),
                                String.format(AtlasClientV2.API_V2.DELETE_ENTITY_BY_ATTRIBUTE.getNormalizedPath(), deleteRequest.getTypeName()));
                    }

                    try {
                        AtlasEntityType type = (AtlasEntityType) typeRegistry.getType(deleteRequest.getTypeName());

                        atlasEntityStore.deleteByUniqueAttributes(type, Collections.singletonMap(deleteRequest.getAttribute(), (Object) deleteRequest.getAttributeValue()));
                    } catch (ClassCastException cle) {
                        LOG.error("Failed to delete entity {}", deleteRequest);
                    }
                }
                break;

                case ENTITY_FULL_UPDATE: {
                    final HookNotificationV1.EntityUpdateRequest updateRequest = (HookNotificationV1.EntityUpdateRequest) hookMessage;
                    final AtlasEntity.AtlasEntitiesWithExtInfo entities = instanceConverter.toAtlasEntities(updateRequest.getEntities());

                    if (auditLog == null) {
                        auditLog = new AuditLog(messageUser, THREAD_NAME_PREFIX,
                                AtlasClientV2.API_V2.UPDATE_ENTITY.getMethod(),
                                AtlasClientV2.API_V2.UPDATE_ENTITY.getNormalizedPath());
                    }

                    atlasEntityStore.createOrUpdate(new AtlasEntityStream(entities), false);
                }
                break;

                case ENTITY_CREATE_V2: {
                    final HookNotification.EntityCreateRequestV2 createRequestV2 = (HookNotification.EntityCreateRequestV2) hookMessage;
                    final AtlasEntity.AtlasEntitiesWithExtInfo entities = createRequestV2.getEntities();

                    if (auditLog == null) {
                        auditLog = new AuditLog(messageUser, THREAD_NAME_PREFIX,
                                AtlasClientV2.API_V2.CREATE_ENTITY.getMethod(),
                                AtlasClientV2.API_V2.CREATE_ENTITY.getNormalizedPath());
                    }

                    atlasEntityStore.createOrUpdate(new AtlasEntityStream(entities), false);
                }
                break;

                case ENTITY_PARTIAL_UPDATE_V2: {
                    final HookNotification.EntityPartialUpdateRequestV2 partialUpdateRequest = (HookNotification.EntityPartialUpdateRequestV2) hookMessage;
                    final AtlasObjectId entityId = partialUpdateRequest.getEntityId();
                    final AtlasEntity.AtlasEntityWithExtInfo entity = partialUpdateRequest.getEntity();

                    if (auditLog == null) {
                        auditLog = new AuditLog(messageUser, THREAD_NAME_PREFIX,
                                AtlasClientV2.API_V2.UPDATE_ENTITY.getMethod(),
                                AtlasClientV2.API_V2.UPDATE_ENTITY.getNormalizedPath());
                    }
                    atlasEntityStore.updateEntity(entityId, entity, true);
                }
                break;

                case ENTITY_FULL_UPDATE_V2: {
                    final HookNotification.EntityUpdateRequestV2 updateRequest = (HookNotification.EntityUpdateRequestV2) hookMessage;
                    final AtlasEntity.AtlasEntitiesWithExtInfo entities = updateRequest.getEntities();

                    if (auditLog == null) {
                        auditLog = new AuditLog(messageUser, THREAD_NAME_PREFIX,
                                AtlasClientV2.API_V2.UPDATE_ENTITY.getMethod(),
                                AtlasClientV2.API_V2.UPDATE_ENTITY.getNormalizedPath());
                    }

                    atlasEntityStore.createOrUpdate(new AtlasEntityStream(entities), false);
                }
                break;

                case ENTITY_DELETE_V2: {
                    final HookNotification.EntityDeleteRequestV2 deleteRequest = (HookNotification.EntityDeleteRequestV2) hookMessage;
                    final List<AtlasObjectId> entities = deleteRequest.getEntities();

                    try {
                        for (AtlasObjectId entity : entities) {
                            if (auditLog == null) {
                                auditLog = new AuditLog(messageUser, THREAD_NAME_PREFIX,
                                        AtlasClientV2.API_V2.DELETE_ENTITY_BY_ATTRIBUTE.getMethod(),
                                        String.format(AtlasClientV2.API_V2.DELETE_ENTITY_BY_ATTRIBUTE.getNormalizedPath(), entity.getTypeName()));
                            }

                            AtlasEntityType type = (AtlasEntityType) typeRegistry.getType(entity.getTypeName());

                            atlasEntityStore.deleteByUniqueAttributes(type, entity.getUniqueAttributes());
                        }
                    } catch (ClassCastException cle) {
                        LOG.error("Failed to do delete entities {}", entities);
                    }
                }
                break;

                default:
                    throw new IllegalStateException("Unknown notification type: " + hookMessage.getType().name());
            }
            return auditLog;
        }
    }
}