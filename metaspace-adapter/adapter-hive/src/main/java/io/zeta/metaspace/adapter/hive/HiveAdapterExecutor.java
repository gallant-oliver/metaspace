package io.zeta.metaspace.adapter.hive;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        AdapterTransformer adapterTransformer = getAdapter().getAdapterTransformer();
        String querySQL = "show tblproperties " + adapterTransformer.caseSensitive(tableName);
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
     * 获取指定数据库表描述为空的表总个数
     * @param db
     * @param pool
     * @return
     */
    public float getTblRemarkCountByDb(String db,  String pool,  Map<String, Object> map) {
        float emptyCount = 0;
        String tableSql = "show tables";
        Connection connection = getAdapterSource().getConnection();
        List<String> tableNameList = new ArrayList<>();
        queryResult(connection, tableSql, resultSet -> {
            try {
                String tableName = "";
                while (resultSet.next()) {
                    tableName = resultSet.getString("name");
                    tableNameList.add(tableName);
                }
                return tableName;
            } catch (SQLException e) {
                throw new AtlasBaseException("获取指定数据库表所有表名", e);
            } finally {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new AtlasBaseException("数据库连接关闭失败", e);
                }
            }
        });

        // 初始化
        map.put("emptyCount",emptyCount);
        // 拿到指定数据库所有表
        map.put("emptyTblNameList", tableNameList);
        AdapterTransformer adapterTransformer = getAdapter().getAdapterTransformer();
        for (String tableName : tableNameList){
            String querySQL = "DESCRIBE FORMATTED " + adapterTransformer.caseSensitive(tableName);
            queryResult(connection, querySQL, resultSet -> {
                try {
                    String type = "";
                    String comment = "";
                    while (resultSet.next()) {
                        type = resultSet.getString("type");
                        comment = resultSet.getString("comment");
                        if (StringUtils.isBlank(type) || StringUtils.isBlank(comment)){
                            // 表描述为空+1
                            map.put("emptyCount", (Float)(map.get("emptyCount")) + 1);

                        } else {
                            // 1.15.0的表不保留
                            map.remove(tableName);
                        }
                    }
                    return emptyCount;
                } catch (SQLException e) {
                    throw new AtlasBaseException("获取指定数据库表描述为空的表总个数失败", e);
                }
            });
        }
        return (Float) map.get("emptyCount");
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
