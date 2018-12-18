package io.zeta.metaspace.web.util;

import java.util.Collection;

public class CollectionUtil {

    private static final StringBuffer STRING_BUFFER = new StringBuffer();

    public static <T> String formatCollection(Collection<T> collection) {
        // 清空buffer
        STRING_BUFFER.setLength(0);
        STRING_BUFFER.append("[");
        for (T elem : collection) {
            STRING_BUFFER.append(elem.toString() + ", ");
        }
        STRING_BUFFER.setLength(STRING_BUFFER.length() - 2);
        STRING_BUFFER.append("]");
        return STRING_BUFFER.toString();
    }
}
