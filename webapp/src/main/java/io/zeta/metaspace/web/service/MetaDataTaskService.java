package io.zeta.metaspace.web.service;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.sync.SyncTaskInstance;
import io.zeta.metaspace.web.dao.SyncTaskDefinitionDAO;
import io.zeta.metaspace.web.dao.SyncTaskInstanceDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.service.dataquality.TaskManageService;
import io.zeta.metaspace.web.task.quartz.QuartzManager;
import io.zeta.metaspace.web.task.sync.SyncTaskJob;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.LocalCacheUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class MetaDataTaskService {

    private static Configuration conf;

    public final static String SYNC_TASK_STATUS = "metaspace.sync.task.status";

    private static Boolean syncTask;

    @Autowired
    private SyncTaskDefinitionDAO syncTaskDefinitionDAO;
    @Autowired
    private SyncTaskInstanceDAO syncTaskInstanceDAO;
    @Autowired
    private QuartzManager quartzManager;
    @Autowired
    TableDAO tableDAO;
    @Autowired
    UserDAO userDAO;

    static {
        try {
            conf = ApplicationProperties.get();
            syncTask = conf.getBoolean(SYNC_TASK_STATUS, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void init() {
        if(syncTask){
            syncTaskInstanceDAO.updateStatusAndAppendLogAllFail(SyncTaskInstance.Status.FAIL, "????????????????????????????????????");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(TaskManageService.class);

    public void checkDuplicateName(String id, String name, String tenantId) {
        if (syncTaskDefinitionDAO.countByName(id, name, tenantId) != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
        }
    }


    public void checkSyncTaskDefinition(SyncTaskDefinition definition) {
        if (StringUtils.isNoneEmpty(definition.getCrontab())) {
            if (definition.getCronStartTime() == null || definition.getCronEndTime() == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????????????????????????????");
            }

            if (definition.getCronStartTime().after(definition.getCronEndTime())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????????????????");
            }

            if (!CronExpression.isValidExpression(definition.getCrontab())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
            }
        }

        if (!definition.isSyncAll() && CollectionUtils.isEmpty(definition.getSchemas())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "??????????????????????????????");
        }
        if (null == definition.getCategoryGuid()) {
            definition.setCategoryGuid("1");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void createSyncTaskDefinition(SyncTaskDefinition syncTaskDefinition, String tenantId) {
        try {
            syncTaskDefinition.setId(UUID.randomUUID().toString());
            checkDuplicateName(syncTaskDefinition.getId(), syncTaskDefinition.getName(), tenantId);
            checkSyncTaskDefinition(syncTaskDefinition);

            String userId=AdminUtils.getUserData().getUserId();
            syncTaskDefinition.setCreator(userDAO.getUserInfo(userId).getUsername());
            syncTaskDefinition.setTenantId(tenantId);
            syncTaskDefinitionDAO.insert(syncTaskDefinition);

            if (syncTaskDefinition.isEnable()) {
                startSyncJob(syncTaskDefinition.getId());
            }
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int updateSyncTaskDefinition(SyncTaskDefinition syncTaskDefinition, String tenantId) {
        try {
            if (StringUtils.isEmpty(syncTaskDefinition.getId())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }
            SyncTaskDefinition oldDefinition = syncTaskDefinitionDAO.getById(syncTaskDefinition.getId());
            if (oldDefinition == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }
            if(syncTaskDefinition.getCategoryGuid() == null){
                syncTaskDefinition.setCategoryGuid(oldDefinition.getCategoryGuid());
            }

            if (oldDefinition.isEnable()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
            }

            checkDuplicateName(syncTaskDefinition.getId(), syncTaskDefinition.getName(), tenantId);
            checkSyncTaskDefinition(syncTaskDefinition);

            return syncTaskDefinitionDAO.update(syncTaskDefinition);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateSyncTaskDefinitionEnable(String id, boolean enable, String tenantId) {
        try {
            if (StringUtils.isEmpty(id)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }

            SyncTaskDefinition definition = syncTaskDefinitionDAO.getById(id);
            if (definition == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }

            if (definition.isEnable() == enable) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????" + (enable ? "??????" : "??????"));
            }

            HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "????????????????????????????????????: " + definition.getName() + (enable ? "??????" : "??????"));

            if (enable) {
                startSyncJob(id);
            } else {
                stopSyncJob(id);
            }

            return syncTaskDefinitionDAO.updateEnable(id, enable);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????????????????");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteSyncTaskDefinition(List<String> ids, String tenantId) {
        try {
            List<String> name = new ArrayList<>();
            if (ids != null && !ids.isEmpty()) {
                for (String id : ids) {
                    SyncTaskDefinition syncTaskDefinition = syncTaskDefinitionDAO.getById(id);
                    if (syncTaskDefinition == null || !tenantId.equals(syncTaskDefinition.getTenantId())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
                    }
                    if (syncTaskDefinition.isEnable()) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????????????????");
                    }
                    name.add(syncTaskDefinition.getName());
                }
            }
            HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "????????????????????????: " + String.join(",",name));
            return syncTaskDefinitionDAO.delete(ids);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteSyncTaskInstance(List<String> ids, String tenantId) {
        try {
            List<String> name = new ArrayList<>();
            if (ids != null && !ids.isEmpty()) {
                for (String id : ids) {
                    SyncTaskInstance syncTaskInstance = syncTaskInstanceDAO.getById(id);
                    if (syncTaskInstance == null) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
                    }
                    if (SyncTaskInstance.Status.RUN.equals(syncTaskInstance.getStatus())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????????????????");
                    }
                    name.add(syncTaskInstance.getName());
                }
            }
            HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "????????????????????????: " + String.join(",",name));
            return syncTaskInstanceDAO.delete(ids);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????????????????");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PageResult<SyncTaskDefinition> getSyncTaskDefinitionList(Parameters parameters, String tenantId) {
        try {
            PageResult<SyncTaskDefinition> pageResult = new PageResult<>();
            String query = parameters.getQuery();
            if (Objects.nonNull(query)) {
                parameters.setQuery(query.replaceAll("_", "/_").replaceAll("%", "/%"));
            }
            List<SyncTaskDefinition> result;
            try {
                result = syncTaskDefinitionDAO.pageList(parameters, tenantId);
            } catch (SQLException e) {
                LOG.error("SQL????????????", e);
                result = new ArrayList<>();
            }
            pageResult.setCurrentSize(result.size());
            pageResult.setLists(result);
            pageResult.setTotalSize(result.size() == 0 ? 0 : result.get(0).getTotal());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PageResult<SyncTaskInstance> getSyncTaskInstanceList(String definitionId, Parameters parameters, SyncTaskInstance.Status status, String tenantId) {
        try {
            PageResult<SyncTaskInstance> pageResult = new PageResult<>();
            List<SyncTaskInstance> result = syncTaskInstanceDAO.pageList(definitionId, parameters, status);
            result = result.stream().peek(syncTaskInstance -> {
                if (Arrays.asList(SyncTaskInstance.Status.FAIL, SyncTaskInstance.Status.SUCCESS).contains(syncTaskInstance.getStatus())) {
                    syncTaskInstance.setDuration((syncTaskInstance.getUpdateTime().getTime() - syncTaskInstance.getStartTime().getTime()) / 1000);
                } else {
                    syncTaskInstance.setUpdateTime(null);
                }
            }).collect(Collectors.toList());
            pageResult.setCurrentSize(result.size());
            pageResult.setLists(result);
            pageResult.setTotalSize(result.size() == 0 ? 0 : result.get(0).getTotal());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????????????????");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String getSyncTaskInstanceLog(String instanceId) {
        try {
            if (StringUtils.isEmpty(instanceId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }
            if (syncTaskInstanceDAO.getById(instanceId) == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
            }

            return syncTaskInstanceDAO.getLog(instanceId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????????????????");
        }
    }

    /**
     * ????????????
     */
    public void startSyncJob(String definitionId) {
        try {
            if (StringUtils.isEmpty(definitionId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }
            SyncTaskDefinition definition = syncTaskDefinitionDAO.getById(definitionId);
            if (definition == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }

            String jobName = buildJobName(definitionId);
            String jobGroupName = buildJobGroupName(definitionId);
            String triggerName = buildTriggerName(definitionId);
            String triggerGroupName = buildTriggerGroupName(definitionId);

            if (StringUtils.isNotEmpty(definition.getCrontab())) {
                quartzManager.addCronJobWithTimeRange(jobName, jobGroupName, triggerName, triggerGroupName, SyncTaskJob.class,
                        definition.getCrontab(), 1, definition.getCronStartTime(), definition.getCronEndTime(), true,
                        definition.getCreator());
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "????????????????????????");
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("will never fire")) {
                throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "???????????????????????????????????????");
            }
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????");
        }
    }


    public String buildJobName(String definitionId) {
        return "job_" + definitionId;
    }


    public static String buildJobGroupName(String definitionId) {
        return "job_group_" + definitionId;
    }

    public String buildTriggerName(String definitionId) {
        return "trigger_" + definitionId;
    }

    public String buildTriggerGroupName(String definitionId) {
        return "trigger_group_" + definitionId;
    }

    /**
     * ????????????
     */
    public void stopSyncJob(String definitionId) {
        try {
            if (StringUtils.isEmpty(definitionId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }
            SyncTaskDefinition definition = syncTaskDefinitionDAO.getById(definitionId);
            if (definition == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }

            String jobName = buildJobName(definitionId);
            String jobGroupName = buildJobGroupName(definitionId);
            String triggerName = buildTriggerName(definitionId);
            String triggerGroupName = buildTriggerGroupName(definitionId);

            quartzManager.removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????");
        }
    }

    /**
     * ????????????
     */
    public void startManualJob(String definitionId) {
        try {
            if (StringUtils.isEmpty(definitionId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }
            SyncTaskDefinition definition = syncTaskDefinitionDAO.getById(definitionId);
            if (definition == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }
    
            HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "??????????????????????????????: " + definition.getName());
    
            String now = LocalDateTime.now().toString();
            String jobName = buildJobName(definitionId + now);
            String jobGroupName = buildJobGroupName(definitionId);
            List<String> schemas = definition.getSchemas();
            quartzManager.addSimpleJob(jobName, jobGroupName, SyncTaskJob.class,
                    new HashMap<String, Object>(2) {
                        {
                            put("executor", AdminUtils.getUserName());
                            put("isSimple", true);
                        }
                    });
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "??????????????????");
        }
    }

    /**
     * ??????????????????
     */
    public void stopSyncTaskInstance(String instanceId) {
        try {
            if (StringUtils.isEmpty(instanceId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "???????????????");
            }
            SyncTaskInstance instance = syncTaskInstanceDAO.getById(instanceId);
            if (instance == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "?????????????????????");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "????????????????????????: " + instance.getName());
            syncTaskInstanceDAO.updateStatusAndAppendLog(instanceId, SyncTaskInstance.Status.FAIL, "????????????????????????");
            LocalCacheUtils.RDBMS_METADATA_GATHER_ENABLE_CACHE.put(instanceId, "fail");
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "????????????????????????");
        }
    }
}
