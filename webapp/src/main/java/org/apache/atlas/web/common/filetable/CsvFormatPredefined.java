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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

public enum CsvFormatPredefined {
    COMMA(",", CSVFormat.EXCEL), TAB("\t", CSVFormat.TDF), SEMICOLON(";", CSVFormat.EXCEL);
    private final String delimiter;
    private final CSVFormat format;

    CsvFormatPredefined(String delimiter, CSVFormat format) {
        this.delimiter = delimiter;
        this.format = format;
    }

    public String delimiter() {
        return delimiter;
    }

    public CSVFormat format() {
        return format.withRecordSeparator('\n').withQuote('"').withQuoteMode(QuoteMode.ALL);
    }

    public static CsvFormatPredefined of(String value) {
        for (CsvFormatPredefined formatPredefined : CsvFormatPredefined.values()) {
            if (formatPredefined.name().equalsIgnoreCase(value) || formatPredefined.delimiter().equals(value)) {
                return formatPredefined;
            }
        }
        return null;
    }
}