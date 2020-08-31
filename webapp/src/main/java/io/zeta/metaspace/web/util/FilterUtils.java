package io.zeta.metaspace.web.util;

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
}
