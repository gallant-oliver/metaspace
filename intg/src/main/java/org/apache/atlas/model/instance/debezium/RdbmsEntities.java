package org.apache.atlas.model.instance.debezium;

import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.hadoop.util.hash.Hash;

import java.util.*;

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
         * 表
         */
        RDBMS_TABLE(3),
        /**
         * 列
         */
        RDBMS_COLUMN(4),
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

    public enum OperateType {
        /**
         * 新增
         */
        ADD(),
        /**
         * 删除
         */
        DROP(),
        /**
         * 更新
         */
        MODIFY();
    }

    /**
     * 实体集合,有序map不允许被覆盖
     */
    private final Map<OperateType, Map<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>> entityMap = new HashMap<OperateType, Map<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>>(){
        {

            put(OperateType.ADD,  new TreeMap<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>((o1, o2) ->
                    o1.order == o2.order ? 0 : (o1.order > o2.order ? 1 : -1)
            ));
            put(OperateType.DROP,  new TreeMap<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>((o1, o2) ->
                    o1.order == o2.order ? 0 : (o1.order < o2.order ? 1 : -1)
            ));
            put(OperateType.MODIFY,  new TreeMap<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>((o1, o2) ->
                    o1.order == o2.order ? 0 : (o1.order > o2.order ? 1 : -1)
            ));
        }
    };

    /**
     * key:未修改之前实体的qualifyName
     * value:修改之后的实体
     */
    private final Map<String, AtlasEntity.AtlasEntityWithExtInfo> renameMap = new HashMap<>();

    /**
     * 数据血缘
     */
    private AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities;

    public Map<OperateType, Map<EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>> getEntityMap() {
        return entityMap;
    }

    public AtlasEntity.AtlasEntitiesWithExtInfo getBloodEntities() {
        return bloodEntities;
    }

    public Map<String, AtlasEntity.AtlasEntityWithExtInfo> getRenameMap() {
        return renameMap;
    }

    public void setBloodEntities(AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities) {
        this.bloodEntities =  bloodEntities;
    }

    public static EntityType getType(String type){
        for(EntityType entityType :EntityType.values()){
            if(entityType.name().equalsIgnoreCase(type)){
                return entityType;
            }
        }
        return null;
    }
}