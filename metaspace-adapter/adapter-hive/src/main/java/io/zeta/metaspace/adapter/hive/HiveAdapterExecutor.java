package io.zeta.metaspace.adapter.hive;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HiveAdapterExecutor extends AbstractAdapterExecutor {
    public HiveAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public boolean tableExists(String proxyUser, String db, String tableName) {
        String sql = "show tables in " + db + " like '" + tableName + "'";
        Connection connection = getAdapterSource().getConnection(proxyUser, db, MetaspaceConfig.getHiveJobQueueName());
        return queryResult(connection, sql, resultSet -> {
            try {
                return resultSet.next();
            } catch (SQLException e) {
                throw new AtlasBaseException("Hive服务异常", e);
            }
        });
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "show tblproperties " + tableName;
        Connection connection = getAdapterSource().getConnection(MetaspaceConfig.getHiveAdmin(), db, pool);
        return queryResult(connection, querySQL, resultSet -> {
            try {
                float totalSize = 0;
                while (resultSet.next()) {
                    String str = resultSet.getString(1);
                    if ("totalSize".equals(str)) {
                        totalSize = resultSet.getLong(2);
                        break;
                    }
                }
                return totalSize;
            } catch (SQLException e) {
                throw new AtlasBaseException("Hive服务异常", e);
            }
        });
    }

    /**
     * 表或者字段增加转义符号
     */
    @Override
    public String addEscapeChar(String string) {
        return "`"+string+"`";
    }

    @Override
    public String addSchemaEscapeChar(String string) {
        return "`"+string+"`";
    }

}
