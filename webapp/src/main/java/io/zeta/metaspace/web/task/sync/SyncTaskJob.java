package io.zeta.metaspace.web.task.sync;

import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.sync.SyncTaskInstance;
import io.zeta.metaspace.web.dao.DbDAO;
import io.zeta.metaspace.web.dao.SyncTaskDefinitionDAO;
import io.zeta.metaspace.web.dao.SyncTaskInstanceDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.metadata.RDBMSMetaDataProvider;
import io.zeta.metaspace.web.service.indexmanager.IndexCounter;
import io.zeta.metaspace.web.util.HiveMetaStoreBridgeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 元数据采集任务 job
 */
@Slf4j
public class SyncTaskJob implements Job {
    @Autowired
    private RDBMSMetaDataProvider rdbmsMetaDataProvider;
    @Autowired
    private HiveMetaStoreBridgeUtils hiveMetaStoreBridgeUtils;
    @Autowired
    private SyncTaskInstanceDAO syncTaskInstanceDAO;
    @Autowired
    private SyncTaskDefinitionDAO syncTaskDefinitionDAO;
    @Autowired
    TableDAO tableDAO;
    @Autowired
    private IndexCounter indexCounter;
    @Autowired
    private DbDAO dbDAO;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String instanceId = UUID.randomUUID().toString();
        TableSchema schema = new TableSchema();
        SyncTaskDefinition definition = null;
        try {
            String group = jobExecutionContext.getJobDetail().getKey().getGroup();
            String definitionId = group.replace("job_group_", "");
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            String executor = (String) jobDataMap.getOrDefault("executor", null);
            definition = syncTaskDefinitionDAO.getById(definitionId);
            if (definition == null) {
                throw new AtlasBaseException("采集任务找不到任务定义");
            }
            if (null == definition.getCategoryGuid()) {
                definition.setCategoryGuid("1");
            }
            List<Database> sourceDbs = dbDAO.getSourceDbs(definition.getDataSourceId(), definition.getSchemas());
            if (!CollectionUtils.isEmpty(sourceDbs)) {
                List<String> schemas = definition.getSchemas();
                if (!CollectionUtils.isEmpty(schemas)) {
                    sourceDbs = sourceDbs.stream().filter(sdb -> schemas.contains(sdb.getDatabaseName())).
                            collect(Collectors.toList());
                }
                for (Database sdb : sourceDbs) {
                    String sourceDbRelationId = dbDAO.getSourceDbRelationId(sdb.getDatabaseId(), definition.getDataSourceId());
                    if (null == sourceDbRelationId) {
                        dbDAO.insertSourceDbRelation(UUID.randomUUID().toString(), sdb.getDatabaseId(), definition.getDataSourceId());
                    }
                }
            }
            SyncTaskInstance instance = new SyncTaskInstance();
            instance.setId(instanceId);
            instance.setDefinitionId(definitionId);
            instance.setName(definition.getName() + "_" + LocalDateTime.now().toString());
            instance.setExecutor(executor);
            instance.setStatus(SyncTaskInstance.Status.RUN);
            syncTaskInstanceDAO.insert(instance);

            String dataSourceId = definition.getDataSourceId();
            schema.setInstance(dataSourceId);
            schema.setAll(definition.isSyncAll());
            schema.setDatabases(definition.getSchemas());
            schema.setDefinition(definition);
            if ("hive".equalsIgnoreCase(dataSourceId)) {
                hiveMetaStoreBridgeUtils.importDatabases(instance.getId(), schema);
                indexCounter.plusOneSuccess("HIVE");
            } else {
                rdbmsMetaDataProvider.importDatabases(instance.getId(), schema);
                indexCounter.plusOneSuccess(schema.getDefinition().getDataSourceType());
            }
        } catch (Exception e) {
            SyncTaskInstance syncTaskInstance = syncTaskInstanceDAO.getById(instanceId);
            if (syncTaskInstance != null) {
                syncTaskInstanceDAO.updateStatusAndAppendLog(instanceId, SyncTaskInstance.Status.FAIL, "执行异常：" + e.getMessage());
            }
            if (null == definition) {
                throw new AtlasBaseException(e.getMessage());
            }
            {
                indexCounter.plusOneFail(definition.getDataSourceType());
            }
            if (null != definition)

                log.error("任务实例异常 " + instanceId, e);
            throw new AtlasBaseException(e);
        }
    }

}