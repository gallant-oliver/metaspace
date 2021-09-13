package io.zeta.metaspace.web.util;

import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;

import java.util.ArrayList;
import java.util.List;
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
}
