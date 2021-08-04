package io.zeta.metaspace;

import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class MetaspaceConfig {
    private static Configuration conf;
    private static String hbaseConf;
    private static String hiveConfig;
    private static String metaspaceUrl;
    private static boolean dataService;
    private static boolean operateLogModuleMoon;
    private static String[] dataSourceType;
    private static String[] dataSourceApiType;
    private static String[] userGroupAuthMenus;
    public static List<String> systemCategory = new ArrayList<String>() {{
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
    }};
    private static String[] sourceInfoRegisterType;
    private final static String hiveAdmin = "metaspace";

    private static int okHttpTimeout;

    public static int getOkHttpTimeout() {
        return conf.getInt("metaspace.okhttp.read.timeout", 30);
    }

    public static String getHiveJobQueueName() {
        return conf.getString("metaspace.hive.queue", "metaspace");
    }

    public static String getHiveAdmin() {
        return hiveAdmin;
    }

    public static boolean getDataService() {
        return dataService;
    }

    public static boolean getOperateLogModuleMoon() {
        return operateLogModuleMoon;
    }

    public static String[] getDataSourceType() {
        if (ArrayUtils.isEmpty(dataSourceType)) {
            throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.datasource.type未正确配置");
        }
        return dataSourceType;
    }

    public static String[] getSourceInfoRegisterType(){
        if (ArrayUtils.isEmpty(sourceInfoRegisterType)) {
            throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.sourceinfo.register.type未正确配置");
        }
        return sourceInfoRegisterType;
    }

    public static Integer[] getUserGroupAuthMenus() {
        if (ArrayUtils.isEmpty(userGroupAuthMenus)) {
            throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.userGroup.auth.menus未正确配置");
        }
        return Arrays.stream(userGroupAuthMenus).map(Integer::parseInt).collect(Collectors.toList()).toArray(new Integer[]{});
    }
    public static String[] getDataSourceApiType() {
        if (ArrayUtils.isEmpty(dataSourceApiType)) {
            throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.datasource.api.type未正确配置");
        }
        return dataSourceApiType;
    }

    public static String getImpalaResourcePool() {
        return conf.getString("metaspace.impala.resource.pool", "metaspace");
    }

    public static String getHiveConfig() {
        return hiveConfig;
    }


    public static String getHbaseConf() {
        return hbaseConf;
    }

    public static Queue<String> getHiveUrlQueue() {
        String[] hiveUrlArr = conf.getStringArray("metaspace.hive.url");
        if (hiveUrlArr == null || hiveUrlArr.length == 0) {
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
            dataService = conf.getBoolean("metaspace.dataservice", false);
            operateLogModuleMoon = conf.getBoolean("metaspace.operationlog.module.moon", false);
            dataSourceType = conf.getStringArray("metaspace.datasource.type");
            dataSourceApiType = conf.getStringArray("metaspace.datasource.api.type");
            userGroupAuthMenus = conf.getStringArray("metaspace.userGroup.auth.menus");
            sourceInfoRegisterType = conf.getStringArray("metaspace.sourceinfo.register.type");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
