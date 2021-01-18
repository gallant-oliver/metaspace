package io.zeta.metaspace.web.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.commons.configuration.Configuration;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class ZkLockUtils {
    private CuratorFramework client;
    private final String DEFAULT_BASE_LOCK_NODE = "/METASPACE_LOCK";
    private String BASE_LOCK_NODE;

    @PostConstruct
    private void init() throws AtlasException {
        try {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

            Configuration applicationProperties = ApplicationProperties.get();
            String zkConnect = applicationProperties.getString("atlas.kafka.zookeeper.connect");
            int sessionTimeout = applicationProperties.getInt("atlas.kafka.zookeeper.session.timeout.ms", 400);
            int connectionTimeout = applicationProperties.getInt("atlas.kafka.zookeeper.connection.timeout.ms", 200);
            BASE_LOCK_NODE = applicationProperties.getString("metaspace.zk.lock.base.node", DEFAULT_BASE_LOCK_NODE);

            client = CuratorFrameworkFactory.builder().
                    connectString(zkConnect).
                    connectionTimeoutMs(connectionTimeout).
                    sessionTimeoutMs(sessionTimeout).
                    retryPolicy(retryPolicy).
                    build();
            client.start();
        } catch (Exception e) {
            throw new AtlasException("初始化 ZK 失败 ", e);
        }
    }

    public InterProcessMutex getInterProcessMutex(String lockPath) {
        return new InterProcessMutex(client, buildLockPath(lockPath));
    }

    public String buildLockPath(String lockPath) {
        return BASE_LOCK_NODE +"/"+ lockPath;
    }

}
