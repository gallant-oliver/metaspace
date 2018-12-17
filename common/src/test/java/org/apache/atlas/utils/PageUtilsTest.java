package org.apache.atlas.utils;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;
import io.zeta.metaspace.utils.PageUtils;

import java.util.Iterator;
import java.util.List;

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
public class PageUtilsTest {

    @Test
    public void testPageList() {
        Iterator<Integer> it = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).iterator();
        int offset = 0;
        int limit = 5;
        List list = Lists.newArrayList(0, 1, 2, 3, 4);
        List<Integer> ret = PageUtils.pageList(it, offset, limit);
        assertEquals(ret, list);
    }

    @Test
    public void testPageList2() {
        Iterator<Integer> it = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).iterator();
        int offset = 5;
        int limit = 5;
        List list = Lists.newArrayList(5, 6, 7, 8, 9);
        List<Integer> ret = PageUtils.pageList(it, offset, limit);
        assertEquals(ret, list);
    }


}