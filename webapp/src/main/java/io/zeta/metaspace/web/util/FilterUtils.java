package io.zeta.metaspace.web.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FilterUtils {
    private static List<String> skipUrl = new ArrayList<String>(){{
        add("v2/entity/uniqueAttribute/type/");
        add("api/metaspace/v2/entity/");
        add("/api/metaspace/admin/status");
        add("api/metaspace/v2/entity");
        add("api/metaspace/metadata/supplementTable");
        add("api/metaspace/metadata/business/trust");
        add("api/metaspace/role/roles/sso");
        add("api/metaspace/role/users/sso");
        add("api/metaspace/market/business");
        add("api/metaspace/metadata/refreshcache");
        add("api/metaspace/api");
        add("/api/metaspace/tenant");
        add("/api/metaspace/admin/version");
        add("/api/metaspace/cache");
        add("api/metaspace/metadata/update/supplementTable");
        add("/api/metaspace/dataquality/monitor");
    }};

    public static boolean isSkipUrl(String requestURL) {

        if (skipUrl.stream().anyMatch(url->requestURL.contains(url))) {

            return true;
        }
        return false;
    }

    public static boolean isDataService(String requestURL){
        return requestURL.contains("/api/metaspace/dataservice");
    }
    
    private static String replaceWithCase(String src,String search,String replace,Boolean ignoreCase){
        return StringUtils.replacePattern(src,ignoreCase?"(?i)"+search : search,replace);
    }

    /**
     * 筛选sql中的关键字
     * @param source
     * @return
     */
    public static String filterSqlKeys(String source)
    {
        if (StringUtils.isBlank(source)){
            return "";
        }
            
        //半角括号替换为全角括号
        source = source.replace("'", "'''").replace(";","");
        //去除执行SQL语句的命令关键字
        source = replaceWithCase(source, "select", "", Boolean.TRUE);
        source = replaceWithCase(source, "insert", "", Boolean.TRUE);
        source = replaceWithCase(source, "update", "", Boolean.TRUE);
        source = replaceWithCase(source, "delete", "", Boolean.TRUE);
        source = replaceWithCase(source, "drop", "", Boolean.TRUE);
        source = replaceWithCase(source, "truncate", "", Boolean.TRUE);
        source = replaceWithCase(source, "declare", "", Boolean.TRUE);
        source = replaceWithCase(source, "xp_cmdshell", "", Boolean.TRUE);
        source = replaceWithCase(source, "/add", "", Boolean.TRUE);
        source = replaceWithCase(source, "net user", "", Boolean.TRUE);
        //去除执行存储过程的命令关键字 
        source = replaceWithCase(source, "exec", "", Boolean.TRUE);
        source = replaceWithCase(source, "execute", "", Boolean.TRUE);
        //去除系统存储过程或扩展存储过程关键字
        source = replaceWithCase(source, "xp_", "x p_", Boolean.TRUE);
        source = replaceWithCase(source, "sp_", "s p_", Boolean.TRUE);
        //防止16进制注入
        source = replaceWithCase(source, "0x", "0 x", Boolean.TRUE);
        return source;
    }

}
