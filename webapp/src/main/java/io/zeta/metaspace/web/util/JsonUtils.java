package io.zeta.metaspace.web.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.gridsum.scrm.ronghotels.exception.UnCheckedException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * json 工具类
 *
 * @author 周磊
 * @date 2021/9/26 14:10
 */
@Slf4j
@UtilityClass
public class JsonUtils {
    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String toJson(Object object) {
        return gson.toJson(object);
    }
    
    public String toJson(Object src, Type typeOfSrc) {
        return gson.toJson(src, typeOfSrc);
    }
    
    public <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    public <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
    
    public <T> T fromJson(String json, TypeToken<T> typeToken) {
        return gson.fromJson(json, typeToken.getType());
    }
    
    public <T> T fromJson(JsonElement json, TypeToken<T> typeToken) {
        return gson.fromJson(json, typeToken.getType());
    }
    
    public JsonObject toJsonObject(String json) {
        return gson.fromJson(json, JsonObject.class);
    }
    
    public JsonArray toJsonArray(String json) {
        return gson.fromJson(json, JsonArray.class);
    }
    
    public <T> T fromJsonByJackson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            return null;
        }
    }
    
    public String toJsonByJackson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("get json by jackson error:", e);
            throw new UnCheckedException("get json by jackson error:".concat(e.getMessage()));
        }
    }
}
