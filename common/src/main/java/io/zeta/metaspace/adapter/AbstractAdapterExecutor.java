package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.model.result.PageResult;
import lombok.Getter;
import org.apache.atlas.exception.AtlasBaseException;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.io.BufferedReader;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

@Getter
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
    public MetaDataInfo getMeteDataInfo() {
        MetaDataInfo metaDataInfo = new MetaDataInfo();
        SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard).setRetrieveRoutines(false).toOptions())
                .includeSchemas(getAdapter().getSchemaRegularExpressionRule())
                .includeTables(getAdapter().getTableRegularExpressionRule())
                .toOptions();
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

    /**
     * 解析 ResultSet 忽略某些字段，用来忽略 Oracle 分页的 TEMP_COLUMN_RNUM 和 总数 total_rows__
     */
    public boolean isIgnoreColumn(String columnName) {
        return AdapterTransformer.TOTAL_COLUMN_ALIAS.equalsIgnoreCase(columnName);
    }

    public PageResult<LinkedHashMap<String, Object>> extractResultSetToPageResult(ResultSet resultSet) {
        PageResult<LinkedHashMap<String, Object>> pageResult = new PageResult<>();
        List<LinkedHashMap<String, Object>> resultSetToMap = extractResultSetToMap(resultSet);
        pageResult.setLists(resultSetToMap);
        pageResult.setCurrentSize(resultSetToMap.size());
        pageResult.setTotalSize((Long) resultSetToMap.stream().findAny().map(map -> map.get(AdapterTransformer.TOTAL_COLUMN_ALIAS)).orElse(0L));
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
}
