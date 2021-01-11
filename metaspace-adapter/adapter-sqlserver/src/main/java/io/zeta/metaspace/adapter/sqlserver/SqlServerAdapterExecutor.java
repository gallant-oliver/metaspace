package io.zeta.metaspace.adapter.sqlserver;

import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.utils.ByteFormat;
import io.zeta.metaspace.utils.DateUtils;
import org.apache.atlas.exception.AtlasBaseException;
import schemacrawler.schema.Schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class SqlServerAdapterExecutor extends AbstractAdapterExecutor {
    public SqlServerAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "select * from sys.tables t join sys.schemas s on t.schema_id=s.schema_id where t.name=? and s.name=?";

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
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
        String querySQL = "SET NOCOUNT ON;exec sp_spaceused '%s.[%s]', true; ";
        db=db.replaceAll("'","''");
        tableName=tableName.replaceAll("'","''");
        querySQL=String.format(querySQL,db,tableName);
        Connection connection = getAdapterSource().getConnection();
        return queryResult(connection, querySQL, resultSet -> {
            try {
                float totalSize = 0;
                while (resultSet.next()) {
                    String str = resultSet.getString("data");
                    if (str != null && str.length() != 0) {
                        totalSize = ByteFormat.parse(str);
                        break;
                    }
                }
                return totalSize;
            } catch (SQLException e) {
                throw new AtlasBaseException("查询表大小失败", e);
            }
        });
    }

    /**
     * sqlserver不需要添加，获取到的库会带上"
     */
    @Override
    public String addSchemaEscapeChar(String string) {
        return string;
    }
}
