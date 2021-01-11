package io.zeta.metaspace.adapter.oracle;

import com.healthmarketscience.sqlbuilder.*;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.dataquality2.HiveNumericType;
import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerColumn;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerForeignKey;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerIndex;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerTable;
import io.zeta.metaspace.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.IndexType;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;
import sf.util.Utility;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class OracleAdapterExecutor extends AbstractAdapterExecutor {

    public static final Map<String, String> BRACKETS = new HashMap<String, String>() {{
        put("[", "]");
        put("{", "}");
        put("<", ">");
        put("(", ")");
    }};

    public OracleAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    /**
     * oracle 因为不规则表名在获取列、索引或者外键时会异常中断获取元数据，
     * 所以 oracle 获取元数据的时候不获取列、索引和外键
     */
    @Override
    public MetaDataInfo getMeteDataInfo(TableSchema tableSchema) {
        MetaDataInfo metaDataInfo = new MetaDataInfo();
        SchemaCrawlerOptions options = getAdapter().getSchemaCrawlerOptions(tableSchema);
        try (Connection connection = getAdapterSource().getConnection()) {
            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);
            metaDataInfo.getIncompleteTables().addAll(catalog.getTables());
            metaDataInfo.getTables().addAll(catalog.getTables());
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
        return metaDataInfo;
    }

    @Override
    public SchemaCrawlerTable getTable(String schemaName, String tableName) {
        try {
            SchemaCrawlerTable table = new SchemaCrawlerTable();
            table.setColumns(getColumns(schemaName, tableName));
            table.setIndexes(getIndexes(schemaName, tableName));
            table.setForeignKeys(getForeignKey(schemaName, tableName));
            return table;
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
    }

    /**
     * 获取列信息
     */
    @Override
    public List<SchemaCrawlerColumn> getColumns(String schemaName, String tableName) {
        String sql = "SELECT  NULL AS table_cat,\n" +
                "       t.owner AS table_schem,\n" +
                "       t.table_name AS table_name,\n" +
                "       t.column_name AS column_name,\n" +
                "       t.data_type\n" +
                "              AS data_type,\n" +
                "       t.data_type AS type_name,\n" +
                "       DECODE (t.data_precision,   null, DECODE(t.data_type, 'NUMBER', DECODE(t.data_scale, null, 0  , 38), DECODE (t.data_type, 'CHAR', t.char_length, 'VARCHAR', t.char_length, 'VARCHAR2', t.char_length, 'NVARCHAR2', t.char_length, 'NCHAR', t.char_length, 'NUMBER', 0,  t.data_length)  ),    t.data_precision)\n" +
                "              AS column_size,\n" +
                "       0 AS buffer_length,\n" +
                "       DECODE (t.data_type,  'NUMBER', DECODE(t.data_precision, null, DECODE(t.data_scale, null, -127  , t.data_scale),  t.data_scale), t.data_scale) AS decimal_digits,\n" +
                "       10 AS num_prec_radix,\n" +
                "       DECODE (t.nullable, 'N', 0, 1) AS nullable,\n" +
                "       NULL AS remarks,\n" +
                "       t.data_default AS column_def,\n" +
                "       0 AS sql_data_type,\n" +
                "       0 AS sql_datetime_sub,\n" +
                "       t.data_length AS char_octet_length,\n" +
                "       t.column_id AS ordinal_position,\n" +
                "       DECODE (t.nullable, 'N', 'NO', 'YES') AS is_nullable,\n" +
                "         null as SCOPE_CATALOG,\n" +
                "       null as SCOPE_SCHEMA,\n" +
                "       null as SCOPE_TABLE,\n" +
                "       null as SOURCE_DATA_TYPE,\n" +
                "       'NO' as IS_AUTOINCREMENT,\n" +
                "        case when p.COLUMN_NAME is null then 'No' else 'YES' end as is_primary_key \n" +
                "FROM all_tab_columns t left join (select col.column_name,col.owner,col.table_name from all_constraints con,all_cons_columns col where con.constraint_name=col.constraint_name and con.constraint_type='P' and con.owner LIKE ? ESCAPE '/'\n" +
                "  AND con.table_name LIKE ? ESCAPE '/') p on t.owner=p.owner and t.table_name=p.table_name and p.column_name=t.column_name \n" +
                "WHERE t.owner LIKE ? ESCAPE '/'\n" +
                "  AND t.table_name LIKE ? ESCAPE '/'\n" +
                "ORDER BY table_schem, table_name, ordinal_position\n";
        String escapeSchemaName = schemaName.replace("/", "//").replace("%", "/%").replace("_", "/_");
        String escapeTableName = tableName.replace("/", "//").replace("%", "/%").replace("_", "/_");

        ArrayList<SchemaCrawlerColumn> columns = new ArrayList<>();
        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, escapeSchemaName);
            statement.setString(2, escapeTableName);
            statement.setString(3, escapeSchemaName);
            statement.setString(4, escapeTableName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                SchemaCrawlerColumn column = new SchemaCrawlerColumn();
                column.setName(resultSet.getString("column_name"));
                column.setDataType(resultSet.getString("data_type"));
                column.setLength(resultSet.getInt("column_size"));
                column.setDefaultValue(resultSet.getString("column_def"));
                column.setComment(resultSet.getString("remarks"));
                column.setNullable(resultSet.getString("is_nullable").equalsIgnoreCase("YES"));
                column.setPrimaryKey(resultSet.getString("is_primary_key").equalsIgnoreCase("YES"));
                columns.add(column);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return columns;
    }

    /**
     * 获取索引信息
     */
    @Override
    public List<SchemaCrawlerIndex> getIndexes(String schemaName, String tableName) {
        String sql = "select null as table_cat,\n" +
                "       i.owner as table_schem,\n" +
                "       i.table_name,\n" +
                "       decode (i.uniqueness, 'UNIQUE', 0, 1) as NON_UNIQUE,\n" +
                "       null as index_qualifier,\n" +
                "       i.index_name,\n" +
                "       1 as type,\n" +
                "       c.column_position as ordinal_position,\n" +
                "       c.column_name,\n" +
                "       null as asc_or_desc,\n" +
                "       i.distinct_keys as cardinality,\n" +
                "       i.leaf_blocks as pages,\n" +
                "       null as filter_condition\n" +
                "from all_indexes i, all_ind_columns c\n" +
                "where i.table_name = ?\n" +
                "  and i.owner = ?\n" +
                "  and i.index_name = c.index_name\n" +
                "  and i.table_owner = c.table_owner\n" +
                "  and i.table_name = c.table_name\n" +
                "  and i.owner = c.index_owner \n" +
                "  and i.index_name not in (select index_name from all_constraints where constraint_type='P' and owner = ? and table_name = ? )\n" +
                "order by type, index_name, ordinal_position";

        Map<String, SchemaCrawlerIndex> indexMap = new HashMap<>();
        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            statement.setString(3, schemaName);
            statement.setString(4, tableName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String indexName = resultSet.getString("index_name");
                SchemaCrawlerIndex index = indexMap.get(indexName);
                if (index == null) {
                    index = new SchemaCrawlerIndex();
                    index.setName(indexName);
                    index.setIndexType(Utility.enumValueFromId(resultSet.getInt("type"), IndexType.unknown));
                    index.setUnique(resultSet.getInt("NON_UNIQUE") == 0);
                    index.setComment("");
                }
                index.addColumns(resultSet.getString("column_name"));
                indexMap.put(indexName, index);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }

        return new ArrayList<>(indexMap.values());
    }

    /**
     * 获取外键信息
     * 因为 SchemaCrawler 外键设置的两张表都会返回外键信息，所以正反查询了两边
     */
    @Override
    public List<SchemaCrawlerForeignKey> getForeignKey(String schemaName, String tableName) {
        String sql1 = "SELECT NULL AS table_cat, \n" +
                "       c.owner AS table_schema, \n" +
                "       c.table_name, \n" +
                "       c.column_name, \n" +
                "       c2.owner r_table_schema ,\n" +
                "       c2.table_name r_table_name,\n" +
                "       c2.column_name r_column_name,\n" +
                "       c.POSITION, \n" +
                "       c.constraint_name AS foreign_key_name \n" +
                "FROM all_cons_columns c, all_constraints k , all_cons_columns c2\n" +
                "WHERE k.constraint_type = 'R' \n" +
                "  AND k.constraint_name = c.constraint_name  \n" +
                "  AND k.table_name = c.table_name  \n" +
                "  AND k.owner = c.owner  \n" +
                "  AND k.r_constraint_name = c2.constraint_name \n" +
                "  AND c2.POSITION = c.POSITION \n" +
                "  AND c.table_name =  ?\n" +
                "  AND c.owner = ?  \n" +
                "ORDER BY POSITION ";

        Map<String, SchemaCrawlerForeignKey> foreignKeyMap = new HashMap<>();

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql1);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("foreign_key_name");
                SchemaCrawlerForeignKey foreignKey = foreignKeyMap.get(name);
                if (foreignKey == null) {
                    foreignKey = new SchemaCrawlerForeignKey();
                    foreignKey.setName(name);
                }
                foreignKey.addForeignKeyColumn(
                        resultSet.getString("table_schema"),
                        resultSet.getString("table_name"),
                        resultSet.getString("column_name")
                );
                foreignKey.addPrimaryKeyColumn(
                        resultSet.getString("r_table_schema"),
                        resultSet.getString("r_table_name"),
                        resultSet.getString("r_column_name")
                );
                foreignKeyMap.put(name, foreignKey);
            }

        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }


        String sql2 = "SELECT NULL AS table_cat, \n" +
                "       c.owner AS table_schema, \n" +
                "       c.table_name, \n" +
                "       c.column_name, \n" +
                "       c2.owner r_table_schema ,\n" +
                "       c2.table_name r_table_name,\n" +
                "       c2.column_name r_column_name,\n" +
                "       c.POSITION, \n" +
                "       c.constraint_name AS foreign_key_name \n" +
                "FROM all_cons_columns c, all_constraints k , all_cons_columns c2\n" +
                "WHERE k.constraint_type = 'R' \n" +
                "  AND k.constraint_name = c.constraint_name  \n" +
                "  AND k.table_name = c.table_name  \n" +
                "  AND k.owner = c.owner  \n" +
                "  AND k.r_constraint_name = c2.constraint_name \n" +
                "  AND c2.POSITION = c.POSITION \n" +
                "  AND c2.table_name =  ?\n" +
                "  AND c2.owner = ?  \n" +
                "ORDER BY POSITION ";

        Set<String> foreignKeySet = new HashSet<>(foreignKeyMap.keySet());
        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql2);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("foreign_key_name");
                if (foreignKeySet.contains(name)) {
                    continue;
                }
                SchemaCrawlerForeignKey foreignKey = foreignKeyMap.get(name);
                if (foreignKey == null) {
                    foreignKey = new SchemaCrawlerForeignKey();
                    foreignKey.setName(name);
                }
                foreignKey.addForeignKeyColumn(
                        resultSet.getString("table_schema"),
                        resultSet.getString("table_name"),
                        resultSet.getString("column_name")
                );
                foreignKey.addPrimaryKeyColumn(
                        resultSet.getString("r_table_schema"),
                        resultSet.getString("r_table_name"),
                        resultSet.getString("r_column_name")
                );
                foreignKeyMap.put(name, foreignKey);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return new ArrayList<>(foreignKeyMap.values());
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "SELECT o.created AS create_time from all_objects o where o.object_name=? and o.owner=?";

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String time = resultSet.getString("create");
                return DateUtils.parseDateTime(time);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return null;
    }

    @Override
    public boolean isIgnoreColumn(String columnName) {
        return super.isIgnoreColumn(columnName) || AdapterTransformer.TEMP_COLUMN_RNUM.equalsIgnoreCase(columnName);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getSchemaPage(Parameters parameters) {
        if (parameters.getQuery()==null){
            parameters.setQuery("");
        }
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new AliasedObject(new CustomSql("USERNAME"), "\"schemaName\""))
                .addCustomFromTable(new CustomSql("ALL_USERS"))
                .addCondition(BinaryCondition.like(new CustomSql("USERNAME"), new CustomSql("'%" + parameters.getQuery() + "%'")));
        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, parameters.getLimit(), parameters.getOffset());
        log.info("schema sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getSchemaPage(Parameters parameters, String proxyUser) {
        return getSchemaPage(parameters);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getTablePage(String schemaName, Parameters parameters) {
        schemaName = addAlternativeQuoting(schemaName);
        if (parameters.getQuery()==null){
            parameters.setQuery("");
        }
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new AliasedObject(new CustomSql("TABLE_NAME"), "\"tableName\""))
                .addCustomFromTable(new CustomSql("ALL_TABLES"))
                .addCondition(BinaryCondition.equalTo(new CustomSql("OWNER"), new CustomSql(schemaName)))
                .addCondition(BinaryCondition.like(new CustomSql("TABLE_NAME"), new CustomSql("'%" + parameters.getQuery() + "%'")));
        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, parameters.getLimit(), parameters.getOffset());
        log.info("table sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getColumnPage(String schemaName, String tableName, Parameters parameters,boolean isNum) {
        schemaName = addAlternativeQuoting(schemaName);
        tableName = addAlternativeQuoting(tableName);
        if (parameters.getQuery()==null){
            parameters.setQuery("");
        }
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new AliasedObject(new CustomSql("COLUMN_NAME"), "\"columnName\""))
                .addCustomColumns(new AliasedObject(new CustomSql("DATA_TYPE"), "\"type\""))
                .addCustomFromTable(new CustomSql("ALL_TAB_COLS"))
                .addCondition(BinaryCondition.like(new CustomSql("COLUMN_NAME"), new CustomSql("'%" + parameters.getQuery() + "%'")))
                .addCondition(ComboCondition.and().addConditions(BinaryCondition.equalTo(new CustomSql("OWNER"), new CustomSql(schemaName)), BinaryCondition.equalTo(new CustomSql("TABLE_NAME"), new CustomSql(tableName))));
        query = getAdapter().getAdapterTransformer().addTotalCount(query);
        query = getAdapter().getAdapterTransformer().addLimit(query, parameters.getLimit(), parameters.getOffset())
                .addCondition(BinaryCondition.like(new CustomSql("\"columnName\""), new CustomSql("'%" + parameters.getQuery() + "%'")));

        // 过滤数值型字段
        if (isNum){
            List<String> columnType = Arrays.stream(HiveNumericType.values()).filter(type-> type.getCode() != 7).map(HiveNumericType::getName).collect(Collectors.toList());
            query.addCondition(new InCondition(new FunctionCall("lower").addCustomParams(new CustomSql("DATA_TYPE")),columnType));
        }
        log.info("column sql:" + query.toString());
        return queryResult(query.toString(), this::extractResultSetToPageResult);
    }

    /**
     * 处理包含单引号的表名库名
     * https://livesql.oracle.com/apex/livesql/file/content_CIREYU9EA54EOKQ7LAMZKRF6P.html
     * 注意 jdbc 不支持执行 q'(str))'
     */
    private String addAlternativeQuoting(String str) {
        if (StringUtils.isNotEmpty(str) && str.contains("'")) {
            Map.Entry<String, String> entry = BRACKETS.entrySet().stream().filter(e ->
                    (!str.contains(e.getKey()) && !str.contains(e.getValue()) || (!str.contains(e.getKey() + "'") && !str.contains(e.getValue() + "'") && !str.endsWith(e.getValue())))
            ).findAny().orElse(null);
            if (entry != null) {
                return "q'" + entry.getKey() + str + entry.getValue() + "'";
            }
        }
        return "'" + str + "'";
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "select sum(num_rows * avg_row_len) data_length from ALL_TABLES where table_name = '%s' and owner='%s'";
        db=db.replaceAll("'","''");
        tableName=tableName.replaceAll("'","''");
        querySQL=String.format(querySQL,tableName,db);
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


    @Override
    public String getCreateTableSql(String schema, String table) {
        String querySql = "select dbms_metadata.get_ddl('TABLE','" + table + "','" + schema + "') from dual";
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
}
