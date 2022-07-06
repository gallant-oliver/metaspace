package io.zeta.metaspace.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 表字段对比枚举类
 *
 * @author w
 */
public enum TableColumnContrast {
    FIELD_ADD("ADD", "字段新增"),
    FIELD_DELETE("DELETE", "字段缺失"),
    FIELD_TYPE_CHANGE("TYPECHANGE ", "字段类型变更");

    private String code;
    @Getter
    private String desc;

    @JsonValue
    public String getCode() {
        return code;
    }

    TableColumnContrast(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
