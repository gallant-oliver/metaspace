package io.zeta.metaspace.model.enums;

import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 数据质量-数据标准-数据标准类型
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-12
 */
public enum DataStandardType {
    /**
     * 数据标准
     */
    DATA_STANDARD(1, "数据标准"),
    /**
     * 命名标准
     */
    NAMING_STANDARD(2, "命名标准"),
    ;
    
    @Getter
    private int code;
    @Getter
    private String desc;
    
    DataStandardType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static DataStandardType valueOf(int code) {
        return Stream.of(DataStandardType.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElse(null);
    }
}
