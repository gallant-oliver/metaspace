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

package io.zeta.metaspace.web.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lixiang03
 * @Data 2020/3/24 16:36
 */
public class MetaspaceContext {
    private static ThreadLocal<Map<String,Object>> threadLocal = new InheritableThreadLocal<>();

    public static Object get(String key) {
        Map context = threadLocal.get();
        if (context==null){
            context = new HashMap();
            threadLocal.set(context);
        }
        return context.get(key);
    }

    public static void set(String key,Object value){
        Map<String,Object> context=threadLocal.get();
        if (context==null){
            context = new HashMap();
            threadLocal.set(context);
        }
        context.put(key,value);
    }

    public static void clear(){
        threadLocal.remove();
    }
}
