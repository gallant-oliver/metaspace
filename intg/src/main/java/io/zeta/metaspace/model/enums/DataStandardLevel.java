package io.zeta.metaspace.model.enums;

import lombok.Getter;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

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
    Source_layer(1, "贴源层"),
    /**
     * 基础层
     */
    Base_layer(2, "基础层"),
    /**
     * 通用层
     */
    Common_layer(4, "通用层"),
    /**
     * 应用层
     */
    Application_layer(5, "应用层"),
    ;
    
    @Getter
    private int code;
    @Getter
    private String desc;
    
    DataStandardLevel(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static DataStandardLevel parseByCode(int code) {
        return Stream.of(DataStandardLevel.values())
                .filter(obj -> Objects.equals(code, obj.getCode()))
                .findFirst()
                .orElseThrow(() -> new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据标准层级枚举值无效!"));
    }
    
    public static DataStandardLevel parseByDesc(String desc) {
        return Stream.of(DataStandardLevel.values())
                .filter(obj -> Objects.equals(desc, obj.getDesc()))
                .findFirst()
                .orElseThrow(() -> new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据标准层级值无效!"));
    }
}
