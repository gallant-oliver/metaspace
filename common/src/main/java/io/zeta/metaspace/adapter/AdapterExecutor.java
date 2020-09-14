package io.zeta.metaspace.adapter;

import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerColumn;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerForeignKey;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerIndex;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerTable;
import org.apache.atlas.exception.AtlasBaseException;
import schemacrawler.schema.Table;

import java.time.LocalDateTime;
import java.util.List;

public interface AdapterExecutor {

    AdapterSource getAdapterSource();

    MetaDataInfo getMeteDataInfo();

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
     * 获取表创建时间
     */
    default LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        throw new AtlasBaseException(getAdapterSource().getAdapter().getName() + " 未实现");
    }

}
