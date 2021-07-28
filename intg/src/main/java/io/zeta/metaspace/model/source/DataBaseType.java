package io.zeta.metaspace.model.source;

import lombok.Data;

@Data
public class DataBaseType {

    /**
     * 数据库类型码
     */
    private String code;

    /**
     * 数据库类型名
     */
    private String name;
}
