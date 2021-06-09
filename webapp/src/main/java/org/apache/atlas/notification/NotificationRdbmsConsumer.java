package org.apache.atlas.notification;

import com.google.common.annotations.VisibleForTesting;

import org.apache.atlas.*;
import org.apache.atlas.listener.ActiveStateChangeHandler;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.apache.atlas.notification.rdbms.Conversion;
import org.apache.atlas.notification.rdbms.DebeziumConnector;
import org.apache.atlas.repository.converters.AtlasInstanceConverter;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.web.filters.AuditLog;
import org.apache.atlas.web.service.ServiceState;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Map;
import java.util.Properties;

@Component
@Order(5)
@DependsOn(value = {"atlasTypeDefStoreInitializer", "atlasTypeDefGraphStoreV2"})
public class NotificationRdbmsConsumer extends AbstractKafkaNotificationConsumer {

    private Conversion conversion;

    @Inject
    public NotificationRdbmsConsumer(ServiceState serviceState,
                                     AtlasEntityStore atlasEntityStore,
                                     AtlasInstanceConverter instanceConverter,
                                     AtlasTypeRegistry typeRegistry, Conversion conversion) throws AtlasException {
        super(serviceState, NotificationInterface.NotificationType.RDBMS, atlasEntityStore,instanceConverter,typeRegistry);
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

            Properties connectorProperties = DebeziumConnector.getConnectorConfig(name);

            RdbmsEntities rdbmsEntities = conversion.convert(rdbmsMessage,connectorProperties);

            System.out.println("atlas实体及数据血缘【"+rdbmsEntities+"】插入JanusGraph数据库，并调用监听器，将数据插入PG");

            return null;
        }
    }
}