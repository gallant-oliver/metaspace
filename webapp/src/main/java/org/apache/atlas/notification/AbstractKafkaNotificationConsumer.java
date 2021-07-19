package org.apache.atlas.notification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.atlas.*;
import org.apache.atlas.ha.HAConfiguration;
import org.apache.atlas.kafka.KafkaNotification;
import org.apache.atlas.kafka.NotificationProvider;
import org.apache.atlas.listener.ActiveStateChangeHandler;
import org.apache.atlas.model.notification.HookNotification;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.repository.converters.AtlasInstanceConverter;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.service.Service;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.web.service.ServiceState;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public abstract class AbstractKafkaNotificationConsumer implements Service, ActiveStateChangeHandler {
    //public static final String CONSUMER_THREADS_PROPERTY         = "atlas.notification.hook.numthreads";
    public static final String CONSUMER_DISABLED                 = "atlas.notification.consumer.disabled";
    public static final String CONSUMER_THREAD_CORE_SIZE                 = "consumer.thread.core.size";
    public static final String CONSUMER_THREAD_MAX_SIZE                 = "consumer.thread.size.size";
    private static final Logger LOG        = LoggerFactory.getLogger(AbstractKafkaNotificationConsumer.class);
    protected static Configuration applicationProperties;
    private NotificationInterface notificationInterface;
    protected List<AbstractKafkaConsumerRunnable> consumers;
    private static ExecutorService executors;
    private Map<String, Future> futures;
    private final boolean consumerDisabled;
    private static final String THREAD_NAME_PREFIX = AbstractKafkaNotificationConsumer.class.getSimpleName();
    protected final ServiceState serviceState;
    private NotificationInterface.NotificationType notificationType;
    protected final AtlasEntityStore atlasEntityStore;
    protected final AtlasInstanceConverter instanceConverter;
    protected final AtlasTypeRegistry typeRegistry;
    static {
        try{
            applicationProperties = ApplicationProperties.get();
            executors = new ThreadPoolExecutor(applicationProperties.getInt(CONSUMER_THREAD_CORE_SIZE, 2),
                    applicationProperties.getInt(CONSUMER_THREAD_MAX_SIZE, 32),
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                    new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PREFIX + " thread-%d").build());
        }catch (AtlasException e){
            throw new RuntimeException("初始化消费者调度器失败",e);
        }
    }
    public AbstractKafkaNotificationConsumer(ServiceState serviceState,
                                             NotificationInterface.NotificationType notificationType,
                                             AtlasEntityStore atlasEntityStore,AtlasInstanceConverter instanceConverter,
                                             AtlasTypeRegistry typeRegistry) throws AtlasException{



        this.notificationInterface = new KafkaNotification(applicationProperties);
        this.consumerDisabled = applicationProperties.getBoolean(CONSUMER_DISABLED, false);
        this.serviceState = serviceState;
        this.notificationType = notificationType;
        this.atlasEntityStore = atlasEntityStore;
        this.instanceConverter = instanceConverter;
        this.typeRegistry = typeRegistry;
    }

    @Override
    public void instanceIsActive() throws AtlasException {
        LOG.info("Reacting to active state: initializing Kafka consumers");
        startConsumers(executors);
    }

    @Override
    public void instanceIsPassive() {
        LOG.info("Reacting to passive state: shutting down Kafka consumers.");
        stop();
    }

    @Override
    public void start() throws AtlasException {
        if (consumerDisabled) {
            LOG.info("Hook consumer stopped. No hook messages will be processed. " +
                    "Set property '{}' to false to start consuming hook messages.", CONSUMER_DISABLED);
            return;
        }
        startInternal(applicationProperties, null);
    }

    @Override
    public void stop(){
        //Allow for completion of outstanding work
        try {
            stopConsumerThreads();
            if (executors != null) {
                executors.shutdown();

                int timeout = 5000;
                if (!executors.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                    LOG.error("Timed out waiting for consumer threads to shut down, exiting uncleanly");
                }

                executors = null;
            }

            notificationInterface.close();
        } catch (InterruptedException e) {
            LOG.error("Failure in shutting down consumers");
        }
    }

    void startInternal(Configuration configuration, ExecutorService executorService) throws AtlasException{
        if (consumers == null) {
            consumers = new ArrayList<>();
        }
        if (futures == null) {
            futures = new HashMap<>();
        }
        if (executorService != null) {
            executors = executorService;
        }
        if (!HAConfiguration.isHAEnabled(configuration)) {
            LOG.info("HA is disabled, starting consumers inline.");

            startConsumers(executorService);
        }
    }

    private void stopConsumerThreads() {
        LOG.info("==> stopConsumerThreads()");

        if (consumers != null) {
            for (AbstractKafkaConsumerRunnable consumerRunnable : consumers) {
                consumerRunnable.shutdown();
            }
            consumers.clear();
        }
        if (futures!=null){
            futures.clear();
        }

        LOG.info("<== stopConsumerThreads()");
    }

    public Map<String,Boolean> isAlive(){
        Map<String,Boolean> map = new HashMap<>();
        if (futures==null){
            return map;
        }
        for (String name :futures.keySet()){
            Future future = futures.get(name);
            map.put(name,!future.isDone());
        }
        return map;
    }
    public abstract AbstractKafkaConsumerRunnable getConsumerRunnable(NotificationConsumer<Notification> consumer) throws AtlasException;

    private void startConsumers(ExecutorService executorService) throws AtlasException{
//        int  numThreads = applicationProperties.getInt(CONSUMER_THREADS_PROPERTY, 1);
        List<NotificationConsumer<Notification>> notificationConsumers = notificationInterface.createConsumers(notificationType, 1);
        for (final NotificationConsumer<Notification> consumer : notificationConsumers) {
            AbstractKafkaConsumerRunnable consumerRunnable = getConsumerRunnable(consumer);
            consumers.add(consumerRunnable);
            Future<?> submit = executors.submit(consumerRunnable);
            futures.put(consumerRunnable.getName(),submit);
        }
    }
}