package io.zeta.metaspace.adapter.utils;

import io.zeta.metaspace.adapter.AdapterBaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

@Slf4j
public class KerberosTestUtils {
    private static final org.apache.commons.configuration.Configuration APPLICATION_PROPERTIES;
    public static final String ATLAS_AUTHENTICATION_PREFIX = "atlas.authentication.";
    public static final String AUTHENTICATION_KERBEROS_METHOD = ATLAS_AUTHENTICATION_PREFIX + "method.kerberos";
    public static final String AUTHENTICATION_PRINCIPAL = ATLAS_AUTHENTICATION_PREFIX + "principal";
    public static final String AUTHENTICATION_KEYTAB = ATLAS_AUTHENTICATION_PREFIX + "keytab";

    static {
        try {
            APPLICATION_PROPERTIES = ApplicationProperties.get();
        } catch (AtlasException e) {
            throw new AdapterBaseException(e);
        }
    }

    public static UserGroupInformation login() {
        try {
            String user = APPLICATION_PROPERTIES.getString(AUTHENTICATION_PRINCIPAL);
            String keyTabPath = APPLICATION_PROPERTIES.getString(AUTHENTICATION_KEYTAB);
            Configuration conf = new Configuration();
            conf.set("hadoop.security.authentication", "Kerberos");
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab(user, keyTabPath);
            return UserGroupInformation.getLoginUser();
        } catch (Exception e) {
            throw new AdapterBaseException(e);
        }
    }
}
