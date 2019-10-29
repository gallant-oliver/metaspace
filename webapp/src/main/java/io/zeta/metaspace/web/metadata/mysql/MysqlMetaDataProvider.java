package io.zeta.metaspace.web.metadata.mysql;

import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.metadata.MetaDataProvider;
import io.zeta.metaspace.web.metadata.RMDBEnum;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import schemacrawler.schema.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
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

    //更新数据源的点
    @Override
    protected AtlasEntity.AtlasEntityWithExtInfo toInstanceEntity(AtlasEntity.AtlasEntityWithExtInfo instanceEntity, String instanceId) {
        if (instanceEntity == null) {
            instanceEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getInstanceTypeName()));
        }
        AtlasEntity entity = instanceEntity.getEntity();
        entity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getInstanceQualifiedName(instanceId));
        entity.setAttribute(ATTRIBUTE_NAME, dataSourceInfo.getSourceName());
        entity.setAttribute(ATTRIBUTE_RDBMS_TYPE, getRMDBType());
        entity.setAttribute(ATTRIBUTE_PLATFORM, getRMDBType());
        entity.setAttribute(ATTRIBUTE_HOSTNAME, dataSourceInfo.getIp());
        entity.setAttribute(ATTRIBUTE_PORT, dataSourceInfo.getPort());
        entity.setAttribute(ATTRIBUTE_COMMENT, dataSourceInfo.getDescription());
        entity.setAttribute(ATTRIBUTE_CONTACT_INFO, catalog.getJdbcDriverInfo().getConnectionUrl());
        List<AtlasEntity> dbEntities = new ArrayList<>();
        for (Schema schema : databaseNames) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntity = null;
            String dbQualifiedName = getDBQualifiedName(instanceId, schema.getFullName());
            if (metaDataContext.isKownEntity(dbQualifiedName)) {
                dbEntity = metaDataContext.getEntity(dbQualifiedName);
            }else {
                dbEntity = toDBEntity(instanceEntity, null, instanceId, schema.getFullName());
                metaDataContext.putEntity(dbQualifiedName, dbEntity);
            }
            dbEntities.add(dbEntity.getEntity());
            instanceEntity.addReferredEntity(dbEntity.getEntity());
        }
        entity.setAttribute(ATTRIBUTE_DATABASES, getObjectIds(dbEntities));

        return instanceEntity;
    }

    @Override
    protected AtlasEntity.AtlasEntityWithExtInfo toDBEntity(AtlasEntity.AtlasEntityWithExtInfo instance, AtlasEntity.AtlasEntityWithExtInfo dbEntity, String instanceId, String databaseName) {
        AtlasEntity instanceEntity = instance.getEntity();
        if (dbEntity == null) {
            dbEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getDatabaseTypeName()));
        }

        String DBId = null;
        if (instance.getReferredEntities()!=null){
            List<String> DBIds = instance.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(getDatabaseTypeName())&&entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(databaseName)).map(entity->entity.getGuid()).collect(Collectors.toList());
            if(DBIds!=null && DBIds.size()!=0){
                DBId = DBIds.get(0);
            }
        }

        AtlasEntity entity = dbEntity.getEntity();
        if (DBId!=null) {
            entity.setGuid(DBId);
        }

        entity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getDBQualifiedName(instanceId, databaseName));
        entity.setAttribute(ATTRIBUTE_NAME, databaseName.toLowerCase());
        entity.setAttribute(ATTRIBUTE_CLUSTER_NAME, clusterName);
        entity.setAttribute(ATTRIBUTE_PRODOROTHER, "");
        entity.setAttribute(ATTRIBUTE_INSTANCE, getObjectId(instanceEntity));
        //todo ATTRIBUTE_TABLES 需要在表插入janusgraph以后才能设置
