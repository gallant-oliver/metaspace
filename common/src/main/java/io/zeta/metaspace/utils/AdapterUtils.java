package io.zeta.metaspace.utils;

import io.zeta.metaspace.DataSourcePoolConfig;
import io.zeta.metaspace.KerberosConfig;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.*;
import io.zeta.metaspace.model.datasource.DataSource;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class AdapterUtils {
    private static PluginManager pluginManagerInstance = null;
    private static CopyOnWriteArrayList<Adapter> ADAPTERS;
    private static final Map<String, AdapterSource> adapterSourceMap = new ConcurrentHashMap<>();

    private static synchronized PluginManager getPluginManager() {
        if (pluginManagerInstance == null) {
            PluginManager pluginManager = new DefaultPluginManager() {
                protected Path createPluginsRoot() {
                    String pluginsDir = System.getProperty("metaspace.adapter.dir");
                    if (pluginsDir == null) {
                        pluginsDir = "adapters";
                    }
                    return Paths.get(pluginsDir);
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

    /**
     * 找到支持的组件，并且启动组件
     */
    public static Collection<Adapter> findDatabaseAdapters() {
        if (ADAPTERS == null || ADAPTERS.size() == 0) {
            synchronized (AdapterUtils.class) {
                if (ADAPTERS == null || ADAPTERS.size() == 0) {
                    ADAPTERS = new CopyOnWriteArrayList<>();
                    PluginManager pluginManager = getPluginManager();
                    List<PluginWrapper> plugins = pluginManager.getPlugins();
                    for (PluginWrapper plugin : plugins) {
                        List<AdapterExtensionPoint> extensions = pluginManager.getExtensions(AdapterExtensionPoint.class, plugin.getPluginId());
                        if (extensions.isEmpty()) {
                            throw new AdapterBaseException(String.format("Adapter[%s] not found in adapters.", plugin.getPluginId()));
                        } else if (extensions.size() > 1) {
                            throw new InternalException(String.format("%d adapters are found by name[%s].", extensions.size(), plugin.getPluginId()));
                        }
                        AdapterExtensionPoint element = extensions.stream().findAny().get();
                        PluginDescriptor descriptor = plugin.getDescriptor();
                        ADAPTERS.add(element.build(descriptor));
                    }
                }
            }
        }
        return ADAPTERS;
    }

    public static Adapter getAdapter(String engine) {
        return ADAPTERS.stream()
                .filter(adapter -> adapter.getName().equalsIgnoreCase(engine))
                .findAny()
                .orElseThrow(() -> new AdapterBaseException("找不到数据源插件：" + engine));
    }

    /**
     * 获取 AdapterSource ，如果判断不需要重载则直接返回缓冲中的
     */
    public static AdapterSource getAdapterSource(DataSourceInfo dataSourceInfo) {
        AdapterSource adapterSource = null;
        String dataSourceId = dataSourceInfo.getSourceId();
        if (StringUtils.isNotEmpty(dataSourceId) && adapterSourceMap.containsKey(dataSourceId)) {
            adapterSource = adapterSourceMap.get(dataSourceId);
            if (adapterSource != null) {
                if (judgeNotReload(adapterSource.getDataSourceInfo(), dataSourceInfo)) {
                    return adapterSource;
                } else {
                    log.info("数据源重新加载 " + dataSourceId + "新配置 :" + dataSourceInfo.toString());
                    adapterSource.closeDataSource();
                }
            }
        }
        adapterSource = getAdapter(dataSourceInfo.getSourceType()).getNewAdapterSource(dataSourceInfo, DataSourcePoolConfig.getDefaultDataSourcePool());
        if (StringUtils.isNotEmpty(dataSourceId)) {
            adapterSourceMap.put(dataSourceId, adapterSource);
        }
        return adapterSource;
    }

    /**
     * 判断是否需要重载
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
}
