// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.metadata.Oracle;
import java.io.PrintStream;

import static io.zeta.metaspace.web.metadata.BaseFields.*;

import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.metadata.MetaDataProvider;
import io.zeta.metaspace.web.metadata.RMDBEnum;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnReference;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.ResultsColumn;
import schemacrawler.schema.ResultsColumns;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.ExcludeAll;
import schemacrawler.schemacrawler.RegularExpressionExclusionRule;
import schemacrawler.schemacrawler.RegularExpressionInclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;
import sf.util.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Singleton;

/**
 * @author lixiang03
 * @Data 2019/9/25 15:42
 */
@Singleton
@Component
public class OracleMetaDataProvider extends MetaDataProvider implements IMetaDataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OracleMetaDataProvider.class);
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected Catalog            addCatalog;
    protected Collection<Table>  addTableNames;
    public OracleMetaDataProvider(){
        super();
    }
    @Override
    protected String getRMDBType(){
        return RMDBEnum.ORACLE.getName();
    }

    @Override
    protected AtlasEntity.AtlasEntityWithExtInfo toInstanceEntity(AtlasEntity.AtlasEntityWithExtInfo instanceEntity, String instanceId) {
        if (instanceEntity == null){
            instanceEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getInstanceTypeName()));
        }
        AtlasEntity entity = instanceEntity.getEntity();
        entity.setAttribute(ATTRIBUTE_QUALIFIED_NAME,getInstanceQualifiedName(instanceId));
        entity.setAttribute(ATTRIBUTE_NAME,dataSourceInfo.getSourceName());
        entity.setAttribute(ATTRIBUTE_RDBMS_TYPE,getRMDBType());
        entity.setAttribute(ATTRIBUTE_PLATFORM,getRMDBType());
        entity.setAttribute(ATTRIBUTE_HOSTNAME,dataSourceInfo.getIp());
        entity.setAttribute(ATTRIBUTE_PORT,dataSourceInfo.getPort());
        entity.setAttribute(ATTRIBUTE_COMMENT, dataSourceInfo.getDescription());
        entity.setAttribute(ATTRIBUTE_CONTACT_INFO,catalog.getJdbcDriverInfo().getConnectionUrl());
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
        if(dbEntity == null){
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
            long time = formatter.parse(tableCreateTime.get("create")).getTime();
            table.setAttribute(ATTRIBUTE_CREATE_TIME, time);
        }catch (Exception e){
            table.setAttribute(ATTRIBUTE_CREATE_TIME, null);
        }

        table.setAttribute(ATTRIBUTE_COMMENT, tableName.getRemarks());
        table.setAttribute(ATTRIBUTE_NAME_PATH, tableName.getFullName());
        List<AtlasEntity> columns =null;
        List<AtlasEntity> indexes =null;
        List<AtlasEntity> foreignKeys = null;
        if (addTableNames.stream().anyMatch(table1 -> table1.getName().equals(tableName.getName()))){
            columns = toColumns(tableEntity, databaseName, instanceGuid,tableName);
            indexes = toIndexes(columns, tableEntity, databaseName, instanceGuid,tableName);;
            foreignKeys = toForeignKeys(columns, tableEntity, dbEntity, instanceId, databaseName,tableName);
        }else {
            columns = toColumns(tableName.getColumns(), tableEntity, databaseName, instanceGuid);
            indexes = toIndexes(tableName.getIndexes(), columns, tableEntity, databaseName, instanceGuid);
            foreignKeys = toForeignKeys(tableName.getForeignKeys(), columns, tableEntity, dbEntity, instanceId, databaseName);
        }
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
        for (ForeignKey foreignKey:foreignKeys){
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

            foreignEntity.setAttribute(ATTRIBUTE_NAME,foreignKey.getName());
            foreignEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME,getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), foreignKey.getName()));
            foreignEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
            foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS,getKeyColumns(foreignKey,columns));
            ret.add(foreignEntity);
        }
        return ret;
    }

    private List<AtlasEntity> toForeignKeys(List<AtlasEntity> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity, AtlasEntity dbEntity, String instanceGuid,String databaseName,Table tableName) throws AtlasBaseException {
        List<AtlasEntity> ret = new ArrayList<>();
        AtlasEntity table = tableEntity.getEntity();
        String query = "SELECT NULL AS table_cat,\n" +
                       "       c.owner AS table_schem,\n" +
                       "       c.table_name,\n" +
                       "       c.column_name,\n" +
                       "       c.constraint_name AS foreign_key_name\n" +
                       "FROM all_cons_columns c, all_constraints k\n" +
                       "WHERE k.constraint_type = 'R'\n" +
                       "  AND k.constraint_name = c.constraint_name \n" +
                       "  AND k.table_name = c.table_name \n" +
                       "  AND k.owner = c.owner \n" +
                       "  AND k.table_name =  ?\n" +
                       "  AND k.owner = ? \n" +
                       "ORDER BY column_name";
        Map<String,AtlasEntity> map = new HashMap<>();

        try (final Connection connection = getConnection();
             final PreparedStatement prepare = connection.prepareStatement(query)){
            prepare.setString(1,tableName.getName());
            prepare.setString(2,databaseName);
            ResultSet resultSet = prepare.executeQuery();
            while(resultSet.next()){
                String name = resultSet.getString("foreign_key_name");
                AtlasEntity foreignEntity = map.get(name);
                if ( foreignEntity == null){
                    foreignEntity = new AtlasEntity(RDBMS_FOREIGN_KEY);
                    if (tableEntity.getReferredEntities()!=null){
                        List<String> keyIds = tableEntity.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_FOREIGN_KEY)&&entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(name)).map(entity->entity.getGuid()).collect(Collectors.toList());
                        String keyId = null;
                        if(keyIds!=null && keyIds.size()!=0){
                            keyId = keyIds.get(0);
                        }
                        if (keyId!=null) {
                            foreignEntity.setGuid(keyId);
                        }
                    }

                    map.put(name,foreignEntity);
                    foreignEntity.setAttribute(ATTRIBUTE_NAME,name);
                    foreignEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME,getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), name));
                    foreignEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
                    List<AtlasObjectId> columnObjectIds = new ArrayList<>();
                    String column_name = resultSet.getString("column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(column_name)).findFirst().ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
                    foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS,columnObjectIds);

                    ret.add(foreignEntity);
                }else {
                    List<AtlasObjectId> columnObjectIds = (List<AtlasObjectId>)foreignEntity.getAttribute(ATTRIBUTE_KEY_COLUMNS);
                    String column_name = resultSet.getString("column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(column_name)).findFirst().ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
                }

            }
        }catch (SQLException e){
            LOG.info("获取表"+table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString()+"外键失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        //删除数据库中不存在的
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
        List<AtlasEntity> ret = new ArrayList<>();
        AtlasEntity table = tableEntity.getEntity();
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

    private List<AtlasEntity> toColumns(AtlasEntity.AtlasEntityWithExtInfo tableEntity,String databaseName, String instanceGuid,Table tableName) throws AtlasBaseException {
        AtlasEntity table = tableEntity.getEntity();
        String tableTmp = tableName.getName().replace("/", "//").replace("%","/%").replace("_","/_");
        databaseName = databaseName.replace("/", "//").replace("%","/%").replace("_","/_");
        List<String> columnNames = new ArrayList<>();
        String query ="SELECT  NULL AS table_cat,\n" +
                    "       t.owner AS table_schem,\n" +
                    "       t.table_name AS table_name,\n" +
                    "       t.column_name AS column_name,\n" +
                    "       t.data_type\n" +
                    "              AS data_type,\n" +
                    "       t.data_type AS type_name,\n" +
                    "       DECODE (t.data_precision,                null, DECODE(t.data_type,                        'NUMBER', DECODE(t.data_scale,                                    null, 0                                   , 38),          DECODE (t.data_type, 'CHAR', t.char_length,                   'VARCHAR', t.char_length,                   'VARCHAR2', t.char_length,                   'NVARCHAR2', t.char_length,                   'NCHAR', t.char_length,                   'NUMBER', 0,           t.data_length)                           ),         t.data_precision)\n" +
                    "              AS column_size,\n" +
                    "       0 AS buffer_length,\n" +
                    "       DECODE (t.data_type,                'NUMBER', DECODE(t.data_precision,                                 null, DECODE(t.data_scale,                                              null, -127                                             , t.data_scale),                                  t.data_scale),                t.data_scale) AS decimal_digits,\n" +
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
                    "FROM all_tab_columns t left join (select col.column_name,col.owner,col.table_name from all_constraints con,all_cons_columns col where con.constraint_name=col.constraint_name and con.constraint_type='P' and con.owner LIKE '"+ databaseName+"' ESCAPE '/'\n" +
                    "  AND con.table_name LIKE '" + tableTmp + "' ESCAPE '/') p on t.owner=p.owner and t.table_name=p.table_name and p.column_name=t.column_name \n" +
                    "WHERE t.owner LIKE '"+ databaseName+"' ESCAPE '/'\n" +
                    "  AND t.table_name LIKE '" + tableTmp + "' ESCAPE '/'\n" +
                    "ORDER BY table_schem, table_name, ordinal_position\n";
        List<AtlasEntity> ret = new ArrayList<>();

        try (final Connection connection = getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(query)){
            while(results.next()){
                AtlasEntity columnEntity = new AtlasEntity(RDBMS_COLUMN);
                //再次导入时，要保持guid一致，不然可能会导致entity在没有变化的情况下，依旧更新
                String columnName = results.getString("column_name");
                if (tableEntity.getReferredEntities()!=null){
                    List<String> columnIds = tableEntity.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_COLUMN)&&entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(columnName)).map(entity->entity.getGuid()).collect(Collectors.toList());
                    String columnId = null;
                    if(columnIds!=null && columnIds.size()!=0){
                        columnId = columnIds.get(0);
                    }
                    if (columnId!=null) {
                        columnEntity.setGuid(columnId);
                    }
                }

                columnEntity.setAttribute(ATTRIBUTE_NAME, columnName);
                columnEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), results.getString("column_name")));
                columnEntity.setAttribute(ATTRIBUTE_DATA_TYPE, results.getString("data_type"));
                columnEntity.setAttribute(ATTRIBUTE_LENGTH, results.getString("column_size"));
                columnEntity.setAttribute(ATTRIBUTE_DEFAULT_VALUE, results.getString("column_def"));
                columnEntity.setAttribute(ATTRIBUTE_COMMENT, results.getString("remarks"));
                columnEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
                columnEntity.setAttribute(ATTRIBUTE_ISNULLABLE, results.getString("is_nullable").equalsIgnoreCase("YES")?true:false);
                columnEntity.setAttribute(ATTRIBUTE_ISPRIMARYKEY, results.getString("is_primary_key").equalsIgnoreCase("YES")?true:false);
                ret.add(columnEntity);
                columnNames.add(results.getString("column_name"));
            }

        }catch (SQLException e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        return ret;
    }

    private List<AtlasEntity> toIndexes( List<AtlasEntity> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String databaseName, String instanceGuid,Table tableName) throws AtlasBaseException {
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> ret = new ArrayList<>();
        String query = "select null as table_cat,\n" +
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
        Map<String,AtlasEntity> map = new HashMap<>();

        try (final Connection connection = getConnection();
             final PreparedStatement prepare = connection.prepareStatement(query)){
            prepare.setString(1,tableName.getName());
            prepare.setString(2,databaseName);
            prepare.setString(3,databaseName);
            prepare.setString(4,tableName.getName());
            ResultSet resultSet = prepare.executeQuery();
            while(resultSet.next()){
                String indexName = resultSet.getString("index_name").toLowerCase();
                AtlasEntity indexEntity = map.get(indexName);
                if ( indexEntity == null){
                    indexEntity = new AtlasEntity(RDBMS_INDEX);
                    if (tableEntity.getReferredEntities()!=null){
                        List<String> indexIds = tableEntity.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_INDEX)&&entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(indexName.toLowerCase())).map(entity->entity.getGuid()).collect(Collectors.toList());
                        String indexId = null;
                        if(indexIds!=null && indexIds.size()!=0){
                            indexId = indexIds.get(0);
                        }
                        if (indexId!=null) {
                            indexEntity.setGuid(indexId);
                        }
                    }
                    indexEntity.setAttribute(ATTRIBUTE_NAME, indexName.toLowerCase());
                    indexEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), indexName));
                    indexEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
                    int value = resultSet.getInt("type");
                    indexEntity.setAttribute(ATTRIBUTE_INDEX_TYPE, Utility.enumValueFromId(value, IndexType.unknown));
                    indexEntity.setAttribute(ATTRIBUTE_ISUNIQUE, resultSet.getInt("NON_UNIQUE")==0);
                    List<AtlasObjectId> indexObjectIds = new ArrayList<>();
                    String column_name = resultSet.getString("column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(column_name)).findFirst().ifPresent(atlasEntity -> indexObjectIds.add(getObjectId(atlasEntity)));

                    indexEntity.setAttribute(ATTRIBUTE_COLUMNS, indexObjectIds);
                    indexEntity.setAttribute(ATTRIBUTE_COMMENT, "");
                    ret.add(indexEntity);
                    map.put(indexName,indexEntity);
                }else{
                    List<AtlasObjectId> indexObjectIds = (List<AtlasObjectId>)indexEntity.getAttribute(ATTRIBUTE_COLUMNS);
                    String column_name = resultSet.getString("column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(column_name)).findFirst().ifPresent(atlasEntity -> indexObjectIds.add(getObjectId(atlasEntity)));
                }

            }
        }catch (SQLException e){
            LOG.info("获取表"+table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString()+"索引失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        return ret;
    }

    private Map<String, String> getTableCreateTime(String databaseName,String tableName) {
        Map<String, String> pair = new HashMap<>();
        final String                   query = "SELECT o.created from all_objects o where o.object_name=? and o.owner=?";

        try (final Connection connection = getConnection();
             final PreparedStatement prepare = connection.prepareStatement(query)) {
            // Get result set metadata
            prepare.setString(1,tableName);
            prepare.setString(2,databaseName);
            ResultSet results = prepare.executeQuery();
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
        return "SYS";
    }

    @Override
    protected String getSkipTables() {
        //return "(.*/.*|.*\\..*\\..*|.*[- =%].*)";
        return "[^/.]*\\.\"?([a-zA-Z]{1}\\w*(\\$|\\#)*\\w*)\"?";
    }


    @Override
    protected void addTableNames(){
        optionsBuilder.includeTables(new RegularExpressionExclusionRule(getSkipTables()));
        optionsBuilder.includeColumns(new ExcludeAll());
        SchemaInfoLevel schemaInfoLevel = SchemaInfoLevelBuilder.builder()
                //                    .setRetrieveDatabaseInfo(true)
                .setRetrieveTables(true)
                .setRetrieveTableColumns(true)
                .setRetrieveColumnDataTypes(true)
                .setRetrievePrimaryKeyDefinitions(true)
                //                    .setRetrieveForeignKeys(false)
                //                    .setRetrieveSequenceInformation(false)
                //                    .set
                .toOptions();
        optionsBuilder.withSchemaInfoLevel(schemaInfoLevel);
        optionsBuilder.tableTypes("TABLE");
        SchemaCrawlerOptions options = optionsBuilder.toOptions();
        addCatalog = null;
        try(Connection connection = getConnection()) {
            addCatalog = SchemaCrawlerUtility.getCatalog(connection, options);
        }catch (Exception e){
            LOG.error("import metadata error", e);
        }
        addTableNames = addCatalog.getTables();
        if(tableNames==null){
            tableNames = addTableNames;
        }else{
            tableNames.addAll(addTableNames);
        }

    }
    @Override
    protected void jdbcConnectionProperties(Map<String,String> map){
        map.put("remarksReporting","true");
    }

}
