package io.zeta.metaspace.adapter.postgresql;

import com.healthmarketscience.sqlbuilder.AliasedObject;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.Subquery;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.dataquality2.HiveNumericType;
import io.zeta.metaspace.model.metadata.Parameters;
import com.healthmarketscience.sqlbuilder.ValueObject;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PostgresqlAdapterExecutor extends AbstractAdapterExecutor {
    public PostgresqlAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        long time = System.currentTimeMillis();
        String formatTime = DateUtils.formatDateTime(time);
        return DateUtils.parseDateTime(formatTime);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getSchemaPage(Parameters parameters) {
        String userName = getAdapterSource().getDataSourceInfo().getUserName();
        if (parameters.getQuery()==null){
            parameters.setQuery("");
        }
        SelectQuery query = new SelectQuery()
                .addCustomFromTable(
                        new Subquery(new SelectQuery()
                                             .addCustomColumns(new AliasedObject(new CustomSql("table_schema"), "\"schemaName\""))
                                             .addCustomFromTable(new CustomSql("information_schema.table_privileges"))
                                             .addCondition(BinaryCondition.equalTo(new CustomSql("grantee"), new CustomSql("'" + userName + "'")))
                                             .addCondition(BinaryCondition.equalTo(new CustomSql("privilege_type"), new CustomSql("'SELECT'")))
                                             .addCondition(BinaryCondition.like(new CustomSql("table_schema"), new CustomSql("'%" + parameters.getQuery() + "%'")))
                                             .setIsDistinct(true)
                        ) + " t1"
                ).addAllColumns();

        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, parameters.getLimit(), parameters.getOffset());
        log.info("table sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getTablePage(String schemaName, Parameters parameters) {
        String userName = getAdapterSource().getDataSourceInfo().getUserName();
        if (parameters.getQuery()==null){
            parameters.setQuery("");
        }
        schemaName = schemaName.replaceAll("'","''");
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new AliasedObject(new CustomSql("TABLE_NAME"), "\"tableName\""))
                .addCustomFromTable(new CustomSql("information_schema.table_privileges"))
                .addCondition(BinaryCondition.equalTo(new CustomSql("grantee"), new CustomSql("'" + userName + "'")))
                .addCondition(BinaryCondition.equalTo(new CustomSql("privilege_type"), new CustomSql("'SELECT'")))
                .addCondition(BinaryCondition.equalTo(new CustomSql("table_schema"), new CustomSql("'" + schemaName + "'")))
                .addCondition(BinaryCondition.like(new CustomSql("TABLE_NAME"), new CustomSql("'%" + parameters.getQuery() + "%'")));
        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, parameters.getLimit(), parameters.getOffset());
        log.info("table sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getColumnPage(String schemaName, String tableName, Parameters parameters,boolean isNum) {
        schemaName = schemaName.replaceAll("'","''");
        if (parameters.getQuery()==null){
            parameters.setQuery("");
        }
        tableName = tableName.replaceAll("'","''");
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new AliasedObject(new CustomSql("column_name"), "\"columnName\""))
                .addCustomColumns(new AliasedObject(new CustomSql("udt_name"), "\"type\""))
                .addCustomFromTable(new CustomSql("information_schema.columns"))
                .addCondition(ComboCondition.and().addConditions(BinaryCondition.equalTo(new CustomSql("table_schema"), new CustomSql("'" + schemaName + "'")), BinaryCondition.equalTo(new CustomSql("table_name"), new CustomSql("'" + tableName + "'"))))
                .addCondition(BinaryCondition.like(new CustomSql("column_name"), new CustomSql("'%" + parameters.getQuery() + "%'")));
        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, parameters.getLimit(), parameters.getOffset());

        // 过滤数值型字段
        if (isNum){
            List<String> columnType = Arrays.stream(HiveNumericType.values()).filter(type-> type.getCode() != 7).map(HiveNumericType::getName).collect(Collectors.toList());
            query.addCondition(new InCondition(new CustomSql("udt_name"),columnType));
        }
        log.info("column sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "select pg_relation_size('%s.%s') size; ";
        db=db.replaceAll("'","''");
        tableName=tableName.replaceAll("'","''");
        querySQL=String.format(querySQL,db,tableName);
        Connection connection = getAdapterSource().getConnection();
        return queryResult(connection, querySQL, resultSet -> {
            try {
                float totalSize = 0;
                while (resultSet.next()) {
                    totalSize = resultSet.getLong("size");
                }
                return totalSize;
            } catch (SQLException e) {
                throw new AtlasBaseException("查询表大小失败", e);
            }
        });
    }

}
