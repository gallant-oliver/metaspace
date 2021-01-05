package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.dataquality2.HiveNumericType;
import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.cache.annotation.Cacheable;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Slf4j

public abstract class AbstractAdapterExecutor implements AdapterExecutor {
    private final AdapterSource adapterSource;
    private final Adapter adapter;

    public AbstractAdapterExecutor(AdapterSource adapterSource) {
        this.adapterSource = adapterSource;
        this.adapter = adapterSource.getAdapter();
    }

    /**
     * 使用 SchemaCrawler 获取元数据信息
     */
    @Override
    public MetaDataInfo getMeteDataInfo(TableSchema tableSchema) {
        MetaDataInfo metaDataInfo = new MetaDataInfo();
        SchemaCrawlerOptions options = getAdapter().getSchemaCrawlerOptions(tableSchema);
        try (Connection connection = getAdapterSource().getConnection()) {
            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);
            metaDataInfo.setJdbcUrl(catalog.getJdbcDriverInfo().getConnectionUrl());
            metaDataInfo.setSchemas(catalog.getSchemas());
            metaDataInfo.setTables(catalog.getTables());
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
        return metaDataInfo;
    }

    @Override
    public void execute(Connection connection, List<String> sqls) {
        try (Connection con = connection) {
            Statement statement = con.createStatement();
            for (String sql : sqls) {
                statement.execute(sql);
            }
        } catch (Exception e) {
            throw new AdapterBaseException(e);
        }
    }

    @Override
    public <T> T queryResult(String sql, Function<ResultSet, T> call) {
        return queryResult(getAdapterSource().getConnection(), sql, call);
    }

    @Override
    public <T> T queryResult(Connection connection, String sql, Function<ResultSet, T> call) {
        try (Connection con = connection) {
            PreparedStatement statement = con.prepareStatement(sql);
            return call.apply(statement.executeQuery());
        } catch (Exception e) {
            throw new AdapterBaseException(e);
        }
    }

    @Override
    public <T> T queryResultByFetchSize(Connection connection, String sql,Function<ResultSet, T> call) {
        try (Connection con = connection) {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setFetchSize(20);
            return call.apply(statement.executeQuery());
        } catch (Exception e) {
            throw new AdapterBaseException(e);
        }
    }





    /**
     * 解析 ResultSet 忽略某些字段，用来忽略 Oracle 分页的 TEMP_COLUMN_RNUM 和 总数 total_rows__
     */
    public boolean isIgnoreColumn(String columnName) {
        return AdapterTransformer.TEMP_COLUMN_RNUM.equalsIgnoreCase(columnName);
    }

    public PageResult<LinkedHashMap<String, Object>> extractResultSetToPageResult(ResultSet resultSet) {
        PageResult<LinkedHashMap<String, Object>> pageResult = new PageResult<>();
        List<LinkedHashMap<String, Object>> resultSetToMap = extractResultSetToMap(resultSet);
        pageResult.setLists(resultSetToMap);
        pageResult.setCurrentSize(resultSetToMap.size());
        pageResult.setTotalSize(Long.valueOf(resultSetToMap.stream().findAny().map(map -> {
            Object obj;
            if (map.containsKey(AdapterTransformer.TOTAL_COLUMN_ALIAS)){
                obj = map.get(AdapterTransformer.TOTAL_COLUMN_ALIAS);
            }else{
                obj = map.get(AdapterTransformer.TOTAL_COLUMN_ALIAS.toLowerCase());
            }
            return obj;
        }).orElse(0L).toString()) );
        resultSetToMap.forEach(map->map.remove(AdapterTransformer.TOTAL_COLUMN_ALIAS));
        return pageResult;
    }

