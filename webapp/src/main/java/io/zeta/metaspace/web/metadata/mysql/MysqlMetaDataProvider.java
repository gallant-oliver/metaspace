package io.zeta.metaspace.web.metadata.mysql;

import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.metadata.MetaDataProvider;
import io.zeta.metaspace.web.metadata.RMDBEnum;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import schemacrawler.schema.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.metadata.BaseFields.*;

/**
 * mysql元数据获取
 * @author zhuxuetong
 * @date 2019-08-21 17:27
 */
@Singleton
@Component
public class MysqlMetaDataProvider extends MetaDataProvider implements IMetaDataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MysqlMetaDataProvider.class);

    public MysqlMetaDataProvider() {
        super();
    }

    @Override
    protected String getRMDBType() {
        return RMDBEnum.MYSQL.getName();
    }


    @Override
    protected AtlasEntity toInstanceEntity(AtlasEntity dbEntity, String instanceId) {
        if (dbEntity == null) {
            dbEntity = new AtlasEntity(getInstanceTypeName());
        }
        dbEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getInstanceQualifiedName(clusterName, instanceId));
        dbEntity.setAttribute(ATTRIBUTE_NAME, dataSourceInfo.getSourceName());
        dbEntity.setAttribute(ATTRIBUTE_RDBMS_TYPE, getRMDBType());
        dbEntity.setAttribute(ATTRIBUTE_PLATFORM, getRMDBType());
        dbEntity.setAttribute(ATTRIBUTE_HOSTNAME, dataSourceInfo.getIp());
        dbEntity.setAttribute(ATTRIBUTE_PORT, dataSourceInfo.getPort());
        dbEntity.setAttribute(ATTRIBUTE_COMMENT, dataSourceInfo.getDescription());
        dbEntity.setAttribute(ATTRIBUTE_CONTACT_INFO, catalog.getJdbcDriverInfo().getConnectionUrl());
        return dbEntity;
    }

    @Override
    protected AtlasEntity toDBEntity(AtlasEntity instanceEntity, AtlasEntity dbEntity, String instanceId, String databaseName) {
        if (dbEntity == null) {
            dbEntity = new AtlasEntity(getDatabaseTypeName());
        }
        dbEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getDBQualifiedName(clusterName, instanceId, databaseName));
        dbEntity.setAttribute(ATTRIBUTE_NAME, databaseName.toLowerCase());
        dbEntity.setAttribute(ATTRIBUTE_CLUSTER_NAME, clusterName);
        dbEntity.setAttribute(ATTRIBUTE_PRODOROTHER, "");
        dbEntity.setAttribute(ATTRIBUTE_INSTANCE, getObjectId(instanceEntity));
        return dbEntity;
    }

    @Override
    protected AtlasEntity.AtlasEntityWithExtInfo toTableEntity(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName, AtlasEntity.AtlasEntityWithExtInfo tableEntity) {
        if (null == tableEntity) {
            tableEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getTableTypeName()));
        }
        AtlasEntity table = tableEntity.getEntity();
        table.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getTableQualifiedName(clusterName, instanceId, databaseName, tableName.getName()));
        table.setAttribute(ATTRIBUTE_NAME, tableName.getName().toLowerCase());
        table.setAttribute(ATTRIBUTE_DB, getObjectId(dbEntity));
        Map<String, String> tableCreateTimeAndComment = getTableCreateTimeAndComment(tableName.getName());
        table.setAttribute(ATTRIBUTE_CREATE_TIME, tableCreateTimeAndComment.get("create_time"));
        table.setAttribute(ATTRIBUTE_COMMENT, tableCreateTimeAndComment.get("comment"));
        table.setAttribute(ATTRIBUTE_NAME_PATH, tableName.getFullName());
        List<AtlasEntity> columns = toColumns(tableName.getColumns(), table);
        List<AtlasEntity> indexes = toIndexes(tableName.getIndexes(), columns, table);
        List<AtlasEntity> foreignKeys = toForeignKeys(tableName.getForeignKeys(), columns, table);
        table.setAttribute(ATTRIBUTE_COLUMNS, getObjectIds(columns));
        //todo 如果column第一次导入，index和fk回报错
//        table.setAttribute(ATTRIBUTE_INDEXES, getObjectIds(indexes));
//        table.setAttribute(ATTRIBUTE_FOREIGN_KEYS, getObjectIds(foreignKeys));
        return tableEntity;
    }

    private List<AtlasEntity> toForeignKeys(Collection<ForeignKey> foreignKeys, List<AtlasEntity> columns, AtlasEntity table) {
        List<AtlasEntity> ret = new ArrayList<>();
        for (ForeignKey foreignKey : foreignKeys) {
            AtlasEntity foreignEntity = new AtlasEntity(RDBMS_FOREIGN_KEY);
            foreignEntity.setAttribute(ATTRIBUTE_NAME, foreignKey.getName());
            foreignEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), foreignKey.getName()));
            foreignEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
            foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS, getKeyColumns(foreignKey,columns));
