package io.zeta.metaspace;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;

public class MetaspaceConfig {
    private static Configuration conf;
    private static String hdfsConf;
    private static String hiveUrl;
    private static String hbaseConf;
    private static String hiveConfig;

    public static String getHiveConfig() {
        return hiveConfig;
    }

    public static String getHbaseConf() {
        return hbaseConf;
    }

    public static String getHdfsConf() {
        return hdfsConf;
    }

    public static String getHiveUrl() {
        return hiveUrl;
    }

    static {
        try {
            conf = ApplicationProperties.get();
            hdfsConf = conf.getString("metaspace.hdfs.conf");
            hiveUrl = conf.getString("metaspace.hive.url");
            hbaseConf = conf.getString("metaspace.hbase.conf");
            hiveConfig = conf.getString("metaspace.hive.conf");
            if (hdfsConf == null || hdfsConf.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hdfs.conf未正确配置");
            }
            if (hiveUrl == null || hiveUrl.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.url未正确配置");
            }
            if (hbaseConf == null || hbaseConf.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hbase.conf未正确配置");
            }
            if (hiveConfig == null || hiveConfig.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.conf未正确配置");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
