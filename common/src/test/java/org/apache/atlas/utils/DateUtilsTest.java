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
    public void test() {
        String date = "2018-11-30";
        String month = "2018-11";
        String year = "2018";

        Assert.assertEquals("2018-11-29", DateUtils.yesterday(date));
        Assert.assertEquals("2018-10", DateUtils.lastMonth(date));
        Assert.assertEquals("2017", DateUtils.lastYear(date));

        Assert.assertEquals("2018-12-01", DateUtils.nextDay(date));
        Assert.assertEquals("2018-12", DateUtils.nextMonth(month));
        Assert.assertEquals("2019", DateUtils.nextYear(year));
    }



}
