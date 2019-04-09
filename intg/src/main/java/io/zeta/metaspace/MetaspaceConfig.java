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
    private static String impalaConf;

    public static String getHbaseConf() {
        return hbaseConf;
    }

    public static String getHdfsConf() {
        return hdfsConf;
    }

    public static String getHiveUrl() {
        return hiveUrl;
    }

    public static String getImpalaConf() {
        return impalaConf;
    }

    static {
        try {
            conf = ApplicationProperties.get();
            hdfsConf = conf.getString("metaspace.hdfs.conf");
            hiveUrl = conf.getString("metaspace.hive.url");
            hbaseConf = conf.getString("metaspace.hbase.conf");
            impalaConf = conf.getString("metaspace.impala.conf");
            if (hdfsConf == null || hdfsConf.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hdfs.conf未正确配置");
            }
            if (hiveUrl == null || hiveUrl.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.url未正确配置");
            }
            if (hbaseConf == null || hbaseConf.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hbase.conf未正确配置");
            }
            if (impalaConf == null || impalaConf.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.impalaConf.conf未正确配置");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
