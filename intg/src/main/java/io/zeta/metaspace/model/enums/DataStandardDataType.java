package io.zeta.metaspace.model.enums;

import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 数据质量-数据标准-数据类型
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-12
 */
public enum DataStandardDataType {
    STRING("STRING", "字符串"),
    INT("INT", "整数"),
    BIGINT("BIGINT", "大整数"),
    FLOAT("FLOAT", "单精度浮点数"),
    DOUBLE("DOUBLE", "双精度浮点数"),
    DECIMAL("DECIMAL", "高精度浮点数"),
    BOOLEAN("BOOLEAN", "布尔值"),
    TIMESTAMP("TIMESTAMP", "时间戳"),
    DATE("DATE", "日期"),
    TIME("TIME", "时间"),
    ;
    
    @Getter
    private String code;
    @Getter
    private String desc;
    
    DataStandardDataType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static DataStandardDataType parse(String code) {
        return Stream.of(DataStandardDataType.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElse(null);
    }
}
