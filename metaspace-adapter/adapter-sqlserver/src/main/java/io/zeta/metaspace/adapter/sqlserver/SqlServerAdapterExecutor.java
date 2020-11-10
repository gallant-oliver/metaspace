package io.zeta.metaspace.adapter.sqlserver;

import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.utils.DateUtils;
import org.apache.atlas.exception.AtlasBaseException;

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
}
