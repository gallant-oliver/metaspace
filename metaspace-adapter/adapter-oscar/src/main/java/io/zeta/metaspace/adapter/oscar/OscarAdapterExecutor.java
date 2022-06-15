package io.zeta.metaspace.adapter.oscar;

import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class OscarAdapterExecutor extends AbstractAdapterExecutor {

    public OscarAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "SELECT o.created AS create_time from all_objects o where o.object_name=? and o.owner=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try (Connection connection = getAdapterSource().getConnection()) {
            statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String time = resultSet.getString("create_time");
                return DateUtils.parseDateTime(time);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        } finally {
            try {
                statement.close();
                resultSet.close();
            } catch (Exception e) {
                throw new AtlasBaseException(e);
            }
        }
        return null;
    }

    @Override
    public boolean isIgnoreColumn(String columnName) {
        return super.isIgnoreColumn(columnName) || AdapterTransformer.TEMP_COLUMN_RNUM.equalsIgnoreCase(columnName);
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "select s.size as data_length from sys_class c join v_segment_info s on c.oid = s.relid where c.relname='%s' and c.relnamespace = (select oid from sys_namespace n where n.nspname = '%s');";
        db = db.replaceAll("'", "''");
        tableName = tableName.replaceAll("'", "''");
        querySQL = String.format(querySQL, tableName, db);
        Connection connection = getAdapterSource().getConnection();
        return queryResult(connection, querySQL, resultSet -> {
            try {
                float totalSize = 0;
                while (resultSet.next()) {
                    totalSize = resultSet.getLong("data_length");
                }
                return totalSize;
            } catch (SQLException e) {
                throw new AtlasBaseException("查询表大小失败", e);
            } finally {
                try {
                    connection.close();
                } catch (Exception e) {
                    throw new AtlasBaseException("关闭神通数据库连接报错", e);
                }
            }
        });
    }


    @Override
    public String getCreateTableSql(String schema, String table) {
        String tableName = table.replaceAll("\"", "");
        String schemaName = schema.replaceAll("\"", "");
        String querySql = "select sys_get_tabledef from v_sys_table where schemaname = '" + schemaName + "' and tablename = '" + tableName + "'";
        return queryResult(querySql, resultSet -> {
            try {
                String sql = null;
                if (resultSet.next()) {
                    sql = resultSet.getString(1);
                }
                return sql;
            } catch (SQLException e) {
                throw new AtlasBaseException("查询建表语句失败", e);
            }
        });
    }

    @Override
    public Map<String, String> getUserObject(String schemaName, List<String> tableNameList) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtils.isEmpty(tableNameList)) {
            return result;
        }
        List<String> tableList = tableNameList.stream()
                .map(v -> v.contains(".") ? v.substring(v.lastIndexOf(".") + 1) : v).collect(Collectors.toList());
        StringBuilder sql = new StringBuilder(" Select owner,object_name,object_type From all_objects Where object_name in ( ");
        int length = tableNameList.size();
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                sql.append("?");
            } else {
                sql.append("?,");
            }
        }
        sql.append(")");

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try (Connection connection = getAdapterSource().getConnection()) {
            statement = connection.prepareStatement(sql.toString());
            int index = 1;
            for (String tableName : tableList) {
                statement.setString(index++, tableName.toUpperCase());
            }
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String owner = resultSet.getString("owner");
                String objectName = resultSet.getString("object_name");
                String objectType = resultSet.getString("object_type"); //TABLE VIEW

                result.put(objectName, objectType);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        } finally {
            try {
                statement.close();
                resultSet.close();
            } catch (SQLException e) {
                throw new AtlasBaseException(e);
            }
        }
        return result;
    }
}
