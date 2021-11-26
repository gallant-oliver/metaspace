package io.zeta.metaspace.model.enums;

import lombok.Getter;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

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
    
    public static DataStandardType parseByCode(int code) {
        return Stream.of(DataStandardType.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElseThrow(() -> new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据标准类型枚举值无效!"));
    }
    
    public static DataStandardType parseByDesc(String desc) {
        return Stream.of(DataStandardType.values())
                .filter(obj -> Objects.equals(desc, obj.getDesc()))
                .findFirst()
                .orElseThrow(() -> new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据标准类型值无效!"));
    }
}
