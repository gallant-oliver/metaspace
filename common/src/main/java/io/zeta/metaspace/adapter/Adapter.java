package io.zeta.metaspace.adapter;


import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import org.pf4j.PluginDescriptor;
import schemacrawler.schemacrawler.IncludeAll;
import schemacrawler.schemacrawler.InclusionRule;

import java.util.Arrays;

public interface Adapter {
    PluginDescriptor getDescriptor();

    /**
     * 获取插件名称，也是数据源类型
     */
    default String getName() {
        return getDescriptor().getPluginId();
    }

    /**
     *  用数据源信息和连接池配置创建新的 AdapterSource
     */
    AdapterSource getNewAdapterSource(DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool);

    AdapterTransformer getAdapterTransformer();

    /**
     * 获取元数据时排除的 schema 正则
     */
    default InclusionRule getSchemaRegularExpressionRule() {
        return null;
    }

    /**
     * 获取元数据时排除的 Table 正则
     */
    default InclusionRule getTableRegularExpressionRule() {
        return null;
    }

    /**
     * 是否支持元数据同步
     */
    default boolean isSupportMetaDataSync() {
        return true;
    }
}
