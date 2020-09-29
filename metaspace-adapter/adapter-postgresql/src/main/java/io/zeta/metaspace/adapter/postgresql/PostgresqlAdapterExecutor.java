package io.zeta.metaspace.adapter.postgresql;

import com.healthmarketscience.sqlbuilder.AliasedObject;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.ValueObject;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@Slf4j
public class PostgresqlAdapterExecutor extends AbstractAdapterExecutor {
    public PostgresqlAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "select create_time from information_schema.tables where table_schema= ? and table_name = ?";

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
    public PageResult<LinkedHashMap<String, Object>> getSchemaPage(long limit, long offset) {
        String userName = getAdapterSource().getDataSourceInfo().getUserName();
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new AliasedObject(new CustomSql("table_schema"), "\"schemaName\""))
                .addCustomFromTable(new CustomSql("information_schema.table_privileges"))
                .addCondition(BinaryCondition.equalTo(new CustomSql("grantee"), new ValueObject(userName)))
                .addCondition(BinaryCondition.equalTo(new CustomSql("privilege_type"), new ValueObject("SELECT")));
        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, limit, offset);
        query.setIsDistinct(true);
        log.info("table sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getTablePage(String schemaName, long limit, long offset) {
        String userName = getAdapterSource().getDataSourceInfo().getUserName();
        schemaName = schemaName.replaceAll("'","''");
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new AliasedObject(new CustomSql("TABLE_NAME"), "\"tableName\""))
                .addCustomFromTable(new CustomSql("information_schema.table_privileges"))
                .addCondition(BinaryCondition.equalTo(new CustomSql("grantee"), new ValueObject(userName)))
                .addCondition(BinaryCondition.equalTo(new CustomSql("privilege_type"), new ValueObject("SELECT")))
                .addCondition(BinaryCondition.equalTo(new CustomSql("table_schema"), new ValueObject(schemaName)));
        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, limit, offset);
        log.info("table sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getColumnPage(String schemaName, String tableName, long limit, long offset) {
        schemaName = schemaName.replaceAll("'","''");
        tableName = tableName.replaceAll("'","''");
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new AliasedObject(new CustomSql("column_name"), "\"columnName\""))
                .addCustomColumns(new AliasedObject(new CustomSql("udt_name"), "\"type\""))
                .addCustomFromTable(new CustomSql("information_schema.columns"))
                .addCondition(ComboCondition.and().addConditions(BinaryCondition.equalTo(new CustomSql("table_schema"), new ValueObject(schemaName)), BinaryCondition.equalTo(new CustomSql("table_name"), new ValueObject(tableName))));
        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, limit, offset);
        log.info("column sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }


}
