package io.zeta.metaspace;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

public class SSOConfig {
    private static Configuration conf;
    private static String loginURL;
    private static String infoURL;
    private static String organizationURL;
    private static String organizationCountURL;
    private static String userInfoURL;

    public static String getLoginURL() {
        return loginURL;
    }


    public static String getInfoURL() {
        return infoURL;
    }

    public static String getOrganizationURL() {
        return organizationURL;
    }

    public static String getOrganizationCountURL() {
        return organizationCountURL;
    }

    public static String getUserInfoURL() {
        return userInfoURL;
    }

    static {
        try {
            conf = ApplicationProperties.get();
            loginURL = conf.getString("sso.login.url");
            infoURL = conf.getString("sso.info.url");
            organizationURL = conf.getString("sso.organization.url");
            organizationCountURL = conf.getString("sso.organization.count.url");
            userInfoURL = conf.getString("sso.user.info.url");
            if (loginURL == null || loginURL.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.login.url未正确配置");
            }
            if (infoURL == null || infoURL.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.info.url未正确配置");
            }
            if(StringUtils.isEmpty(organizationURL)) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.organization.url未正确配置");
            }
            if(StringUtils.isEmpty(organizationCountURL)) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.organization.count.url未正确配置");
            }
            if(StringUtils.isEmpty(userInfoURL)) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.user.info.url未正确配置");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
