package io.zeta.metaspace;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;

public class MetaspaceConfig {
    private static Configuration conf;
    private static String[] hiveUrlArr;
    private static String hbaseConf;
    private static String impalaUrl;

    private static String hiveConfig;

    public static String getHiveConfig() {
        return hiveConfig;
    }


    public static String getHbaseConf() {
        return hbaseConf;
    }

    public static String[] getHiveUrl() {
        return hiveUrlArr;
    }

    public static String getImpalaConf() {
            return impalaUrl;
    }

    static {
        try {
            conf = ApplicationProperties.get();
            hiveUrlArr = conf.getStringArray("metaspace.hive.url");
            hbaseConf = conf.getString("metaspace.hbase.conf");
            impalaUrl = conf.getString("metaspace.impala.url");
            hiveConfig = conf.getString("metaspace.hive.conf");
            if (hiveUrlArr == null || hiveUrlArr.length==0) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.url未正确配置");
            }
            if (hbaseConf == null || hbaseConf.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hbase.conf未正确配置");
            }

            if (impalaUrl == null || impalaUrl.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.impala.url未正确配置");
            }

            if (hiveConfig == null || hiveConfig.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.conf未正确配置");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
