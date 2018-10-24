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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DateUtilsTest {

    @Test
    public void testInDay() {
        long now = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        String today = DateUtils.formatDateString(LocalDate.now());
        String yesterday = DateUtils.formatDateString(LocalDate.now().minusDays(1));

        assertTrue(DateUtils.inDay(today, now));
        assertFalse(DateUtils.inDay(yesterday, now));
    }
}
