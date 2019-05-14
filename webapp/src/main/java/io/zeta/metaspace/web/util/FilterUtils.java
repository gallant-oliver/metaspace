package io.zeta.metaspace.web.util;

public class FilterUtils {

    public static boolean isSkipUrl(String requestURL) {
        if (requestURL.contains("v2/entity/uniqueAttribute/type/") || requestURL.endsWith("api/metaspace/v2/entity/") || requestURL.contains("/api/metaspace/admin/status") || requestURL.contains("api/metaspace/v2/entity")
                ||requestURL.contains("api/metaspace/metadata/supplementTable") || requestURL.contains("api/metaspace/metadata/business/trust")) {
            return true;
        }
        return false;
    }
}
