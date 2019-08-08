
package io.zeta.metaspace.model.metadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.zeta.metaspace.WhiteSpaceRemovalDeserializer;

import java.io.Serializable;

public class OperateLogRequest implements Serializable {

    private Query query;
    private int offset;
    private int limit;

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    private static class Query {
        @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
        private String type;
        @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
        private String starttime;
        @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
        private String endtime;
        @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
        private String result;
        @JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
        private String keyword;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStarttime() {
            return starttime;
        }

        public void setStarttime(String starttime) {
            this.starttime = starttime;
        }

        public String getEndtime() {
            return endtime;
        }

        public void setEndtime(String endtime) {
            this.endtime = endtime;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
    }
}
