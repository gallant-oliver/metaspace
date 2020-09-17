package io.zeta.metaspace.adapter.hive;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.adapter.AbstractAdapterSource;
import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.adapter.AdapterBaseException;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePool;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class HiveAdapterSource extends AbstractAdapterSource {
    private static final String JDBC_PREFIX = "jdbc:hive2://";
    private String DISCOVERY_MODE = "serviceDiscoveryMode=zooKeeper";

    public HiveAdapterSource(Adapter adapter, DataSourceInfo dataSourceInfo, DataSourcePool dataSourcePool) {
        super(adapter, dataSourceInfo, dataSourcePool);
        try {
            Class.forName(getDriverClass());
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
    }

    @Override
    public DataSource initDataSource() {
        return null;
    }

    /**
     * 获取 hive 的 jdbcUrl ，通过判断地址中是否包含 serviceDiscoveryMode=zooKeeper 来确定是否使用 zk 方式连接
     * 兼容旧配置，需要注意传入地址不能包含前缀  jdbc:hive://
     */
    @Override
    public String getJdbcUrl(String proxyUser, String schema) {
        List<String> urls = new ArrayList<>();
        String ip = getDataSourceInfo().getIp();
        if (ip.contains(DISCOVERY_MODE)) {
            if (StringUtils.isNotEmpty(schema)) {
                ip = ip.replaceFirst("/;", "/" + schema + ";");
            }
            urls.add(ip);
        } else {
            urls = Arrays.stream(ip.split(","))
                    .map(hiveUrl -> hiveUrl + "/" + (StringUtils.isNotEmpty(schema) ? schema : ""))
                    .collect(Collectors.toList());
        }

        return new Gson().toJson(urls.stream().map(str -> {
            StringBuilder url = new StringBuilder(JDBC_PREFIX).append(str);
            url.append(getDataSourceInfo().getJdbcParameter());
            if (StringUtils.isNotEmpty(proxyUser)) {
                url.append(";hive.server2.proxy.user=").append(proxyUser);
            }
            return url.toString();
        }).collect(Collectors.toList()));
    }


    @Override
    public String getDriverClass() {
        return "org.apache.hive.jdbc.HiveDriver";
    }


    @Override
    public Connection getConnection(String proxyUser, String schema, String pool) {
        List<String> jdbcUrls = new Gson().fromJson(getJdbcUrl(proxyUser, schema), new TypeToken<List<String>>() {
        }.getType());

        Properties properties = new Properties();
        if (pool == null || pool.length() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "资源池不能为空");
        properties.setProperty("hiveconf:tez.queue.name", pool);
        properties.setProperty("hiveconf:mapreduce.job.queuename", pool);

        for (String jdbcUrl : jdbcUrls) {
            try {
                return DriverManager.getConnection(jdbcUrl, properties);
            } catch (Exception e) {
                log.info("获取 Hive 连接失败:" + e.getMessage(), e);
            }
        }
        throw new AdapterBaseException("获取 Hive 连接失败: 没有可用连接地址");
    }

    @Override
    public Connection getConnectionForDriver() {
        return null;
    }

    @Override
    public AdapterExecutor getNewAdapterExecutor() {
        return new HiveAdapterExecutor(this);
    }

    @Override
    public void closeDataSource() {

    }

    @Override
    public Connection getConnection() {
        return getConnection(null, "", getDataSourceInfo().getPool());
    }
}
