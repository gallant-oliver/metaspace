package io.zeta.metaspace.utils;

import io.zeta.metaspace.DataSourcePoolConfig;
import io.zeta.metaspace.KerberosConfig;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.*;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourceType;
import io.zeta.metaspace.plugin.UnitTestPluginRepository;
import io.zeta.metaspace.plugin.UnitTestPomPluginDescriptorFinder;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.*;
import org.springframework.util.Base64Utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class AdapterUtils {
    private static PluginManager pluginManagerInstance = null;
    private static CopyOnWriteArrayList<Adapter> ADAPTERS;
    private static final Map<String, AdapterSource> adapterSourceMap = new ConcurrentHashMap<>();

    private static synchronized PluginManager getPluginManager(boolean isUnitTest) {
        if (pluginManagerInstance == null) {
            PluginManager pluginManager = new DefaultPluginManager() {
                protected Path createPluginsRoot() {
                    String pluginsDir = System.getProperty("metaspace.adapter.dir");
                    if (pluginsDir == null) {
                        pluginsDir = "adapters";
                    }
                    return Paths.get(pluginsDir);
                }

                @Override
                protected PluginDescriptorFinder createPluginDescriptorFinder() {
                    if (isUnitTest) {
                        return new CompoundPluginDescriptorFinder().add(new UnitTestPomPluginDescriptorFinder());
                    }
                    return super.createPluginDescriptorFinder();
                }

                @Override
                protected PluginRepository createPluginRepository() {
                    if (isUnitTest) {
                        return new CompoundPluginRepository()
                                .add(new UnitTestPluginRepository(getPluginsRoot()));
                    }
                    return super.createPluginRepository();
                }
            };
            log.info("Loading plugin from dir[" + pluginManager.getPluginsRoot().toAbsolutePath() + "]...");
            pluginManager.loadPlugins();
            pluginManager.startPlugins();

            List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
            for (PluginWrapper plugin : startedPlugins) {
                String pluginId = plugin.getDescriptor().getPluginId();
                log.info("load plugin ... pluginId ={},descriptor={}", pluginId, plugin.getDescriptor());
            }
            Runtime.getRuntime().addShutdownHook(new Thread(pluginManager::stopPlugins));
            pluginManagerInstance = pluginManager;
        }
        return pluginManagerInstance;
    }

    public static Collection<Adapter> findDatabaseAdapters() {
        return findDatabaseAdapters(false);
    }

    /**
     * ??????????????????????????????????????????
     */
    public static Collection<Adapter> findDatabaseAdapters(boolean isUnitTest) {
        if (ADAPTERS == null || ADAPTERS.size() == 0) {
            synchronized (AdapterUtils.class) {
                if (ADAPTERS == null || ADAPTERS.size() == 0) {
                    ADAPTERS = new CopyOnWriteArrayList<>();
                    PluginManager pluginManager = getPluginManager(isUnitTest);
                    List<PluginWrapper> plugins = pluginManager.getPlugins();
                    for (PluginWrapper plugin : plugins) {
                        List<AdapterExtensionPoint> extensions = pluginManager.getExtensions(AdapterExtensionPoint.class, plugin.getPluginId());
                        if (extensions.isEmpty()) {
                            throw new AdapterBaseException(String.format("Adapter[%s] not found in adapters.", plugin.getPluginId()));
                        } else if (extensions.size() > 1) {
                            throw new InternalException(String.format("%d adapters are found by name[%s].", extensions.size(), plugin.getPluginId()));
                        }
                        AdapterExtensionPoint element = extensions.stream().findAny().orElse(null);
                        PluginDescriptor descriptor = plugin.getDescriptor();
                        if (element != null) {
                            ADAPTERS.add(element.build(descriptor));
                        }
                    }
                }
            }
        }
        return ADAPTERS;
    }

    public static Adapter getAdapter(DataSourceType dataSourceType) {
        return getAdapter(dataSourceType.getName());
    }
    public static Adapter getAdapter(String engine) {
        return ADAPTERS.stream()
                .filter(adapter -> adapter.getName().equalsIgnoreCase(engine))
                .findAny()
                .orElseThrow(() -> new AdapterBaseException("???????????????????????????" + engine));
    }

    /**
     * ?????? AdapterSource ?????????????????????????????????????????????????????????
     */
    public static AdapterSource getAdapterSource(DataSourceInfo dataSourceInfo) {
        AdapterSource adapterSource = null;
        String dataSourceId = dataSourceInfo.getSourceId();
        String dataSourceType = dataSourceInfo.getSourceType();
        //mysql???????????????????????????????????????????????????????????????utf8
        if("MYSQL".equalsIgnoreCase(dataSourceType)){
            String param = dataSourceInfo.getJdbcParameter();
            if(StringUtils.isBlank(param)){
                    param = "characterEncoding=utf8";
                    dataSourceInfo.setJdbcParameter(param);
            }else{
                if(!param.contains("characterEncoding")){
                    param += "&characterEncoding=utf8";
                    dataSourceInfo.setJdbcParameter(param);
                }
            }
        }
        //??????????????????????????????????????????id?????????????????????????????? ?????????????????????????????????????????????????????????
        if(StringUtils.isBlank(dataSourceId)){
            log.info("?????????????????????sourceId??????,??????????????????");
            List<String> connectionConfigList = Arrays.asList(dataSourceInfo.getIp(),dataSourceInfo.getPort(),
                    dataSourceInfo.getUserName(),dataSourceInfo.getDatabase(),dataSourceInfo.getServiceType());
            dataSourceId = String.join("-",connectionConfigList);
        }
        synchronized (dataSourceId.intern()) {
            if (StringUtils.isNotEmpty(dataSourceId) && adapterSourceMap.containsKey(dataSourceId)) {
                adapterSource = adapterSourceMap.get(dataSourceId);
                if (adapterSource != null) {
                    if (judgeNotReload(adapterSource.getDataSourceInfo(), dataSourceInfo)) {
                        return adapterSource;
                    } else {
                        log.info("????????????????????? " + dataSourceId + "????????? :" + dataSourceInfo.toString());
                        adapterSource.closeDataSource();
                    }
                }
            }
            log.info("??????????????? {}", dataSourceId);
            adapterSource = getAdapter(dataSourceInfo.getSourceType()).getNewAdapterSource(dataSourceInfo, DataSourcePoolConfig.getDefaultDataSourcePool());
            if (StringUtils.isNotEmpty(dataSourceId)) {
                adapterSourceMap.put(dataSourceId, adapterSource);
            }
        }
        return adapterSource;
    }

    /**
     * ????????????????????????
     */
    public static boolean judgeNotReload(DataSourceInfo oldConfig, DataSourceInfo newConfig) {
        if (!Objects.equals(oldConfig.getIp(), newConfig.getIp())) {
            return false;
        }

        if (!Objects.equals(oldConfig.getPort(), newConfig.getPort())) {
            return false;
        }

        if (!Objects.equals(oldConfig.getDatabase(), newConfig.getDatabase())) {
            return false;
        }

        if (!Objects.equals(oldConfig.getUserName(), newConfig.getUserName())) {
            return false;
        }

        if (!Objects.equals(oldConfig.getPassword(), newConfig.getPassword())) {
            return false;
        }

        return Objects.equals(oldConfig.getJdbcParameter(), newConfig.getJdbcParameter());
    }


    public static AdapterExecutor getAdapterExecutor(DataSourceInfo dataSourceInfo) {
        return getAdapterSource(dataSourceInfo).getNewAdapterExecutor();
    }

    public static AdapterSource getHiveAdapterSource() {
        return getAdapter("HIVE").getNewAdapterSource(getBuildInHive(), null);
    }

    public static AdapterSource getImpalaAdapterSource() {
        return getAdapter("IMPALA").getNewAdapterSource(getBuildInImpala(), null);
    }

    public static DataSourceInfo getBuildInImpala() {
        DataSourceInfo dataSourceInfo = new DataSourceInfo();
        dataSourceInfo.setIp(MetaspaceConfig.getImpalaConf());
        dataSourceInfo.setJdbcParameter(KerberosConfig.isKerberosEnable() ? ";" + KerberosConfig.getImpalaJdbc() : "");
        dataSourceInfo.setPool(MetaspaceConfig.getImpalaResourcePool());
        return dataSourceInfo;
    }

    public static DataSourceInfo getBuildInHive() {
        DataSourceInfo dataSourceInfo = new DataSourceInfo();
        dataSourceInfo.setIp(String.join(",", MetaspaceConfig.getHiveUrlQueue()));
        dataSourceInfo.setJdbcParameter(KerberosConfig.isKerberosEnable() ? ";principal=" + KerberosConfig.getHivePrincipal() : "");
        dataSourceInfo.setPool(MetaspaceConfig.getHiveJobQueueName());
        return dataSourceInfo;
    }

    public static String getInstanceQualifiedName(DataSourceInfo dataSourceInfo) {
        String ip = dataSourceInfo.getIp();
        String port = dataSourceInfo.getPort();
        String instanceQualifiedName = String.format("%s:%s", ip, port);
        String serviceType = dataSourceInfo.getSourceType();
        if("ORACLE".equalsIgnoreCase(serviceType)){
            instanceQualifiedName = instanceQualifiedName + ":" + dataSourceInfo.getDatabase().toUpperCase();
        }
        return instanceQualifiedName;
    }

    public static String getDBQualifiedName(DataSourceInfo dataSourceInfo, String dbName) {
        return String.format("%s:%s", getInstanceQualifiedName(dataSourceInfo), dbName.toUpperCase());
    }

    public static String getTableQualifiedName(DataSourceInfo dataSourceInfo, String dbName, String tableName) {
        return String.format("%s:%s", getDBQualifiedName(dataSourceInfo,dbName), tableName.toUpperCase());
    }

    public static String getColumnQualifiedName(DataSourceInfo dataSourceInfo, String dbName, String tableName, String colName) {
        return String.format("%s:%s", getTableQualifiedName(dataSourceInfo,dbName,tableName), colName);
    }
    public static String getColumnQualifiedName(String tableTableQualifiedName, String colName) {
        return String.format("%s:%s", tableTableQualifiedName, colName);
    }
}
