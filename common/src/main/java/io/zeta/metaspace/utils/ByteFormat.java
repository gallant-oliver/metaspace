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

package io.zeta.metaspace.utils;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * @author lixiang03
 * @Data 2020/4/21 15:52
 */
public class ByteFormat{
    private static final String GB = "GB";
    private static final String MB = "MB";
    private static final String KB = "KB";
    private static final String B = "B";

    public static float parse(String arg0) {
        int factor = 0;
        String substring=arg0.substring(0, arg0.length() - 2);
        if (arg0.contains(GB)) {
            factor = 1073741824;
        }
        else if (arg0.contains(MB)) {
            factor = 1048576;
        }
        else if (arg0.contains(KB)) {
            factor = 1024;
        }else if (arg0.contains(B)){
            factor = 1;
            substring = arg0.substring(0, arg0.length() - 1);
        }

        return Float.valueOf(substring) * factor;
    }
}
