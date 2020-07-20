package io.zeta.metaspace;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MetaspaceConfig {
    private static Configuration conf;
    private static String hbaseConf;
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

    private static int okHttpTimeout;

    public static int getOkHttpTimeout() {
        return conf.getInt("metaspace.okhttp.read.timeout",30);
    }

    public static String getHiveJobQueueName() {
        return conf.getString("metaspace.hive.queue","metaspace");
    }
    public static String getHiveAdmin(){
        return hiveAdmin;
    }

    public static String getImpalaResourcePool() {
        return conf.getString("metaspace.impala.resource.pool");
    }

    public static String getHiveConfig() {
        return hiveConfig;
    }


    public static String getHbaseConf() {
        return hbaseConf;
    }

    public static Queue<String> getHiveUrlQueue() {
        String[] hiveUrlArr = conf.getStringArray("metaspace.hive.url");
        if (hiveUrlArr == null || hiveUrlArr.length==0) {
            throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hive.url未正确配置"));
        }
        Queue<String> hiveUrlQueue = new LinkedList<>();
        hiveUrlQueue.addAll(Arrays.asList(hiveUrlArr));
        return hiveUrlQueue;
    }

    public static String getImpalaConf() {
        String impalaUrl = conf.getString("metaspace.impala.url");
        if (StringUtils.isEmpty(impalaUrl)) {
            throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.impala.url未正确配置"));
        }
        return impalaUrl;
    }

    public static String getMetaspaceUrl() {
        return metaspaceUrl;
    }

    static {
        try {
            conf = ApplicationProperties.get();
            hbaseConf = conf.getString("metaspace.hbase.conf");
            hiveConfig = conf.getString("metaspace.hive.conf");
            metaspaceUrl = conf.getString("metaspace.request.address");
            if (StringUtils.isEmpty(hbaseConf)) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.hbase.conf未正确配置");
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
