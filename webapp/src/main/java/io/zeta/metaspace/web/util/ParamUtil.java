package io.zeta.metaspace.web.util;

import java.util.*;

public class ParamUtil {
    private ParamUtil(){
    }
    public static Boolean isNull(Object... objArray){
        if (objArray == null || objArray.length == 0){
            return Boolean.TRUE;
        }
        for (Object obj:objArray){
            if (obj == null){
                return Boolean.TRUE;
            }
            if (obj instanceof String && ( ((String) obj).replace(" ","").isEmpty()||((String) obj).replace(" ","").equals("null"))){
                    return Boolean.TRUE;
                }
            if (obj instanceof List && ((List<?>) obj).isEmpty()){
                return Boolean.TRUE;
            }
            if (obj instanceof Map && ((Map<?,?>) obj).isEmpty()){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
