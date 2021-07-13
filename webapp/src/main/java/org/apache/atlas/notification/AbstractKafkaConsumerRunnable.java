package org.apache.atlas.notification;

import com.google.common.annotations.VisibleForTesting;
import io.zeta.metaspace.web.service.indexmanager.IndexCounter;
import kafka.utils.ShutdownableThread;
import org.apache.atlas.*;
import org.apache.atlas.kafka.AtlasKafkaMessage;
import org.apache.atlas.model.notification.HookNotification;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.filters.AuditFilter;
import org.apache.atlas.web.filters.AuditLog;
import org.apache.atlas.web.service.ServiceState;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractKafkaConsumerRunnable extends ShutdownableThread {

    public static final String CONSUMER_RETRY_INTERVAL           = "atlas.notification.consumer.retry.interval";
    public static final String CONSUMER_MIN_RETRY_INTERVAL       = "atlas.notification.consumer.min.retry.interval";
    public static final String CONSUMER_MAX_RETRY_INTERVAL       = "atlas.notification.consumer.max.retry.interval";
    public static final String CONSUMER_RETRIES_PROPERTY         = "atlas.notification.hook.maxretries";
    public static final String CONSUMER_FAILED_CACHE_SIZE_PROPERTY = "atlas.notification.hook.failedcachesize";
    private static final Logger LOG        = LoggerFactory.getLogger(AbstractKafkaConsumerRunnable.class);
    private static final Logger PERF_LOG   = AtlasPerfTracer.getPerfLogger(AbstractKafkaConsumerRunnable.class);
    private static final Logger FAILED_LOG = LoggerFactory.getLogger("FAILED");
    protected NotificationConsumer<Notification> consumer;
    protected final AtomicBoolean shouldRun      = new AtomicBoolean(false);
    protected final ServiceState serviceState;
    public static final int SERVER_READY_WAIT_TIME_MS = 1000;
    protected Configuration applicationProperties;
    @VisibleForTesting
    final int consumerRetryInterval;
    private final AdaptiveWaiter adaptiveWaiter;
    private final int minWaitDuration;
    private final int maxWaitDuration;
    @VisibleForTesting
    protected final FailedCommitOffsetRecorder failedCommitOffsetRecorder;
    private final List<Notification> failedMessages = new ArrayList<>();
    private final int maxRetries;
    private final int failedMsgCacheSize;
    private static final int    SC_OK          = 200;
    private static final int    SC_BAD_REQUEST = 400;
    private AtlasClientV2 atlasClientV2 = null;
    private String[] restAddresses;
    private final IndexCounter indexCounter = new IndexCounter();
    public AbstractKafkaConsumerRunnable(NotificationConsumer<Notification> consumer, ServiceState serviceState, String name) throws AtlasException {
        super(name, false);
        this.applicationProperties = ApplicationProperties.get();
        consumerRetryInterval = applicationProperties.getInt(CONSUMER_RETRY_INTERVAL, 500);
        // 500 ms  by default
        minWaitDuration       = applicationProperties.getInt(CONSUMER_MIN_RETRY_INTERVAL, consumerRetryInterval);
        //  30 sec by default
        maxWaitDuration       = applicationProperties.getInt(CONSUMER_MAX_RETRY_INTERVAL, minWaitDuration * 60);
        this.adaptiveWaiter = new AdaptiveWaiter(minWaitDuration, maxWaitDuration, minWaitDuration);
        this.consumer = consumer;
        this.serviceState = serviceState;
        this.failedCommitOffsetRecorder = new FailedCommitOffsetRecorder();
        this.maxRetries = applicationProperties.getInt(CONSUMER_RETRIES_PROPERTY, 3);
        this.failedMsgCacheSize = applicationProperties.getInt(CONSUMER_FAILED_CACHE_SIZE_PROPERTY, 20);
        restAddresses = applicationProperties.getStringArray(AtlasConstants.ATLAS_REST_ADDRESS_KEY);
        if (ArrayUtils.isEmpty(restAddresses)) {
            restAddresses = new String[] {AtlasConstants.DEFAULT_ATLAS_REST_ADDRESS};
        }
    }
    @Override
    public void doWork() {
        LOG.info("==> KafkaConsumer doWork()");
        shouldRun.set(true);
        if (!serverAvailable(new Timer())) {
            return;
        }
        //开始时间
        long process_start = 0;
        //处理次数
        long process_time = 0;
        //处理总耗时
        long process_total_cost = 0;
        //当前处理耗时
        long current_cost = 0;
        try {
            while (shouldRun.get()) {
                long start = 0;
                try {
                    List<AtlasKafkaMessage<Notification>> messages = consumer.receive();
                    start = System.currentTimeMillis();
                    for (AtlasKafkaMessage<Notification> msg : messages) {
                        process_start = System.currentTimeMillis();
                        LOG.info("offset:{},收到消息", msg.getOffset());
                        handleMessage(msg);
                        current_cost = System.currentTimeMillis() - process_start;
                        process_total_cost += current_cost;
                        process_time++;
                        LOG.info("offset:{},本次处理元数据耗时{}ms, 处理元数据总次数{}, 平均耗时{}ms", msg.getOffset(), current_cost, process_time, process_total_cost / process_time);
                    }
                    int size = messages.size();
                    if (size > 0) {
                        LOG.info("接收数据条数为{}, 总耗时为{}ms", size, System.currentTimeMillis() - start);
                    }
                } catch (IllegalStateException ex) {
                    LOG.error("消费kafka数据发生错误", ex);
                    adaptiveWaiter.pause(ex);
                } catch (Exception e) {
                    LOG.warn("Exception in NotificationHookConsumer, 本次处理总耗时{}", System.currentTimeMillis() - start, e);
                    if (shouldRun.get()) {
                        adaptiveWaiter.pause(e);
                    } else {
                        break;
                    }
                }
            }
        } finally {
            if (consumer != null) {
                LOG.info("closing NotificationConsumer");

                consumer.close();
            }

            LOG.info("<== KafkaConsumer doWork()");
        }
    }

    protected void handleMessage(AtlasKafkaMessage<Notification> kafkaMsg){
        AtlasPerfTracer  perf        = null;
        Notification message     = kafkaMsg.getMessage();
        long             startTime   = System.currentTimeMillis();
        boolean          isFailedMsg = false;
        AuditLog auditLog = null;

        if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
            perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, message.getTypeName());
        }

        try {
            if(failedCommitOffsetRecorder.isMessageReplayed(kafkaMsg.getOffset())) {
                commit(kafkaMsg);
                return;
            }

            for (int numRetries = 0; numRetries < maxRetries; numRetries++) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("handleMessage({}): attempt {}", message.getTypeName(), numRetries);
                }

                try {
                    auditLog = dealMessage(kafkaMsg.getMessage());
                    break;
                } catch (Throwable e) {
                    LOG.warn("Error handling message", e);
                    try {
                        LOG.info("Sleeping for {} ms before retry", consumerRetryInterval);

                        Thread.sleep(consumerRetryInterval);
                    } catch (InterruptedException ie) {
                        LOG.error("Notification consumer thread sleep interrupted");
                    }

                    if (numRetries == (maxRetries - 1)) {
                        LOG.warn("Max retries exceeded for message {}", message, e);

                        isFailedMsg = true;

                        failedMessages.add(message);
                        commit(kafkaMsg);
                        if (failedMessages.size() >= failedMsgCacheSize) {
                            recordFailedMessages();
                        }
                        return;
                    }
                } finally {
                    RequestContext.clear();
                }
            }

        } finally {
            AtlasPerfTracer.log(perf);

            if (auditLog != null) {
                auditLog.setHttpStatus(isFailedMsg ? SC_BAD_REQUEST : SC_OK);
                auditLog.setTimeTaken(System.currentTimeMillis() - startTime);

                AuditFilter.audit(auditLog);
            }
        }
    }

    protected abstract AuditLog dealMessage(Notification message)throws AtlasException;

    private void commit(AtlasKafkaMessage<Notification> kafkaMessage) {
        boolean commitSucceessStatus = false;
        try {
            recordFailedMessages();

            TopicPartition partition = new TopicPartition(kafkaMessage.getTopic(), kafkaMessage.getPartition());

            consumer.commit(partition, kafkaMessage.getOffset() + 1);
            commitSucceessStatus = true;
        } finally {
            failedCommitOffsetRecorder.recordIfFailed(commitSucceessStatus, kafkaMessage.getOffset());
        }
    }

    private void recordFailedMessages() {
        //logging failed messages
        for (Notification message : failedMessages) {
            FAILED_LOG.error("[DROPPED_NOTIFICATION] {}", AbstractNotification.getMessageJson(message));
        }

        failedMessages.clear();
    }

    boolean serverAvailable(Timer timer) {
        try {
            while (serviceState.getState() != ServiceState.ServiceStateValue.ACTIVE) {
                try {
                    LOG.info("METASPACE Server is not ready. Waiting for {} milliseconds to retry...", SERVER_READY_WAIT_TIME_MS);

                    timer.sleep(SERVER_READY_WAIT_TIME_MS);
                } catch (InterruptedException e) {
                    LOG.info("Interrupted while waiting for Atlas Server to become ready, " + "exiting consumer thread.", e);

                    return false;
                }
            }
        } catch (Throwable e) {
            LOG.info("Handled AtlasServiceException while waiting for Atlas Server to become ready, exiting consumer thread.", e);

            return false;
        }

        LOG.info("METASPACE Server is ready, can start reading Kafka events.");

        return true;
    }

    static class FailedCommitOffsetRecorder {
        private Long currentOffset;

        public void recordIfFailed(boolean commitStatus, long offset) {
            if(commitStatus) {
                currentOffset = null;
            } else {
                currentOffset = offset;
            }
        }

        public boolean isMessageReplayed(long offset) {
            return currentOffset != null && currentOffset == offset;
        }

        public Long getCurrentOffset() {
            return currentOffset;
        }
    }

    static class Timer {
        public void sleep(int interval) throws InterruptedException {
            Thread.sleep(interval);
        }
    }

    static class AdaptiveWaiter {
        private final long increment;
        private final long maxDuration;
        private final long minDuration;
        private final long resetInterval;
        private       long lastWaitAt;

        @VisibleForTesting
        long waitDuration;

        public AdaptiveWaiter(long minDuration, long maxDuration, long increment) {
            this.minDuration   = minDuration;
            this.maxDuration   = maxDuration;
            this.increment     = increment;
            this.waitDuration  = minDuration;
            this.lastWaitAt    = 0;
            this.resetInterval = maxDuration * 2;
        }

        public void pause(Exception ex) {
            setWaitDurations();

            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} in NotificationHookConsumer. Waiting for {} ms for recovery.", ex.getClass().getName(), waitDuration, ex);
                }

                Thread.sleep(waitDuration);
            } catch (InterruptedException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} in NotificationHookConsumer. Waiting for recovery interrupted.", ex.getClass().getName(), e);
                }
            }
        }

        private void setWaitDurations() {
            long timeSinceLastWait = (lastWaitAt == 0) ? 0 : System.currentTimeMillis() - lastWaitAt;

            lastWaitAt = System.currentTimeMillis();

            if (timeSinceLastWait > resetInterval) {
                waitDuration = minDuration;
            } else {
                waitDuration += increment;
                if (waitDuration > maxDuration) {
                    waitDuration = maxDuration;
                }
            }
        }
    }
    @VisibleForTesting
    public void refreshCache() throws AtlasServiceException {
        try {
            if (null == atlasClientV2) {
                atlasClientV2 = new AtlasClientV2(restAddresses);
            }
            atlasClientV2.refreshCache();
        } catch (AtlasException e) {
            LOG.error("刷新缓存失败，原因:", e.getMessage());
        }
    }
}