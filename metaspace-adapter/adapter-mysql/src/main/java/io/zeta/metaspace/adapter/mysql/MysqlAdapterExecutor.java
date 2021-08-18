package io.zeta.metaspace.adapter.mysql;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.utils.DateUtils;
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

public class MysqlAdapterExecutor extends AbstractAdapterExecutor {
    public MysqlAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "select create_time from information_schema.tables where table_schema= ? and table_name = ?";

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String time = resultSet.getString("create_time");
                return DateUtils.parseDateTime(time);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return null;
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "select data_length from information_schema.tables where table_schema='%s' and table_name='%s'";
        db = db.replaceAll("'", "''");
        tableName = tableName.replaceAll("'", "''");
        querySQL = String.format(querySQL, db, tableName);
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
            }
        });
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


    @Override
    public String getCreateTableSql(String schema, String table) {
        String querySql = "SHOW CREATE TABLE " + schema + "." + table;
        return queryResult(querySql, resultSet -> {
            try {
                String sql = null;
                if (resultSet.next()) {
                    sql = resultSet.getString(2);
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
        if(CollectionUtils.isEmpty(tableNameList)){
            return result;
        }
        List<String> tableList = tableNameList.stream()
                .map(v->v.contains(".") ? v.substring(v.lastIndexOf(".")+1) : v).collect(Collectors.toList());
        StringBuilder sql = new StringBuilder(" SELECT table_schema,table_name,table_type FROM information_schema.tables WHERE table_name IN ( ");
        int length = tableNameList.size();
        for(int i = 0;i < length;i++){
            if(i == length-1){
                sql.append("?");
            }else{
                sql.append("?,");
            }
        }
        sql.append(")");

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            int index = 1;
            for (String tableName : tableList){
                statement.setString(index++, tableName.toUpperCase());
            }
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String owner = resultSet.getString("table_schema");
                String objectName = resultSet.getString("table_name");
                String objectType = resultSet.getString("table_type"); //TABLE VIEW

                result.put(objectName,objectType);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return result;
    }
}
