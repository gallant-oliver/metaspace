package io.zeta.metaspace.web.util;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取正式请求 IP 地址工具类
 * https://stackoverflow.com/questions/29910074/how-to-get-client-ip-address-in-java-httpservletrequest
 */
public class RequestIpUtils {
    public static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};
    private static final String SEPARATOR = ",";

    public static String getRealRequestIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                //处理多个代理的情况
                if (ip.length() > 15 && ip.indexOf(SEPARATOR) > 0) {
                    return ip.substring(0, ip.indexOf(SEPARATOR));
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
