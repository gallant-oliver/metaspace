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
package org.apache.atlas.kafka;

import com.google.common.annotations.VisibleForTesting;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.notification.AbstractNotification;
import org.apache.atlas.notification.NotificationConsumer;
import org.apache.atlas.notification.NotificationException;
import org.apache.atlas.service.Service;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Kafka specific access point to the Atlas notification framework.
 */
@Component
@Order(3)
public class KafkaNotification extends AbstractNotification implements Service {
    public static final Logger LOG = LoggerFactory.getLogger(KafkaNotification.class);

    public    static final String PROPERTY_PREFIX            = "atlas.kafka";
    public    static final String ATLAS_HOOK_TOPIC           = "ATLAS_HOOK";
    public    static final String ATLAS_ENTITIES_TOPIC       = "ATLAS_ENTITIES";
    public    static final String ORACLE_METADATA_TOPIC       = "oracle.metadata.topic";
    protected static final String CONSUMER_GROUP_ID_PROPERTY = "group.id";
    public static final String DEFAULT_GROUP_ID = "metaspace";

    private static final Map<NotificationType, List<String>> TOPIC_MAP = new HashMap<NotificationType, List<String>>() {
        {
            String oracleMetadataToipic = null;
            try{
                Configuration configuration = ApplicationProperties.get();
                oracleMetadataToipic = configuration.getString(ORACLE_METADATA_TOPIC, "ORACLE_METADATA");
                put(NotificationType.HOOK, Arrays.asList(ATLAS_HOOK_TOPIC));
                put(NotificationType.ENTITIES, Arrays.asList(ATLAS_ENTITIES_TOPIC));
                put(NotificationType.RDBMS, Arrays.asList(oracleMetadataToipic));
            }catch (Exception e){
                throw new RuntimeException("?????????KafkaNotification.TOPIC_MAP??????", e);
            }
        }
    };

    private final Properties    properties;
    private final Long          pollTimeOutMs;
    private       KafkaConsumer consumer;
    private       KafkaProducer producer;

    // ----- Constructors ----------------------------------------------------

    /**
     * Construct a KafkaNotification.
     *
     * @param applicationProperties  the application properties used to configure Kafka
     *
     * @throws AtlasException if the notification interface can not be created
     */
    @Inject
    public KafkaNotification(Configuration applicationProperties) throws AtlasException {
        super(applicationProperties);

        LOG.info("==> KafkaNotification()");

        Configuration kafkaConf = ApplicationProperties.getSubsetConfiguration(applicationProperties, PROPERTY_PREFIX);

        properties    = ConfigurationConverter.getProperties(kafkaConf);
        pollTimeOutMs = kafkaConf.getLong("poll.timeout.ms", 1000);
        String offsetReset = kafkaConf.getString("auto.offset.reset", "earliest");

        //Override default configs
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset);

        boolean oldApiCommitEnableFlag = kafkaConf.getBoolean("auto.commit.enable", false);

        //set old autocommit value if new autoCommit property is not set.
        properties.put("enable.auto.commit", kafkaConf.getBoolean("enable.auto.commit", oldApiCommitEnableFlag));
        properties.put("session.timeout.ms", kafkaConf.getString("session.timeout.ms", "30000"));

