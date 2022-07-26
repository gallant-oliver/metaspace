package io.zeta.metaspace.utils;


import org.apache.atlas.exception.AtlasBaseException;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * @author Gridsum
 */
public class SSLSocketClient {

    private SSLSocketClient() {}

    /**
     * 获取这个SSLSocketFactory
     *
     * @return SSLSocketFactory
     */
    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
    }

    /**
     * 获取TrustManager
     *
     * @return TrustManager
     */
    private static TrustManager[] getTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    /**
     * 获取HostnameVerifier
     *
     * @return HostnameVerifier
     */
    public static HostnameVerifier getHostnameVerifier() {
        return (s, sslSession) -> true;
    }

}
