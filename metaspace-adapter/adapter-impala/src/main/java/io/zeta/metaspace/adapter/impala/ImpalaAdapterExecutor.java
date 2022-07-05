package io.zeta.metaspace.adapter.impala;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.utils.ByteFormat;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImpalaAdapterExecutor extends AbstractAdapterExecutor {
    public ImpalaAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        AdapterTransformer adapterTransformer = getAdapter().getAdapterTransformer();
        String querySQL = "show table stats " + adapterTransformer.caseSensitive(tableName);
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
                            // 非空描述的表不保留
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
