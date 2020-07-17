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

package io.zeta.metaspace.web.metadata.oracle;

import static io.zeta.metaspace.web.metadata.BaseFields.*;

import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.metadata.AbstractMetaDataProvider;
import io.zeta.metaspace.web.metadata.RMDBEnum;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.ExcludeAll;
import schemacrawler.schemacrawler.RegularExpressionExclusionRule;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;

/**
 * @author lixiang03
 * @Data 2019/9/25 15:42
 */
@Singleton
@Component
public class OracleMetaDataProvider extends AbstractMetaDataProvider implements IMetaDataProvider {
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
    protected AtlasEntity.AtlasEntityWithExtInfo toTableEntity(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String instanceGuid) throws AtlasBaseException {
        tableEntity = super.toTableEntity(dbEntity,instanceId,databaseName,tableName,tableEntity,instanceGuid);
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> columns =null;
        List<AtlasEntity> indexes =null;
        List<AtlasEntity> foreignKeys = null;
        if (addTableNames.stream().anyMatch(table1 -> table1.getName().equals(tableName.getName()))){
            columns = toColumns(tableEntity, databaseName, instanceGuid,tableName);
            indexes = toIndexes(columns, tableEntity, databaseName, instanceGuid,tableName);;
            foreignKeys = toForeignKeys(columns, tableEntity, dbEntity, instanceId, databaseName,tableName);
        }else {
            columns = toColumns(tableName.getColumns(), tableEntity);
            indexes = toIndexes(tableName.getIndexes(), columns, tableEntity, databaseName, instanceGuid);
            foreignKeys = toForeignKeys(tableName.getForeignKeys(), columns, tableEntity, databaseName);
        }
        setTableAttribute(columns,indexes,foreignKeys,tableEntity,table);
        return tableEntity;
    }


