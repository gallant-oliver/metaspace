package io.zeta.metaspace;


import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;


public class SecurePlusConfig {
    private static Configuration conf;
    private static boolean SecurePlusEnable;
    private static String SecurePlusPrivilegeREST;

    public static boolean getSecurePlusEnable() {
        String enable = conf.getString("metaspace.secureplus.enable");
        SecurePlusEnable = enable != null && enable.equals("true");
        return SecurePlusEnable;
    }

    public static String getSecurePlusPrivilegeREST() {
        String securePlusHost = conf.getString("security.center.host");
        if (securePlusHost == null || securePlusHost.equals("")) {
            throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "security.center.host未正确配置"));
        }
        SecurePlusPrivilegeREST=securePlusHost+"/service/privilege/hivetable";
        return SecurePlusPrivilegeREST;
    }

    static {
        try {
            conf = ApplicationProperties.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
