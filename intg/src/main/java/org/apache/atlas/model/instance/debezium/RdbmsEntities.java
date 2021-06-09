package org.apache.atlas.model.instance.debezium;

import org.apache.atlas.model.instance.AtlasEntity;

import java.util.List;

public class RdbmsEntities {
    /**
     * 实体集合
     */
    private List<AtlasEntity.AtlasEntityWithExtInfo> entities;

    /**
     * 数据血缘
     */
    private AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities;

    public RdbmsEntities(){
        super();
    }

    public RdbmsEntities(List<AtlasEntity.AtlasEntityWithExtInfo> entities){
        super();
        this.entities = entities;
    }

    public RdbmsEntities(AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities){
        super();
        this.bloodEntities = bloodEntities;
    }

    public RdbmsEntities(List<AtlasEntity.AtlasEntityWithExtInfo> entities, AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities){
        super();
        this.entities = entities;
        this.bloodEntities = bloodEntities;
    }

    public List<AtlasEntity.AtlasEntityWithExtInfo> getEntities() {
        return entities;
    }

    public void setEntities(List<AtlasEntity.AtlasEntityWithExtInfo> entities) {
        this.entities = entities;
    }

    public AtlasEntity.AtlasEntitiesWithExtInfo getBloodEntities() {
        return bloodEntities;
    }

    public void setBloodEntities(AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities) {
        this.bloodEntities = bloodEntities;
    }
}