    private List<AtlasEntity> toForeignKeys(List<AtlasEntity> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity, AtlasEntity dbEntity, String instanceGuid,String databaseName,Table tableName) throws AtlasBaseException {
        List<AtlasEntity> ret = new ArrayList<>();
        AtlasEntity table = tableEntity.getEntity();
        String query = "SELECT NULL AS table_cat, \n" +
                       "       c.owner AS table_schem, \n" +
                       "       c.table_name, \n" +
                       "       c.column_name, \n" +
                       "       c2.owner r_table_schem ,\n" +
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
        String foreignQuery = "SELECT NULL AS table_cat, \n" +
                              "       c.owner AS table_schem, \n" +
                              "       c.table_name, \n" +
                              "       c.column_name, \n" +
                              "       c2.owner r_table_schem ,\n" +
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
        Map<String,AtlasEntity> map = new HashMap<>();
        Map<String,AtlasEntity.AtlasEntityWithExtInfo> tableMap = new HashMap<>();

        try (final Connection connection = getConnection()){
            PreparedStatement prepare = connection.prepareStatement(query);
            prepare.setString(1,tableName.getName());
            prepare.setString(2,databaseName);
            ResultSet resultSet = prepare.executeQuery();
            while(resultSet.next()){
                String name = resultSet.getString("foreign_key_name");

                String primaryTableName = resultSet.getString("r_table_name");
                String primaryDbName = resultSet.getString("r_table_schem");
                String primaryColumnName = resultSet.getString("r_column_name");
                String tableQualifiedName = getTableQualifiedName(table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString().split("\\.")[0],primaryDbName,primaryTableName);
                AtlasEntity foreignEntity = map.get(name);
                AtlasEntity.AtlasEntityWithExtInfo primaryTableInfo = null;
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


                    primaryTableInfo = findEntity(getTableTypeName(), tableQualifiedName);
                    if (primaryTableInfo!=null){
                        primaryTableInfo = getById(primaryTableInfo.getEntity().getGuid(),false);
                        foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_TABLE, getObjectId(primaryTableInfo.getEntity()));
                        List<AtlasEntity> primaryColumns = primaryTableInfo.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_COLUMN)).collect(Collectors.toList());
                        List<AtlasObjectId> columnObjectIds = new ArrayList<>();
                        primaryColumns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(primaryColumnName)).findFirst().ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));

                        foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_COLUMNS, columnObjectIds);
                        primaryTableInfo.addReferredEntity(foreignEntity);
                        ((List<AtlasObjectId>)primaryTableInfo.getEntity().getAttribute(ATTRIBUTE_FOREIGN_KEYS)).add(getObjectId(foreignEntity));
                    }
                    tableMap.put(foreignEntity.getGuid(),primaryTableInfo);

                    foreignEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
                    List<AtlasObjectId> columnObjectIds = new ArrayList<>();
                    String columnName = resultSet.getString("column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(columnName)).findFirst().ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
                    foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS,columnObjectIds);

                    ret.add(foreignEntity);
                }else {
                    primaryTableInfo = tableMap.get(foreignEntity.getGuid());
                    if (primaryTableInfo!=null){
                        List<AtlasEntity> primaryColumns = primaryTableInfo.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_COLUMN)).collect(Collectors.toList());
                        List<AtlasObjectId> columnObjectIds = (List<AtlasObjectId>)foreignEntity.getAttribute(ATTRIBUTE_KEY_COLUMNS);
                        primaryColumns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(primaryColumnName)).findFirst().ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
                    }
                    List<AtlasObjectId> columnObjectIds = (List<AtlasObjectId>)foreignEntity.getAttribute(ATTRIBUTE_KEY_COLUMNS);
                    String columnName = resultSet.getString("column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(columnName)).findFirst().ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
                }

            }

            for (String guid: tableMap.keySet()){
                AtlasEntity.AtlasEntityWithExtInfo primaryTableInfo = tableMap.get(guid);
                if (primaryTableInfo!=null){
                    AtlasEntity foreignEntity = primaryTableInfo.getReferredEntity(guid);
                    AtlasObjectId tableObjectIds = (AtlasObjectId)foreignEntity.getAttribute(ATTRIBUTE_TABLE);
                    foreignEntity.setAttribute(ATTRIBUTE_TABLE, null);
                    List<AtlasObjectId> columnObjectIds = (List<AtlasObjectId>)foreignEntity.getAttribute(ATTRIBUTE_KEY_COLUMNS);
                    foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS, new ArrayList<AtlasObjectId>());
                    createOrUpdateEntity(primaryTableInfo);
                    foreignEntity.setAttribute(ATTRIBUTE_TABLE, tableObjectIds);
                    foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS, columnObjectIds);
                }
            }

            PreparedStatement foreignPrepare = connection.prepareStatement(foreignQuery);
            foreignPrepare.setString(1,tableName.getName());
            foreignPrepare.setString(2,databaseName);
            ResultSet foreignResultSet = foreignPrepare.executeQuery();
            while(foreignResultSet.next()){
                String name = foreignResultSet.getString("foreign_key_name");
                String foreignTableName = foreignResultSet.getString("table_name");
                String foreignDbName = foreignResultSet.getString("table_schem");
                String tableQualifiedName = getTableQualifiedName(table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString().split("\\.")[0],foreignDbName,foreignTableName);
                String foreignQualifiedName = getColumnQualifiedName(tableQualifiedName,name);
                AtlasEntity foreignEntity = map.get(foreignQualifiedName);
                if (foreignEntity==null){
                    AtlasEntity.AtlasEntityWithExtInfo info = findEntity(RDBMS_FOREIGN_KEY, foreignQualifiedName);
                    if (info!=null){
                        info = getById(info.getEntity().getGuid(),true);
                        foreignEntity = info.getEntity();
                        foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_TABLE, getObjectId(table));
                        List<AtlasObjectId> columnObjectIds = new ArrayList<>();
                        String primaryColumn = foreignResultSet.getString("r_column_name");
                        columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(primaryColumn)).findFirst().ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
                        foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_COLUMNS, columnObjectIds);
                        ret.add(foreignEntity);
                        map.put(foreignQualifiedName,foreignEntity);
                    }
                }else{
                    List<AtlasObjectId> columnObjectIds = (List<AtlasObjectId>)foreignEntity.getAttribute(ATTRIBUTE_REFERENCES_COLUMNS);
                    String primaryColumn = foreignResultSet.getString("r_column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(primaryColumn)).findFirst().ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
                }
            }

        }catch (SQLException e){
            LOG.info("获取表"+table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString()+"外键失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        //删除数据库中不存在的
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
                    String columnName = resultSet.getString("column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(columnName)).findFirst().ifPresent(atlasEntity -> indexObjectIds.add(getObjectId(atlasEntity)));

                    indexEntity.setAttribute(ATTRIBUTE_COLUMNS, indexObjectIds);
                    indexEntity.setAttribute(ATTRIBUTE_COMMENT, "");
                    ret.add(indexEntity);
                    map.put(indexName,indexEntity);
                }else{
                    List<AtlasObjectId> indexObjectIds = (List<AtlasObjectId>)indexEntity.getAttribute(ATTRIBUTE_COLUMNS);
                    String columnName = resultSet.getString("column_name");
                    columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(columnName)).findFirst().ifPresent(atlasEntity -> indexObjectIds.add(getObjectId(atlasEntity)));
                }

            }
        }catch (SQLException e){
            LOG.info("获取表"+table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString()+"索引失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        return ret;
    }

    protected Map<String, String> getTableCreateTime(String databaseName,String tableName) {
        Map<String, String> pair = new HashMap<>();
        final String                   query = "SELECT o.created from all_objects o where o.object_name=? and o.owner=?";

        try (final Connection connection = getConnection();
             final PreparedStatement prepare = connection.prepareStatement(query)) {
            // Get result set metadata
            prepare.setString(1,tableName);
            prepare.setString(2,databaseName);
            ResultSet results = prepare.executeQuery();
            resultToMap(results,pair);
        } catch (Exception e) {
            LOG.info("获取oracle表创建时间和备注报错", e);
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