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

package org.apache.atlas.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class BytesUtils {

    private static final int UNIT = 1024;

    public static String humanReadableByteCount(long bytes) {
        if (bytes < UNIT) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(UNIT));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(UNIT, exp), pre);
    }

    public static String kb(long bytes) {
        int exp = (int) (Math.log(bytes) / Math.log(UNIT));
        return String.format("%.2f", bytes / Math.pow(UNIT, 1));
    }


}
