package org.apache.atlas.model.instance.debezium;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbmsMessage {
    private static final long serialVersionUID = 1L;
    private Object schema;

    private Payload payload;

    public Object getSchema() {
        return schema;
    }
    public void setSchema(Object schema) {
        this.schema = schema;
    }
    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
    @JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Payload {
        private static final long serialVersionUID = 1L;
        private Map<String,Object> before;
        private Map<String,Object> after;
        private Source source;
        private String op;
        @JsonProperty(value = "ts_ms")
        private long tsMs;
        private String databaseName;
        private String ddl;

        public Map<String, Object> getBefore() {
            return before;
        }

        public void setBefore(Map<String, Object> before) {
            this.before = before;
        }

        public Map<String, Object> getAfter() {
            return after;
        }

        public void setAfter(Map<String, Object> after) {
            this.after = after;
        }

        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }

        public long getTsMs() {
            return tsMs;
        }

        public void setTsMs(long tsMs) {
            this.tsMs = tsMs;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getDdl() {
            return ddl;
        }

        public void setDdl(String ddl) {
            this.ddl = ddl;
        }
    }


}