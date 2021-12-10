package io.zeta.metaspace.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 需求管理-资源类型
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-07
 */
public enum ResourceType {
    API(1, "API"),
    TABLE(2, "中间库"),
    MESSAGE_QUEUE(3, "消息队列"),
    ;
    
    @Getter
    @JsonValue
    private int code;
    @Getter
    private String desc;
    
    ResourceType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    @JsonCreator
    public static ResourceType parseByCode(int code) {
        return Stream.of(ResourceType.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElse(null);
    }
    
    public static ResourceType parseByDesc(String desc) {
        return Stream.of(ResourceType.values())
                .filter(obj -> Objects.equals(desc, obj.getDesc()))
                .findFirst()
                .orElse(null);
    }
}
