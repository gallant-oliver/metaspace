package io.zeta.metaspace.adapter.impala;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.utils.ByteFormat;
import org.apache.atlas.exception.AtlasBaseException;

import java.sql.Connection;
import java.sql.SQLException;

public class ImpalaAdapterExecutor extends AbstractAdapterExecutor {
    public ImpalaAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "show table stats " + tableName;
        Connection connection = getAdapterSource().getConnection(MetaspaceConfig.getHiveAdmin(), db, pool);
        return queryResult(connection, querySQL, resultSet -> {
            try {
                float totalSize = 0;
                while (resultSet.next()) {
                    String str = resultSet.getString("size");
                    if (str != null && str.length() != 0) {
                        totalSize = ByteFormat.parse(str);
                        break;
                    }
                }
                return totalSize;
            } catch (SQLException e) {
                throw new AtlasBaseException("Impala服务异常", e);
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
