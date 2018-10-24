package org.apache.atlas.utils;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

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
public class BytesUtilsTest {

    @Test
    public void testHumanReadableByteCount() {
        String b = BytesUtils.humanReadableByteCount(123);
        String kb = BytesUtils.humanReadableByteCount(123123);
        String mb = BytesUtils.humanReadableByteCount(123123123);
        String gb = BytesUtils.humanReadableByteCount(323123123112L);
        String tb = BytesUtils.humanReadableByteCount(3231231231121234L);
        assertEquals(b,"123 B");
        assertEquals(kb,"120.24 KB");
        assertEquals(mb,"117.42 MB");
        assertEquals(gb,"300.93 GB");
        assertEquals(tb,"2.87 PB");
    }
}