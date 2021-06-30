package org.apache.atlas.notification;

import com.google.common.annotations.VisibleForTesting;

import org.apache.atlas.*;
import org.apache.atlas.listener.ActiveStateChangeHandler;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.apache.atlas.notification.rdbms.Conversion;
import org.apache.atlas.notification.rdbms.KafkaConnector;
import org.apache.atlas.repository.converters.AtlasInstanceConverter;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStream;
import org.apache.atlas.repository.store.graph.v2.EntityStream;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.web.filters.AuditLog;
import org.apache.atlas.web.service.ServiceState;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

@Component
@Order(5)
@DependsOn(value = {"atlasTypeDefStoreInitializer", "atlasTypeDefGraphStoreV2"})
public class NotificationRdbmsConsumer extends AbstractKafkaNotificationConsumer {

    private Conversion conversion;

    private AtlasEntityStore entityStore;

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

            System.out.println("atlas实体及数据血缘【"+rdbmsEntities+"】插入JanusGraph数据库，并调用监听器，将数据插入PG");

//            SortedMap<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> entityMap = rdbmsEntities.getEntityMap();
//            Set<Map.Entry<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>> entries = entityMap.entrySet();
//            if(entries.isEmpty()){
//                return null;
//            }
//            for (Map.Entry<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> entry : entries) {
//                List<AtlasEntity.AtlasEntityWithExtInfo> entities = entry.getValue();
//                for (AtlasEntity.AtlasEntityWithExtInfo entity : entities) {
//                    atlasEntityStore.createOrUpdate(new AtlasEntityStream(entity), false);
//                }
//            }
//
//            AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities = rdbmsEntities.getBloodEntities();
//            if(null != bloodEntities){
//                EntityStream entityStream = new AtlasEntityStream(bloodEntities);
//                atlasEntityStore.createOrUpdate(entityStream, false);
//            }
            return null;
        }
    }

}