    public List<LinkedHashMap<String, Object>> extractResultSetToMap(ResultSet resultSet) {
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    if (isIgnoreColumn(columnName)) {
                        continue;
                    }
                    Object value = resultSet.getObject(columnName);
                    if (value instanceof Clob) {
                        Clob clob = (Clob) value;
                        StringBuilder buffer = new StringBuilder();
                        clob.getCharacterStream();
                        BufferedReader br = new BufferedReader(clob.getCharacterStream());
                        clob.getCharacterStream();
                        String line = br.readLine();
                        while (line != null) {
                            buffer.append(line);
                            line = br.readLine();
                        }
                        value = buffer.toString();
                    } else if (value instanceof Timestamp) {
                        Timestamp timValue = (Timestamp) value;
                        value = timValue.toString();
                    } else {
                        value = getAdapter().getAdapterTransformer().convertColumnValue(value);
                    }

                    map.put(columnName, value);
                }
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            throw new AdapterBaseException("解析查询结果失败", e);
        }
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getSchemaPage(Parameters parameters) {
        PageResult<LinkedHashMap<String, Object>> pageResult = new PageResult<>();
        Collection<Schema> allSchema = getAllSchema(parameters.getQuery());
        ArrayList<Schema> schemas = new ArrayList<>(allSchema);
        Stream<Schema> skip = schemas.stream().skip(parameters.getOffset());
        if (parameters.getLimit()!=-1){
            skip = skip.limit(parameters.getLimit());
        }
        List<LinkedHashMap<String, Object>> lists = skip.map(schema -> {
            LinkedHashMap<String, Object> schemaName = new LinkedHashMap<>();
            schemaName.put("schemaName", schema.getFullName());
            return schemaName;
        }).collect(Collectors.toList());
        pageResult.setTotalSize(allSchema.size());
        pageResult.setLists(lists);
        pageResult.setCurrentSize(lists.size());

        return pageResult;
    }

    public Collection<Schema> getAllSchema(String query){
        SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.minimum).setRetrieveRoutines(false).setRetrieveTables(false).toOptions());

        if (query!=null){
            schemaCrawlerOptionsBuilder = schemaCrawlerOptionsBuilder.includeSchemas(s -> s.contains(query));
        }
        SchemaCrawlerOptions options = schemaCrawlerOptionsBuilder
                .toOptions();
        try (Connection connection = getAdapterSource().getConnection()) {
            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);
            Collection<Schema> schemas = catalog.getSchemas();
            return schemas;
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getTablePage(String schemaName,Parameters parameters) {
        PageResult<LinkedHashMap<String, Object>> pageResult = new PageResult<>();
        Collection<Table> allTable = getAllTable(schemaName,parameters.getQuery());
        ArrayList<Table> tables = new ArrayList<>(allTable);
        Stream<Table> skip = tables.stream().skip(parameters.getOffset());
        if (parameters.getLimit()!=-1){
            skip = skip.limit(parameters.getLimit());
        }
        List<LinkedHashMap<String, Object>> lists = skip.map(table -> {
            LinkedHashMap<String, Object> tableName = new LinkedHashMap<>();
            tableName.put("tableName", table.getName());
            return tableName;
        }).collect(Collectors.toList());
        pageResult.setTotalSize(tables.size());
        pageResult.setLists(lists);
        pageResult.setCurrentSize(lists.size());


        return pageResult;
    }


    public Collection<Table> getAllTable(String schemaName,String query){
        SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.minimum).setRetrieveRoutines(false).toOptions())
                .includeSchemas(s -> s.equals(schemaName));
        if (query!=null){
            schemaCrawlerOptionsBuilder = schemaCrawlerOptionsBuilder.includeTables(s -> s.contains(query));
        }
        SchemaCrawlerOptions options = schemaCrawlerOptionsBuilder
                .toOptions();
        try (Connection connection = getAdapterSource().getConnection()) {
            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);
            Collection<Table> tables = catalog.getTables();
            return tables;
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
    }

    @Override
    public PageResult<LinkedHashMap<String, Object>> getColumnPage(String schemaName, String tableName, Parameters parameters,boolean isNum) {
        PageResult<LinkedHashMap<String, Object>> pageResult = new PageResult<>();
        List<Column> allColumn = getAllColumn(schemaName,tableName,parameters.getQuery());

        // 过滤数值型字段
        if (isNum){
            List<String> columnType = Arrays.stream(HiveNumericType.values()).filter(type-> type.getCode() != 7).map(HiveNumericType::getName).collect(Collectors.toList());
            allColumn = allColumn.stream().filter(column -> columnType.contains(column.getType().toString().toLowerCase())).collect(Collectors.toList());
        }
        Stream<Column> skip = allColumn.stream().skip(parameters.getOffset());
        if (parameters.getLimit()!=-1){
            skip = skip.limit(parameters.getLimit());
        }
        List<LinkedHashMap<String, Object>> lists = skip.map(column -> {
            LinkedHashMap<String, Object> columnName = new LinkedHashMap<>();
            columnName.put("columnName", column.getName());
            columnName.put("type",column.getType().toString());
            return columnName;
        }).collect(Collectors.toList());
        pageResult.setTotalSize(allColumn.size());
        pageResult.setLists(lists);
        pageResult.setCurrentSize(lists.size());


        return pageResult;
    }


    public List<Column> getAllColumn(String schemaName, String tableName,String query){
        SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard).setRetrieveRoutines(false).setRetrieveForeignKeys(false).setRetrieveIndexes(false).toOptions())
                .includeSchemas(s -> s.equals(schemaName))
                .includeTables(s->s.contains(tableName));
        if (query!=null){
            schemaCrawlerOptionsBuilder = schemaCrawlerOptionsBuilder.includeColumns(new InclusionRule() {
                @Override
                public boolean test(String s) {
                    return s.contains(query);
                }
            });
        }
        SchemaCrawlerOptions options = schemaCrawlerOptionsBuilder
                .toOptions();
        try (Connection connection = getAdapterSource().getConnection()) {
            Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);
            Collection<Table> tables = catalog.getTables();
            List<Column> columns = new ArrayList<>();
            tables.forEach(table -> {
                if (table.getName().equals(tableName)){
                    columns.addAll(table.getColumns());
                }
            });
            return columns;
        } catch (Exception e) {
            throw new AtlasBaseException(e);
        }
    }
}
