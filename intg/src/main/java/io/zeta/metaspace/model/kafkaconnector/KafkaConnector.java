package io.zeta.metaspace.model.kafkaconnector;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class KafkaConnector {

    @JsonIgnore
    private String id;
    private String name;
    @JsonIgnore
    private String host;
    private Config config;
    private String type;
    private List<Task> tasks;

    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Config {
        @JsonProperty("connector.class")
        private String connectorClass = "io.zeta.metaspace.connector.oracle.OracleSourceConnector";
        @JsonProperty("db.user")
        private String dbUser;
        @JsonProperty("db.ip")
        private String dbIp;
        @JsonProperty("tasks.max")
        private int tasksMax = 1;
        @JsonProperty("db.port")
        private int dbPort;
        private String name;
        @JsonProperty("db.password")
        private String dbPassword;
        @JsonProperty("db.name")
        private String dbName;
        @JsonProperty("db.type")
        private String dbType;
        @JsonProperty("db.fetch.size")
        private int dbFetchSize = 10;
        @JsonProperty("start.scn")
        private long startScn = -1L;
        @JsonProperty("conn.type")
        private String serviceType;  //SID 或者 SERVICE_NAME
    }

    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Task {
        private String connector;
        private Integer task;
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