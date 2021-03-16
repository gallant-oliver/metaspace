package io.zeta.metaspace.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;

public enum IndexType {
    /**
     * 原子指标
     */
    INDEXATOMIC(1),
    /**
     * 派生指标
     */
    INDEXDERIVE(2),
    /**
     * 复合指标
     */
    INDEXCOMPOSITE(3);

    private int intValue;
    private static HashMap<Integer, IndexType> mappings;
    private static synchronized HashMap<Integer, IndexType> getMappings() {
        if (mappings == null) {
            mappings = new HashMap();
        }
        return mappings;
    }
    private IndexType(int intValue) {
        this.intValue = intValue;
        getMappings().put(intValue, this);
    }
    @JsonValue
    public int getValue() {
        return this.intValue;
    }
    @JsonCreator
    public static IndexType forValue(int value) {
        return (IndexType)getMappings().get(value);
    }

    public static boolean contains(int value){
        return getMappings().containsKey(value);
    }

}
