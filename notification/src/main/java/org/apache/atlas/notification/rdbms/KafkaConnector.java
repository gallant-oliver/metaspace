package org.apache.atlas.notification.rdbms;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.zeta.metaspace.utils.OKHttpClient;
import org.apache.atlas.ApplicationProperties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

public class KafkaConnector {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnector.class);
    private static final Cache<String, Instance> CONNECTOR_CACHE = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(60000, TimeUnit.MINUTES).build();

    private static final String KAFKA_CONNECTOR_URL = "oracle.kafka.connect.url";
    private static final String ORACLE_INIT_CONNECTOR_NAME = "oracle.init.connector.name";
    private static final Configuration CONF;
    private static final ObjectMapper MAPPER ;

    static{
        try{
            MAPPER = new ObjectMapper().configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            CONF = ApplicationProperties.get();
        }catch (Exception e){
            throw new RuntimeException("初始化DebeziumConnector失败");
        }
    }

    public static Properties getConnectorConfig(String connectorName){
        Properties config = null;
        Instance connector = getConnector(connectorName);
        if(null != connector) {
            config = connector.getConfig();
        }
        return config;
    }

    public static Instance getConnector(String connectorName){

        return getConnector(connectorName, true);
    }

    public static Instance getConnector(String connectorName, boolean useCache){
        Instance instance = null;
        if(useCache){
            instance = CONNECTOR_CACHE.getIfPresent(connectorName);
        }
        if(null == instance){
            String url = CONF.getString(KAFKA_CONNECTOR_URL);
            try{
                String content = OKHttpClient.doGet(url + "/connectors/" + connectorName, null, null);
                instance = MAPPER.readValue(content, Instance.class);
                if(null != instance && instance.getName() != null){
                    CONNECTOR_CACHE.put(connectorName,instance);
                }else{
                    instance = null;
                }
            }catch (Exception e){
                LOG.warn("获取connector失败:" + e.getMessage());
            }
        }

        String url = CONF.getString(KAFKA_CONNECTOR_URL);
        return instance;
    }

    public synchronized static Instance addConnector(Instance instance){

        Instance newInstance = null;
        Instance connector = getConnector(instance.getName());
        if(null != connector){
            throw new RuntimeException("添加connector失败: 名称为" + instance.getName() + "的connector已经存在");
        }
        try{
            String json = MAPPER.writeValueAsString(instance);
            String url = CONF.getString(KAFKA_CONNECTOR_URL);
            String content = OKHttpClient.doPost(url + "/connectors", json);
            newInstance = MAPPER.readValue(content, Instance.class);
            CONNECTOR_CACHE.put(newInstance.getName(),newInstance);
        }catch (Exception e){
            throw new RuntimeException("添加connector失败",e);
        }
        String initConnectorName = CONF.getString(ORACLE_INIT_CONNECTOR_NAME,"oracle_init_connector");
        removeConnector(initConnectorName);
        return newInstance;
    }

    public synchronized static void removeConnector(String connectorName){

        Instance connector = getConnector(connectorName, false);
        if(null != connector){
            try{
                String url = CONF.getString(KAFKA_CONNECTOR_URL);
                OKHttpClient.doDelete(url + "/connectors/" + connectorName);

            }catch (Exception e){
                throw new RuntimeException("删除connector失败",e);
            }
        }
        if(null != CONNECTOR_CACHE.getIfPresent(connectorName)){
            CONNECTOR_CACHE.invalidate(connectorName);
        }
    }

    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Instance{
        private String name;

        private Properties config;

        private String type;

        private List<Task> tasks;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Properties getConfig() {
            return config;
        }

        public void setConfig(Properties config) {
            this.config = config;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Task> getTasks() {
            return tasks;
        }

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
        }
    }
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Task{

        private String connector;

        private Integer task;

        public String getConnector() {
            return connector;
        }

        public void setConnector(String connector) {
            this.connector = connector;
        }

        public Integer getTask() {
            return task;
        }

        public void setTask(Integer task) {
            this.task = task;
        }
    }
}
