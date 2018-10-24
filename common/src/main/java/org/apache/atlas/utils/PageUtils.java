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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PageUtils {

    /**
     *
     * @param it
     * @param offset 开始索引，从0开始
     * @param limit 最多返回的条数
     * @param <T>
     * @return
     */
    public static <T> List<T> pageList(Iterator<T> it, int offset, int limit) {

        List ret = new ArrayList();
        int index = -1;

        while (it.hasNext()) {
            T next = it.next();
            index++;
            if (index < offset) {
                continue;
            }
            if (ret.size() >= limit) {
                break;
            }
            ret.add(next);
        }
        return ret;
    }

}
