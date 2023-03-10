package org.apache.atlas.notification.rdbms;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
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

public class KafkaConnectorUtil {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectorUtil.class);
    private static final Cache<String, KafkaConnector> CONNECTOR_CACHE = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(60000, TimeUnit.MINUTES).build();

    private static final List<String> KAFKA_CONNECTOR_URLS;
    private static final ObjectMapper MAPPER;
    private static final String URL_PREFIX;
    private static final Map<String, String> CONNECTOR_CLASS_MAP;
    static {
        try {
            MAPPER = new ObjectMapper().configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            Configuration conf = ApplicationProperties.get();
            KAFKA_CONNECTOR_URLS = Arrays.asList(conf.getStringArray("oracle.kafka.connect.urls"));
            String url = KAFKA_CONNECTOR_URLS.get(0);
            URL_PREFIX = url.substring(0,url.indexOf("://")+3);
            CONNECTOR_CLASS_MAP = new HashMap<String, String>(){
                {
                    put("ORACLE", "io.zeta.metaspace.connector.oracle.OracleSourceConnector");
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("?????????Connector??????");
        }
    }

    public static String getConnectorClassByType(String dbType){
        if(dbType == null){
            throw new RuntimeException("??????ConnectorClass??????????????????????????????");
        }
        String type = dbType.toUpperCase();
        if(!CONNECTOR_CLASS_MAP.containsKey(type)){
            throw new RuntimeException("Connector??????????????????" + dbType + "????????????");
        }
        return CONNECTOR_CLASS_MAP.get(type);
    }

    /**
     * ??????Connector???????????????
     * @return
     */
    public static List<String> getConnectorUrls(){
        return new ArrayList<>(KAFKA_CONNECTOR_URLS);
    }

    /**
     * ??????connector??????
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
                LOG.warn("??????connector??????, url : {}, message : {}" ,url, e.getMessage());
            }
        }
        return connectorMap;
    }

    private static String getHost(String url) {
        return url.substring(URL_PREFIX.length());
    }

    /**
     * ??????connector??????
     * @param connectorName
     * @return
     */
    public static KafkaConnector getConnector(String connectorName) {

        return getConnector(connectorName, true);
    }

    /**
     * ??????connector??????
     * @param connectorName
     * @return
     */
    public static KafkaConnector.Config getConnectorConfig(String connectorName) {
        KafkaConnector.Config config = null;
        KafkaConnector connector = getConnector(connectorName);
        if (null != connector) {
            config = connector.getConfig();
        }
        return config;
    }


    /**
     * ??????connector??????
     * @param connectorName
     * @param useCache
     * @return
     */
    public static KafkaConnector getConnector(String connectorName, boolean useCache) {
        KafkaConnector instance = null;
        if (useCache) {
            instance = CONNECTOR_CACHE.getIfPresent(connectorName);
        }
        if (null == instance) {
            for (String url : KAFKA_CONNECTOR_URLS) {
                try {
                    String content = OKHttpClient.doGet(url + "/connectors/" + connectorName, null, null, 0);
                    instance = MAPPER.readValue(content, KafkaConnector.class);
                    if (null != instance && instance.getName() != null) {
                        instance.setHost(getHost(url));
                        CONNECTOR_CACHE.put(connectorName, instance);
                        break;
                    } else {
                        instance = null;
                    }
                } catch (Exception e) {
                    LOG.warn("??????connector??????:" + e.getMessage());
                }
            }
            if (null == instance && null != CONNECTOR_CACHE.getIfPresent(connectorName)) {
                CONNECTOR_CACHE.invalidate(connectorName);
            }

        }
        return instance;
    }

    /**
     * ??????connector??????
     * @param connectorName
     * @return
     */
    public static KafkaConnector.Status getConnectorStatus(String connectorName) {
        KafkaConnector.Status status = null;
        for (String url : KAFKA_CONNECTOR_URLS) {
            try {
                String content = OKHttpClient.doGet(url + "/connectors/" + connectorName + "/status", null, null, 0);
                status = MAPPER.readValue(content, KafkaConnector.Status.class);
                if (null != status && status.getName() != null) {
                    break;
                } else {
                    status = null;
                }
            } catch (Exception e) {
                LOG.warn("??????connector??????:" + e.getMessage());
            }
        }
        return status;
    }

    public synchronized static boolean startConnector(KafkaConnector kafkaConnector) {
        KafkaConnector newInstance = null;
        KafkaConnector connector = getConnector(kafkaConnector.getName(), false);
        if (null != connector) {
            LOG.warn("?????????{}???connector?????????????????????????????????????????????", kafkaConnector.getName());
            return false;
        }
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(ArrayList.class, String.class);
            String addUrl = KAFKA_CONNECTOR_URLS.get(0);
            int size = Integer.MAX_VALUE;
            for (String url : KAFKA_CONNECTOR_URLS) {
                List<String> connectorNames = null;
                try {
                    String content = OKHttpClient.doGet(url + "/connectors", null, null, 0);
                    connectorNames = MAPPER.readValue(content, javaType);
                } catch (Exception e) {
                    LOG.warn("connector url {} ????????????:{}", url, e.getMessage());
                }
                if(CollectionUtils.isEmpty(connectorNames)){
                    addUrl = url;
                    break;
                }else if (connectorNames.size() < size) {
                    addUrl = url;
                    size = connectorNames.size();
                }
            }
            String json = MAPPER.writeValueAsString(kafkaConnector);
            LOG.debug("??????connector,url {}/connectors; body {}", addUrl, json);
            String content = OKHttpClient.doPost(addUrl + "/connectors", null, null, json, 0);
            newInstance = MAPPER.readValue(content, KafkaConnector.class);
            if(null == newInstance || !kafkaConnector.getName().equalsIgnoreCase(newInstance.getName())){
                throw new RuntimeException("??????kafka connector???????????????????????????????????????" + content );
            }
            newInstance.setHost(getHost(addUrl));
            CONNECTOR_CACHE.put(newInstance.getName(), newInstance);
        } catch (Exception e) {
            LOG.error("??????connector??????" ,e);
            throw new RuntimeException("??????connector??????", e);
        }
        return true;
    }

    /**
     * ??????connector
     * @param connectorName
     */
    public synchronized static boolean stopConnector(String connectorName) {

        boolean result = true;
        KafkaConnector connector = getConnector(connectorName, false);
        if (null == connector) {
            LOG.warn("?????????{}???connector?????????????????????????????????????????????", connectorName);
            result = false;
        } else{
            try {
                String url = connector.getHost();
                OKHttpClient.doDelete(URL_PREFIX + url + "/connectors/" + connectorName,null,null, 0);
            } catch (Exception e) {
                result = false;
                LOG.error("??????connector??????" ,e);
                throw new RuntimeException("??????connector??????", e);
            }
        }
        if (null != CONNECTOR_CACHE.getIfPresent(connectorName)) {
            CONNECTOR_CACHE.invalidate(connectorName);
        }
        return result;
    }

    /**
     * ??????connector
     * @param connectorName
     */
    public synchronized static void pauseConnector(String connectorName) {

        KafkaConnector connector = getConnector(connectorName, false);
        if (null == connector) {
            throw new RuntimeException("connector " + connectorName + "?????????????????????");
        }
        try {
            String url = connector.getHost();
            OKHttpClient.doPut(URL_PREFIX + url + "/connectors/" + connectorName + "/pause", null, null, 0);
        } catch (Exception e) {
            LOG.error("??????connector??????" ,e);
            throw new RuntimeException("??????connector??????", e);
        }
    }

    /**
     * ??????connector
     * @param connectorName
     */
    public synchronized static void resumeConnector(String connectorName) {

        KafkaConnector connector = getConnector(connectorName, false);
        if (null == connector) {
            throw new RuntimeException("connector " + connectorName + "?????????????????????");
        }
        try {
            String url = connector.getHost();
            OKHttpClient.doPut(URL_PREFIX + url + "/connectors/" + connectorName + "/resume", null, null, 0);
        } catch (Exception e) {
            LOG.error("??????connector??????" ,e);
            throw new RuntimeException("??????connector??????", e);
        }
    }

    /**
     * ??????connector
     * @param connectorName
     */
    public synchronized static void restartConnector(String connectorName) {

        KafkaConnector connector = getConnector(connectorName, false);
        if (null == connector) {
            throw new RuntimeException("connector " + connectorName + "?????????????????????");
        }
        try {
            String url = connector.getHost();
            OKHttpClient.doPost(URL_PREFIX + url + "/connectors/" + connectorName + "/restart", null, null, null,0);
        } catch (Exception e) {
            LOG.error("??????connector??????" ,e);
            throw new RuntimeException("??????connector??????", e);
        }
    }


    /**
     * ??????connector class?????????????????????
     * @param connectorClass
     * @return
     */
    public static String getRdbmsType(String connectorClass){
        String rdbmsType = "";
        if(connectorClass != null){
            String[] arr = connectorClass.split("\\.");
            rdbmsType = arr.length > 1 ? arr[arr.length-2] : arr[0];
        }
        return rdbmsType.toUpperCase();
    }
}
