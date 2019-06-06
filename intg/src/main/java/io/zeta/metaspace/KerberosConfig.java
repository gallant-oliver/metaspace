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
    private static String hivePrincipal;
    private static String impalaPrincipal;
    private static String impalaJdbc;

    public static boolean isKerberosEnable() {
        return kerberosEnable;
    }

//    public static String getMetaspaceAdmin() {
//        return metaspaceAdmin;
//    }
//
//    public static String getMetaspaceKeytab() {
//        return metaspaceKeytab;
//    }

    public static String getHivePrincipal() {
        return hivePrincipal;
    }

    public static String getImpalaPrincipal() {
        return impalaPrincipal;
    }

    public static String getImpalaJdbc() {
        return impalaJdbc;
    }

    static {
        try {
            conf = ApplicationProperties.get();
            String enable = conf.getString("atlas.authentication.method.kerberos");
            //默认关闭
            kerberosEnable = enable != null && enable.equals("true");
            if (kerberosEnable) {
//                metaspaceAdmin = conf.getString("metaspace.kerberos.admin");
//                metaspaceKeytab = conf.getString("metaspace.kerberos.keytab");
                hivePrincipal = conf.getString("metaspace.hive.principal");
                /*impalaPrincipal = conf.getString("metaspace.impala.principal");
                impalaJdbc = conf.getString("metaspace.impala.kerberos.jdbc");*/
//                if (metaspaceAdmin == null || metaspaceAdmin.equals("")) {
//                    throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.kerberos.admin未正确配置");
//                }
//                if (metaspaceKeytab == null || metaspaceKeytab.equals("")) {
//                    throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.kerberos.keytab未正确配置");
//                }
                if (hivePrincipal == null || hivePrincipal.equals("")) {
                    throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.principal未正确配置");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
