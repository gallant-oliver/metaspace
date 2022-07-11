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
import java.util.stream.Collectors;

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
     *
     * @param db
     * @param pool
     * @return
     */
    public float getTblRemarkCountByDb(AdapterSource adapterSource, String user, String db, String pool, Map<String, Object> map) {
        float emptyCount = 0;
        String tableSql = "show tables";
        Connection connection = adapterSource.getConnection(user, db, pool);
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
                throw new AtlasBaseException("获取指定数据库表所有表名失败", e);
            } finally {
                try {
                    resultSet.close();
                    connection.close();
                } catch (Exception e) {
                    throw new AtlasBaseException("关闭数据库连接失败", e);
                }
            }
        });

        // 初始化
        map.put("emptyCount", emptyCount);
        // 复制集合
        List<String> copyList = tableNameList.stream().collect(Collectors.toList());
        AdapterTransformer adapterTransformer = getAdapter().getAdapterTransformer();
        for (String tableName : tableNameList) {
            Connection connectionNew = null;
            try {
                connectionNew = adapterSource.getConnection(user, db, pool);
                String querySQL = "DESCRIBE FORMATTED " + adapterTransformer.caseSensitive(tableName);
                map.put("flag", "false");
                queryResult(connectionNew, querySQL, resultSet -> {
                    try {
                        String type = "";
                        String comment = "";
                        while (resultSet.next()) {
                            type = resultSet.getString("type");
                            if (StringUtils.isNotBlank(type)){
                                type = type.trim();
                            }
                            comment = resultSet.getString("comment");
                            if (StringUtils.isNotBlank(type) && "comment".equals(type) && StringUtils.isNotBlank(comment)) {
                                // 表描述不为空的表删除
                                copyList.remove(tableName);
                                map.put("flag", "true");
                                break;
                            }
                        }
                        if ("false".equals(map.get("flag").toString())) {
                            // 表描述为空的表个数+1
                            map.put("emptyCount", (Float) (map.get("emptyCount")) + 1);
                        }
                        return emptyCount;
                    } catch (SQLException e) {
                        throw new AtlasBaseException("获取指定数据库表描述为空的表总个数失败", e);
                    } finally {
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            throw new AtlasBaseException("关闭数据库连接失败", e);
                        }
                    }
                });
            } catch (Exception e) {
                throw new AtlasBaseException("获取指定数据库表描述为空的表总个数失败", e);
            } finally {
                try {
                    connectionNew.close();
                } catch (SQLException e) {
                    throw new AtlasBaseException("关闭数据库连接失败", e);
                }
            }

        }
        // 拿到指定数据库下表描述为空的所有表
        map.put("emptyTblNameList", copyList);
        return (Float) map.get("emptyCount");
    }

    /**
     * 表或者字段增加转义符号
     */
    @Override
    public String addEscapeChar(String string) {
        return "`" + string + "`";
    }

    @Override
    public String addSchemaEscapeChar(String string) {
        return "`" + string + "`";
    }
}
