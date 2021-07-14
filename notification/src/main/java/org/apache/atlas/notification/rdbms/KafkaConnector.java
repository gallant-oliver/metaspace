package org.apache.atlas.notification.rdbms;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.tools.hat.internal.model.HackJavaValue;
import io.zeta.metaspace.utils.OKHttpClient;
import lombok.Data;
import org.apache.atlas.ApplicationProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

public class KafkaConnector {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnector.class);
    private static final Cache<String, Instance> CONNECTOR_CACHE = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(60000, TimeUnit.MINUTES).build();

    private static final List<String> KAFKA_CONNECTOR_URLS;
    private static final String ORACLE_INIT_CONNECTOR;
    private static final ObjectMapper MAPPER;
    private static final String URL_PREFIX;
    static {
        try {
            MAPPER = new ObjectMapper().configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            Configuration conf = ApplicationProperties.get();
            ORACLE_INIT_CONNECTOR = conf.getString("oracle.init.connector.name", "oracle_init_connector");
            KAFKA_CONNECTOR_URLS = Arrays.asList(conf.getStringArray("oracle.kafka.connect.urls"));
            String url = KAFKA_CONNECTOR_URLS.get(0);
            URL_PREFIX = url.substring(0,url.indexOf("://")+3);
        } catch (Exception e) {
            throw new RuntimeException("初始化Connector失败");
        }
    }

    /**
     * 查询Connector服务器列表
     * @return
     */
    public static List<String> getConnectorUrls(){
        return new ArrayList<>(KAFKA_CONNECTOR_URLS);
    }

    /**
     * 查询connector列表
     * @return
     */
    public static Map<String, List<String>> getConnectors() {
        Map<String, List<String>> connectorMap = new HashMap<>();
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(ArrayList.class, String.class);
        for (String url : KAFKA_CONNECTOR_URLS) {
            try {
                String content = OKHttpClient.doGet(url + "/connectors", null, null, 0);
                List<String> connectors = MAPPER.readValue(content, javaType);
                if(CollectionUtils.isNotEmpty(connectors)){
                    connectorMap.put(getHost(url), connectors);
                }
            } catch (Exception e) {
                LOG.warn("获取connector失败, url : {}, message : {}" ,url, e.getMessage());
            }
        }
        return connectorMap;
    }

    private static String getHost(String url) {
        return url.substring(URL_PREFIX.length());
    }

    /**
     * 查询connector详情
     * @param connectorName
     * @return
     */
    public static Instance getConnector(String connectorName) {

        return getConnector(connectorName, true);
    }

    /**
     * 查询connector配置
     * @param connectorName
     * @return
     */
    public static Properties getConnectorConfig(String connectorName) {
        Properties config = null;
        Instance connector = getConnector(connectorName);
        if (null != connector) {
            config = connector.getConfig();
        }
        return config;
    }


    /**
     * 查询connector详情
     * @param connectorName
     * @param useCache
     * @return
     */
    public static Instance getConnector(String connectorName, boolean useCache) {
        Instance instance = null;
        if (useCache) {
            instance = CONNECTOR_CACHE.getIfPresent(connectorName);
        }
        if (null == instance) {
            for (String url : KAFKA_CONNECTOR_URLS) {
                try {
                    String content = OKHttpClient.doGet(url + "/connectors/" + connectorName, null, null, 0);
                    instance = MAPPER.readValue(content, Instance.class);
                    if (null != instance && instance.getName() != null) {
                        instance.setHost(getHost(url));
                        CONNECTOR_CACHE.put(connectorName, instance);
                        break;
                    } else {
                        instance = null;
                    }
                } catch (Exception e) {
                    LOG.warn("获取connector失败:" + e.getMessage());
                }
            }

        }
        return instance;
    }

    /**
     * 查询connector状态
     * @param connectorName
     * @return
     */
    public static Status getConnectorStatus(String connectorName) {
        Status status = null;
        for (String url : KAFKA_CONNECTOR_URLS) {
            try {
                String content = OKHttpClient.doGet(url + "/connectors/" + connectorName + "/status", null, null, 0);
                status = MAPPER.readValue(content, Status.class);
                if (null != status && status.getName() != null) {
                    break;
                } else {
                    status = null;
                }
            } catch (Exception e) {
                LOG.warn("获取connector失败:" + e.getMessage());
            }
        }
        return status;
    }

    public synchronized static Instance addConnector(Instance instance) {
        Instance newInstance = null;
        Instance connector = getConnector(instance.getName());
        if (null != connector) {
            throw new RuntimeException("添加connector失败: 名称为" + instance.getName() + "的connector已经存在");
        }
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(ArrayList.class, String.class);
            String addUrl = null;
            int size = Integer.MAX_VALUE;
            for (String url : KAFKA_CONNECTOR_URLS) {
                List<String> connectorNames = null;
                try {
                    String content = OKHttpClient.doGet(url + "/connectors", null, null, 0);
                    connectorNames = MAPPER.readValue(content, javaType);
                } catch (Exception e) {
                    LOG.warn("connector url {} 调用失败:{}", url, e.getMessage());
                }
                if (CollectionUtils.isNotEmpty(connectorNames) && connectorNames.size() < size) {
                    addUrl = url;
                    size = connectorNames.size();
                }
            }
            String json = MAPPER.writeValueAsString(instance);
            String content = OKHttpClient.doPost(addUrl + "/connectors", null, null, json, 0);
            newInstance = MAPPER.readValue(content, Instance.class);
            newInstance.setHost(getHost(addUrl));
            CONNECTOR_CACHE.put(newInstance.getName(), newInstance);
        } catch (Exception e) {
            LOG.error("添加connector失败" ,e);
            throw new RuntimeException("添加connector失败", e);
        }
        removeConnector(ORACLE_INIT_CONNECTOR);
        return newInstance;
    }

    /**
     * 删除connector
     * @param connectorName
     */
    public synchronized static boolean removeConnector(String connectorName) {

        boolean result = true;
        Instance connector = getConnector(connectorName, false);
        if (null == connector) {
            LOG.warn("connector {} 不存在，删除操作未执行", connectorName);
            result = false;
        } else{
            try {
                String url = connector.getHost();
                OKHttpClient.doDelete(URL_PREFIX + url + "/connectors/" + connectorName,null,null, 0);
            } catch (Exception e) {
                result = false;
                LOG.error("删除connector失败" ,e);
                throw new RuntimeException("删除connector失败", e);
            }
        }
        if (null != CONNECTOR_CACHE.getIfPresent(connectorName)) {
            CONNECTOR_CACHE.invalidate(connectorName);
        }
        return result;
    }

    /**
     * 暂停connector
     * @param connectorName
     */
    public synchronized static void pauseConnector(String connectorName) {

        Instance connector = getConnector(connectorName, false);
        if (null == connector) {
            throw new RuntimeException("connector " + connectorName + "不存在");
        }
        try {
            String url = connector.getHost();
            OKHttpClient.doPut(URL_PREFIX + url + "/connectors/" + connectorName + "/pause", null, null, 0);
        } catch (Exception e) {
            LOG.error("暂停connector失败" ,e);
            throw new RuntimeException("暂停connector失败", e);
        }
    }

    /**
     * 恢复connector
     * @param connectorName
     */
    public synchronized static void resumeConnector(String connectorName) {

        Instance connector = getConnector(connectorName, false);
        if (null == connector) {
            throw new RuntimeException("connector " + connectorName + "不存在");
        }
        try {
            String url = connector.getHost();
            OKHttpClient.doPut(URL_PREFIX + url + "/connectors/" + connectorName + "/resume", null, null, 0);
        } catch (Exception e) {
            LOG.error("恢复connector失败" ,e);
            throw new RuntimeException("恢复connector失败", e);
        }
    }

    /**
     * 重启connector
     * @param connectorName
     */
    public synchronized static void restartConnector(String connectorName) {

        Instance connector = getConnector(connectorName, false);
        if (null == connector) {
            throw new RuntimeException("connector " + connectorName + "不存在");
        }
        try {
            String url = connector.getHost();
            OKHttpClient.doPost(URL_PREFIX + url + "/connectors/" + connectorName + "/restart", null, null, null,0);
        } catch (Exception e) {
            LOG.error("重启connector失败" ,e);
            throw new RuntimeException("重启connector失败", e);
        }
    }

    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Instance {
        @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Task {
            private String connector;
            private Integer task;
        }
        private String name;
        @JsonIgnore
        private String host;
        private Properties config;
        private String type;
        private List<Task> tasks;
    }


    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Status {
        @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Task {
            private Integer id;
            private String state;
            @JsonProperty("worker_id")
            private String workerId;
        }
        private String name;
        private Map<String,Object> connector;
        private String type;
        private List<Task> tasks;
    }
}
