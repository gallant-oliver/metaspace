// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/7/24 16:32
 */
package io.zeta.metaspace.model.dataquality2;

import io.zeta.metaspace.model.dataquality.RuleType;

public enum HiveNumericType {
    TINYINT(0, "tinyint"),
    SMALLINT(1, "smallint"),
    INT(2, "int"),
    BIGINT(3, "bigint"),
    FLOAT(4, "float"),
    DOUBLE(5, "double"),
    DECIMAL(6, "decimal"),
    MEDIUMINT(8,"mediumint"),
    INTEGER(9,"integer"),
    BINARY_FLOAT(10,"binary_float"),
    BINARY_DOUBLE(11,"binary_double"),
    NUMBER(12,"number"),
    FLOAT4(13,"float4"),
    FLOAT8(14,"float8"),
    INT2(15,"int2"),
    INT4(16,"int4"),
    INT8(17,"int8"),
    NUMERIC(18,"numeric"),
    DECFLOAT(19,"decfloat"),
    REAL(20,"real"),
    UNKNOW(7, "unknow");
    String name;
    Integer code;
    HiveNumericType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static HiveNumericType getHiveNumericTypeByName(String name) {
        HiveNumericType defaultType = HiveNumericType.UNKNOW;
        for(HiveNumericType hnt : HiveNumericType.values()) {
            if(name.equals(hnt.name)) {
                return hnt;
            }
        }
        return defaultType;
    }

    public static boolean isNumericType(String name) {
        HiveNumericType currentType = getHiveNumericTypeByName(name);
        return currentType == HiveNumericType.UNKNOW? false:true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
