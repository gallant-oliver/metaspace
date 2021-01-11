package io.zeta.metaspace.model.measure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.List;

@Data
public class MeasureMetrics {
    public String name;
    public Long tmst;
    public Value value;
    public JsonObject metadata;

    @Data
    public static class Value{
        public List<JsonObject> data;
    }
}
