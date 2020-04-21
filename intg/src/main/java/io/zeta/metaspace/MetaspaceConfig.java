package io.zeta.metaspace;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MetaspaceConfig {
    private static Configuration conf;
    private static String[] hiveUrlArr;
    private static String hbaseConf;
    private static String impalaUrl;
    private static String hiveJobQueueName;
    private static String impalaResourcePool;
    private static String hiveConfig;
    private static String metaspaceUrl;
    public static List<String> systemCategory = new ArrayList<String>(){{
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
    }};
    private final static String hiveAdmin="metaspace";

    public static String getHiveJobQueueName() {
        return hiveJobQueueName;
    }
    public static String getHiveAdmin(){
        return hiveAdmin;
    }

    public static String getImpalaResourcePool() {
        return impalaResourcePool;
    }

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

    public static String getMetaspaceUrl() {
        return metaspaceUrl;
    }

    static {
        try {
            conf = ApplicationProperties.get();
            impalaResourcePool = conf.getString("metaspace.impala.resource.pool");
            hiveJobQueueName = conf.getString("metaspace.hive.queue");
            hiveUrlArr = conf.getStringArray("metaspace.hive.url");
            hbaseConf = conf.getString("metaspace.hbase.conf");
            impalaUrl = conf.getString("metaspace.impala.url");
            hiveConfig = conf.getString("metaspace.hive.conf");
            metaspaceUrl = conf.getString("metaspace.request.address");
            if (hiveUrlArr == null || hiveUrlArr.length==0) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.url未正确配置");
            }
            if (StringUtils.isEmpty(hbaseConf)) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hbase.conf未正确配置");
            }

            if (StringUtils.isEmpty(impalaUrl)) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.impala.url未正确配置");
            }

            if (StringUtils.isEmpty(hiveConfig)) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.conf未正确配置");
            }

            if (StringUtils.isEmpty(metaspaceUrl)) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.request.address未正确配置");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
