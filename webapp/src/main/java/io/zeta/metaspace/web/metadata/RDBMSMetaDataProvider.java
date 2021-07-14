package io.zeta.metaspace.web.metadata;

import com.google.common.collect.Lists;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerColumn;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerForeignKey;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerIndex;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerTable;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.sync.SyncTaskInstance;
import io.zeta.metaspace.utils.AbstractMetaspaceGremlinQueryProvider;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.CollectThreadPoolUtil;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.web.dao.SyncTaskInstanceDAO;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.util.LocalCacheUtils;
import io.zeta.metaspace.web.util.ZkLockUtils;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.metadata.BaseFields.*;

/**
 * @author zhuxuetong
 * @date 2019-08-21 17:31  HiveMetaStoreBridgeUtils
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RDBMSMetaDataProvider implements IMetaDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RDBMSMetaDataProvider.class);
    private static final String clusterName = AtlasConfiguration.ATLAS_CLUSTER_NAME.getString();
    private static final int LOCK_TIME_OUT_TIME = AtlasConfiguration.LOCK_TIME_OUT_TIME.getInt(); //M

    @Autowired
    private AtlasEntityStore entitiesStore;
    @Autowired
    private AtlasGraph graph;
    @Autowired
    private AtlasTypeRegistry atlasTypeRegistry;
    @Autowired
    private AtlasEntityStore atlasEntityStore;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private SyncTaskInstanceDAO syncTaskInstanceDAO;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private ZkLockUtils zkLockUtils;

    /**
     * 因为用 new 创建实例，所以不能自动注入相关参数
     */
    public RDBMSMetaDataProvider() {
    }

    private EntityGraphRetriever entityRetriever;

    private AbstractMetaspaceGremlinQueryProvider gremlinQueryProvider = AbstractMetaspaceGremlinQueryProvider.INSTANCE;
    protected MetaDataContext metaDataContext = new MetaDataContext();

    private boolean isSource;

    private DataSourceInfo dataSourceInfo;
    private AdapterExecutor adapterExecutor;
    private MetaDataInfo metaDataInfo;

    @PostConstruct
    public void initObject() {
        this.entityRetriever = new EntityGraphRetriever(atlasTypeRegistry);
    }

    /**
     * 每次开始导入之前，需要执行的初始化，获取元数据
     */
    protected void init(TableSchema tableSchema) {
        dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(tableSchema.getInstance());
        tableSchema.getDefinition().setDataSourceType(dataSourceInfo.getSourceType());
        adapterExecutor = AdapterUtils.getAdapterExecutor(dataSourceInfo);
        metaDataInfo = adapterExecutor.getMeteDataInfo(tableSchema);
    }


    public void importDatabases(String taskInstanceId, TableSchema tableSchema) throws Exception {
        LOG.info("import metadata start at {}", new Date());
        syncTaskInstanceDAO.updateStatusAndAppendLog(taskInstanceId, SyncTaskInstance.Status.RUN, "开始初始化数据库并拉取元数据");

        init(tableSchema);
        metaDataContext = new MetaDataContext();

        String instanceId = tableSchema.getInstance();
        this.checkTaskEnable(taskInstanceId);
        syncTaskInstanceDAO.appendLog(taskInstanceId, "初始化成功，导入数据源和数据库");
        //根据数据源id获取图数据库中的数据源，如果有，则更新，如果没有，则创建
        AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo = registerInstance(instanceId, taskInstanceId, tableSchema.isAll(), tableSchema.getDefinition());
        this.checkTaskEnable(taskInstanceId);
        syncTaskInstanceDAO.appendLog(taskInstanceId, "导入数据源和数据库成功，开始导入表");
        String instanceGuid = atlasEntityWithExtInfo.getEntity().getGuid();
        Collection<Schema> schemas = metaDataInfo.getSchemas();
        if (!CollectionUtils.isEmpty(schemas) && !tableSchema.isAllDatabase()) {
            LOG.info("Found {} databases", schemas.size());

            ThreadPoolExecutor threadPoolExecutor = CollectThreadPoolUtil.getCollectThreadPoolExecutor();
            List<CompletableFuture<Void>> completableFutureList = new ArrayList<>(schemas.size());
            //导入table
            for (Schema database : schemas) {
                completableFutureList.add(CompletableFuture.runAsync(() -> {
                    AtlasEntity.AtlasEntityWithExtInfo dbEntity;
                    String dbQualifiedName = getDBQualifiedName(instanceId, database.getFullName());
                    if (metaDataContext.isKownEntity(dbQualifiedName)) {
                        dbEntity = metaDataContext.getEntity(dbQualifiedName);
                    } else {
                        dbEntity = findDatabase(instanceId, database.getFullName());
                        clearRelationshipAttributes(dbEntity);
                        metaDataContext.putEntity(dbQualifiedName, dbEntity);
                    }
                    importTables(dbEntity.getEntity(), instanceId, database.getFullName(), getTables(database), false, instanceGuid, taskInstanceId, null);
                }, threadPoolExecutor));
            }
            CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[]{})).join();
            dataManageService.updateRelations(tableSchema.getDefinition());
        } else {
            LOG.info("No database found");
        }
        this.checkTaskEnable(taskInstanceId);
        syncTaskInstanceDAO.updateStatusAndAppendLog(taskInstanceId, SyncTaskInstance.Status.SUCCESS, "导入结束");
        LOG.info("import metadata end at {}", new Date());
    }


    protected AtlasEntity.AtlasEntityWithExtInfo registerInstance(String instanceId, String taskInstanceId, boolean allDatabase, SyncTaskDefinition definition) throws Exception {
        AtlasEntity.AtlasEntityWithExtInfo ret;
        String instanceQualifiedName = getInstanceQualifiedName(instanceId);

        InterProcessMutex lock = zkLockUtils.getInterProcessMutex(instanceQualifiedName);

        try {
            LOG.debug("尝试拿锁 : " + Thread.currentThread().getName() + " " + instanceQualifiedName);
            if (lock.acquire(LOCK_TIME_OUT_TIME, TimeUnit.MINUTES)) {
                LOG.debug("拿锁成功 : " + Thread.currentThread().getName() + " " + instanceQualifiedName);
                if (metaDataContext.isKownEntity(instanceQualifiedName)) {
                    ret = metaDataContext.getEntity(instanceQualifiedName);
                } else {
                    ret = findInstance(instanceId);
                    clearRelationshipAttributes(ret);
                    metaDataContext.putEntity(instanceQualifiedName, ret);
                }
                this.checkTaskEnable(taskInstanceId);
                AtlasEntity.AtlasEntityWithExtInfo instanceEntity;
                if (ret == null) {
                    instanceEntity = toInstanceEntity(null, instanceId, allDatabase);
                    ret = registerEntity(instanceEntity, definition);
                    isSource = false;
                } else {
                    LOG.info("Instance {} is already registered - id={}. Updating it.", instanceId, ret.getEntity().getGuid());
                    ret = toInstanceEntity(ret, instanceId, allDatabase);
                    entitiesStore.createOrUpdate(new AtlasEntityStream(ret, definition), false);
                    isSource = true;
                }
                return ret;
            }
            throw new AtlasBaseException("获取锁超时：" + instanceQualifiedName);
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                LOG.error("lock release error", e);
            }
        }
    }

    protected AtlasEntity.AtlasEntityWithExtInfo toInstanceEntity(AtlasEntity.AtlasEntityWithExtInfo instanceEntity, String instanceId, boolean allDatabase) {
        if (instanceEntity == null) {
            instanceEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getInstanceTypeName()));
        }
        AtlasEntity entity = instanceEntity.getEntity();
        entity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getInstanceQualifiedName(instanceId));
        entity.setAttribute(ATTRIBUTE_NAME, dataSourceInfo.getSourceName());
        entity.setAttribute(ATTRIBUTE_RDBMS_TYPE, dataSourceInfo.getSourceType());
        entity.setAttribute(ATTRIBUTE_PLATFORM, dataSourceInfo.getSourceType());
        entity.setAttribute(ATTRIBUTE_HOSTNAME, dataSourceInfo.getIp());
        entity.setAttribute(ATTRIBUTE_PORT, dataSourceInfo.getPort());
        entity.setAttribute(ATTRIBUTE_COMMENT, dataSourceInfo.getDescription());
        entity.setAttribute(ATTRIBUTE_CONTACT_INFO, metaDataInfo.getJdbcUrl());
        List<AtlasEntity> dbEntities = new ArrayList<>();
        for (Schema schema : metaDataInfo.getSchemas()) {
            AtlasEntity.AtlasEntityWithExtInfo dbEntity = null;
            String dbQualifiedName = getDBQualifiedName(instanceId, schema.getFullName());
            if (metaDataContext.isKownEntity(dbQualifiedName)) {
                dbEntity = metaDataContext.getEntity(dbQualifiedName);
            } else {
                dbEntity = toDBEntity(instanceEntity, null, instanceId, schema.getFullName());
                metaDataContext.putEntity(dbQualifiedName, dbEntity);
            }
            dbEntities.add(dbEntity.getEntity());
            instanceEntity.addReferredEntity(dbEntity.getEntity());
        }
        List<AtlasObjectId> objectIds = getObjectIds(dbEntities);
        List<AtlasObjectId> attributes = (List<AtlasObjectId>) entity.getAttribute(ATTRIBUTE_DATABASES);
        if (attributes != null) {
            for (AtlasObjectId atlasObjectId : attributes) {
                AtlasEntity atlasEntity = instanceEntity.getReferredEntity(atlasObjectId.getGuid());
                atlasEntity = toDBEntity(instanceEntity, atlasEntity, instanceId);
                instanceEntity.addReferredEntity(atlasEntity);
            }
            if (allDatabase) {
                for (AtlasObjectId atlasObjectId : attributes) {
                    AtlasEntity referredEntity = instanceEntity.getReferredEntity(atlasObjectId.getGuid());
                    if (referredEntity.getStatus() == null) {
                        deleteEntityById(referredEntity);
                    }

                    if (!objectIds.contains(atlasObjectId) && (referredEntity == null || !"DELETED".equals(referredEntity.getStatus().toString()))) {
                        objectIds.add(atlasObjectId);
                    }
                }
            } else {
                objectIds.addAll(attributes);
            }
        }

        entity.setAttribute(ATTRIBUTE_DATABASES, objectIds);

        return instanceEntity;
    }


    protected int importTable(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName, final boolean failOnError,
                              String instanceGuid, String taskInstanceId, SyncTaskDefinition definition) {
        try {
            registerTable(dbEntity, instanceId, databaseName, tableName, instanceGuid, taskInstanceId, definition);
            return 1;
        } catch (Exception e) {
            LOG.error("Import failed for {} {}", getTableTypeName(), tableName, e);
            if (failOnError) {
                throw new AtlasBaseException(AtlasErrorCode.INTERNAL_UNKNOWN_ERROR);
            }
            return 0;
        }
    }


    protected List<AtlasObjectId> getKeyColumns(SchemaCrawlerForeignKey foreignKey, List<AtlasEntity> columns) {
        return columns.stream()
                .filter(atlasEntity -> foreignKey.getForeignKeyColumnNames().contains(atlasEntity.getAttribute(ATTRIBUTE_NAME).toString()))
                .map(RDBMSMetaDataProvider::getObjectId)
                .collect(Collectors.toList());
    }

    protected List<AtlasObjectId> getPrimaryKeyColumns(SchemaCrawlerForeignKey foreignKey, List<AtlasEntity> columns) {
        return columns.stream()
                .filter(atlasEntity -> foreignKey.getPrimaryKeyColumnNames().contains(atlasEntity.getAttribute(ATTRIBUTE_NAME).toString()))
                .map(RDBMSMetaDataProvider::getObjectId)
                .collect(Collectors.toList());
    }

    protected List<AtlasEntity> toColumns(List<SchemaCrawlerColumn> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity) throws AtlasBaseException {
        List<AtlasEntity> ret = new ArrayList<>();
        AtlasEntity table = tableEntity.getEntity();
        for (SchemaCrawlerColumn column : columns) {
            AtlasEntity columnEntity = new AtlasEntity(RDBMS_COLUMN);
            //再次导入时，要保持guid一致，不然可能会导致entity在没有变化的情况下，依旧更新
            if (tableEntity.getReferredEntities() != null) {
                List<String> columnIds = tableEntity.getReferredEntities().values().stream().filter(entity -> entity.getStatus() == AtlasEntity.Status.ACTIVE && entity.getTypeName().equalsIgnoreCase(RDBMS_COLUMN) && entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(column.getName())).map(entity -> entity.getGuid()).collect(Collectors.toList());
                String columnId = null;
                if (columnIds != null && columnIds.size() != 0) {
                    columnId = columnIds.get(0);
                }
                if (columnId != null) {
                    columnEntity.setGuid(columnId);
                }
            }

            columnEntity.setAttribute(ATTRIBUTE_NAME, column.getName());
            columnEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), column.getName()));
            columnEntity.setAttribute(ATTRIBUTE_DATA_TYPE, column.getDataType());
            columnEntity.setAttribute(ATTRIBUTE_LENGTH, column.getLength());
            columnEntity.setAttribute(ATTRIBUTE_DEFAULT_VALUE, column.getDefaultValue());
            columnEntity.setAttribute(ATTRIBUTE_COMMENT, column.getComment());
            columnEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
            columnEntity.setAttribute(ATTRIBUTE_ISNULLABLE, column.isNullable());
            columnEntity.setAttribute(ATTRIBUTE_ISPRIMARYKEY, column.isPrimaryKey());
            ret.add(columnEntity);
        }
        return ret;
    }

    protected List<AtlasEntity> toIndexes(Collection<SchemaCrawlerIndex> indexes, List<AtlasEntity> columns, AtlasEntity.AtlasEntityWithExtInfo tableEntity, String databaseName, String instanceGuid) throws AtlasBaseException {
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> ret = new ArrayList<>();
        for (SchemaCrawlerIndex index : indexes) {
            AtlasEntity indexEntity = new AtlasEntity(RDBMS_INDEX);
            if (tableEntity.getReferredEntities() != null) {
                List<String> indexIds = tableEntity.getReferredEntities().values().stream().filter(entity -> entity.getStatus() == AtlasEntity.Status.ACTIVE && entity.getTypeName().equalsIgnoreCase(RDBMS_INDEX) && entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(index.getName())).map(entity -> entity.getGuid()).collect(Collectors.toList());
                String indexId = null;
                if (indexIds != null && indexIds.size() != 0) {
                    indexId = indexIds.get(0);
                }

                if (indexId != null) {
                    indexEntity.setGuid(indexId);
                }
            }
            //名称统一小写
            indexEntity.setAttribute(ATTRIBUTE_NAME, index.getName().toLowerCase());
            indexEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), index.getName()));
            indexEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
            indexEntity.setAttribute(ATTRIBUTE_INDEX_TYPE, index.getIndexType());
            indexEntity.setAttribute(ATTRIBUTE_ISUNIQUE, index.isUnique());
            List<AtlasEntity> indexColumns = columns.stream()
                    .filter(column -> index.getColumns().stream().anyMatch(indexColumn -> indexColumn.equalsIgnoreCase(String.valueOf(column.getAttribute(ATTRIBUTE_NAME)))))
                    .collect(Collectors.toList());
            indexEntity.setAttribute(ATTRIBUTE_COLUMNS, getObjectIds(indexColumns));
            indexEntity.setAttribute(ATTRIBUTE_COMMENT, index.getComment());
            ret.add(indexEntity);
        }
        return ret;
    }

    protected List<AtlasEntity> toForeignKeys(Collection<SchemaCrawlerForeignKey> foreignKeys, List<AtlasEntity> columns,
                                              AtlasEntity.AtlasEntityWithExtInfo tableEntity, String databaseName, SyncTaskDefinition definition) throws AtlasBaseException {
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> ret = new ArrayList<>();
        for (SchemaCrawlerForeignKey foreignKey : foreignKeys) {
            SchemaCrawlerColumn foreignKeyColumn = foreignKey.getForeignKeyColumns().get(0);
            SchemaCrawlerColumn primaryKeyColumn = foreignKey.getPrimaryKeyColumns().get(0);
            if (foreignKeyColumn.getSchemaName().equalsIgnoreCase(databaseName) && foreignKeyColumn.getTableName().equalsIgnoreCase(table.getAttribute(ATTRIBUTE_NAME).toString())) {
                AtlasEntity foreignEntity = new AtlasEntity(RDBMS_FOREIGN_KEY);
                if (tableEntity.getReferredEntities() != null) {
                    List<String> keyIds = tableEntity.getReferredEntities().values().stream().filter(entity -> entity.getStatus() == AtlasEntity.Status.ACTIVE && entity.getTypeName().equalsIgnoreCase(RDBMS_FOREIGN_KEY) && entity.getAttribute(ATTRIBUTE_NAME).toString().equalsIgnoreCase(foreignKey.getName())).map(entity -> entity.getGuid()).collect(Collectors.toList());
                    String keyId = null;
                    if (keyIds != null && keyIds.size() != 0) {
                        keyId = keyIds.get(0);
                    }
                    if (keyId != null) {
                        foreignEntity.setGuid(keyId);
                    }
                }
                foreignEntity.setAttribute(ATTRIBUTE_NAME, foreignKey.getName());
                foreignEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getColumnQualifiedName((String) table.getAttribute(ATTRIBUTE_QUALIFIED_NAME), foreignKey.getName()));
                //todo ATTRIBUTE_REFERENCES_TABLE 和 ATTRIBUTE_REFERENCES_COLUMNS 需要需要表已经存在janusgraph中
                //            visitForeignEntity(foreignEntity, foreignKey, dbEntity, instanceId);
                String tableQualifiedName = getTableQualifiedName(table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString().split("\\.")[0], primaryKeyColumn.getSchemaName(), primaryKeyColumn.getTableName());
                AtlasEntity.AtlasEntityWithExtInfo tableInfo = findEntity(getTableTypeName(), tableQualifiedName);
                if (tableInfo != null) {
                    tableInfo = getById(tableInfo.getEntity().getGuid(), false);
                    foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_TABLE, getObjectId(tableInfo.getEntity()));
                    List<AtlasEntity> primaryColumns = tableInfo.getReferredEntities().values().stream().filter(entity -> entity.getStatus() == AtlasEntity.Status.ACTIVE && entity.getTypeName().equalsIgnoreCase(RDBMS_COLUMN)).collect(Collectors.toList());
                    foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_COLUMNS, getPrimaryKeyColumns(foreignKey, primaryColumns));
                    tableInfo.addReferredEntity(foreignEntity);
                    ((List<AtlasObjectId>) tableInfo.getEntity().getAttribute(ATTRIBUTE_FOREIGN_KEYS)).add(getObjectId(foreignEntity));
                    entitiesStore.createOrUpdate(new AtlasEntityStream(tableInfo, definition), false);
                }
                foreignEntity.setAttribute(ATTRIBUTE_TABLE, getObjectId(table));
                foreignEntity.setAttribute(ATTRIBUTE_KEY_COLUMNS, getKeyColumns(foreignKey, columns));

                ret.add(foreignEntity);
            } else {
                String tableQualifiedName = getTableQualifiedName(table.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString().split("\\.")[0], foreignKeyColumn.getSchemaName(), foreignKeyColumn.getTableName());
                String foreignQualifiedName = getColumnQualifiedName(tableQualifiedName, foreignKey.getName());
                AtlasEntity.AtlasEntityWithExtInfo info = findEntity(RDBMS_FOREIGN_KEY, foreignQualifiedName);
                if (info != null) {
                    info = getById(info.getEntity().getGuid(), true);
                    AtlasEntity foreignEntity = info.getEntity();
                    foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_TABLE, getObjectId(table));
                    foreignEntity.setAttribute(ATTRIBUTE_REFERENCES_COLUMNS, getPrimaryKeyColumns(foreignKey, columns));
                    ret.add(foreignEntity);
                }
            }
        }
        return ret;
    }

    protected void resultToMap(ResultSet results, Map<String, String> pair) throws SchemaCrawlerException, SQLException {
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
    protected AtlasEntity.AtlasEntityWithExtInfo toDBEntity(AtlasEntity.AtlasEntityWithExtInfo instance, AtlasEntity.AtlasEntityWithExtInfo dbEntity, String instanceId, String databaseName) {
        AtlasEntity instanceEntity = instance.getEntity();
        if (dbEntity == null) {
            dbEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getDatabaseTypeName()));
        }

        String dbId = null;
        if (instance.getReferredEntities() != null) {
            List<String> dbIds = instance.getReferredEntities().values().stream().filter(entity -> entity.getStatus() == AtlasEntity.Status.ACTIVE && entity.getTypeName().equalsIgnoreCase(getDatabaseTypeName()) && entity.getAttribute(ATTRIBUTE_NAME).toString().equals(databaseName)).map(entity -> entity.getGuid()).collect(Collectors.toList());
            if (dbIds != null && dbIds.size() != 0) {
                dbId = dbIds.get(0);
            }
        }

        AtlasEntity entity = dbEntity.getEntity();

        if (dbId != null) {
            entity.setGuid(dbId);
            entity.setStatus(AtlasEntity.Status.ACTIVE);
        }

        entity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getDBQualifiedName(instanceId, databaseName));
        entity.setAttribute(ATTRIBUTE_NAME, databaseName);
        entity.setAttribute(ATTRIBUTE_CLUSTER_NAME, clusterName);
        entity.setAttribute(ATTRIBUTE_PRODOROTHER, "");
        entity.setAttribute(ATTRIBUTE_INSTANCE, getObjectId(instanceEntity));
        return dbEntity;
    }

    protected AtlasEntity toDBEntity(AtlasEntity.AtlasEntityWithExtInfo instance, AtlasEntity entity, String instanceId) {
        String databaseName = (String) entity.getAttribute("name");
        entity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getDBQualifiedName(instanceId, databaseName));
        entity.setAttribute(ATTRIBUTE_NAME, databaseName);
        entity.setAttribute(ATTRIBUTE_CLUSTER_NAME, clusterName);
        entity.setAttribute(ATTRIBUTE_PRODOROTHER, "");
        entity.setAttribute(ATTRIBUTE_INSTANCE, getObjectId(instance.getEntity()));
        return entity;
    }

    /**
     * 将表信息组装成atlas entity
     *
     * @param dbEntity
     * @param databaseName
     * @param table
     * @param tableEntity
     * @return
     */
    protected AtlasEntity.AtlasEntityWithExtInfo toTableEntity(AtlasEntity dbEntity, String instanceId, String databaseName,
                                                               Table table, AtlasEntity.AtlasEntityWithExtInfo tableEntity, String instanceGuid, SyncTaskDefinition definition) throws AtlasBaseException {
        if (null == tableEntity) {
            tableEntity = new AtlasEntity.AtlasEntityWithExtInfo(new AtlasEntity(getTableTypeName()));
        }
        AtlasEntity tableAtlasEntity = tableEntity.getEntity();
        tableAtlasEntity.setAttribute(ATTRIBUTE_QUALIFIED_NAME, getTableQualifiedName(instanceId, databaseName, table.getName()));
        tableAtlasEntity.setAttribute(ATTRIBUTE_NAME, table.getName());
        tableAtlasEntity.setAttribute(ATTRIBUTE_DB, getObjectId(dbEntity));
        tableAtlasEntity.setAttribute(ATTRIBUTE_TYPE, table.getTableType().toString().toUpperCase());
        tableAtlasEntity.setAttribute(ATTRIBUTE_OWNER, table.getSchema());
        try {
            long time = adapterExecutor.getTableCreateTime(databaseName, table.getName()).toInstant(ZoneOffset.of("+8")).toEpochMilli();
            tableAtlasEntity.setAttribute(ATTRIBUTE_CREATE_TIME, time);
        } catch (Exception e) {
            tableAtlasEntity.setAttribute(ATTRIBUTE_CREATE_TIME, null);
        }

        tableAtlasEntity.setAttribute(ATTRIBUTE_COMMENT, table.getRemarks());
        tableAtlasEntity.setAttribute(ATTRIBUTE_NAME_PATH, table.getFullName());
        tableAtlasEntity.setUpdateTime(new Date());

        boolean isIncompleteTable = metaDataInfo.isIncompleteTable(table.getFullName());

        SchemaCrawlerTable schemaCrawlerTable = isIncompleteTable ? adapterExecutor.getTable(table) : new SchemaCrawlerTable(table);

        List<AtlasEntity> columns = toColumns(schemaCrawlerTable.getColumns(), tableEntity);
        List<AtlasEntity> indexes = toIndexes(schemaCrawlerTable.getIndexes(), columns, tableEntity, databaseName, instanceGuid);
        List<AtlasEntity> foreignKeys = toForeignKeys(schemaCrawlerTable.getForeignKeys(), columns, tableEntity, databaseName, definition);

        tableAtlasEntity.setAttribute(ATTRIBUTE_COLUMNS, getObjectIds(columns));
        for (AtlasEntity column : columns) {
            tableEntity.addReferredEntity(column);
        }
        tableAtlasEntity.setAttribute(ATTRIBUTE_INDEXES, getObjectIds(indexes));
        for (AtlasEntity index : indexes) {
            tableEntity.addReferredEntity(index);
        }
        tableAtlasEntity.setAttribute(ATTRIBUTE_FOREIGN_KEYS, getObjectIds(foreignKeys));
        for (AtlasEntity foreignKey : foreignKeys) {
            tableEntity.addReferredEntity(foreignKey);
        }

        return tableEntity;
    }

    protected void setTableAttribute(List<AtlasEntity> columns, List<AtlasEntity> indexes, List<AtlasEntity> foreignKeys, AtlasEntity.AtlasEntityWithExtInfo tableEntity, AtlasEntity table) {
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


    public AtlasEntity.AtlasEntityWithExtInfo findInstance(String instanceId) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching Atlas for instance {}", instanceId);
        }
        return findEntity(getInstanceTypeName(), getInstanceQualifiedName(instanceId));
    }


    /**
     * 获取数据源类型
     *
     * @return
     */
    public String getInstanceTypeName() {
        return RMDB_INSTANCE;
    }

    /**
     * 获取数据源类型
     *
     * @return
     */
    public String getDatabaseTypeName() {
        return RMDB_DB;
    }

    /**
     * 获取数据源类型
     *
     * @return
     */
    public String getTableTypeName() {
        return RMDB_TABLE;
    }

    protected AtlasEntity.AtlasEntityWithExtInfo registerTable(AtlasEntity dbEntity, String instanceId, String databaseName,
                                                               Table tableName, String instanceGuid, String taskInstanceId, SyncTaskDefinition definition) throws Exception {
        String tableQualifiedName = getTableQualifiedName(instanceId, databaseName, tableName.getName());
        AtlasEntity.AtlasEntityWithExtInfo ret = findEntity(getTableTypeName(), tableQualifiedName);

        InterProcessMutex lock = zkLockUtils.getInterProcessMutex(tableQualifiedName);

        try {
            LOG.debug("尝试拿锁 : " + Thread.currentThread().getName() + " " + tableQualifiedName);
            if (lock.acquire(LOCK_TIME_OUT_TIME, TimeUnit.MINUTES)) {
                LOG.debug("拿锁成功 : " + Thread.currentThread().getName() + " " + tableQualifiedName);
                //监听任务是否被手动停止
                this.checkTaskEnable(taskInstanceId);
                AtlasEntity.AtlasEntityWithExtInfo tableEntity;
                if (ret == null) {
                    tableEntity = toTableEntity(dbEntity, instanceId, databaseName, tableName, null, instanceGuid, definition);
                    ret = registerEntity(tableEntity, definition);
                } else {
                    LOG.debug("Table {}.{} is already registered with id {}. Updating entity.", databaseName, tableName, ret.getEntity().getGuid());
                    ret = toTableEntity(dbEntity, instanceId, databaseName, tableName, ret, instanceGuid, definition);

                    entitiesStore.createOrUpdate(new AtlasEntityStream(ret, definition), false);
                    AtlasRelatedObjectId atlasRelatedObjectId = new AtlasRelatedObjectId();
                    atlasRelatedObjectId.setDisplayText(String.valueOf(dbEntity.getAttribute(ATTRIBUTE_NAME)));
                    ret.getEntity().setRelationshipAttribute("db", atlasRelatedObjectId);
                    dataManageService.updateEntityInfo(Arrays.asList(ret.getEntity()), definition);
                }
                return ret;
            }
            throw new AtlasBaseException("获取锁超时：" + tableQualifiedName);
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                LOG.error("lock release error", e);
            }
        }
    }


    /**
     * Gets the atlas entity for the database
     *
     * @param databaseName database Name
     * @return AtlasEntity for database if exists, else null
     * @throws Exception
     */
    private AtlasEntity.AtlasEntityWithExtInfo findDatabase(String instanceId, String databaseName) {
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
     *
     * @param dbName Name of the Hive database
     * @return Unique qualified name to identify the Database instance in Atlas.
     */
    protected String getDBQualifiedName(String instanceId, String dbName) {
        return String.format("%s.%s@%s", instanceId, dbName, clusterName);
    }

    protected String getTableQualifiedName(String instanceId, String dbName, String tableName) {
        return String.format("%s.%s.%s@%s", instanceId, dbName, tableName, clusterName);
    }

    protected static String getColumnQualifiedName(String tableQualifiedName, final String colName) {
        final String[] parts = tableQualifiedName.split("@");
        final String tableName = parts[0];
        final String clusterName = parts[1];

        return String.format("%s.%s@%s", tableName, colName, clusterName);
    }


    protected AtlasEntity.AtlasEntityWithExtInfo findEntity(final String typeName, final String qualifiedName) throws AtlasBaseException {
        AtlasEntity.AtlasEntityWithExtInfo ret = null;
        try {
            ret = atlasEntityStore.getByUniqueAttributes(atlasTypeRegistry.getEntityTypeByName(typeName), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName), true);
        } catch (AtlasBaseException e) {
            return null;
        }
        clearRelationshipAttributes(ret);

        return ret;
    }

    protected AtlasEntity.AtlasEntityWithExtInfo getById(final String guid, boolean isMinExtInfo) throws AtlasBaseException {
        AtlasEntity.AtlasEntityWithExtInfo ret = null;
        try {
            ret = atlasEntityStore.getById(guid, isMinExtInfo);
        } catch (AtlasBaseException e) {
            return null;
        }
        clearRelationshipAttributes(ret);

        return ret;
    }


    /**
     * Imports all tables for the given db
     *
     * @param dbEntity
     * @param databaseName
     * @param failOnError
     * @throws Exception
     */
    private int importTables(AtlasEntity dbEntity, String instanceId, String databaseName, Collection<Table> tableNames,
                             final boolean failOnError, String instanceGuid, String taskInstanceId, SyncTaskDefinition definition) {
        int tablesImported = 0;

        if (!CollectionUtils.isEmpty(tableNames)) {
            LOG.info("Found {} tables to import in database {}", tableNames.size(), databaseName);

            try {
                //删除JanusGraph中已经不存在table
                if (isSource) {
                    deleteTableEntity(instanceGuid, databaseName, tableNames);
                }

                List<Integer> tablesImportedList = new ArrayList<>(tableNames.size());
                ThreadPoolExecutor threadPoolExecutor = CollectThreadPoolUtil.getCollectThreadPoolExecutor();
                List<CompletableFuture<Void>> completableFutureList = new ArrayList<>(tableNames.size());
                for (Table tableName : tableNames) {
                    completableFutureList.add(CompletableFuture.runAsync(() -> {
                        this.checkTaskEnable(taskInstanceId);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("导入{}表的元数据", tableName.getFullName());
                        }
                        int imported = importTable(dbEntity, instanceId, databaseName, tableName, failOnError, instanceGuid, taskInstanceId, definition);
                        if (imported == 1) {
                            this.checkTaskEnable(taskInstanceId);
                            LOG.info("成功导入表:{}", tableName.getFullName());
                            syncTaskInstanceDAO.appendLog(taskInstanceId, "成功导入表: " + tableName.getFullName());
                        }
                        tablesImportedList.add(imported);
                    }, threadPoolExecutor));
                }
                CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[]{})).join();
                tablesImported = tablesImportedList.stream().mapToInt(Integer::intValue).sum();
            } catch (AtlasBaseException e) {
                throw e;
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
     * 检查任务是否被手动停止
     *
     * @param taskInstanceId
     * @throws AtlasBaseException
     */
    private void checkTaskEnable(String taskInstanceId) throws AtlasBaseException {
        String value = LocalCacheUtils.RDBMS_METADATA_GATHER_ENABLE_CACHE.getIfPresent(taskInstanceId);
        if (StringUtils.isNotBlank(value) && value.equals("fail")) {
            LOG.error("终止元数据同步,数据源：{}", dataSourceInfo.getSourceName());
            throw new AtlasBaseException("终止元数据同步,数据源：" + dataSourceInfo.getSourceName());
        }
    }


    /**
     * Registers an entity in atlas
     *
     * @param entity
     * @return
     * @throws Exception
     */
    protected AtlasEntity.AtlasEntityWithExtInfo registerEntity(AtlasEntity.AtlasEntityWithExtInfo entity, SyncTaskDefinition definition) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating {} entity: {}", entity.getEntity().getTypeName(), entity);
        }

        AtlasEntity.AtlasEntityWithExtInfo ret = null;
        EntityMutationResponse response = atlasEntityStore.createOrUpdate(new AtlasEntityStream(entity, definition), false);
        List<AtlasEntityHeader> createdEntities = response.getEntitiesByOperation(EntityMutations.EntityOperation.CREATE);

        if (CollectionUtils.isNotEmpty(createdEntities)) {
            for (AtlasEntityHeader createdEntity : createdEntities) {
                if (ret == null) {
                    ret = atlasEntityStore.getById(createdEntity.getGuid());
                    dataManageService.addEntity(Arrays.asList(ret.getEntity()), definition);
                    LOG.debug("Created {} entity: name={}, guid={}", ret.getEntity().getTypeName(), ret.getEntity().getAttribute(ATTRIBUTE_QUALIFIED_NAME), ret.getEntity().getGuid());
                } else if (ret.getEntity(createdEntity.getGuid()) == null) {
                    AtlasEntity.AtlasEntityWithExtInfo newEntity = atlasEntityStore.getById(createdEntity.getGuid());

                    ret.addReferredEntity(newEntity.getEntity());

                    if (MapUtils.isNotEmpty(newEntity.getReferredEntities())) {
                        for (Map.Entry<String, AtlasEntity> entry : newEntity.getReferredEntities().entrySet()) {
                            ret.addReferredEntity(entry.getKey(), entry.getValue());
                        }
                    }
                    dataManageService.addEntity(Arrays.asList(newEntity.getEntity()), definition);
                    LOG.debug("Created {} entity: name={}, guid={}", newEntity.getEntity().getTypeName(), newEntity.getEntity().getAttribute(ATTRIBUTE_QUALIFIED_NAME), newEntity.getEntity().getGuid());
                }
            }
        }

        clearRelationshipAttributes(ret);
        return ret;
    }

    private void deleteTableEntity(String instanceId, String databaseName, Collection<Table> tableNames) throws AtlasBaseException {
        String tableQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_BY_STATE), instanceId, databaseName, AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(tableQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String> attributes = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity tableEntity = dbEntityWithExtInfo.getEntity();
                String tableNameInGraph = tableEntity.getAttribute(ATTRIBUTE_NAME).toString();
                if (tableNames.stream().map(NamedObject::getName).noneMatch(name -> name.equals(tableNameInGraph))) {
                    LOG.info("表{}已经在数据源删除，同样在metaspace中删除之", tableNameInGraph);
                    deleteEntity(tableEntity);
                }
            }
        }
    }


    protected void deleteColumnEntity(AtlasEntity.AtlasEntityWithExtInfo tableInfo) throws AtlasBaseException {
        AtlasEntity tableEntity = tableInfo.getEntity();
        List<AtlasObjectId> columns = new ArrayList<>();
        if (tableEntity.getAttribute(ATTRIBUTE_COLUMNS) != null) {
            columns = (List<AtlasObjectId>) tableEntity.getAttribute(ATTRIBUTE_COLUMNS);
        }

        String columnQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_COLUMN_BY_STATE), tableEntity.getGuid(), AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(columnQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String> attributes = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity columnEntity = dbEntityWithExtInfo.getEntity();
                String columnNameInGraph = columnEntity.getAttribute(ATTRIBUTE_NAME).toString();
                String qualifiedName = columnEntity.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString();
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
        if (tableEntity.getAttribute(ATTRIBUTE_FOREIGN_KEYS) != null) {
            foreignKeys = (List<AtlasObjectId>) tableEntity.getAttribute(ATTRIBUTE_FOREIGN_KEYS);
        }
        String foreignKeyQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_FOREIGNKEY_BY_STATE), tableEntity.getGuid(), AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(foreignKeyQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String> attributes = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity foreignKeyEntity = dbEntityWithExtInfo.getEntity();
                String foreignKeyNameInGraph = foreignKeyEntity.getAttribute(ATTRIBUTE_NAME).toString();
                String qualifiedName = foreignKeyEntity.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString();
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
        if (tableEntity.getAttribute(ATTRIBUTE_INDEXES) != null) {
            indexes = (List<AtlasObjectId>) tableEntity.getAttribute(ATTRIBUTE_INDEXES);
        }
        String indexQuery = String.format(gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.RDBMS_DB_TABLE_INDEX_BY_STATE), tableEntity.getGuid(), AtlasEntity.Status.ACTIVE);
        List<AtlasVertex> vertices = (List) graph.executeGremlinScript(indexQuery, false);
        for (AtlasVertex vertex : vertices) {
            if (Objects.nonNull(vertex)) {
                List<String> attributes = Lists.newArrayList(ATTRIBUTE_NAME, BaseFields.ATTRIBUTE_QUALIFIED_NAME);
                AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = entityRetriever.toAtlasEntityWithAttribute(vertex, attributes, null, true);
                AtlasEntity indexEntity = dbEntityWithExtInfo.getEntity();
                String indexNameInGraph = indexEntity.getAttribute(ATTRIBUTE_NAME).toString();
                String qualifiedName = indexEntity.getAttribute(ATTRIBUTE_QUALIFIED_NAME).toString();
                if (indexes.stream().noneMatch(index -> index.getUniqueAttributes().get(ATTRIBUTE_QUALIFIED_NAME).toString().equals(qualifiedName))) {
                    LOG.info("列{}已经在数据源删除，同样在metaspace中删除之", indexNameInGraph);
                    deleteEntity(indexEntity);
                }

            }
        }
    }


    private void deleteEntity(AtlasEntity tableEntity) throws AtlasBaseException {
        AtlasEntityType type = (AtlasEntityType) atlasTypeRegistry.getType(tableEntity.getTypeName());
        final AtlasObjectId objectId = getObjectId(tableEntity);
        atlasEntityStore.deleteByUniqueAttributes(type, objectId.getUniqueAttributes());
        dataManageService.updateStatus(Arrays.asList(tableEntity));
    }

    private void deleteEntityById(AtlasEntity tableEntity) throws AtlasBaseException {
        atlasEntityStore.deleteById(tableEntity.getGuid());
        dataManageService.updateStatus(Arrays.asList(tableEntity));
    }

    public static AtlasObjectId getObjectId(AtlasEntity entity) {
        String qualifiedName = (String) entity.getAttribute(ATTRIBUTE_QUALIFIED_NAME);
        AtlasObjectId ret = new AtlasObjectId(entity.getGuid(), entity.getTypeName(), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName));

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

    public Collection<Table> getTables(final Schema schema) {
        final Collection<Table> tables = new ArrayList<>();
        for (final Table table : this.metaDataInfo.getTables()) {
            if (test(table, schema)) {
                tables.add(table);
            }
        }
        return tables;
    }

    public boolean test(final DatabaseObject databaseObject, final Schema schema) {
        return databaseObject != null && databaseObject.getSchema()
                .equals(schema);
    }
}
