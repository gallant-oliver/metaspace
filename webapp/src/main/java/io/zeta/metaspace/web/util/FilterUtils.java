package io.zeta.metaspace.web.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FilterUtils {

    private FilterUtils() {
    }

    /**
     * 特殊排序参数
     */
    private static final List<String> SPECIAL_CONDITIONS = Lists.newArrayList("update", "update ", " update", " update ",
            "create", "create ", " create", " create ");

    private static List<String> skipUrl = new ArrayList<>();

    private static List<String> permissionUrl = new ArrayList<>();

    static {
        skipUrl.add("v2/entity/uniqueAttribute/type/");
        skipUrl.add("api/metaspace/v2/entity/");
        skipUrl.add("/api/metaspace/admin/status");
        skipUrl.add("api/metaspace/v2/entity");
        skipUrl.add("api/metaspace/metadata/supplementTable");
        skipUrl.add("api/metaspace/metadata/business/trust");
        skipUrl.add("api/metaspace/market/business");
        skipUrl.add("api/metaspace/metadata/refreshcache");
        skipUrl.add("api/metaspace/api");
        skipUrl.add("/api/metaspace/tenant");
        skipUrl.add("/api/metaspace/admin/version");
        skipUrl.add("/api/metaspace/cache");
        skipUrl.add("api/metaspace/metadata/update/supplementTable");

        permissionUrl.add("api/metaspace/health/check");
        permissionUrl.add("api/metaspace/health/check/password");
        permissionUrl.add("api/metaspace/businesses/excel/file/template");
        permissionUrl.add("api/metaspace/businesses/excel/category/template");
        permissionUrl.add("api/metaspace/businesses/excel/allcategory/template");
        permissionUrl.add("api/metaspace/public/tenant/requirements/download/file");
        permissionUrl.add("api/metaspace/info/deriveTable/exportById");
        permissionUrl.add("api/metaspace/info/deriveTable/importTemplate");
    }

    /**
     * 这种实现方式存在风险，而且粒度也不好控制
     *
     * @param requestURL
     * @return
     */
    public static boolean isSkipUrl(String requestURL) {
        return skipUrl.stream().anyMatch(requestURL::contains);
    }

    public static boolean isDataService(String requestURL) {
        return requestURL.contains("/api/metaspace/dataservice");
    }

    /**
     * 健康检查接口，不做sso认证
     *
     * @param requestURL
     * @return
     */
    public static Boolean isHealthCheck(String requestURL) {
        String result = StringUtils.substring(requestURL, requestURL.indexOf("api/metaspace"), requestURL.length());
        return permissionUrl.contains(result);
    }

    private static String replaceWithCase(String src, String search, String replace) {
        return StringUtils.replacePattern(src, "(?i)" + search, replace);
    }

    /**
     * 筛选sql中的关键字
     *
     * @param source
     * @return
     */
    public static String filterSqlKeys(String source) {
        if (StringUtils.isBlank(source)) {
            return "";
        }

        //半角括号替换为全角括号
        source = source.replace("'", "'''").replace(";", "");
        //去除执行SQL语句的命令关键字
        source = replaceWithCase(source, "select", "");
        source = replaceWithCase(source, "insert", "");

        if (SPECIAL_CONDITIONS.contains(source.toLowerCase())) {
            source = replaceWithCase(source, "update", "");
        }
        source = replaceWithCase(source, "delete", "");
        source = replaceWithCase(source, "drop", "");
        source = replaceWithCase(source, "truncate", "");
        source = replaceWithCase(source, "declare", "");
        source = replaceWithCase(source, "xp_cmdshell", "");
        source = replaceWithCase(source, "/add", "");
        source = replaceWithCase(source, "net user", "");
        //去除执行存储过程的命令关键字 
        source = replaceWithCase(source, "exec", "");
        source = replaceWithCase(source, "execute", "");
        //去除系统存储过程或扩展存储过程关键字
        source = replaceWithCase(source, "xp_", "x p_");
        source = replaceWithCase(source, "sp_", "s p_");
        //防止16进制注入
        source = replaceWithCase(source, "0x", "0 x");
        return source;
    }

}
