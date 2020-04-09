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
        return SecurePlusEnable;
    }

    public static String getSecurePlusPrivilegeREST() {
        return SecurePlusPrivilegeREST;
    }

    static {
        try {
            conf = ApplicationProperties.get();
            String enable = conf.getString("metaspace.secureplus.enable");
            SecurePlusEnable = enable != null && enable.equals("true");
            if (SecurePlusEnable) {
                String SecurePlusHost = conf.getString("security.center.host");
                if (SecurePlusHost == null || SecurePlusHost.equals("")) {
                    throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "security.center.host未正确配置");
                }
                SecurePlusPrivilegeREST=SecurePlusHost+"/service/privilege/hivetable";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
