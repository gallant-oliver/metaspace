package io.zeta.metaspace.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtils.class);
    public static String getNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long currentTime = System.currentTimeMillis();
        return sdf.format(currentTime);
    }

    public static String date2String(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String getNow2() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        long currentTime = System.currentTimeMillis();
        return sdf.format(currentTime);
    }

    public static Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static Date get29day() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -29);
        return calendar.getTime();
    }

    public static String getDate(long date) {
        return new SimpleDateFormat("MM/dd").format(date);
    }

    public static Date getlast(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static Date getNext(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static long getLong(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = simpleDateFormat.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }

    public static String timestampToString(Timestamp tt){
        String str=null;
        if(tt==null){
            return str;
        }
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            str=sdf.format(tt);
        }catch (Exception e){
            LOG.error("timestampToString失败", e);
        }
        return str;
    }
}
