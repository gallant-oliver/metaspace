package io.zeta.metaspace.adapter.db2;

import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.utils.DateUtils;
import org.apache.atlas.exception.AtlasBaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Db2AdapterExecutor extends AbstractAdapterExecutor {
    public Db2AdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "select CREATE_TIME from syscat.tables where TABNAME = ? AND TABSCHEMA  = ?";

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String time = resultSet.getString("CREATE_TIME");
                return LocalDateTime.parse(time,  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS"));
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return null;
    }
}
