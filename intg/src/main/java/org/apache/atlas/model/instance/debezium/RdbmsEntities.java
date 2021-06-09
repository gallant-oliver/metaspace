package org.apache.atlas.model.instance.debezium;

import org.apache.atlas.model.instance.AtlasEntity;

import java.util.List;

public class RdbmsEntities {
    /**
     * 实体集合
     */
    private List<AtlasEntity.AtlasEntityWithExtInfo> entity;

    /**
     * 数据血缘
     */
    private AtlasEntity.AtlasEntitiesWithExtInfo entities;

    public List<AtlasEntity.AtlasEntityWithExtInfo> getEntity() {
        return entity;
    }

    public void setEntity(List<AtlasEntity.AtlasEntityWithExtInfo> entity) {
        this.entity = entity;
    }

    public AtlasEntity.AtlasEntitiesWithExtInfo getEntities() {
        return entities;
    }

    public void setEntities(AtlasEntity.AtlasEntitiesWithExtInfo entities) {
        this.entities = entities;
    }
}