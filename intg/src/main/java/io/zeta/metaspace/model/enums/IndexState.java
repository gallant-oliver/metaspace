package io.zeta.metaspace.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;

/**
 * @author : zlt
 * @className : IndexState
 * @package: io.zeta.metaspace.model.enums
 * @Description : TODO
 * @date : 2021/3/18 15:22
 */
public enum IndexState {
    /**
     * 新建
     */
    CREATE(1),
    /**
     * 已发布
     */
    PUBLISH(2),
    /**
     * 已下线
     */
    OFFLINE(3),
    /**
     * 审核中
     */
    APPROVAL(4);

    private int intValue;
    private static HashMap<Integer, IndexState> mappings;
    private static synchronized HashMap<Integer, IndexState> getMappings() {
        if (mappings == null) {
            mappings = new HashMap();
        }
        return mappings;
    }
    private IndexState(int intValue) {
        this.intValue = intValue;
        getMappings().put(intValue, this);
    }
    @JsonValue
    public int getValue() {
        return this.intValue;
    }
    @JsonCreator
    public static IndexState forValue(int value) {
        return (IndexState)getMappings().get(value);
    }

    public static boolean contains(int value){
        return getMappings().containsKey(value);
    }
}