        LOG.info("<== KafkaNotification()");
    }

    @VisibleForTesting
    protected KafkaNotification(Properties properties) {
        super();

        LOG.info("==> KafkaNotification()");

        this.properties    = properties;
        this.pollTimeOutMs = 1000L;

        LOG.info("<== KafkaNotification()");
    }

    @VisibleForTesting
    public List<String> getTopicName(NotificationType notificationType) {
        return TOPIC_MAP.get(notificationType);
    }

    public synchronized void addTopic(NotificationType notificationType, String topic) {
        if(null == notificationType || StringUtils.isBlank(topic)){
            String message = String.format("??????topic?????????????????????????????????notificationType = %s, topic = %s", notificationType, topic);
            throw new RuntimeException(message);
        }
        List<String> topics = TOPIC_MAP.get(notificationType);
        if(topics.contains(topic)){
            String message = String.format("??????topic????????????????????? topic %s?????????notificationType???%s????????????", topic, notificationType);
            throw new RuntimeException(message);
        }
        topics.add(topic);
    }

    // ----- Service ---------------------------------------------------------

    @Override
    public void start() throws AtlasException {
        LOG.info("==> KafkaNotification.start()");

        LOG.info("<== KafkaNotification.start()");
    }

    @Override
    public void stop() {
        LOG.info("==> KafkaNotification.stop()");

        LOG.info("<== KafkaNotification.stop()");
    }


    // ----- NotificationInterface -------------------------------------------
    @Override
    public <T> List<NotificationConsumer<T>> createConsumers(NotificationType notificationType, int numConsumers) {
        return createConsumers(notificationType, numConsumers, Boolean.valueOf(properties.getProperty("enable.auto.commit", properties.getProperty("auto.commit.enable","false"))));
    }

    @VisibleForTesting
    public <T> List<NotificationConsumer<T>> createConsumers(NotificationType notificationType, int numConsumers, boolean autoCommitEnabled) {
        LOG.info("==> KafkaNotification.createConsumers(notificationType={}, numConsumers={}, autoCommitEnabled={})", notificationType, numConsumers, autoCommitEnabled);

        Properties         consumerProperties = getConsumerProperties(notificationType);
        AtlasKafkaConsumer kafkaConsumer      = new AtlasKafkaConsumer(notificationType, getKafkaConsumer(consumerProperties, notificationType, autoCommitEnabled), autoCommitEnabled, pollTimeOutMs);

        List<NotificationConsumer<T>> consumers = Collections.singletonList(kafkaConsumer);

        LOG.info("<== KafkaNotification.createConsumers(notificationType={}, numConsumers={}, autoCommitEnabled={})", notificationType, numConsumers, autoCommitEnabled);

        return consumers;
    }

    @Override
    public void close() {
        LOG.info("==> KafkaNotification.close()");

        if (producer != null) {
            producer.close();

            producer = null;
        }

        LOG.info("<== KafkaNotification.close()");
    }


    // ----- AbstractNotification --------------------------------------------
    @Override
    public void sendInternal(NotificationType type, List<String> messages) throws NotificationException {
        if (producer == null) {
            createProducer();
        }

        sendInternalToProducer(producer, type, messages);
    }

    @VisibleForTesting
    void sendInternalToProducer(Producer p, NotificationType type, List<String> messages) throws NotificationException {
        String               topic           = TOPIC_MAP.get(type).get(0);
        List<MessageContext> messageContexts = new ArrayList<>();

        for (String message : messages) {
            ProducerRecord record = new ProducerRecord(topic, message);

//            if (LOG.isDebugEnabled()) {
            LOG.info("Sending message for topic {}: {}", topic, message);
//            }

            Future future = p.send(record);

            messageContexts.add(new MessageContext(future, message));
        }

        List<String> failedMessages       = new ArrayList<>();
        Exception    lastFailureException = null;

        for (MessageContext context : messageContexts) {
            try {
                RecordMetadata response = context.getFuture().get();

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sent message for topic - {}, partition - {}, offset - {}", response.topic(), response.partition(), response.offset());
                }
            } catch (Exception e) {
                lastFailureException = e;

                failedMessages.add(context.getMessage());
            }
        }

        if (lastFailureException != null) {
            throw new NotificationException(lastFailureException, failedMessages);
        }
    }


    public KafkaConsumer getKafkaConsumer(Properties consumerProperties, NotificationType type, boolean autoCommitEnabled) {
        if(this.consumer == null) {
            try {
                List<String> topics = TOPIC_MAP.get(type);

                consumerProperties.put("enable.auto.commit", autoCommitEnabled);
                this.consumer = new KafkaConsumer(consumerProperties);

                this.consumer.subscribe(topics);
            } catch (Exception ee) {
                LOG.error("Exception in getKafkaConsumer ", ee);
            }
        }

        return this.consumer;
    }
    public KafkaConsumer getNewKafkaConsumer(Properties consumerProperties, NotificationType type, boolean autoCommitEnabled) {
        KafkaConsumer newConsumer = null;
        try {
            List<String> topics = TOPIC_MAP.get(type);

            consumerProperties.put("enable.auto.commit", autoCommitEnabled);
            newConsumer = new KafkaConsumer(consumerProperties);

            newConsumer.subscribe(topics);
        } catch (Exception ee) {
            LOG.error("Exception in getKafkaConsumer ", ee);
        }
        return newConsumer;
    }


    @VisibleForTesting
    public
        // Get properties for consumer request
    Properties getConsumerProperties(NotificationType type) {
        // find the configured group id for the given notification type
        String groupId = properties.getProperty(type.toString().toLowerCase() + "." + CONSUMER_GROUP_ID_PROPERTY, DEFAULT_GROUP_ID);

        Properties consumerProperties = new Properties();

        consumerProperties.putAll(properties);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        return consumerProperties;
    }

    private synchronized void createProducer() {
        LOG.info("==> KafkaNotification.createProducer()");

        if (producer == null) {
            producer = new KafkaProducer(properties);
        }

        LOG.info("<== KafkaNotification.createProducer()");
    }

    private class MessageContext {
        private final Future<RecordMetadata> future;
        private final String                 message;

        public MessageContext(Future<RecordMetadata> future, String message) {
            this.future  = future;
            this.message = message;
        }

        public Future<RecordMetadata> getFuture() {
            return future;
        }

        public String getMessage() {
            return message;
        }
    }
}