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
        loginURL = conf.getString("sso.login.url");
        if (loginURL == null || loginURL.equals("")) {
            throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.login.url未正确配置"));
        }
        return loginURL;
    }


    public static String getInfoURL() {
        infoURL = conf.getString("sso.info.url");
        if (infoURL == null || infoURL.equals("")) {
            throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.info.url未正确配置"));
        }
        return infoURL;
    }

    public static String getOrganizationURL() {
        organizationURL = conf.getString("sso.organization.url");
        if(StringUtils.isEmpty(organizationURL)) {
            throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.organization.url未正确配置"));
        }
        return organizationURL;
    }

    public static String getOrganizationCountURL() {
        organizationCountURL = conf.getString("sso.organization.count.url");
        if(StringUtils.isEmpty(organizationCountURL)) {
            throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.organization.count.url未正确配置"));
        }
        return organizationCountURL;
    }

    public static String getUserInfoURL() {
        userInfoURL = conf.getString("sso.user.info.url");
        if(StringUtils.isEmpty(userInfoURL)) {
            throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.user.info.url未正确配置"));
        }
        return userInfoURL;
    }

    static {
        try {
            conf = ApplicationProperties.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