//      entity.setAttribute(ATTRIBUTE_TABLES, getObjectIds(tableEntities));
        return dbEntity;
    }

    @Override
    protected AtlasEntity.AtlasEntityWithExtInfo toTableEntity(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String instanceGuid) throws AtlasBaseException {
        if (null == tableEntity) {
            tableEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getTableTypeName()));
        }
        AtlasEntity table = tableEntity.getEntity();
        table.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getTableQualifiedName(instanceId, databaseName, tableName.getName()));
        table.setAttribute(ATTRIBUTE_NAME, tableName.getName().toLowerCase());
        table.setAttribute(ATTRIBUTE_DB, getObjectId(dbEntity));
        Map<String, String> tableCreateTime = getTableCreateTime(databaseName,tableName.getName());
        try {
            long time = formatter.parse(tableCreateTime.get("create_time")).getTime();
            table.setAttribute(ATTRIBUTE_CREATE_TIME, time);
        }catch (Exception e){
            table.setAttribute(ATTRIBUTE_CREATE_TIME, null);
        }

        table.setAttribute(ATTRIBUTE_COMMENT, tableName.getRemarks());
        table.setAttribute(ATTRIBUTE_NAME_PATH, tableName.getFullName());
        List<AtlasEntity> columns = toColumns(tableName.getColumns(), tableEntity,databaseName,instanceGuid);
        List<AtlasEntity> indexes = toIndexes(tableName.getIndexes(), columns, tableEntity,databaseName,instanceGuid);
        List<AtlasEntity> foreignKeys = toForeignKeys(tableName.getForeignKeys(), columns, tableEntity, dbEntity, instanceId,instanceGuid);
        table.setAttribute(ATTRIBUTE_COLUMNS, getObjectIds(columns));
        for (AtlasEntity column : columns) {
            tableEntity.addReferredEntity(column);
        }
        table.setAttribute(ATTRIBUTE_INDEXES, getObjectIds(indexes));
        for (AtlasEntity index : indexes) {
            tableEntity.addReferredEntity(index);
        }
        table.setAttribute(ATTRIBUTE_FOREIGN_KEYS, getObjectIds(foreignKeys));
        for (AtlasEntity foreignKey : foreignKeys) {
            tableEntity.addReferredEntity(foreignKey);
        }
        return tableEntity;
    }

    private List<AtlasEntity> toForeignKeys(Collection<ForeignKey> foreignKeys, List<AtlasEntity> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity, AtlasEntity dbEntity, String instanceGuid,String databaseName) throws AtlasBaseException {
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> ret = new ArrayList<>();
        for (ForeignKey foreignKey : foreignKeys) {
            AtlasEntity foreignEntity = new AtlasEntity(RDBMS_FOREIGN_KEY);
            if (tableEntity.getReferredEntities()!=null){
                List<String> keyIds = tableEntity.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_FOREIGN_KEY)&&entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(foreignKey.getName())).map(entity->entity.getGuid()).collect(Collectors.toList());
                String keyId = null;
                if(keyIds!=null && keyIds.size()!=0){
                    keyId = keyIds.get(0);
                }
                if (keyId!=null) {
                    foreignEntity.setGuid(keyId);
                }
            }
            foreignEntity.setAttribute(ATTRIBUTE_NAME, foreignKey.getName());
            foreignEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), foreignKey.getName()));
            foreignEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
            foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS, getKeyColumns(foreignKey, columns));
            //todo ATTRIBUTE_REFERENCES_TABLE 和 ATTRIBUTE_REFERENCES_COLUMNS 需要需要表已经存在janusgraph中
//            visitForeignEntity(foreignEntity, foreignKey, dbEntity, instanceId);

            ret.add(foreignEntity);
        }
        return ret;
    }

//    private void visitForeignEntity(AtlasEntity foreignEntity, ForeignKey foreignKey, AtlasEntity dbEntity, String instanceId) {
//        for (ForeignKeyColumnReference columnReference : foreignKey.getColumnReferences()) {
//            Table parentTable = columnReference.getForeignKeyColumn().getParent();
//            try {
//                AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo = findEntity(getTableTypeName(), getTableQualifiedName(instanceId, parentTable.getSchema().getFullName(), parentTable.getName()));
//                if (null == atlasEntityWithExtInfo) {
//                    atlasEntityWithExtInfo = registerTable(dbEntity, instanceId, parentTable.getSchema().getFullName(), parentTable);
//                }
//                foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_TABLE, getObjectId(atlasEntityWithExtInfo.getEntity()));
//
//            } catch (Exception e) {
//                LOG.error("导入表{}元数据失败", parentTable.getFullName(), e);
//            }
//        }
//    }

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

    private List<AtlasEntity> toIndexes(Collection<Index> indexes, List<AtlasEntity> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String databaseName, String instanceGuid) throws AtlasBaseException {
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> ret = new ArrayList<>();
        for (Index index : indexes) {
            AtlasEntity indexEntity = new AtlasEntity(RDBMS_INDEX);

            if (tableEntity.getReferredEntities()!=null){
                List<String> indexIds = tableEntity.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_INDEX)&&entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(index.getName())).map(entity->entity.getGuid()).collect(Collectors.toList());
                String indexId = null;
                if(indexIds!=null && indexIds.size()!=0){
                    indexId = indexIds.get(0);
                }
                if (indexId!=null) {
                    indexEntity.setGuid(indexId);
                }
            }
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

    private List<AtlasEntity> toColumns(List<Column> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String databaseName, String instanceGuid) throws AtlasBaseException {
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> ret = new ArrayList<>();
        for (Column column : columns) {
            AtlasEntity columnEntity = new AtlasEntity(RDBMS_COLUMN);
            //再次导入时，要保持guid一致，不然可能会导致entity在没有变化的情况下，依旧更新
            if (tableEntity.getReferredEntities()!=null){
                List<String> columnIds = tableEntity.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_COLUMN)&&entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(column.getName())).map(entity->entity.getGuid()).collect(Collectors.toList());
                String columnId = null;
                if(columnIds!=null && columnIds.size()!=0){
                    columnId = columnIds.get(0);
                }
                if (columnId!=null) {
                    columnEntity.setGuid(columnId);
                }
            }
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

    private Map<String, String> getTableCreateTime(String databaseName,String tableName) {
        Map<String, String> pair = new HashMap<>();
        final String                   query = String.format("select create_time from information_schema.tables where table_schema= '%s' and table_name = '%s'", databaseName,tableName);
        try (final Connection connection = getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(query)) {
            // Get result set metadata
            final ResultsColumns resultColumns = SchemaCrawlerUtility
                    .getResultsColumns(results);
            if (results.next()) {
                for (final ResultsColumn column : resultColumns) {
                    String columnLabel = column.getLabel();
                    pair.put(columnLabel.toLowerCase(), results.getString(columnLabel));
                }
            }
        } catch (Exception e) {
            LOG.info("获取表创建时间和备注报错", e);
            return new HashMap<>();
        }
        return pair;
    }

    @Override
    protected String getSkipSchemas() {
        return "sys";
    }

    @Override
    protected String getSkipTables() {
        return null;
    }

    @Override
    protected void jdbcConnectionProperties(Map<String,String> map){
        map.put("useInformationSchema","true");
    }
}