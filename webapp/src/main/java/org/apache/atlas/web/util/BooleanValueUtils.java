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

package org.apache.atlas.web.util;

import com.google.common.base.Preconditions;

/**
 * 字符串数值转BOOLEAN类型Utils
 *
 */
public class BooleanValueUtils {


    private static final String CHINESE_BOOLEAN_TRUE = "是";
    private static final String JAVA_BOOLEAN_TRUE = "true";
    private static final String ENGLISH_BOOLEAN_TRUE = "yes";
    private static final String EXCEL_BOOLEAN_TRUE = "1";

    private static final String CHINESE_BOOLEAN_FALSE = "否";
    private static final String JAVA_BOOLEAN_FALSE = "false";
    private static final String ENGLISH_BOOLEAN_FALSE = "no";
    private static final String EXCEL_BOOLEAN_FALSE = "0";

    public static String parseValue(String value) {
        Preconditions.checkNotNull(value, "Value to be parsed for BOOLEAN should not be null or empty");

        value = value.trim().toLowerCase();
        switch (value) {
            case CHINESE_BOOLEAN_TRUE:
            case JAVA_BOOLEAN_TRUE:
            case ENGLISH_BOOLEAN_TRUE:
            case EXCEL_BOOLEAN_TRUE:
                return JAVA_BOOLEAN_TRUE;
            case CHINESE_BOOLEAN_FALSE:
            case JAVA_BOOLEAN_FALSE:
            case ENGLISH_BOOLEAN_FALSE:
            case EXCEL_BOOLEAN_FALSE:
                return JAVA_BOOLEAN_FALSE;
            default:
                throw new RuntimeException("Value [" + value + "] not allowed to be converted into BOOLEAN type");
        }
    }
}
