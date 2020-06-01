package io.zeta.metaspace.web.metadata;

import com.google.common.collect.Lists;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.AbstractMetaspaceGremlinQueryProvider;
import io.zeta.metaspace.web.model.TableSchema;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.util.AESUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.*;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStream;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnReference;
import schemacrawler.schema.Index;
import schemacrawler.schema.ResultsColumn;
import schemacrawler.schema.ResultsColumns;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.*;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.metadata.BaseFields.*;

/**
 * @author zhuxuetong
 * @date 2019-08-21 17:31
 */
public abstract class AbstractMetaDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetaDataProvider.class);
    @Inject
    private  AtlasEntityStore       entitiesStore;
    private              SimpleDateFormat simpleDateFormat     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private volatile     AtomicInteger    totalTables          = new AtomicInteger(0);
    private volatile     AtomicInteger    updatedTables        = new AtomicInteger(0);
    private volatile     AtomicLong       startTime            = new AtomicLong(0);
    private volatile AtomicLong           endTime              = new AtomicLong(0);
    public static final String            CLUSTER_NAME         = "atlas.cluster.name";
    public static final String            DEFAULT_CLUSTER_NAME = "primary";
    public String                         clusterName;
    @Inject
    AtlasGraph graph;
    private AbstractMetaspaceGremlinQueryProvider gremlinQueryProvider;
    private EntityGraphRetriever entityRetriever;
    @Inject
    AtlasTypeRegistry atlasTypeRegistry;
    @Inject
    AtlasEntityStore  atlasEntityStore;
    @Inject
    private   DataSourceService  dataSourceService;
    private   Configuration      atlasConf;
    protected Catalog            catalog;
    protected Collection<Schema> databaseNames;
    protected Collection<Table>  tableNames;
    protected DataSourceInfo     dataSourceInfo;
    protected SchemaCrawlerOptionsBuilder optionsBuilder;
    private DatabaseConnectionSource dataSource;
    protected MetaDataContext metaDataContext;
    protected SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean isSource;
    private boolean isThread = false;

    public boolean isThread() {
        return isThread;
    }

    public void setThread(boolean thread) {
        isThread = thread;
    }

    public void set(AtlasEntityStore entitiesStore, DataSourceService  dataSourceService, AtlasTypeRegistry atlasTypeRegistry, AtlasGraph graph){
        this.entitiesStore=entitiesStore;
        this.dataSourceService=dataSourceService;
        this.atlasEntityStore=entitiesStore;
        this.atlasTypeRegistry=atlasTypeRegistry;
        this.graph=graph;
    }

    protected AbstractMetaDataProvider() {
        entityRetriever = new EntityGraphRetriever(atlasTypeRegistry);
        gremlinQueryProvider = AbstractMetaspaceGremlinQueryProvider.INSTANCE;
        try {
            atlasConf = ApplicationProperties.get();
        } catch (AtlasException e) {
            LOG.error("init config error,", e);
        }
        clusterName = atlasConf.getString(CLUSTER_NAME, DEFAULT_CLUSTER_NAME);
        metaDataContext = new MetaDataContext();
    }

    /**
     * 每次开始导入之前，需要执行的初始化
     */
    protected void init(TableSchema tableSchema) throws Exception {
        dataSource = null;
        dataSourceInfo = dataSourceService.getDataSourceInfo(tableSchema.getInstance());
        optionsBuilder = SchemaCrawlerOptionsBuilder
                .builder()
                .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard());
        skipSchemaTable();
        SchemaCrawlerOptions options = optionsBuilder.toOptions();
        catalog=null;
        try(Connection connection = getConnection()) {
            catalog = SchemaCrawlerUtility.getCatalog(connection, options);
        }catch (Exception e){
            LOG.error("import metadata error", e);
            throw e;
        }
        databaseNames = catalog.getSchemas();
        tableNames = catalog.getTables();
        addTableNames();
    }
    protected void addTableNames(){};

    private void skipSchemaTable() {
        //如果不为空，则为要过滤的库
        if (StringUtils.isNotEmpty(getSkipSchemas())) {
            optionsBuilder.includeSchemas(new RegularExpressionExclusionRule(getSkipSchemas()));
        }
        //如果不为空，则为要查询的表
        if (StringUtils.isNotEmpty(getSkipTables())) {
            optionsBuilder.includeTables(new RegularExpressionInclusionRule(getSkipTables()));
        }
    }

    protected Connection getConnection() {

        String           ip             = dataSourceInfo.getIp();
        String           port           = dataSourceInfo.getPort();
        String           sourceType     = dataSourceInfo.getSourceType();
        String           database       = dataSourceInfo.getDatabase();
        String           jdbcParameter  = dataSourceInfo.getJdbcParameter();
        String           userName       = dataSourceInfo.getUserName();
        String           password       = AESUtils.aesDecode(dataSourceInfo.getPassword());
        if (null != dataSource) {
            dataSource.setUserCredentials(new SingleUseUserCredentials(userName, password));
            return dataSource.get();
        }
        String connectUrl = RMDBEnum.of(sourceType).getConnectUrl();
        String connectionUrl = String.format(connectUrl, ip, port, database);
        Map<String,String> map = new HashMap<>();
        jdbcConnectionProperties(map);
        String format = "&";
        if (StringUtils.isNotEmpty(jdbcParameter)) {
            for (String str :jdbcParameter.split(format)){
                String[] strings = str.split("=");
                if (strings.length==2){
                    map.put(strings[0],strings[1]);
                }
            }
        }

        dataSource = new DatabaseConnectionSource(connectionUrl,map);
        dataSource.setUserCredentials(new SingleUseUserCredentials(userName, password));
        return dataSource.get();
    }
    protected abstract void jdbcConnectionProperties(Map<String,String> map);

    /**
     * 导入表
     *
     * @param dbEntity
     * @param databaseName
     * @param tableName
     * @param failOnError
     * @return
     * @throws Exception
     */
    protected int importTable(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName, final boolean failOnError,String instanceGuid) throws Exception {
        try {
            registerTable(dbEntity, instanceId, databaseName, tableName, instanceGuid);
            return 1;
        } catch (Exception e) {
            LOG.error("Import failed for {} {}", getTableTypeName(),tableName, e);
            if (failOnError) {
                throw e;
            }
            return 0;
        }
    }

    public String getClusterName() {
        return clusterName;
    }

    protected abstract String getRMDBType();

    /**
     * 组装instance信息
     *
     * @param instanceId
     * @return
     */
    protected AtlasEntity.AtlasEntityWithExtInfo toInstanceEntity(AtlasEntity.AtlasEntityWithExtInfo instanceEntity, String instanceId){
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
    };

    /**
     * 获取外键关键字段
     * @param foreignKey
     * @param columns
     * @return
     */
    protected List<AtlasObjectId> getKeyColumns(ForeignKey foreignKey, List<AtlasEntity> columns) {
        List<AtlasObjectId> columnObjectIds = new ArrayList<>();
        for (ForeignKeyColumnReference columnReference : foreignKey.getColumnReferences()) {
            String columnName = columnReference.getForeignKeyColumn().getName();
            Optional<AtlasEntity> first = columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(columnName)).findFirst();
            first.ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
        }
        return columnObjectIds;
    }

    protected List<AtlasObjectId> getPrimaryKeyColumns(ForeignKey foreignKey, List<AtlasEntity> columns) {
        List<AtlasObjectId> columnObjectIds = new ArrayList<>();
        for (ForeignKeyColumnReference columnReference : foreignKey.getColumnReferences()) {
            String columnName = columnReference.getPrimaryKeyColumn().getName();
            Optional<AtlasEntity> first = columns.stream().filter(columnEntity -> columnEntity.getAttribute(ATTRIBUTE_NAME).equals(columnName)).findFirst();
            first.ifPresent(atlasEntity -> columnObjectIds.add(getObjectId(atlasEntity)));
        }
        return columnObjectIds;
    }

    protected List<AtlasEntity> toIndexes(Collection<Index> indexes, List<AtlasEntity> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity, String databaseName, String instanceGuid) throws AtlasBaseException {
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

    protected List<AtlasEntity> toColumns(List<Column> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity) throws AtlasBaseException {
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

    protected List<AtlasEntity> toForeignKeys(Collection<ForeignKey> foreignKeys, List<AtlasEntity> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String databaseName) throws AtlasBaseException {
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> ret = new ArrayList<>();
        for (ForeignKey foreignKey : foreignKeys) {
            Column foreignKeyColumn = foreignKey.getColumnReferences().get(0).getForeignKeyColumn();
            Column primaryKeyColumn = foreignKey.getColumnReferences().get(0).getPrimaryKeyColumn();
            if (foreignKeyColumn.getSchema().getFullName().equalsIgnoreCase(databaseName) &&foreignKeyColumn.getParent().getName().equalsIgnoreCase(table.getAttribute(ATTRIBUTE_NAME).toString())){
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
                //todo ATTRIBUTE_REFERENCES_TABLE 和 ATTRIBUTE_REFERENCES_COLUMNS 需要需要表已经存在janusgraph中
                //            visitForeignEntity(foreignEntity, foreignKey, dbEntity, instanceId);
                String tableQualifiedName = getTableQualifiedName(table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString().split("\\.")[0],primaryKeyColumn.getSchema().getFullName(),primaryKeyColumn.getParent().getName());
                AtlasEntity.AtlasEntityWithExtInfo tableInfo = findEntity(getTableTypeName(), tableQualifiedName);
                if (tableInfo!=null){
                    tableInfo = getById(tableInfo.getEntity().getGuid(),false);
                    foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_TABLE, getObjectId(tableInfo.getEntity()));
                    List<AtlasEntity> primaryColumns = tableInfo.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(RDBMS_COLUMN)).collect(Collectors.toList());
                    foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_COLUMNS, getPrimaryKeyColumns(foreignKey, primaryColumns));
                    tableInfo.addReferredEntity(foreignEntity);
                    ((List<AtlasObjectId>)tableInfo.getEntity().getAttribute(ATTRIBUTE_FOREIGN_KEYS)).add(getObjectId(foreignEntity));
                    createOrUpdateEntity(tableInfo);
                }
                foreignEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
                foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS, getKeyColumns(foreignKey, columns));

                ret.add(foreignEntity);
            }else{
                String tableQualifiedName = getTableQualifiedName(table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString().split("\\.")[0],foreignKeyColumn.getSchema().getFullName(),foreignKeyColumn.getParent().getName());
                String foreignQualifiedName = getColumnQualifiedName(tableQualifiedName,foreignKey.getName());
                AtlasEntity.AtlasEntityWithExtInfo info = findEntity(RDBMS_FOREIGN_KEY, foreignQualifiedName);
                if (info!=null){
                    info = getById(info.getEntity().getGuid(),true);
                    AtlasEntity foreignEntity = info.getEntity();
                    foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_TABLE, getObjectId(table));
                    foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_COLUMNS, getPrimaryKeyColumns(foreignKey, columns));
                    ret.add(foreignEntity);
                }
            }
        }
        return ret;
    }

    protected void resultToMap(ResultSet results,Map<String,String> pair) throws SchemaCrawlerException, SQLException {
        final ResultsColumns resultColumns = SchemaCrawlerUtility
                .getResultsColumns(results);
        if (results.next()) {
            for (final ResultsColumn column : resultColumns) {
                String columnLabel = column.getLabel();
                pair.put(columnLabel.toLowerCase(), results.getString(columnLabel));
            }
        }
    }
    /**
     * 将数据库信息组装成atlas entity
     *
     * @param dbEntity
     * @param databaseName
     * @return
     */
    protected AtlasEntity.AtlasEntityWithExtInfo toDBEntity(AtlasEntity.AtlasEntityWithExtInfo instance, AtlasEntity.AtlasEntityWithExtInfo dbEntity, String instanceId, String databaseName){
        AtlasEntity instanceEntity = instance.getEntity();
        if (dbEntity == null) {
            dbEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getDatabaseTypeName()));
        }

        String dbId = null;
        if (instance.getReferredEntities()!=null){
            List<String> dbIds = instance.getReferredEntities().values().stream().filter(entity-> entity.getStatus()==AtlasEntity.Status.ACTIVE&&entity.getTypeName().equalsIgnoreCase(getDatabaseTypeName())&&entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(databaseName)).map(entity->entity.getGuid()).collect(Collectors.toList());
            if(dbIds!=null && dbIds.size()!=0){
                dbId = dbIds.get(0);
            }
        }

        AtlasEntity entity = dbEntity.getEntity();

        if (dbId!=null) {
            entity.setGuid(dbId);
        }

        entity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getDBQualifiedName(instanceId, databaseName));
        entity.setAttribute(ATTRIBUTE_NAME, databaseName.toLowerCase());
        entity.setAttribute(ATTRIBUTE_CLUSTER_NAME, clusterName);
        entity.setAttribute(ATTRIBUTE_PRODOROTHER, "");
        entity.setAttribute(ATTRIBUTE_INSTANCE, getObjectId(instanceEntity));
        return dbEntity;
    }

    /**
     * 将表信息组装成atlas entity
     *
     * @param dbEntity
     * @param databaseName
     * @param tableName
     * @param tableEntity
     * @return
     */
    protected AtlasEntity.AtlasEntityWithExtInfo toTableEntity(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String instanceGuid) throws AtlasBaseException{
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
        return tableEntity;
    }

    protected void setTableAttribute(List<AtlasEntity> columns,List<AtlasEntity> indexes,List<AtlasEntity> foreignKeys,AtlasEntity.AtlasEntityWithExtInfo tableEntity,AtlasEntity table){
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
    }

    protected abstract Map<String, String> getTableCreateTime(String databaseName,String tableName);

    /**
     * 需要跳过的schema名称列表
     * @return
     */
    protected abstract String getSkipSchemas();

    /**
     * 需要跳过的表名列表
     * @return
     */
    protected abstract String getSkipTables();
    /**
     * 需要跳过的列名列表
     * @return
     */

    /**
     * Checks if instance is already registered, else creates and registers db entity
     *
     * @param instanceId 实例id
     * @return
     * @throws Exception
     */
    protected AtlasEntity.AtlasEntityWithExtInfo registerInstance(String instanceId) throws Exception {
        AtlasEntity.AtlasEntityWithExtInfo ret;
        String instanceQualifiedName = getInstanceQualifiedName(instanceId);
        if (metaDataContext.isKownEntity(instanceQualifiedName)) {
            ret = metaDataContext.getEntity(instanceQualifiedName);
        }else {
            ret = findInstance(instanceId);
            clearRelationshipAttributes(ret);
            metaDataContext.putEntity(instanceQualifiedName, ret);
        }
        AtlasEntity.AtlasEntityWithExtInfo instanceEntity;
        if (ret == null) {
            instanceEntity = toInstanceEntity(null, instanceId);
            ret = registerEntity(instanceEntity);
            isSource = false;
        } else {
            LOG.info("Instance {} is already registered - id={}. Updating it.", instanceId, ret.getEntity().getGuid());
            ret = toInstanceEntity(ret, instanceId);
            createOrUpdateEntity(ret);
            isSource = true;
        }

        return ret;
    }

    protected void createOrUpdateEntity(AtlasEntity.AtlasEntityWithExtInfo dbEntity) throws AtlasBaseException {
        entitiesStore.createOrUpdate(new AtlasEntityStream(dbEntity), false);
    }


    public AtlasEntity.AtlasEntityWithExtInfo findInstance(String instanceId) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching Atlas for instance {}", instanceId);
        }
        return findEntity(getInstanceTypeName(), getInstanceQualifiedName(instanceId));
    }


    /**
     * 获取数据源类型
     * @return
     */
    public String getInstanceTypeName() {
        return RMDB_INSTANCE;
    }

    /**
     * 获取数据源类型
     * @return
     */
    public String getDatabaseTypeName() {
        return RMDB_DB;
    }

    /**
     * 获取数据源类型
     * @return
     */
    public String getTableTypeName() {
        return RMDB_TABLE;
    }

    protected AtlasEntity.AtlasEntityWithExtInfo registerTable(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName,String instanceGuid) throws Exception {
        AtlasEntity.AtlasEntityWithExtInfo ret = findEntity(getTableTypeName(), getTableQualifiedName(instanceId, databaseName, tableName.getName()));

        AtlasEntity.AtlasEntityWithExtInfo tableEntity;
        if (ret == null) {
            tableEntity = toTableEntity(dbEntity, instanceId, databaseName, tableName, null,instanceGuid);
            ret = registerEntity(tableEntity);
        } else {
            LOG.info("Table {}.{} is already registered with id {}. Updating entity.", databaseName, tableName, ret.getEntity().getGuid());
            ret = toTableEntity(dbEntity, instanceId, databaseName, tableName, ret,instanceGuid);

            createOrUpdateEntity(ret);
        }

        return ret;
    }


    /**
     * import all database
     *
     * @throws Exception
     */
    public void importDatabases(TableSchema tableSchema) throws Exception {
        entityRetriever = new EntityGraphRetriever(atlasTypeRegistry);
        LOG.info("import metadata start at {}", simpleDateFormat.format(new Date()));
        totalTables.set(0);
        updatedTables.set(0);
        startTime.set(System.currentTimeMillis());
        endTime.set(0);
        metaDataContext = new MetaDataContext();
        init(tableSchema);
        totalTables.set(tableNames.size());
        String instanceId = tableSchema.getInstance();


        //根据数据源id获取图数据库中的数据源，如果有，则更新，如果没有，则创建
        AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo = registerInstance(instanceId);
        String instanceGuid = atlasEntityWithExtInfo.getEntity().getGuid();
        if (!CollectionUtils.isEmpty(databaseNames)) {
            LOG.info("Found {} databases", databaseNames.size());

            //导入table
            for (Schema database : databaseNames) {
                AtlasEntity.AtlasEntityWithExtInfo dbEntity;
                String dbQualifiedName = getDBQualifiedName(instanceId, database.getFullName());
                if (metaDataContext.isKownEntity(dbQualifiedName)) {
                    dbEntity = metaDataContext.getEntity(dbQualifiedName);
                } else {
                    dbEntity = findDatabase(instanceId, database.getFullName());
                    clearRelationshipAttributes(dbEntity);
                    metaDataContext.putEntity(dbQualifiedName, dbEntity);
                }
                importTables(dbEntity.getEntity(), instanceId, database.getFullName(), getTables(database), false,instanceGuid);
            }

        } else {
            LOG.info("No database found");
        }
        LOG.info("import metadata end at {}", simpleDateFormat.format(new Date()));
    }


    /**
     * Gets the atlas entity for the database
     * @param databaseName  database Name
     * @return AtlasEntity for database if exists, else null
     * @throws Exception
     */
    private AtlasEntity.AtlasEntityWithExtInfo findDatabase(String instanceId, String databaseName) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching Atlas for database {}", databaseName);
        }
        return findEntity(getDatabaseTypeName(), getDBQualifiedName(instanceId, databaseName));
    }

    public String getInstanceQualifiedName(String instanceId) {
        return String.format("%s@%s", instanceId, clusterName);
    }

    /**
     * Construct the qualified name used to uniquely identify a Database instance in Atlas.
     * @param dbName Name of the Hive database
     * @return Unique qualified name to identify the Database instance in Atlas.
     */
    protected String getDBQualifiedName(String instanceId, String dbName) {
        return String.format("%s.%s@%s", instanceId, dbName.toLowerCase(), clusterName);
    }

    protected String getTableQualifiedName(String instanceId, String dbName, String tableName) {
        return String.format("%s.%s.%s@%s", instanceId, dbName.toLowerCase(), tableName.toLowerCase(), clusterName);
    }

    protected static String getColumnQualifiedName(String tableQualifiedName, final String colName) {
        final String[] parts       = tableQualifiedName.split("@");
        final String   tableName   = parts[0];
        final String   clusterName = parts[1];

        return String.format("%s.%s@%s", tableName, colName.toLowerCase(), clusterName);
    }


    protected AtlasEntity.AtlasEntityWithExtInfo findEntity(final String typeName, final String qualifiedName) throws AtlasBaseException {
        AtlasEntity.AtlasEntityWithExtInfo ret = null;
        try {
            ret = atlasEntityStore.getByUniqueAttributes(atlasTypeRegistry.getEntityTypeByName(typeName), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName),true);
        } catch (AtlasBaseException e) {
            return null;
        }
        clearRelationshipAttributes(ret);

        return ret;
    }

    protected AtlasEntity.AtlasEntityWithExtInfo getById(final String guid,boolean isMinExtInfo) throws AtlasBaseException {
        AtlasEntity.AtlasEntityWithExtInfo ret = null;
        try {
            ret = atlasEntityStore.getById(guid,isMinExtInfo);
        } catch (AtlasBaseException e) {
            return null;
        }
        clearRelationshipAttributes(ret);

        return ret;
    }

    /**
     * Imports all tables for the given db
     * @param dbEntity
     * @param databaseName
     * @param failOnError
     * @throws Exception
     */
    private int importTables(AtlasEntity dbEntity, String instanceId, String databaseName,Collection<Table> tableNames, final boolean failOnError,String instanceGuid) throws Exception {
        int tablesImported = 0;

        if(!CollectionUtils.isEmpty(tableNames)) {
            LOG.info("Found {} tables to import in database {}", tableNames.size(), databaseName);

            try {
                //删除JanusGraph中已经不存在table
                if (isSource){
                    deleteTableEntity(instanceGuid, databaseName, tableNames);
                }

                for (Table tableName : tableNames) {
                    if (isThread){
                        LOG.error("终止元数据同步,数据源：");
                        throw new InterruptedException("终止元数据同步,数据源："+dataSourceInfo.getSourceName());
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("导入{}表的元数据", tableName.getFullName());
                    }
                    int imported = importTable(dbEntity, instanceId, databaseName, tableName, failOnError,instanceGuid);

                    tablesImported += imported;
                    updatedTables.incrementAndGet();
                }
            } catch (InterruptedException e){
                throw e;
            }catch (Exception e) {
                LOG.error("导入表元数据错误", e);
            } finally {
                if (tablesImported == tableNames.size()) {
                    LOG.info("Successfully imported {} tables from database {}", tablesImported, databaseName);
                } else {
                    LOG.error("Imported {} of {} tables from database {}. Please check logs for errors during import", tablesImported, tableNames.size(), databaseName);
                }
            }
        } else {
            LOG.info("No tables to import in database {}", databaseName);
        }

        return tablesImported;
    }

    /**
     * Registers an entity in atlas
     *
     * @param entity
     * @return
     * @throws Exception
     */
    protected AtlasEntity.AtlasEntityWithExtInfo registerEntity(AtlasEntity.AtlasEntityWithExtInfo entity) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating {} entity: {}", entity.getEntity().getTypeName(), entity);
        }

        AtlasEntity.AtlasEntityWithExtInfo ret             = null;
        EntityMutationResponse response = atlasEntityStore.createOrUpdate(new AtlasEntityStream(entity), false);
        List<AtlasEntityHeader>            createdEntities = response.getEntitiesByOperation(EntityMutations.EntityOperation.CREATE);

        if (CollectionUtils.isNotEmpty(createdEntities)) {
            for (AtlasEntityHeader createdEntity : createdEntities) {
                if (ret == null) {
                    ret = atlasEntityStore.getById(createdEntity.getGuid());
                    LOG.info("Created {} entity: name={}, guid={}", ret.getEntity().getTypeName(), ret.getEntity().getAttribute(ATTRIBUTE_QUALIFIED_NAME), ret.getEntity().getGuid());
                } else if (ret.getEntity(createdEntity.getGuid()) == null) {
                    AtlasEntity.AtlasEntityWithExtInfo newEntity = atlasEntityStore.getById(createdEntity.getGuid());

                    ret.addReferredEntity(newEntity.getEntity());

                    if (MapUtils.isNotEmpty(newEntity.getReferredEntities())) {
                        for (Map.Entry<String, AtlasEntity> entry : newEntity.getReferredEntities().entrySet()) {
                            ret.addReferredEntity(entry.getKey(), entry.getValue());
                        }
                    }

                    LOG.info("Created {} entity: name={}, guid={}", newEntity.getEntity().getTypeName(), newEntity.getEntity().getAttribute(ATTRIBUTE_QUALIFIED_NAME), newEntity.getEntity().getGuid());
                }
            }
        }

        clearRelationshipAttributes(ret);
        return ret;
    }

    private void deleteTableEntity(String instanceId, String databaseName, Collection<Table> tableNames) throws AtlasBaseException {
        String tableQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_BY_STATE), instanceId, databaseName.toLowerCase(), AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(tableQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String>                       attributes          = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity                        tableEntity         = dbEntityWithExtInfo.getEntity();
                String                             tableNameInGraph    = tableEntity.getAttribute(ATTRIBUTE_NAME).toString();
                if (tableNames.stream().noneMatch(table -> table.getName().equalsIgnoreCase(tableNameInGraph))) {
                    LOG.info("表{}已经在数据源删除，同样在metaspace中删除之", tableNameInGraph);
                    deleteEntity(tableEntity);
                }
            }
        }
    }


    protected void deleteColumnEntity(AtlasEntity.AtlasEntityWithExtInfo tableInfo) throws AtlasBaseException {
        AtlasEntity tableEntity = tableInfo.getEntity();
        List<AtlasObjectId> columns = new ArrayList<>();
        if (tableEntity.getAttribute(ATTRIBUTE_COLUMNS)!=null){
            columns = (List<AtlasObjectId>) tableEntity.getAttribute(ATTRIBUTE_COLUMNS);
        }

        String columnQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_COLUMN_BY_STATE), tableEntity.getGuid(),AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(columnQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String>                       attributes          = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity                        columnEntity         = dbEntityWithExtInfo.getEntity();
                String                             columnNameInGraph    = columnEntity.getAttribute(ATTRIBUTE_NAME).toString();
                String                              qualifiedName       = columnEntity.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString();
                if (columns.stream().noneMatch(column -> column.getUniqueAttributes().get(ATTRIBUTE_QUALIFIED_NAME).toString().equals(qualifiedName))) {
                    LOG.info("列{}已经在数据源删除，同样在metaspace中删除之", columnNameInGraph);
                    deleteEntity(columnEntity);
                }
            }
        }
    }

    protected void deleteForeignKeyEntity(AtlasEntity.AtlasEntityWithExtInfo tableInfo) throws AtlasBaseException {
        AtlasEntity tableEntity = tableInfo.getEntity();
        List<AtlasObjectId> foreignKeys = new ArrayList<>();
        if (tableEntity.getAttribute(ATTRIBUTE_FOREIGN_KEYS)!=null){
            foreignKeys = (List<AtlasObjectId>) tableEntity.getAttribute(ATTRIBUTE_FOREIGN_KEYS);
        }
        String foreignKeyQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_FOREIGNKEY_BY_STATE), tableEntity.getGuid() ,AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(foreignKeyQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String>                       attributes          = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity                        foreignKeyEntity         = dbEntityWithExtInfo.getEntity();
                String                             foreignKeyNameInGraph    = foreignKeyEntity.getAttribute(ATTRIBUTE_NAME).toString();
                String                              qualifiedName       = foreignKeyEntity.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString();
                if (foreignKeys.stream().noneMatch(foreignKey -> foreignKey.getUniqueAttributes().get(ATTRIBUTE_QUALIFIED_NAME).toString().equals(qualifiedName))) {
                    LOG.info("列{}已经在数据源删除，同样在metaspace中删除之", foreignKeyNameInGraph);
                    deleteEntity(foreignKeyEntity);
                }
            }
        }
    }

    protected void deleteIndexEntity(AtlasEntity.AtlasEntityWithExtInfo tableInfo) throws AtlasBaseException {
        AtlasEntity tableEntity = tableInfo.getEntity();
        List<AtlasObjectId> indexes = new ArrayList<>();
        if (tableEntity.getAttribute(ATTRIBUTE_INDEXES)!=null){
            indexes = (List<AtlasObjectId>) tableEntity.getAttribute(ATTRIBUTE_INDEXES);
        }
        String indexQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_INDEX_BY_STATE), tableEntity.getGuid() ,AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(indexQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String>                       attributes          = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity                        indexEntity         = dbEntityWithExtInfo.getEntity();
                String                             indexNameInGraph    = indexEntity.getAttribute(ATTRIBUTE_NAME).toString();
                String                              qualifiedName       = indexEntity.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString();
                if (indexes.stream().noneMatch(index -> index.getUniqueAttributes().get(ATTRIBUTE_QUALIFIED_NAME).toString().equals(qualifiedName))) {
                    LOG.info("列{}已经在数据源删除，同样在metaspace中删除之", indexNameInGraph);
                    deleteEntity(indexEntity);
                }

            }
        }
    }


    private void deleteEntity(AtlasEntity tableEntity) throws AtlasBaseException {
        AtlasEntityType     type     = (AtlasEntityType) atlasTypeRegistry.getType(tableEntity.getTypeName());
        final AtlasObjectId objectId = getObjectId(tableEntity);
        atlasEntityStore.deleteByUniqueAttributes(type, objectId.getUniqueAttributes());
    }

    public static AtlasObjectId getObjectId(AtlasEntity entity) {
        String        qualifiedName = (String) entity.getAttribute(ATTRIBUTE_QUALIFIED_NAME);
        AtlasObjectId ret           = new AtlasObjectId(entity.getGuid(), entity.getTypeName(), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName));

        return ret;
    }

    private void clearRelationshipAttributes(AtlasEntity.AtlasEntityWithExtInfo entity) {
        if (entity != null) {
            clearRelationshipAttributes(entity.getEntity());

            if (entity.getReferredEntities() != null) {
                clearRelationshipAttributes(entity.getReferredEntities().values());
            }
        }
    }

    private void clearRelationshipAttributes(Collection<AtlasEntity> entities) {
        if (entities != null) {
            for (AtlasEntity entity : entities) {
                clearRelationshipAttributes(entity);
            }
        }
    }

    private void clearRelationshipAttributes(AtlasEntity entity) {
        if (entity != null && entity.getRelationshipAttributes() != null) {
            entity.getRelationshipAttributes().clear();
        }
    }

    public AtomicInteger getTotalTables() {
        return totalTables;
    }

    public AtomicInteger getUpdatedTables() {
        return updatedTables;
    }

    public AtomicLong getStartTime() {
        return startTime;
    }

    public AtomicLong getEndTime() {
        return endTime;
    }

    public Collection<Table> getTables(final Schema schema)
    {
        final Collection<Table> tables = new ArrayList<>();
        for (final Table table : this.tableNames)
        {
            if (test(table,schema))
            {
                tables.add(table);
            }
        }
        return tables;
    }
    public boolean test(final DatabaseObject databaseObject,final Schema schema)
    {
        return databaseObject != null && databaseObject.getSchema()
                .equals(schema);
    }
}
