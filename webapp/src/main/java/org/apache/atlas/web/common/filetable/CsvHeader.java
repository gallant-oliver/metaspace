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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gridsum.gdp.library.commons.data.schema.Column;
import com.gridsum.gdp.library.commons.data.type.DataType;
import com.gridsum.gdp.library.commons.exception.VerifyException;
import com.gridsum.gdp.library.commons.utils.StringUtils;

public class CsvHeader {
    
    public static final String UTF8_BOM = "\uFEFF";

    final Pattern SQL_TABLE_OR_COLUMN_NAME_PATTERN = Pattern.compile("[a-z_]\\w*");
    public static String removeUTF8BOM(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return "";
        }
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s.trim();
    }

    public static String formatValue(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return "";
        }
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        s = StringUtils.trim(s);
        if (s.isEmpty()) {
            return "";
        }
        if (s.endsWith("\r")) {
            s = StringUtils.substring(s, 1, s.length() - 1);
        }
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = StringUtils.substring(s, 1, s.length() - 1);
        }

        return s;
    }

    private final Column[] columns;

    public CsvHeader(String... headers) {
        this(headers, null);
    }

    public static void valid(Column[] columns) {
        List<Column> nonUniqueColumns = findNonUnique(columns);
        if (nonUniqueColumns.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Column column : nonUniqueColumns) {
                sb.append(",").append(column.getName());
            }
            throw new VerifyException("列[" + sb.substring(1) + "]存在重复！");
        }
    }

    public static List<Column> findNonUnique(Column[] columns) {
        List<Column> lists = Lists.newArrayList();
        Map<Column, Integer> map = Maps.newHashMap();
        for (int i = 0; i < columns.length; i++) {
            Column key = columns[i];
            Integer count = map.get(key);
            if (count != null) {
                map.put(key, count + 1);
            } else {
                map.put(key, 1);
            }
        }
        for (Column column : map.keySet()) {
            Integer count = map.get(column);
            if (count > 1) {
                lists.add(column);
            }
        }
        return lists;
    }

    /**
     * 如果注释不为空优先使用注释，如果为空则判断header是否合法，如果不合法使用header作为注释
     *
     * @param headers
     * @param comments
     */
    public CsvHeader(String[] headers, String[] comments) {
        Preconditions.checkArgument(headers != null && headers.length > 0, "Headers should not be null or empty.");
        columns = new Column[headers.length];
        if (comments == null) {
            withoutComment(headers);
        } else {
            withComment(headers, comments);
        }
        // 2016.12.08
        // Do not check header
        //valid(columns);
    }

    private void withoutComment(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            String header = removeUTF8BOM(headers[i]);
            Column column = new Column();
            column.setType(DataType.STRING);

            // 2016.12.13 header大写转小写
            header = header.toLowerCase();
            if (SQL_TABLE_OR_COLUMN_NAME_PATTERN.matcher(header).matches()) {
                column.setName(Ascii.toLowerCase(header));
            } else {
                column.setName("column" + i);
                column.setComment(header);
            }
            columns[i] = column;
        }
    }

    private void withComment(String[] headers, String[] comments) {
        if (comments.length != headers.length) {
            throw new VerifyException("Headers length must be the same as comments length");
        }
        for (int i = 0; i < comments.length; i++) {
            String header = removeUTF8BOM(comments[i]);
            Column column = new Column();
            column.setType(DataType.STRING);

            // 2016.12.13 header大写转小写
            header = header.toLowerCase();
            if (SQL_TABLE_OR_COLUMN_NAME_PATTERN.matcher(header).matches()) {
                column.setName(Ascii.toLowerCase(header));
            } else {
                column.setName("column" + i);
                column.setComment(header);
            }
            if (!Strings.isNullOrEmpty(comments[i])) {
                column.setComment(removeUTF8BOM(comments[i]));
            }
            columns[i] = column;
        }
    }


    public CsvHeader(List<ColumnExt> headers) {
        Preconditions.checkArgument(!Iterables.isEmpty(headers), "columns should not be null or empty.");
        columns = new Column[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            Column column = headers.get(i);
            String header = column.getName();
            if (SQL_TABLE_OR_COLUMN_NAME_PATTERN.matcher(header).matches()) {
                column.setName(Ascii.toLowerCase(header));
            } else {
                column.setName("column" + i);
                if (Strings.isNullOrEmpty(column.getComment())) {
                    column.setComment(header);
                } else {
                    column.setComment(column.getComment() + "[" + header + "]");
                }
            }
            columns[i] = column;
        }
        // 2016.12.08
        // Do not check header
        //valid(columns);
    }

    public String[] getHeaders() {
        String[] headers = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String header = columns[i].getName();
            headers[i] = header;
        }
        return headers;
    }

    public int size() {
        return columns.length;
    }

    public List<String> getHeaderList() {
        List<String> headers = Lists.newArrayListWithCapacity(columns.length);
        for (int i = 0; i < columns.length; i++) {
            String header = columns[i].getName();
            headers.add(header);
        }
        return headers;
    }

    public Column[] getColumns() {
        return columns;
    }

    public List<Column> getColumnList() {
        List<Column> headers = Lists.newArrayListWithCapacity(columns.length);
        for (Column column : columns) {
            headers.add(column);
        }
        return headers;
    }

}
