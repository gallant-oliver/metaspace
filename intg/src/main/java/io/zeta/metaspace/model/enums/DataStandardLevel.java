package io.zeta.metaspace.model.enums;

import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 数据质量-数据标准-数据层级
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-12
 */
public enum DataStandardLevel {
    /**
     * 贴源层
     */
    Source_layer(1),
    /**
     * 基础层
     */
    Base_layer(2),
    /**
     * 通用层
     */
    Common_layer(4),
    /**
     * 应用层
     */
    Application_layer(5),
    ;
    
    @Getter
    private int code;
    
    DataStandardLevel(int code) {
        this.code = code;
    }
    
    public static DataStandardLevel valueOf(int code) {
        return Stream.of(DataStandardLevel.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElse(null);
    }
}
