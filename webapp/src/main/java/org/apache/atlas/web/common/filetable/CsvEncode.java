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

package org.apache.atlas.web.common.filetable;

import com.gridsum.gdp.library.commons.exception.NotSupportedException;

import com.google.common.base.Ascii;
import org.mozilla.universalchardet.Constants;

public enum CsvEncode {
    UTF8(Constants.CHARSET_UTF_8, "UTF8"), GB18030("GBK", Constants.CHARSET_GB18030, "GB2312", Constants.CHARSET_HZ_GB_2312), Unicode("UTF-16", "UTF16", Constants.CHARSET_UTF_16BE, Constants.CHARSET_UTF_16LE);
    private final String[] codecs;

    CsvEncode(String... codecs) {
        this.codecs = codecs;
    }

    public static CsvEncode of(String code) {
        for (CsvEncode csvEncode : CsvEncode.values()) {
            if (Ascii.equalsIgnoreCase(csvEncode.name(), code)) {
                return csvEncode;
            }
            for (String codec : csvEncode.codecs) {
                if (Ascii.equalsIgnoreCase(codec, code)) {
                    return csvEncode;
                }
            }
        }
        throw NotSupportedException.notSupport("Codec", code);
    }
}
