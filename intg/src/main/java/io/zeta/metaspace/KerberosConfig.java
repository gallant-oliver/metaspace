package io.zeta.metaspace;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;

public class KerberosConfig {
    private static Configuration conf;

    private static boolean kerberosEnable = false;
//    private static String metaspaceAdmin;
//    private static String metaspaceKeytab;

    public static boolean isKerberosEnable() {
        return kerberosEnable;
    }


    public static String getHivePrincipal() throws AtlasBaseException {
        String hivePrincipal = conf.getString("metaspace.hive.principal");
        if (hivePrincipal == null || hivePrincipal.equals("")) {
            throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.principal未正确配置");
        }
        return hivePrincipal;
    }

    public static String getImpalaPrincipal() {
        return conf.getString("metaspace.impala.principal");
    }

    public static String getImpalaJdbc() {
        return conf.getString("metaspace.impala.kerberos.jdbc");
    }

    static {
        try {
            conf = ApplicationProperties.get();
            kerberosEnable = conf.getBoolean("atlas.authentication.method.kerberos",true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
