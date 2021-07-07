package org.apache.atlas.notification;

import com.google.common.annotations.VisibleForTesting;

import org.apache.atlas.*;
import org.apache.atlas.listener.ActiveStateChangeHandler;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.EntityMutationResponse;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.apache.atlas.notification.rdbms.Conversion;
import org.apache.atlas.notification.rdbms.KafkaConnector;
import org.apache.atlas.repository.converters.AtlasInstanceConverter;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStream;
import org.apache.atlas.repository.store.graph.v2.EntityStream;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.web.filters.AuditLog;
import org.apache.atlas.web.service.ServiceState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;

@Component
@Order(5)
@DependsOn(value = {"atlasTypeDefStoreInitializer", "atlasTypeDefGraphStoreV2"})
public class NotificationRdbmsConsumer extends AbstractKafkaNotificationConsumer {
    private static final Logger LOG        = LoggerFactory.getLogger(NotificationRdbmsConsumer.class);
    private Conversion conversion;

    @Inject
    public NotificationRdbmsConsumer(ServiceState serviceState,
                                     AtlasEntityStore entityStore,
                                     AtlasInstanceConverter instanceConverter,
                                     AtlasTypeRegistry typeRegistry, Conversion conversion) throws AtlasException {
        super(serviceState, NotificationInterface.NotificationType.RDBMS, entityStore,instanceConverter,typeRegistry);
        this.conversion = conversion;
    }

    @Override
    public AbstractKafkaConsumerRunnable getConsumerRunnable(NotificationConsumer<Notification> consumer) throws AtlasException {
        return new RdbmsConsumer(consumer,serviceState);
    }

    @Override
    public int getHandlerOrder() {
        return ActiveStateChangeHandler.HandlerOrder.NOTIFICATION_RDBMS_CONSUMER.getOrder();
    }


    @VisibleForTesting
    class RdbmsConsumer extends AbstractKafkaConsumerRunnable {

        public RdbmsConsumer(NotificationConsumer<Notification> consumer, ServiceState serviceState) throws AtlasException {
            super(consumer, serviceState, "atlas-rdbms-consumer-thread");
        }

        @Override
        protected AuditLog dealMessage(Notification message) throws AtlasException{

            RdbmsNotification rdbmsMessage = (RdbmsNotification)message;

            String name = rdbmsMessage.getRdbmsMessage().getPayload().getSource().getName();

            Properties connectorProperties = KafkaConnector.getConnectorConfig(name);
            if(null == connectorProperties){
                throw new RuntimeException("获取connector失败");
            }
            RdbmsEntities rdbmsEntities = conversion.convert(rdbmsMessage,connectorProperties);

            LOG.info("atlas实体及数据血缘【"+rdbmsEntities+"】插入JanusGraph数据库，并调用监听器，将数据插入PG");
            RdbmsEntities.OperateType[] operateTypes = {RdbmsEntities.OperateType.DROP, RdbmsEntities.OperateType.MODIFY, RdbmsEntities.OperateType.ADD};
            deal(rdbmsEntities, operateTypes);
            return null;
        }

        void deal(RdbmsEntities rdbmsEntities, RdbmsEntities.OperateType[] operateTypes){
            Map<RdbmsEntities.OperateType, Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>> entityMap = rdbmsEntities.getEntityMap();
            Map<RdbmsEntities.OperateType, AtlasEntity.AtlasEntitiesWithExtInfo> bloodEntities = rdbmsEntities.getBloodEntities();
            for (RdbmsEntities.OperateType operateType: operateTypes) {
                Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> entityTypeMap = entityMap.get(operateType);
                AtlasEntity.AtlasEntitiesWithExtInfo bloodEntity = bloodEntities.get(operateType);
                switch (operateType){
                    case DROP:
                        dealBlood(bloodEntity, blood -> {
                            List<AtlasEntity> entities = bloodEntity.getEntities();
                            for (AtlasEntity entity : entities) {
                                String typeName = entity.getTypeName();
                                AtlasEntityType entityType = typeRegistry.getEntityTypeByName(typeName);
                                Map<String, Object> attributes = entity.getAttributes();
                                atlasEntityStore.deleteByUniqueAttributes(entityType, attributes);
                            }
                            return null;
                        });
                        dealEntities(entityTypeMap,  entity -> {
                            String typeName = entity.getEntity().getTypeName();
                            Map<String, Object> attributes = entity.getEntity().getAttributes();
                            AtlasEntityType entityType = typeRegistry.getEntityTypeByName(typeName);
                            return atlasEntityStore.deleteByUniqueAttributes(entityType, attributes);
                        });
                        break;
                    default:
                        dealEntities(entityTypeMap,  entity -> atlasEntityStore.createOrUpdate(new AtlasEntityStream(entity), false));
                        dealBlood(bloodEntity, blood ->  atlasEntityStore.createOrUpdate(new AtlasEntityStream(blood), false));
                }
            }
        }

        void dealEntities(Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> entityTypeMap, Function<AtlasEntity.AtlasEntityWithExtInfo, EntityMutationResponse> func){
            if(MapUtils.isEmpty(entityTypeMap)){
                return;
            }
            Set<Map.Entry<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>> entries = entityTypeMap.entrySet();
            for (Map.Entry<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> entry : entries) {
                List<AtlasEntity.AtlasEntityWithExtInfo> entities = entry.getValue();
                for (AtlasEntity.AtlasEntityWithExtInfo entity : entities) {
                    func.apply(entity);
                }
            }
        }

        void dealBlood(AtlasEntity.AtlasEntitiesWithExtInfo bloodEntity, Function<AtlasEntity.AtlasEntitiesWithExtInfo,EntityMutationResponse> func){
            if(null == bloodEntity){
                return;
            }
            func.apply(bloodEntity);
        }
    }
}