package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerColumn;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerForeignKey;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerIndex;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerTable;
import org.apache.atlas.exception.AtlasBaseException;
import schemacrawler.schema.Table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

public interface AdapterExecutor {

    AdapterSource getAdapterSource();

    /**
     * 获取元数据, 除 oracle 仅获取到表外其他数据源获取到列、索引、外键
     */
    MetaDataInfo getMeteDataInfo(TableSchema tableSchema);


    /**
     * 手动获取 oracle 表的元数据
     */
    default SchemaCrawlerTable getTable(String schemaName, String tableName) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    default SchemaCrawlerTable getTable(Table table) {
        return getTable(table.getSchema().getName(), table.getName());
    }

    default List<SchemaCrawlerColumn> getColumns(String schemaName, String tableName) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    default List<SchemaCrawlerIndex> getIndexes(String schemaName, String tableName) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    default List<SchemaCrawlerForeignKey> getForeignKey(String schemaName, String tableName) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    /**
     * 元数据同步时获取表创建时间，当前支持 oracle 和 mysql
     */
    default LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    /**
     * 分页获取库表列，当前支持 oracle
     */
    default PageResult<LinkedHashMap<String, Object>> getSchemaPage(Parameters parameters) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    default PageResult<LinkedHashMap<String, Object>> getTablePage(String schemaName, Parameters parameters) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    default PageResult<LinkedHashMap<String, Object>> getColumnPage(String schemaName, String tableName, Parameters parameters) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    /**
     * 指定连接执行一组 sql 语句
     */
    void execute(Connection connection, List<String> sqls);

    default void execute(String sql) {
        execute(getAdapterSource().getConnection(), sql);
    }

    default void execute(Connection connection, String sql) {
        execute(connection, Collections.singletonList(sql));
    }

    /**
     * 指定连接执行一条查询语句
     * call 是 ResultSet 的自定义解析方法
     */
    <T> T queryResult(Connection connection, String sql, Function<ResultSet, T> call);

    <T> T queryResult(String sql, Function<ResultSet, T> call);

    /**
     * 指定 call 为 extractResultSetToMap
     */
    default List<LinkedHashMap<String, Object>> queryResult(Connection connection, String sql) {
        return queryResult(connection, sql, this::extractResultSetToMap);
    }

    default List<LinkedHashMap<String, Object>> queryResult(String sql) {
        return queryResult(sql, this::extractResultSetToMap);
    }

    /**
     * 解析 ResultSet ，每行解析列名和列值的映射
     */
    List<LinkedHashMap<String, Object>> extractResultSetToMap(ResultSet resultSet);

    /**
     * 解析 ResultSet ，用于解析分页结果，需要存在查询字段 total_rows__
     */
    PageResult<LinkedHashMap<String, Object>> extractResultSetToPageResult(ResultSet resultSet);

    /**
     * hive 判断表是否存在
     */
    default boolean tableExists(String proxyUser, String db, String tableName) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

    /**
     * hive 和 impala 获取表大小
     */
    default float getTableSize(String db, String tableName, String pool) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

}
