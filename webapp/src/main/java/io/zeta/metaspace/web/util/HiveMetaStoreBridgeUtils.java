/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.zeta.metaspace.web.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.AbstractMetaspaceGremlinQueryProvider;
import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.model.TableSchema;
import org.apache.atlas.*;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.hook.AtlasHookException;
import org.apache.atlas.model.instance.*;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntitiesWithExtInfo;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStream;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.utils.HdfsNameServiceResolver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.*;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static io.zeta.metaspace.web.util.BaseHiveEvent.*;


/**
 * A Bridge Utility that imports metadata from the Hive Meta Store
 * and registers them in Atlas.
 */
@Singleton
@Component
public class HiveMetaStoreBridgeUtils implements IMetaDataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(HiveMetaStoreBridgeUtils.class);

    public static final String CONF_PREFIX                     = "atlas.hook.hive.";
    public static final String HIVE_CLUSTER_NAME               = "atlas.cluster.name";
    public static final String HDFS_PATH_CONVERT_TO_LOWER_CASE = CONF_PREFIX + "hdfs_path.convert_to_lowercase";
    public static final String DEFAULT_CLUSTER_NAME            = "primary";
    public static final String TEMP_TABLE_PREFIX               = "_temp-";
    public static final String SEP                             = ":".intern();
    public static final String HDFS_PATH                       = "hdfs_path";

    private volatile AtomicInteger totalTables = new AtomicInteger(0);
    private volatile AtomicInteger updatedTables = new AtomicInteger(0);
    private volatile AtomicLong startTime = new AtomicLong(0);
    private volatile AtomicLong endTime = new AtomicLong(0);
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private final String                  clusterName;
    private Hive hiveMetaStoreClient = null;
    private Configuration atlasConf        = null;
    private final boolean convertHdfsPathToLowerCase;
    private final AtlasGraph graph;
    private final AbstractMetaspaceGremlinQueryProvider gremlinQueryProvider;
    private final EntityGraphRetriever entityRetriever;
    private final AtlasTypeRegistry atlasTypeRegistry;
    private final AtlasEntityStore     atlasEntityStore;
    private static final List<Pattern> hiveTablesToIgnore = new ArrayList<>();
    public static final String HOOK_HIVE_TABLE_IGNORE_PATTERN = CONF_PREFIX + "hive_table.ignore.pattern";;

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

    @Inject
    private HiveMetaStoreBridgeUtils(AtlasTypeRegistry typeRegistry, AtlasGraph atlasGraph, AtlasEntityStore atlasEntityStore) {
        try {
            atlasConf = ApplicationProperties.get();
        } catch (AtlasException e) {
            LOG.error("init config error,", e);
        }
        clusterName = atlasConf.getString(HIVE_CLUSTER_NAME, DEFAULT_CLUSTER_NAME);
        convertHdfsPathToLowerCase = atlasConf.getBoolean(HDFS_PATH_CONVERT_TO_LOWER_CASE, true);
        this.graph = atlasGraph;
        this.gremlinQueryProvider = AbstractMetaspaceGremlinQueryProvider.INSTANCE;
        this.entityRetriever = new EntityGraphRetriever(typeRegistry);
        this.atlasTypeRegistry = typeRegistry;
        this.atlasEntityStore = atlasEntityStore;
        String[] patternHiveTablesToIgnore = atlasConf.getStringArray(HOOK_HIVE_TABLE_IGNORE_PATTERN);
        if (patternHiveTablesToIgnore != null) {
            for (String pattern : patternHiveTablesToIgnore) {
                try {
                    hiveTablesToIgnore.add(Pattern.compile(pattern));

                    LOG.info("{}={}", HOOK_HIVE_TABLE_IGNORE_PATTERN, pattern);
                } catch (Throwable t) {
                    LOG.warn("failed to compile pattern {}", pattern, t);
                    LOG.warn("Ignoring invalid pattern in configuration {}: {}", HOOK_HIVE_TABLE_IGNORE_PATTERN, pattern);
                }
            }
        }
    }


    public String getClusterName() {
        return clusterName;
    }

    public boolean isConvertHdfsPathToLowerCase() {
        return convertHdfsPathToLowerCase;
    }

    /**
     * import all database
     * @throws Exception
     */
    public void importDatabases(TableSchema tableSchema) throws Exception {
        LOG.info("import metadata start at {}", simpleDateFormat.format(new Date()));
        List<String> databaseNames = null;
        totalTables.set(0);
        updatedTables.set(0);
        getStartTime().set(System.currentTimeMillis());
        getEndTime().set(0);
        String databaseToImport = "";
        String tableToImport = "";
        if (null != tableSchema) {
            databaseToImport = tableSchema.getDatabase();
            tableToImport = tableSchema.getTable();
        }
        initHiveMetaStoreClient();
        if (StringUtils.isEmpty(databaseToImport)) {
            databaseNames = hiveMetaStoreClient.getAllDatabases();

        } else {
            databaseNames = hiveMetaStoreClient.getDatabasesByPattern(databaseToImport);
        }
        int tables = 0;
        Map<String, List<String>> database2Table = Maps.newHashMap();
        if (StringUtils.isEmpty(tableToImport)) {
            for (String databaseName : databaseNames) {
                List<String> tablesInDB = database2Table.get(databaseName);
                if (null == tablesInDB) {
                    tablesInDB = Lists.newArrayList();
                }
                tablesInDB.addAll(hiveMetaStoreClient.getAllTables(databaseName));
                database2Table.put(databaseName, tablesInDB);
                tables += tablesInDB.size();
            }

        } else {
            for (String databaseName : databaseNames) {
                List<String> tablesInDB = database2Table.get(databaseName);
                if (null == tablesInDB) {
                    tablesInDB = Lists.newArrayList();
                }
                tablesInDB.addAll(hiveMetaStoreClient.getTablesByPattern(databaseName, tableToImport));
                database2Table.put(databaseName, tablesInDB);
                tables += tablesInDB.size();
            }
        }
        totalTables.set(tables);
        if (!CollectionUtils.isEmpty(databaseNames)) {
            LOG.info("Found {} databases", databaseNames.size());

            //删除JanusGraph中已经不存在的database,以及database中的table
            String databaseQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.FULL_DB_BY_STATE), AtlasEntity.Status.ACTIVE);
            List<AtlasVertex> dbVertices = (List) graph.executeGremlinScript(databaseQuery, false);
            for (AtlasVertex vertex : dbVertices) {
                if (Objects.nonNull(vertex)) {
                    List<String> attributes = Lists.newArrayList(ATTRIBUTE_NAME, ATTRIBUTE_QUALIFIED_NAME);
                    AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                    AtlasEntity dbEntity = dbEntityWithExtInfo.getEntity();
                    String databaseInGraph = dbEntity.getAttribute(ATTRIBUTE_NAME).toString();
                    if (!databaseNames.contains(databaseInGraph)) {
                        deleteTableEntity(databaseInGraph, new ArrayList<>());
                        deleteEntity(dbEntity);
                    }
                }
            }
            for (String databaseName : databaseNames) {
                AtlasEntityWithExtInfo dbEntity = registerDatabase(databaseName);

                if (dbEntity != null) {
                    importTables(dbEntity.getEntity(), databaseName, database2Table.get(databaseName), false);
                }
            }
        } else {
            LOG.info("No database found");
        }
        getEndTime().set(System.currentTimeMillis());
        LOG.info("import metadata end at {}", simpleDateFormat.format(new Date()));
    }

    private void initHiveMetaStoreClient() {
        if (null == hiveMetaStoreClient) {
            HiveConf hiveConf = new HiveConf();
            hiveConf.addResource(new Path(MetaspaceConfig.getHiveConfig() + File.separator + "hive-site.xml"));
            try {
                hiveMetaStoreClient = Hive.get(hiveConf);
            } catch (HiveException e) {
                LOG.error("init hive metastore client error,", e);
            }
        }
    }

    /**
     * Imports all tables for the given db
     * @param dbEntity
     * @param databaseName
     * @param failOnError
     * @throws Exception
     */
    private int importTables(AtlasEntity dbEntity, String databaseName,List<String> tableNames, final boolean failOnError) throws Exception {
        int tablesImported = 0;

        if(!CollectionUtils.isEmpty(tableNames)) {
            LOG.info("Found {} tables to import in database {}", tableNames.size(), databaseName);

            try {
                //删除JanusGraph中已经不存在table
                deleteTableEntity(databaseName, tableNames);

                for (String tableName : tableNames) {
                    int imported = importTable(dbEntity, databaseName, tableName, failOnError);

                    tablesImported += imported;
                    updatedTables.incrementAndGet();
                }
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

    private void deleteTableEntity(String databaseName, List<String> tableNames) throws AtlasBaseException {
        String tableQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_TABLE_BY_STATE), databaseName, AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(tableQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String> attributes = Lists.newArrayList(ATTRIBUTE_NAME, ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity tableEntity = dbEntityWithExtInfo.getEntity();
                String tableNameInGraph = tableEntity.getAttribute(ATTRIBUTE_NAME).toString();
                if (!tableNames.contains(tableNameInGraph)) {
                    deleteEntity(tableEntity);
                }
            }
        }
    }

    private void deleteEntity(AtlasEntity tableEntity) throws AtlasBaseException {
        AtlasEntityType type = (AtlasEntityType) atlasTypeRegistry.getType(tableEntity.getTypeName());
        final AtlasObjectId objectId = getObjectId(tableEntity);
        atlasEntityStore.deleteByUniqueAttributes(type, objectId.getUniqueAttributes());
    }

    public int importTable(AtlasEntity dbEntity, String databaseName, String tableName, final boolean failOnError) throws Exception {
        try {
            Table table = hiveMetaStoreClient.getTable(databaseName, tableName);
            String tableQualifiedName = getTableProcessQualifiedName(clusterName, table);
            if (isMatch(tableQualifiedName)) {
                LOG.info("ignoring table {}", tableQualifiedName);
                return 1;
            }
            AtlasEntityWithExtInfo tableEntity = registerTable(dbEntity, table);

            if (table.getTableType() == TableType.EXTERNAL_TABLE) {
                AtlasEntityWithExtInfo processEntity        = findProcessEntity(tableQualifiedName);

                if (processEntity == null) {
                    String      tableLocation = isConvertHdfsPathToLowerCase() ? lower(table.getDataLocation().toString()) : table.getDataLocation().toString();
                    String      query         = getCreateTableString(table, tableLocation);
                    AtlasEntity pathInst      = toHdfsPathEntity(tableLocation);
                    AtlasEntity tableInst     = tableEntity.getEntity();
                    AtlasEntity processInst   = new AtlasEntity(HiveDataTypes.HIVE_PROCESS.getName());
                    long        now           = System.currentTimeMillis();

                    processInst.setAttribute(ATTRIBUTE_QUALIFIED_NAME, tableQualifiedName);
                    processInst.setAttribute(ATTRIBUTE_NAME, query);
                    processInst.setAttribute(ATTRIBUTE_CLUSTER_NAME, clusterName);
                    processInst.setAttribute(ATTRIBUTE_INPUTS, Collections.singletonList(BaseHiveEvent.getObjectId(pathInst)));
                    processInst.setAttribute(ATTRIBUTE_OUTPUTS, Collections.singletonList(BaseHiveEvent.getObjectId(tableInst)));
                    processInst.setAttribute(ATTRIBUTE_USER_NAME, table.getOwner());
                    processInst.setAttribute(ATTRIBUTE_START_TIME, now);
                    processInst.setAttribute(ATTRIBUTE_END_TIME, now);
                    processInst.setAttribute(ATTRIBUTE_OPERATION_TYPE, "CREATETABLE");
                    processInst.setAttribute(ATTRIBUTE_QUERY_TEXT, query);
                    processInst.setAttribute(ATTRIBUTE_QUERY_ID, query);
                    processInst.setAttribute(ATTRIBUTE_QUERY_PLAN, "{}");
                    processInst.setAttribute(ATTRIBUTE_RECENT_QUERIES, Collections.singletonList(query));

                    AtlasEntitiesWithExtInfo createTableProcess = new AtlasEntitiesWithExtInfo();

                    createTableProcess.addEntity(processInst);
                    createTableProcess.addEntity(pathInst);

                    registerInstances(createTableProcess);
                } else {
                    LOG.info("Process {} is already registered", tableQualifiedName);
                }
            }

            return 1;
        } catch (Exception e) {
            LOG.error("Import failed for hive_table {}", tableName, e);

            if (failOnError) {
                throw e;
            }

            return 0;
        }
    }

    private boolean isMatch(String tableQualifiedName) {
        return hiveTablesToIgnore.stream().anyMatch(pattern -> pattern.matcher(tableQualifiedName).matches());
    }

    /**
     * Checks if db is already registered, else creates and registers db entity
     * @param databaseName
     * @return
     * @throws Exception
     */
    private AtlasEntityWithExtInfo registerDatabase(String databaseName) throws Exception {
        AtlasEntityWithExtInfo ret = null;
        Database               db  = hiveMetaStoreClient.getDatabase(databaseName);

        if (db != null) {
            ret = findDatabase(clusterName, databaseName);

            if (ret == null) {
                ret = registerInstance(new AtlasEntityWithExtInfo(toDbEntity(db)));
            } else {
                LOG.info("Database {} is already registered - id={}. Updating it.", databaseName, ret.getEntity().getGuid());

                ret.setEntity(toDbEntity(db, ret.getEntity()));

                updateInstance(ret);
            }
        }

        return ret;
    }

    private AtlasEntityWithExtInfo registerTable(AtlasEntity dbEntity, Table table) throws AtlasHookException {
        try {
            AtlasEntityWithExtInfo ret;
            AtlasEntityWithExtInfo tableEntity = findTableEntity(table);

            if (tableEntity == null) {
                tableEntity = toTableEntity(dbEntity, table);

                ret = registerInstance(tableEntity);
            } else {
                LOG.info("Table {}.{} is already registered with id {}. Updating entity.", table.getDbName(), table.getTableName(), tableEntity.getEntity().getGuid());

                ret = toTableEntity(dbEntity, table, tableEntity);

                updateInstance(ret);
            }

            return ret;
        } catch (Exception e) {
            throw new AtlasHookException("HiveMetaStoreBridge.registerTable() failed.", e);
        }
    }

    /**
     * Registers an entity in atlas
     * @param entity
     * @return
     * @throws Exception
     */
    private AtlasEntityWithExtInfo registerInstance(AtlasEntityWithExtInfo entity) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating {} entity: {}", entity.getEntity().getTypeName(), entity);
        }

        AtlasEntityWithExtInfo  ret             = null;
        EntityMutationResponse  response        = atlasEntityStore.createOrUpdate(new AtlasEntityStream(entity), false);
        List<AtlasEntityHeader> createdEntities = response.getEntitiesByOperation(EntityMutations.EntityOperation.CREATE);

        if (CollectionUtils.isNotEmpty(createdEntities)) {
            for (AtlasEntityHeader createdEntity : createdEntities) {
                if (ret == null) {
                    ret = atlasEntityStore.getById(createdEntity.getGuid());

                    LOG.info("Created {} entity: name={}, guid={}", ret.getEntity().getTypeName(), ret.getEntity().getAttribute(ATTRIBUTE_QUALIFIED_NAME), ret.getEntity().getGuid());
                } else if (ret.getEntity(createdEntity.getGuid()) == null) {
                    AtlasEntityWithExtInfo newEntity = atlasEntityStore.getById(createdEntity.getGuid());

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

    /**
     * Registers an entity in atlas
     * @param entities
     * @return
     * @throws Exception
     */
    private AtlasEntitiesWithExtInfo registerInstances(AtlasEntitiesWithExtInfo entities) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating {} entities: {}", entities.getEntities().size(), entities);
        }

        AtlasEntitiesWithExtInfo ret = null;
        EntityMutationResponse response = atlasEntityStore.createOrUpdate(new AtlasEntityStream(entities), false);
        List<AtlasEntityHeader>  createdEntities = response.getEntitiesByOperation(EntityMutations.EntityOperation.CREATE);

        if (CollectionUtils.isNotEmpty(createdEntities)) {
            ret = new AtlasEntitiesWithExtInfo();

            for (AtlasEntityHeader createdEntity : createdEntities) {
                AtlasEntityWithExtInfo entity = atlasEntityStore.getById(createdEntity.getGuid());

                ret.addEntity(entity.getEntity());

                if (MapUtils.isNotEmpty(entity.getReferredEntities())) {
                    for (Map.Entry<String, AtlasEntity> entry : entity.getReferredEntities().entrySet()) {
                        ret.addReferredEntity(entry.getKey(), entry.getValue());
                    }
                }

                LOG.info("Created {} entity: name={}, guid={}", entity.getEntity().getTypeName(), entity.getEntity().getAttribute(ATTRIBUTE_QUALIFIED_NAME), entity.getEntity().getGuid());
            }
        }

        clearRelationshipAttributes(ret);

        return ret;
    }

    private void updateInstance(AtlasEntityWithExtInfo entity) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating {} entity: {}", entity.getEntity().getTypeName(), entity);
        }
        atlasEntityStore.createOrUpdate(new AtlasEntityStream(entity), false);
    }

    /**
     * Create a Hive Database entity
     * @param hiveDB The Hive {@link Database} object from which to map properties
     * @return new Hive Database AtlasEntity
     * @throws HiveException
     */
    private AtlasEntity toDbEntity(Database hiveDB) throws HiveException {
        return toDbEntity(hiveDB, null);
    }

    private AtlasEntity toDbEntity(Database hiveDB, AtlasEntity dbEntity) {
        if (dbEntity == null) {
            dbEntity = new AtlasEntity(HiveDataTypes.HIVE_DB.getName());
        }

        String dbName = hiveDB.getName().toLowerCase();

        dbEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getDBQualifiedName(clusterName, dbName));
        dbEntity.setAttribute(ATTRIBUTE_NAME, dbName);
        dbEntity.setAttribute(ATTRIBUTE_DESCRIPTION, hiveDB.getDescription());
        dbEntity.setAttribute(ATTRIBUTE_OWNER, hiveDB.getOwnerName());

        dbEntity.setAttribute(ATTRIBUTE_CLUSTER_NAME, clusterName);
        dbEntity.setAttribute(ATTRIBUTE_LOCATION, HdfsNameServiceResolver.getPathWithNameServiceID(hiveDB.getLocationUri()));
        dbEntity.setAttribute(ATTRIBUTE_PARAMETERS, hiveDB.getParameters());

        if (hiveDB.getOwnerType() != null) {
            dbEntity.setAttribute(ATTRIBUTE_OWNER_TYPE, OWNER_TYPE_TO_ENUM_VALUE.get(hiveDB.getOwnerType().getValue()));
        }

        return dbEntity;
    }
    /**
     * Create a new table instance in Atlas
     * @param  database AtlasEntity for Hive  {@link AtlasEntity} to which this table belongs
     * @param hiveTable reference to the Hive {@link Table} from which to map properties
     * @return Newly created Hive AtlasEntity
     * @throws Exception
     */
    private AtlasEntityWithExtInfo toTableEntity(AtlasEntity database, Table hiveTable) throws AtlasHookException {
        return toTableEntity(database, hiveTable, null);
    }

    private AtlasEntityWithExtInfo toTableEntity(AtlasEntity database, final Table hiveTable, AtlasEntityWithExtInfo table) throws AtlasHookException {
        if (table == null) {
            table = new AtlasEntityWithExtInfo(new AtlasEntity(HiveDataTypes.HIVE_TABLE.getName()));
        }

        AtlasEntity tableEntity        = table.getEntity();
        String      tableQualifiedName = getTableQualifiedName(clusterName, hiveTable);
        long        createTime         = BaseHiveEvent.getTableCreateTime(hiveTable);
        long        lastAccessTime     = hiveTable.getLastAccessTime() > 0 ? hiveTable.getLastAccessTime() : createTime;

        tableEntity.setAttribute(ATTRIBUTE_DB, BaseHiveEvent.getObjectId(database));
        tableEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, tableQualifiedName);
        tableEntity.setAttribute(ATTRIBUTE_NAME, hiveTable.getTableName().toLowerCase());
        tableEntity.setAttribute(ATTRIBUTE_OWNER, hiveTable.getOwner());

        tableEntity.setAttribute(ATTRIBUTE_CREATE_TIME, createTime);
        tableEntity.setAttribute(ATTRIBUTE_LAST_ACCESS_TIME, lastAccessTime);
        tableEntity.setAttribute(ATTRIBUTE_RETENTION, hiveTable.getRetention());
        tableEntity.setAttribute(ATTRIBUTE_PARAMETERS, hiveTable.getParameters());
        tableEntity.setAttribute(ATTRIBUTE_COMMENT, hiveTable.getParameters().get(ATTRIBUTE_COMMENT));
        tableEntity.setAttribute(ATTRIBUTE_TABLE_TYPE, hiveTable.getTableType().name());
        tableEntity.setAttribute(ATTRIBUTE_TEMPORARY, hiveTable.isTemporary());

        if (hiveTable.getViewOriginalText() != null) {
            tableEntity.setAttribute(ATTRIBUTE_VIEW_ORIGINAL_TEXT, hiveTable.getViewOriginalText());
        }

        if (hiveTable.getViewExpandedText() != null) {
            tableEntity.setAttribute(ATTRIBUTE_VIEW_EXPANDED_TEXT, hiveTable.getViewExpandedText());
        }

        AtlasEntity       sdEntity = toStroageDescEntity(hiveTable.getSd(), tableQualifiedName, getStorageDescQFName(tableQualifiedName), tableEntity);
        List<AtlasEntity> partKeys = toColumns(hiveTable.getPartitionKeys(), tableEntity, ATTRIBUTE_PARTITION_KEYS);
        List<AtlasEntity> columns  = toColumns(hiveTable.getCols(), tableEntity, ATTRIBUTE_COLUMNS);

        tableEntity.setAttribute(ATTRIBUTE_STORAGEDESC, BaseHiveEvent.getObjectId(sdEntity));
        tableEntity.setAttribute(ATTRIBUTE_PARTITION_KEYS, BaseHiveEvent.getObjectIds(partKeys));
        tableEntity.setAttribute(ATTRIBUTE_COLUMNS, BaseHiveEvent.getObjectIds(columns));

        table.addReferredEntity(database);
        table.addReferredEntity(sdEntity);

        if (partKeys != null) {
            for (AtlasEntity partKey : partKeys) {
                table.addReferredEntity(partKey);
            }
        }

        if (columns != null) {
            for (AtlasEntity column : columns) {
                table.addReferredEntity(column);
            }
        }

        table.setEntity(tableEntity);

        return table;
    }

    private AtlasEntity toStroageDescEntity(StorageDescriptor storageDesc, String tableQualifiedName, String sdQualifiedName, AtlasEntity tableEntity ) throws AtlasHookException {
        AtlasEntity ret = new AtlasEntity(HiveDataTypes.HIVE_STORAGEDESC.getName());
        //保持Guid一致，如果不一致，更新元数据时会当成存在变动
        AtlasObjectId sd = (AtlasObjectId) tableEntity.getAttribute("sd");
        if (null != sd) {
            String guid = sd.getGuid();
            if (guid != null) {
                ret.setGuid(guid);
            }
        }
        ret.setAttribute(ATTRIBUTE_TABLE, BaseHiveEvent.getObjectId(tableEntity));
        ret.setAttribute(ATTRIBUTE_QUALIFIED_NAME, sdQualifiedName);
        ret.setAttribute(ATTRIBUTE_PARAMETERS, storageDesc.getParameters());
        ret.setAttribute(ATTRIBUTE_LOCATION, HdfsNameServiceResolver.getPathWithNameServiceID(storageDesc.getLocation()));
        ret.setAttribute(ATTRIBUTE_INPUT_FORMAT, storageDesc.getInputFormat());
        ret.setAttribute(ATTRIBUTE_OUTPUT_FORMAT, storageDesc.getOutputFormat());
        ret.setAttribute(ATTRIBUTE_COMPRESSED, storageDesc.isCompressed());
        ret.setAttribute(ATTRIBUTE_NUM_BUCKETS, storageDesc.getNumBuckets());
        ret.setAttribute(ATTRIBUTE_STORED_AS_SUB_DIRECTORIES, storageDesc.isStoredAsSubDirectories());

        if (storageDesc.getBucketCols().size() > 0) {
            ret.setAttribute(ATTRIBUTE_BUCKET_COLS, storageDesc.getBucketCols());
        }

        if (storageDesc.getSerdeInfo() != null) {
            SerDeInfo serdeInfo = storageDesc.getSerdeInfo();

            LOG.debug("serdeInfo = {}", serdeInfo);

            AtlasStruct serdeInfoStruct = new AtlasStruct(HiveDataTypes.HIVE_SERDE.getName());

            serdeInfoStruct.setAttribute(ATTRIBUTE_NAME, serdeInfo.getName());
            serdeInfoStruct.setAttribute(ATTRIBUTE_SERIALIZATION_LIB, serdeInfo.getSerializationLib());
            serdeInfoStruct.setAttribute(ATTRIBUTE_PARAMETERS, serdeInfo.getParameters());

            ret.setAttribute(ATTRIBUTE_SERDE_INFO, serdeInfoStruct);
        }

        if (CollectionUtils.isNotEmpty(storageDesc.getSortCols())) {
            List<AtlasStruct> sortColsStruct = new ArrayList<>();

            for (Order sortcol : storageDesc.getSortCols()) {
                String hiveOrderName = HiveDataTypes.HIVE_ORDER.getName();
                AtlasStruct colStruct = new AtlasStruct(hiveOrderName);
                colStruct.setAttribute("col", sortcol.getCol());
                colStruct.setAttribute("order", sortcol.getOrder());

                sortColsStruct.add(colStruct);
            }

            ret.setAttribute(ATTRIBUTE_SORT_COLS, sortColsStruct);
        }

        return ret;
    }

    private List<AtlasEntity> toColumns(List<FieldSchema> schemaList, AtlasEntity table, String attributeName) throws AtlasHookException {
        List<AtlasEntity> ret = new ArrayList<>();

        int columnPosition = 0;
        for (FieldSchema fs : schemaList) {
            LOG.debug("Processing field {}", fs);

            AtlasEntity column = new AtlasEntity(HiveDataTypes.HIVE_COLUMN.getName());
            ArrayList columns = (ArrayList) table.getAttributes().get(attributeName);
            if (CollectionUtils.isNotEmpty(columns)) {
                AtlasObjectId columnMap = (AtlasObjectId) columns.get(columnPosition);
                if (null != columnMap) {
                    //保持Guid一致
                    String guid = columnMap.getGuid();
                    if (guid != null) {
                        column.setGuid(guid);
                    }
                }
            }
            column.setAttribute(ATTRIBUTE_TABLE, BaseHiveEvent.getObjectId(table));
            column.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), fs.getName()));
            column.setAttribute(ATTRIBUTE_NAME, fs.getName());
            column.setAttribute(ATTRIBUTE_OWNER, table.getAttribute(ATTRIBUTE_OWNER));
            column.setAttribute(ATTRIBUTE_COL_TYPE, fs.getType());
            column.setAttribute(ATTRIBUTE_COL_POSITION, columnPosition++);
            column.setAttribute(ATTRIBUTE_COMMENT, fs.getComment());

            ret.add(column);
        }
        return ret;
    }

    private AtlasEntity toHdfsPathEntity(String pathUri) {
        AtlasEntity ret           = new AtlasEntity(HDFS_PATH);
        String      nameServiceID = HdfsNameServiceResolver.getNameServiceIDForPath(pathUri);
        Path        path          = new Path(pathUri);

        ret.setAttribute(ATTRIBUTE_NAME, Path.getPathWithoutSchemeAndAuthority(path).toString().toLowerCase());
        ret.setAttribute(ATTRIBUTE_CLUSTER_NAME, clusterName);

        if (StringUtils.isNotEmpty(nameServiceID)) {
            // Name service resolution is successful, now get updated HDFS path where the host port info is replaced by resolved name service
            String updatedHdfsPath = HdfsNameServiceResolver.getPathWithNameServiceID(pathUri);

            ret.setAttribute(ATTRIBUTE_PATH, updatedHdfsPath);
            ret.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getHdfsPathQualifiedName(updatedHdfsPath));
            ret.setAttribute(ATTRIBUTE_NAMESERVICE_ID, nameServiceID);
        } else {
            ret.setAttribute(ATTRIBUTE_PATH, pathUri);

            // Only append clusterName for the HDFS path
            if (pathUri.startsWith(HdfsNameServiceResolver.HDFS_SCHEME)) {
                ret.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getHdfsPathQualifiedName(pathUri));
            } else {
                ret.setAttribute(ATTRIBUTE_QUALIFIED_NAME, pathUri);
            }
        }

        return ret;
    }

    /**
     * Gets the atlas entity for the database
     * @param databaseName  database Name
     * @param clusterName    cluster name
     * @return AtlasEntity for database if exists, else null
     * @throws Exception
     */
    private AtlasEntityWithExtInfo findDatabase(String clusterName, String databaseName) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching Atlas for database {}", databaseName);
        }

        String typeName = HiveDataTypes.HIVE_DB.getName();

        return findEntity(typeName, getDBQualifiedName(clusterName, databaseName));
    }

    /**
     * Gets Atlas Entity for the table
     *
     * @param hiveTable
     * @return table entity from Atlas  if exists, else null
     * @throws Exception
     */
    private AtlasEntityWithExtInfo findTableEntity(Table hiveTable)  throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching Atlas for table {}.{}", hiveTable.getDbName(), hiveTable.getTableName());
        }

        String typeName         = HiveDataTypes.HIVE_TABLE.getName();
        String tblQualifiedName = getTableQualifiedName(getClusterName(), hiveTable.getDbName(), hiveTable.getTableName());

        return findEntity(typeName, tblQualifiedName);
    }

    private AtlasEntityWithExtInfo findProcessEntity(String qualifiedName) throws Exception{
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching Atlas for process {}", qualifiedName);
        }

        String typeName = HiveDataTypes.HIVE_PROCESS.getName();

        return findEntity(typeName, qualifiedName);
    }

    private AtlasEntityWithExtInfo findEntity(final String typeName, final String qualifiedName){
        AtlasEntityWithExtInfo ret = null;
        try {
            ret = atlasEntityStore.getByUniqueAttributes(atlasTypeRegistry.getEntityTypeByName(typeName), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName));
            clearRelationshipAttributes(ret);
        } catch (AtlasBaseException e) {
            LOG.warn("{}是新的元数据，保存元数据", qualifiedName);
        }
        return ret;
    }

    private String getCreateTableString(Table table, String location){
        String            colString = "";
        List<FieldSchema> colList   = table.getAllCols();

        if (colList != null) {
            for (FieldSchema col : colList) {
                colString += col.getName() + " " + col.getType() + ",";
            }

            if (colList.size() > 0) {
                colString = colString.substring(0, colString.length() - 1);
                colString = "(" + colString + ")";
            }
        }

        String query = "create external table " + table.getTableName() +  colString + " location '" + location + "'";

        return query;
    }

    private String lower(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }

        return str.toLowerCase().trim();
    }


    /**
     * Construct the qualified name used to uniquely identify a Table instance in Atlas.
     * @param clusterName Name of the cluster to which the Hive component belongs
     * @param table hive table for which the qualified name is needed
     * @return Unique qualified name to identify the Table instance in Atlas.
     */
    private static String getTableQualifiedName(String clusterName, Table table) {
        return getTableQualifiedName(clusterName, table.getDbName(), table.getTableName(), table.isTemporary());
    }

    private String getHdfsPathQualifiedName(String hdfsPath) {
        return String.format("%s@%s", hdfsPath, clusterName);
    }

    /**
     * Construct the qualified name used to uniquely identify a Database instance in Atlas.
     * @param clusterName Name of the cluster to which the Hive component belongs
     * @param dbName Name of the Hive database
     * @return Unique qualified name to identify the Database instance in Atlas.
     */
    public static String getDBQualifiedName(String clusterName, String dbName) {
        return String.format("%s@%s", dbName.toLowerCase(), clusterName);
    }

    /**
     * Construct the qualified name used to uniquely identify a Table instance in Atlas.
     * @param clusterName Name of the cluster to which the Hive component belongs
     * @param dbName Name of the Hive database to which the Table belongs
     * @param tableName Name of the Hive table
     * @param isTemporaryTable is this a temporary table
     * @return Unique qualified name to identify the Table instance in Atlas.
     */
    public static String getTableQualifiedName(String clusterName, String dbName, String tableName, boolean isTemporaryTable) {
        String tableTempName = tableName;

        if (isTemporaryTable) {
            if (SessionState.get() != null && SessionState.get().getSessionId() != null) {
                tableTempName = tableName + TEMP_TABLE_PREFIX + SessionState.get().getSessionId();
            } else {
                tableTempName = tableName + TEMP_TABLE_PREFIX + RandomStringUtils.random(10);
            }
        }

        return String.format("%s.%s@%s", dbName.toLowerCase(), tableTempName.toLowerCase(), clusterName);
    }

    public static String getTableProcessQualifiedName(String clusterName, Table table) {
        String tableQualifiedName = getTableQualifiedName(clusterName, table);
        long   createdTime        = getTableCreatedTime(table);

        return tableQualifiedName + SEP + createdTime;
    }


    /**
     * Construct the qualified name used to uniquely identify a Table instance in Atlas.
     * @param clusterName Name of the cluster to which the Hive component belongs
     * @param dbName Name of the Hive database to which the Table belongs
     * @param tableName Name of the Hive table
     * @return Unique qualified name to identify the Table instance in Atlas.
     */
    public static String getTableQualifiedName(String clusterName, String dbName, String tableName) {
        return getTableQualifiedName(clusterName, dbName, tableName, false);
    }
    public static String getStorageDescQFName(String tableQualifiedName) {
        return tableQualifiedName + "_storage";
    }

    public static String getColumnQualifiedName(final String tableQualifiedName, final String colName) {
        final String[] parts       = tableQualifiedName.split("@");
        final String   tableName   = parts[0];
        final String   clusterName = parts[1];

        return String.format("%s.%s@%s", tableName, colName.toLowerCase(), clusterName);
    }

    public static long getTableCreatedTime(Table table) {
        return table.getTTable().getCreateTime() * MILLIS_CONVERT_FACTOR;
    }

    private void clearRelationshipAttributes(AtlasEntitiesWithExtInfo entities) {
        if (entities != null) {
            if (entities.getEntities() != null) {
                for (AtlasEntity entity : entities.getEntities()) {
                    clearRelationshipAttributes(entity);;
                }
            }

            if (entities.getReferredEntities() != null) {
                clearRelationshipAttributes(entities.getReferredEntities().values());
            }
        }
    }

    private void clearRelationshipAttributes(AtlasEntityWithExtInfo entity) {
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

    private boolean isTableWithDatabaseName(String tableName) {
        boolean ret = false;
        String sub = ".";
        if (tableName.contains(sub)) {
            ret = true;
        }
        return ret;
    }
}
