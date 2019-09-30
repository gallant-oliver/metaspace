package io.zeta.metaspace.web.metadata;

import com.google.common.collect.Lists;
import io.zeta.metaspace.model.dataSource.DataSourceInfo;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.MetaspaceGremlinQueryProvider;
import io.zeta.metaspace.web.model.TableSchema;
import io.zeta.metaspace.web.service.DataSourceService;
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
import schemacrawler.schema.Index;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.*;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.inject.Inject;
import java.sql.Connection;
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
public abstract class MetaDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MetaDataProvider.class);
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
    private MetaspaceGremlinQueryProvider gremlinQueryProvider;
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

    protected MetaDataProvider() {
        entityRetriever = new EntityGraphRetriever(atlasTypeRegistry);
        gremlinQueryProvider = MetaspaceGremlinQueryProvider.INSTANCE;
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
//        String databaseToImport = tableSchema.getDatabase();
//        String tableToImport = tableSchema.getTable();
//        if (StringUtils.isNotEmpty(databaseToImport)) {
//            //获取所有数据库
//            optionsBuilder.includeSchemas(new RegularExpressionInclusionRule(databaseToImport));
//        }
//        if (StringUtils.isNotEmpty(tableToImport)) {
//            //获取指定数据库指定表
//            optionsBuilder.includeTables(new RegularExpressionInclusionRule(tableToImport));
//        }
        skipSchemaTable();
        SchemaCrawlerOptions options = optionsBuilder.toOptions();
        catalog=null;
        try(Connection connection = getConnection()) {
            catalog = SchemaCrawlerUtility.getCatalog(connection, options);
        }catch (Exception e){
            LOG.error("import metadata error", e);
        }
        databaseNames = catalog.getSchemas();
        tableNames = catalog.getTables();
        addTableNames();
    }
    protected void addTableNames(){};

    private void skipSchemaTable() {
        if (StringUtils.isNotEmpty(getSkipSchemas())) {
            optionsBuilder.includeSchemas(new RegularExpressionExclusionRule(getSkipSchemas()));
        }

        if (StringUtils.isNotEmpty(getSkipTables())) {
            optionsBuilder.includeTables(new RegularExpressionExclusionRule(getSkipTables()));
        }
    }

    protected Connection getConnection() {

        String           ip             = dataSourceInfo.getIp();
        String           port           = dataSourceInfo.getPort();
        String           sourceType     = dataSourceInfo.getSourceType();
        String           database       = dataSourceInfo.getDatabase();
        String           jdbcParameter  = dataSourceInfo.getJdbcParameter();
        String           userName       = dataSourceInfo.getUserName();
        String           password       = dataSourceInfo.getPassword();
        if (null != dataSource) {
            dataSource.setUserCredentials(new SingleUseUserCredentials(userName, password));
            return dataSource.get();
        }
        String connectUrl = RMDBEnum.of(sourceType).getConnectUrl();
        String connectionUrl = String.format(connectUrl, ip, port, database);
        if (StringUtils.isNotEmpty(jdbcParameter)) {
            connectionUrl = connectionUrl + "?" + jdbcParameter;
        }

        dataSource = new DatabaseConnectionSource(connectionUrl);
        dataSource.setUserCredentials(new SingleUseUserCredentials(userName, password));
        return dataSource.get();
    }

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
            registerTable(dbEntity, instanceId, databaseName, tableName,instanceGuid);
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
    protected abstract AtlasEntity.AtlasEntityWithExtInfo toInstanceEntity(AtlasEntity.AtlasEntityWithExtInfo instanceEntity, String instanceId);

    /**
     * 将数据库信息组装成atlas entity
     *
     * @param dbEntity
     * @param databaseName
     * @return
     */
    protected abstract AtlasEntity.AtlasEntityWithExtInfo toDBEntity(AtlasEntity instanceEntity, AtlasEntity.AtlasEntityWithExtInfo dbEntity, String instanceId, String databaseName);

    /**
     * 将表信息组装成atlas entity
     *
     * @param dbEntity
     * @param databaseName
     * @param tableName
     * @param tableEntity
     * @return
     */
    protected abstract AtlasEntity.AtlasEntityWithExtInfo toTableEntity(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String instanceGuid) throws AtlasBaseException;

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
        } else {
            LOG.info("Instance {} is already registered - id={}. Updating it.", instanceId, ret.getEntity().getGuid());
            ret = toInstanceEntity(ret, instanceId);
            createOrUpdateEntity(ret);
        }

        return ret;
    }

    private void createOrUpdateEntity(AtlasEntity.AtlasEntityWithExtInfo dbEntity) throws AtlasBaseException {
        entitiesStore.createOrUpdate(new AtlasEntityStream(dbEntity), false);
    }


    public AtlasEntity.AtlasEntityWithExtInfo findInstance(String instanceId) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching Atlas for instance {}", instanceId);
        }
        return findEntity(getInstanceTypeName(), getInstanceQualifiedName(instanceId));
    }


    /**
     * Checks if db is already registered, else creates and registers db entity
     *
     *
     * @param entity
     * @param databaseName
     * @return
     * @throws Exception
     */
    protected AtlasEntity.AtlasEntityWithExtInfo registerDatabase(AtlasEntity entity, String instanceId, String databaseName) throws Exception {
        AtlasEntity.AtlasEntityWithExtInfo ret = findDatabase(instanceId, databaseName);

        AtlasEntity.AtlasEntityWithExtInfo dbEntity;
        if (ret == null) {
            dbEntity = toDBEntity(entity, null, instanceId, databaseName);
            ret = registerEntity(dbEntity);
        } else {
            LOG.info("Database {} is already registered - id={}. Updating it.", databaseName, ret.getEntity().getGuid());
            ret = toDBEntity(entity, ret, instanceId, databaseName);
            createOrUpdateEntity(ret);
        }

        return ret;
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
        init(tableSchema);
        totalTables.set(tableNames.size());
        String instanceId = tableSchema.getInstance();


        //根据数据源id获取图数据库中的数据源，如果有，则更新，如果没有，则创建
        AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo = registerInstance(instanceId);
        String instanceGuid = atlasEntityWithExtInfo.getEntity().getGuid();
        if (!CollectionUtils.isEmpty(databaseNames)) {
            LOG.info("Found {} databases", databaseNames.size());

            //删除JanusGraph中已经不存在的database,以及database中的table
            String databaseQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_RDBMS_DB_BY_STATE), instanceGuid, AtlasEntity.Status.ACTIVE);
            List<AtlasVertex> dbVertices    = (List) graph.executeGremlinScript(databaseQuery, false);
            for (AtlasVertex vertex : dbVertices) {
                if (Objects.nonNull(vertex)) {
                    List<String> attributes = Lists.newArrayList(ATTRIBUTE_NAME, ATTRIBUTE_QUALIFIED_NAME);
                    AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                    AtlasEntity                        dbEntity            = dbEntityWithExtInfo.getEntity();
                    String                             databaseInGraph     = dbEntity.getAttribute(ATTRIBUTE_NAME).toString();
                    if (databaseNames.stream().noneMatch(schema -> schema.getFullName().equalsIgnoreCase(databaseInGraph))) {
                        deleteTableEntity(instanceGuid, databaseInGraph, new ArrayList<>());
                        LOG.info("数据库{}已经在数据源删除，在metaspace中同样删除之", databaseInGraph);
                        deleteEntity(dbEntity);
                    }
                }
            }
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

    protected String getInstanceQualifiedName(String instanceId) {
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
            ret = atlasEntityStore.getByUniqueAttributes(atlasTypeRegistry.getEntityTypeByName(typeName), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName));
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
                deleteTableEntity(instanceGuid, databaseName, tableNames);

                for (Table tableName : tableNames) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("导入{}表的元数据", tableName.getFullName());
                    }
                    int imported = importTable(dbEntity, instanceId, databaseName, tableName, failOnError,instanceGuid);

                    tablesImported += imported;
                    updatedTables.incrementAndGet();
                }
            } catch (Exception e) {
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
    private AtlasEntity.AtlasEntityWithExtInfo registerEntity(AtlasEntity.AtlasEntityWithExtInfo entity) throws Exception {
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
                    deleteColumnEntity(instanceId,databaseName,tableNameInGraph,new ArrayList<>(),null);
                    deleteForeignKeyEntity(instanceId,databaseName,tableNameInGraph,new ArrayList<>(),null);
                    deleteIndexEntity(instanceId,databaseName,tableNameInGraph,new ArrayList<>(),null);
                }
            }
        }
    }

    protected void deleteColumnEntity(String instanceId, String databaseName,String tableName, Collection<Column> columnNames,Collection<String> columnNames2) throws AtlasBaseException {
        String columnQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_COLUMN_BY_STATE), instanceId, databaseName.toLowerCase(), tableName.toLowerCase() ,AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(columnQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String>                       attributes          = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity                        columnEntity         = dbEntityWithExtInfo.getEntity();
                String                             columnNameInGraph    = columnEntity.getAttribute(ATTRIBUTE_NAME).toString();
                if (columnNames!=null){
                    if (columnNames.stream().noneMatch(column -> column.getName().equalsIgnoreCase(columnNameInGraph))) {
                        LOG.info("列{}已经在数据源删除，同样在metaspace中删除之", columnNameInGraph);
                        deleteEntity(columnEntity);
                    }
                }else{
                    if (columnNames2.stream().noneMatch(column -> column.equalsIgnoreCase(columnNameInGraph))) {
                        LOG.info("列{}已经在数据源删除，同样在metaspace中删除之", columnNameInGraph);
                        deleteEntity(columnEntity);
                    }
                }

            }
        }
    }

    protected void deleteForeignKeyEntity(String instanceId, String databaseName, String tableName, Collection<ForeignKey> foreignKeyNames,Collection<String> foreignKeyNames2) throws AtlasBaseException {
        String foreignKeyQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_FOREIGNKEY_BY_STATE), instanceId, databaseName, tableName ,AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(foreignKeyQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String>                       attributes          = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity                        foreignKeyEntity         = dbEntityWithExtInfo.getEntity();
                String                             foreignKeyNameInGraph    = foreignKeyEntity.getAttribute(ATTRIBUTE_NAME).toString();
                if (foreignKeyNames!=null){
                    if (foreignKeyNames.stream().noneMatch(foreignKey -> foreignKey.getName().equalsIgnoreCase(foreignKeyNameInGraph))) {
                        LOG.info("键{}已经在数据源删除，同样在metaspace中删除之", foreignKeyNameInGraph);
                        deleteEntity(foreignKeyEntity);
                    }
                }else{
                    if (foreignKeyNames2.stream().noneMatch(foreignKey -> foreignKey.equalsIgnoreCase(foreignKeyNameInGraph))) {
                        LOG.info("键{}已经在数据源删除，同样在metaspace中删除之", foreignKeyNameInGraph);
                        deleteEntity(foreignKeyEntity);
                    }
                }
            }
        }
    }

    protected void deleteIndexEntity(String instanceId, String databaseName,String tableName, Collection<Index> indexNames,Collection<String> indexNames2) throws AtlasBaseException {
        String indexQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_INDEX_BY_STATE), instanceId, databaseName, tableName ,AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(indexQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String>                       attributes          = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity                        indexEntity         = dbEntityWithExtInfo.getEntity();
                String                             indexNameInGraph    = indexEntity.getAttribute(ATTRIBUTE_NAME).toString();
                if (indexNames!=null){
                    if (indexNames.stream().noneMatch(index -> index.getName().equalsIgnoreCase(indexNameInGraph))) {
                        LOG.info("索引{}已经在数据源删除，同样在metaspace中删除之", indexNameInGraph);
                        deleteEntity(indexEntity);
                    }
                }else{
                    if (indexNames2.stream().noneMatch(index -> index.equalsIgnoreCase(indexNameInGraph))) {
                        LOG.info("索引{}已经在数据源删除，同样在metaspace中删除之", indexNameInGraph);
                        deleteEntity(indexEntity);
                    }
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
