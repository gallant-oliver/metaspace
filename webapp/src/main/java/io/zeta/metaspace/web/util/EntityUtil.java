package io.zeta.metaspace.web.util;

import com.google.gson.JsonObject;
import io.zeta.metaspace.utils.GsonUtils;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityUtil {

    public static final String PARTITION_ATTRIBUTE = "partitionKeys";

    public static List<String> extractPartitionKeyInfo(AtlasEntity entity) {
        List<AtlasObjectId> partitionKeys = new ArrayList<>();
        if (Objects.nonNull(entity.getAttribute(PARTITION_ATTRIBUTE))) {
            Object partitionObjects = entity.getAttribute(PARTITION_ATTRIBUTE);
            if (partitionObjects instanceof ArrayList<?>) {
                partitionKeys = (ArrayList<AtlasObjectId>) partitionObjects;
            }
        }
        List<String> guidList = new ArrayList<>();
        for (AtlasObjectId partitionKey : partitionKeys) {
            guidList.add(partitionKey.getGuid());
        }
        return guidList;
    }

    public static String generateBusinessId(String tenantId,String sourceId,String databaseId,String tableId){
        JsonObject object = new JsonObject();
        object.addProperty("tenantId",tenantId);
        object.addProperty("sourceId",sourceId);
        object.addProperty("databaseId",databaseId);
        object.addProperty("tableId",tableId);
        byte[] bytes = GsonUtils.getInstance().toJson(object).getBytes();
        try {
            String jsonStr = new String(Base64.encodeBase64(bytes),"UTF-8");
            return jsonStr;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Map<String,String> decodeBusinessId(String businessId){
        try {
            String result =  new String(Base64.decodeBase64(businessId),"UTF-8");
            Map<String,String> map = GsonUtils.getInstance().fromJson(result, Map.class);
            return map;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(generateBusinessId("12","23","34324",""));
        System.out.println("eyJ0ZW5hbnRJZCI6IjEyIiwic291cmNlSWQiOiIyMyIsImRhdGFiYXNlSWQiOiIzNDMyNCIsInRhYmxlSWQiOiIifQ==".length());
        System.out.println(decodeBusinessId("eyJ0ZW5hbnRJZCI6IjEyIiwic291cmNlSWQiOiIyMyIsImRhdGFiYXNlSWQiOiIzNDMyNCIsInRhYmxlSWQiOiIifQ=="));
    }
}
