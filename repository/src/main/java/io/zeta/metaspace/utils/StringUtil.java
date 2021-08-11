package io.zeta.metaspace.utils;

import java.util.List;

/**
 * @ClassName StringUtil
 * @Descroption TODO
 * @Author Lvmengliang
 * @Date 2021/8/3 13:24
 * @Version 1.0
 */
public class StringUtil {

    public static String dbsToString(List<String> dbs) {
        if (dbs == null || dbs.size() == 0) {
            return "";
        }
        StringBuffer str = new StringBuffer();
        for (String db : dbs) {
            str.append("'");
            str.append(db.replaceAll("'", "\\\\'"));
            str.append("'");
            str.append(",");
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }
}
