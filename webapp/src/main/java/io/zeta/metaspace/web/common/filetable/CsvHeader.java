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

package io.zeta.metaspace.web.common.filetable;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gridsum.gdp.library.commons.data.type.DataType;
import com.gridsum.gdp.library.commons.exception.VerifyException;
import com.gridsum.gdp.library.commons.utils.StringUtils;

public class CsvHeader {
    
    public static final String UTF8_BOM = "\uFEFF";

    final Pattern SQL_TABLE_OR_ColumnExt_NAME_PATTERN = Pattern.compile("[a-z_]\\w*");
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

    private final ColumnExt[] ColumnExts;

    public CsvHeader(String... headers) {
        this(headers, null);
    }

    public static void valid(ColumnExt[] ColumnExts) {
        List<ColumnExt> nonUniqueColumnExts = findNonUnique(ColumnExts);
        if (nonUniqueColumnExts.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (ColumnExt ColumnExt : nonUniqueColumnExts) {
                sb.append(",").append(ColumnExt.getName());
            }
            throw new VerifyException("列[" + sb.substring(1) + "]存在重复！");
        }
    }

    public static List<ColumnExt> findNonUnique(ColumnExt[] ColumnExts) {
        List<ColumnExt> lists = Lists.newArrayList();
        Map<ColumnExt, Integer> map = Maps.newHashMap();
        for (int i = 0; i < ColumnExts.length; i++) {
            ColumnExt key = ColumnExts[i];
            Integer count = map.get(key);
            if (count != null) {
                map.put(key, count + 1);
            } else {
                map.put(key, 1);
            }
        }
        for (ColumnExt ColumnExt : map.keySet()) {
            Integer count = map.get(ColumnExt);
            if (count > 1) {
                lists.add(ColumnExt);
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
        ColumnExts = new ColumnExt[headers.length];
        if (comments == null) {
            withoutComment(headers);
        } else {
            withComment(headers, comments);
        }
        // Do not check header
        //valid(ColumnExts);
    }

    private void withoutComment(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            String header = removeUTF8BOM(headers[i]);
            ColumnExt ColumnExt = new ColumnExt();
            ColumnExt.setType(DataType.STRING);

            header = header.toLowerCase();
            if (SQL_TABLE_OR_ColumnExt_NAME_PATTERN.matcher(header).matches()) {
                ColumnExt.setName(Ascii.toLowerCase(header));
            } else {
                ColumnExt.setName("column" + i);
                ColumnExt.setComment(header);
            }
            ColumnExts[i] = ColumnExt;
        }
    }

    private void withComment(String[] headers, String[] comments) {
        if (comments.length != headers.length) {
            throw new VerifyException("Headers length must be the same as comments length");
        }
        for (int i = 0; i < comments.length; i++) {
            String header = removeUTF8BOM(comments[i]);
            ColumnExt ColumnExt = new ColumnExt();
            ColumnExt.setType(DataType.STRING);

            header = header.toLowerCase();
            if (SQL_TABLE_OR_ColumnExt_NAME_PATTERN.matcher(header).matches()) {
                ColumnExt.setName(Ascii.toLowerCase(header));
            } else {
                ColumnExt.setName("column" + i);
                ColumnExt.setComment(header);
            }
            if (!Strings.isNullOrEmpty(comments[i])) {
                ColumnExt.setComment(removeUTF8BOM(comments[i]));
            }
            ColumnExts[i] = ColumnExt;
        }
    }


    public CsvHeader(List<ColumnExt> headers) {
        Preconditions.checkArgument(!Iterables.isEmpty(headers), "ColumnExts should not be null or empty.");
        ColumnExts = new ColumnExt[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            ColumnExt ColumnExt = headers.get(i);
            String header = ColumnExt.getName();
            if (SQL_TABLE_OR_ColumnExt_NAME_PATTERN.matcher(header).matches()) {
                ColumnExt.setName(Ascii.toLowerCase(header));
            } else {
                ColumnExt.setName("Column" + i);
                if (Strings.isNullOrEmpty(ColumnExt.getComment())) {
                    ColumnExt.setComment(header);
                } else {
                    ColumnExt.setComment(ColumnExt.getComment() + "[" + header + "]");
                }
            }
            ColumnExts[i] = ColumnExt;
        }
        // Do not check header
        //valid(ColumnExts);
    }

    public String[] getHeaders() {
        String[] headers = new String[ColumnExts.length];
        for (int i = 0; i < ColumnExts.length; i++) {
            String header = ColumnExts[i].getName();
            headers[i] = header;
        }
        return headers;
    }

    public int size() {
        return ColumnExts.length;
    }

    public List<String> getHeaderList() {
        List<String> headers = Lists.newArrayListWithCapacity(ColumnExts.length);
        for (int i = 0; i < ColumnExts.length; i++) {
            String header = ColumnExts[i].getName();
            headers.add(header);
        }
        return headers;
    }

    public ColumnExt[] getColumnExts() {
        return ColumnExts;
    }

    public List<ColumnExt> getColumnExtList() {
        List<ColumnExt> headers = Lists.newArrayListWithCapacity(ColumnExts.length);
        for (ColumnExt ColumnExt : ColumnExts) {
            headers.add(ColumnExt);
        }
        return headers;
    }

}