//            foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_TABLE, getReferencsTable(foreignKey));
//            foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_COLUMNS, getReferencesColumns(foreignKey));

            ret.add(foreignEntity);
        }
        return ret;
    }

    /**
     * 获取外键关键字段
     * @param foreignKey
     * @param columns
     * @return
     */
    private List<AtlasObjectId> getKeyColumns(ForeignKey foreignKey, List<AtlasEntity> columns) {
        List<AtlasObjectId> columnObjectIds = new ArrayList<>();
        for (ForeignKeyColumnReference columnReference : foreignKey.getColumnReferences()) {
            String columnName = columnReference.getPrimaryKeyColumn().getName();
            Optional<AtlasEntity> first = columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(columnName)).findFirst();
            first.ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
        }
        return columnObjectIds;
    }

    private List<AtlasEntity> toIndexes(Collection<Index> indexes, List<AtlasEntity> columns, AtlasEntity table) {
        List<AtlasEntity> ret = new ArrayList<>();
        for (Index index : indexes) {
            AtlasEntity indexEntity = new AtlasEntity(RDBMS_INDEX);
            //名称统一小写
            indexEntity.setAttribute(ATTRIBUTE_NAME, index.getName().toLowerCase());
            indexEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), index.getName()));
            indexEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
            indexEntity.setAttribute(ATTRIBUTE_INDEX_TYPE, index.getIndexType());
            indexEntity.setAttribute(ATTRIBUTE_ISUNIQUE, index.isUnique());
            List<AtlasEntity> indexColumns = columns.stream().filter(column -> index.getColumns().stream()
                    .anyMatch(indexColumn -> indexColumn.getName().equalsIgnoreCase(
                            String.valueOf(column.getAttribute(ATTRIBUTE_NAME)))))
                    .collect(Collectors.toList());
            indexEntity.setAttribute(ATTRIBUTE_COLUMNS, getObjectIds(indexColumns));
            indexEntity.setAttribute(ATTRIBUTE_COMMENT, index.getRemarks());
            ret.add(indexEntity);
        }
        return ret;
    }

    private List<AtlasEntity> toColumns(List<Column> columns, AtlasEntity table) {
        List<AtlasEntity> ret = new ArrayList<>();
        for (Column column : columns) {
            AtlasEntity columnEntity = new AtlasEntity(RDBMS_COLUMN);
            //再次导入时，要保持guid一致，不然可能会导致entity在没有变化的情况下，依旧更新
            columnEntity.setAttribute(ATTRIBUTE_NAME, column.getName());
            columnEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), column.getName()));
            columnEntity.setAttribute(ATTRIBUTE_DATA_TYPE, column.getColumnDataType());
            columnEntity.setAttribute(ATTRIBUTE_LENGTH, column.getSize());
            columnEntity.setAttribute(ATTRIBUTE_DEFAULT_VALUE, column.getDefaultValue());
            columnEntity.setAttribute(ATTRIBUTE_COMMENT, column.getRemarks());
            columnEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
            columnEntity.setAttribute(ATTRIBUTE_ISNULLABLE, column.isNullable());
            columnEntity.setAttribute(ATTRIBUTE_ISPRIMARYKEY, column.isPartOfPrimaryKey());
            ret.add(columnEntity);
        }
        return ret;
    }

    private Map<String, String> getTableCreateTimeAndComment(String tableName) {
        Map<String, String> pair = new HashMap<>();
        final String                   query = String.format("select create_time,table_comment  from information_schema.tables where table_schema= '%s'", tableName);
        try (final Connection connection = getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(query)) {
            // Get result set metadata
            final ResultsColumns resultColumns = SchemaCrawlerUtility
                    .getResultsColumns(results);
            for (final ResultsColumn column : resultColumns) {
                if (results.next()) {
                    String columnLabel = column.getLabel();
                    pair.put(columnLabel, results.getString(columnLabel));
                }
            }
        } catch (Exception e) {
            LOG.info("获取表创建时间和备注报错", e);
            return new HashMap<>();
        }
        return pair;
    }

    @Override
    protected List<String> getSkipSchemas() {
        return Collections.singletonList("sys");
    }

    @Override
    protected List<String> getSkipTables() {
        return null;
    }


}
