package io.zeta.metaspace.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 需求管理 - 过滤操作
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-07
 */
public enum FilterOperation {
    equal("="),
    NOT_equal("!="),
    greater_equal(">="),
    less_equal("<="),
    greater(">"),
    less("<"),
    ;
    
    @Getter
    @JsonValue
    private String desc;
    
    FilterOperation(String desc) {
        this.desc = desc;
    }
    
    @JsonCreator
    public static FilterOperation parseByDesc(String desc) {
        return Stream.of(FilterOperation.values())
                .filter(obj -> Objects.equals(desc, obj.getDesc()))
                .findFirst()
                .orElse(null);
    }
}
