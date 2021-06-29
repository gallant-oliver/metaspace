package org.apache.atlas.notification.rdbms;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.utils.OKHttpClient;
import org.apache.atlas.ApplicationProperties;
import org.apache.commons.configuration.Configuration;
import com.google.common.cache.CacheBuilder;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

public class DebeziumConnector {

    private static final Cache<String, Instance> CONNECTOR_CACHE = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(60000, TimeUnit.MINUTES).build();

    private static final String KAFKA_CONNECTOR_URL = "kafka.connect.url";
    private static final Configuration CONF;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);

    static{
        try{
            CONF = ApplicationProperties.get();

        }catch (Exception e){
            throw new RuntimeException("初始化DebeziumConnector失败");
        }
    }

    public static Properties getConnectorConfig(String connectorName){

        Instance instance = CONNECTOR_CACHE.getIfPresent(connectorName);
        if(null == instance){
            String url = CONF.getString(KAFKA_CONNECTOR_URL);
            try{
                String content = OKHttpClient.doGet(url + "/connectors/" + connectorName, null, null);
                instance = MAPPER.readValue(content, Instance.class);
                CONNECTOR_CACHE.put(connectorName,instance);
            }catch (Exception e){
                throw new RuntimeException("获取debezium配置失败",e);
            }
        }
        return instance.getConfig();

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
