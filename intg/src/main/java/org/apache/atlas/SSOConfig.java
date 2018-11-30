package org.apache.atlas;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;

public class SSOConfig {
    private static Configuration conf;
    private static String loginURL;
    private static String infoURL;

    public static String getLoginURL() {
        return loginURL;
    }


    public static String getInfoURL() {
        return infoURL;
    }


    static {
        try {
            conf = ApplicationProperties.get();
            loginURL = conf.getString("sso.login.url");
            infoURL = conf.getString("sso.info.url");
            if (loginURL == null || loginURL.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.login.url未正确配置");
            }
            if (infoURL == null || infoURL.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.info.url未正确配置");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
