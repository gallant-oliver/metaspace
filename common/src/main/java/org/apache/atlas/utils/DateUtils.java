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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class DateUtils {


    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter YEAR = DateTimeFormatter.ofPattern("yyyy");

    /**
     * 时间戳是否在这天内
     *
     * @param date      日期，格式yyyy-MM-dd
     * @param timestamp 时间戳，毫秒
     * @return
     */
    public static boolean inDay(String date, long timestamp) {
        LocalDate startDay = parseLocalDate(date);
        LocalDate endDay = startDay.plusDays(1);

        LocalDateTime start = LocalDateTime.of(startDay.getYear(), startDay.getMonth(), startDay.getDayOfMonth(), 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(endDay.getYear(), endDay.getMonth(), endDay.getDayOfMonth(), 0, 0, 0);

        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());

        boolean inDay = dateTime.isAfter(start) && dateTime.isBefore(end);
        return inDay;
    }

    public static String today() {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());
    }

    public static String yesterday() {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now().minusDays(1));
    }

    public static String currentMonth() {
        return MONTH.format(LocalDate.now());
    }

    public static String lastMonth() {
        return MONTH.format(LocalDate.now().minusMonths(1));
    }

    public static String currentYear() {
        return YEAR.format(LocalDate.now());
    }

    public static String lastYear() {
        return YEAR.format(LocalDate.now().minusMonths(1));
    }

    public static LocalDate parseLocalDate(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String formatDateString(LocalDate localDate) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
    }

    public static String formatDateTime(long timestamp) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return DATE_TIME.format(date);
    }

}