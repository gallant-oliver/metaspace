package io.zeta.metaspace.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 需求管理-资源状态
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-07
 */
public enum ResourceState {
    ONLINE(1, "上线"),
    OFFLINE(2, "下线"),
    ;
    
    @Getter
    @JsonValue
    private int code;
    @Getter
    private String desc;
    
    ResourceState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    @JsonCreator
    public static ResourceState parseByCode(int code) {
        return Stream.of(ResourceState.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElse(null);
    }
    
    public static ResourceState parseByDesc(String desc) {
        return Stream.of(ResourceState.values())
                .filter(obj -> Objects.equals(desc, obj.getDesc()))
                .findFirst()
                .orElse(null);
    }
}
