package io.zeta.metaspace.web.util;

import java.text.SimpleDateFormat;

public class DateUtils {
    public static String getNow(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long currentTime = System.currentTimeMillis();
        return sdf.format(currentTime);
    }
}
