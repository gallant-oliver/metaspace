package org.apache.atlas.model.instance.debezium;

import org.apache.atlas.model.instance.AtlasEntity;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RdbmsEntities {

    public enum EntityType {
        /**
         * 数据库实例
         */
        RDBMS_INSTANCE(1),
        /**
         * 数据库
         */
        RDBMS_DB(2),
        /**
         * 列
         */
        RDBMS_COLUMN(3),
        /**
         * 表
         */
        RDBMS_TABLE(4),
        /**
         * 关系
         */
        PROCESS(5),
        /**
         * 索引
         */
        RDBMS_INDEX(6),
        /**
         * 外键
         */
        RDBMS_FOREIGN_KEY(7);

        EntityType(int order){
            this.order = order;
        }
        private int order;

        private int getOrder(){
            return this.order;
        }


    }
    /**
     * 实体集合,有序map不允许被覆盖
     */
    private SortedMap<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> entityMap =
            new TreeMap<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>((o1, o2) -> {
                if(o1.order == o2.order){
                    return 0;
                }
                return o1.order < o2.order ? 1 : -1;
            });

    /**
     * 数据血缘
     */
    private AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities;

    public SortedMap<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> getEntityMap() {
        return entityMap;
    }


    public AtlasEntity.AtlasEntitiesWithExtInfo getBloodEntities() {
        return bloodEntities;
    }

    public void setBloodEntities(AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities) {
        this.bloodEntities = bloodEntities;
    }
}