package io.zeta.metaspace.model.enums;

import lombok.Getter;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

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
    DOUBLE("DOUBLE", "双精度"),
    BIGINT("BIGINT", "长整型"),
    BOOLEAN("BOOLEAN", "布尔值"),
    DECIMAL("DECIMAL", "高精度"),
    DATE("DATE", "日期类型"),
    TIMESTAMP("TIMESTAMP", "时间戳类型"),
    ;
    
    @Getter
    private String code;
    @Getter
    private String desc;
    
    DataStandardDataType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static DataStandardDataType parseByCode(String code) {
        return Stream.of(DataStandardDataType.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElseThrow(() -> new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据标准数据类型枚举值无效!"));
    }
    
    public static DataStandardDataType parseByDesc(String desc) {
        return Stream.of(DataStandardDataType.values())
                .filter(obj -> Objects.equals(desc, obj.getDesc()))
                .findFirst()
                .orElseThrow(() -> new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据标准数据类型值无效!"));
    }
}
