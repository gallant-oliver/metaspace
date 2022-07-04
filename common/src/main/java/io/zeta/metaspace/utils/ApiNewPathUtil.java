package io.zeta.metaspace.utils;

import io.zeta.metaspace.model.share.ApiInfoV2;

/**
 * @author huangrongwen
 * @Description: api的path转换工具类
 * @date 2022/6/2818:24
 */
public class ApiNewPathUtil {
    public static StringBuilder getNewPath(ApiInfoV2 apiInfo){
        StringBuilder urlBuffer = new StringBuilder();
        urlBuffer.append("/api/metaspace/dataservice/");
        urlBuffer.append(apiInfo.getGuid());
        urlBuffer.append("/");
        urlBuffer.append(apiInfo.getVersion());
        if (apiInfo.getPath() != null && !apiInfo.getPath().startsWith("/")) {
            urlBuffer.append("/");
        }
        if (apiInfo.getPath() != null) {
            urlBuffer.append(apiInfo.getPath());
        }
        return urlBuffer;
    }
}
