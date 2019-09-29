package io.zeta.metaspace.web.metadata;

import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuxuetong
 * @date 2019-09-19 17:00
 */
public class MetaDataContext {

    private Map<String, AtlasEntity.AtlasEntityWithExtInfo> qualifiedNameEntityMap;

    public MetaDataContext() {
        qualifiedNameEntityMap = new HashMap<>();
    }


    public boolean isKownEntity(String qualifiedName) {
        return StringUtils.isNotEmpty(qualifiedName) && qualifiedNameEntityMap.get(qualifiedName) != null;
    }

    public void putEntity(String qualifiedName, AtlasEntity.AtlasEntityWithExtInfo entity) {
        qualifiedNameEntityMap.put(qualifiedName, entity);
    }

    public AtlasEntity.AtlasEntityWithExtInfo getEntity(String qualifiedName) {
        return qualifiedNameEntityMap.get(qualifiedName);
    }
}
