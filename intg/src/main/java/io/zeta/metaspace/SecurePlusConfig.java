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
                SecurePlusPrivilegeREST = conf.getString("metaspace.secureplus.privilegeREST");
                if (SecurePlusPrivilegeREST == null || SecurePlusPrivilegeREST.equals("")) {
                    throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.secureplus.privilegeREST未正确配置");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
