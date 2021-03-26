package io.zeta.metaspace.web.task.sync;

import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.sync.SyncTaskInstance;
import io.zeta.metaspace.web.dao.SyncTaskDefinitionDAO;
import io.zeta.metaspace.web.dao.SyncTaskInstanceDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.metadata.RDBMSMetaDataProvider;
import io.zeta.metaspace.web.service.DataSourceService;
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
    private DataSourceService dataSourceService;
    @Autowired
    TableDAO tableDAO;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String instanceId = UUID.randomUUID().toString();
        try {
            String group = jobExecutionContext.getJobDetail().getKey().getGroup();
            String definitionId = group.replace("job_group_", "");
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            String executor =  (String)jobDataMap.getOrDefault("executor", null);
            Boolean isSimple =  (Boolean)jobDataMap.getOrDefault("isSimple", false);
            SyncTaskDefinition definition = syncTaskDefinitionDAO.getById(definitionId);
            if (definition == null) {
                throw new AtlasBaseException("采集任务找不到任务定义");
            }
            if(null == definition.getCategoryGuid()){
                definition.setCategoryGuid("1");
            }
            List<String> schemas = definition.getSchemas();
            if(!isSimple){
                if(CollectionUtils.isEmpty(schemas)&&definition.isSyncAll()){
                    tableDAO.updateTableRelationBySourceId(definition.getCategoryGuid(),definition.getDataSourceId());
                }else{
                    tableDAO.updateTableRelationByDb(definition.getCategoryGuid(),definition.getDataSourceId(),schemas);
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
            TableSchema schema = new TableSchema();
            schema.setInstance(dataSourceId);
            schema.setAll(definition.isSyncAll());
            schema.setDatabases(definition.getSchemas());
            schema.setDefinition(definition);
            if ("hive".equalsIgnoreCase(dataSourceId)) {
                hiveMetaStoreBridgeUtils.importDatabases(instance.getId(), schema);
            } else {
                rdbmsMetaDataProvider.importDatabases(instance.getId(), schema);
            }
        } catch (Exception e) {
            SyncTaskInstance syncTaskInstance = syncTaskInstanceDAO.getById(instanceId);
            if (syncTaskInstance != null) {
                syncTaskInstanceDAO.updateStatusAndAppendLog(instanceId, SyncTaskInstance.Status.FAIL, "执行异常：" + e.getMessage());
            }
            log.error("任务实例异常 " + instanceId, e);
            throw new AtlasBaseException(e);
        }
    }
}