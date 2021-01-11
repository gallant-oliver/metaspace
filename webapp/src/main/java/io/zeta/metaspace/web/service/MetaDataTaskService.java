package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.sync.SyncTaskInstance;
import io.zeta.metaspace.web.dao.SyncTaskDefinitionDAO;
import io.zeta.metaspace.web.dao.SyncTaskInstanceDAO;
import io.zeta.metaspace.web.task.quartz.QuartzManager;
import io.zeta.metaspace.web.task.sync.SyncTaskJob;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.janusgraph.util.datastructures.ArraysUtil;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class MetaDataTaskService {

    @Autowired
    private SyncTaskDefinitionDAO syncTaskDefinitionDAO;
    @Autowired
    private SyncTaskInstanceDAO syncTaskInstanceDAO;
    @Autowired
    private QuartzManager quartzManager;

    public void checkDuplicateName(String id, String name, String tenantId) {
        if (syncTaskDefinitionDAO.countByName(id, name, tenantId) != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务名称已存在");
        }
    }


    public void checkSyncTaskDefinition(SyncTaskDefinition definition) {
        if (StringUtils.isNoneEmpty(definition.getCrontab())) {
            if (definition.getCronStartTime() == null || definition.getCronEndTime() == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "定时开始或者结束时间未设置");
            }

            if (definition.getCronStartTime().after(definition.getCronEndTime())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "定时开始时间不能晚于结束时间");
            }

            if (!CronExpression.isValidExpression(definition.getCrontab())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "定时表达式无效");
            }
        }

        if (!definition.isSyncAll() && CollectionUtils.isEmpty(definition.getSchemas())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "自定义数据库不能为空");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void createSyncTaskDefinition(SyncTaskDefinition syncTaskDefinition, String tenantId) {
        try {
            syncTaskDefinition.setId(UUID.randomUUID().toString());
            checkDuplicateName(syncTaskDefinition.getId(), syncTaskDefinition.getName(), tenantId);
            checkSyncTaskDefinition(syncTaskDefinition);

            syncTaskDefinition.setCreator(AdminUtils.getUserName());
            syncTaskDefinition.setTenantId(tenantId);
            syncTaskDefinitionDAO.insert(syncTaskDefinition);

            if (syncTaskDefinition.isEnable()) {
                startSyncJob(syncTaskDefinition.getId());
            }
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "创建任务失败");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int updateSyncTaskDefinition(SyncTaskDefinition syncTaskDefinition, String tenantId) {
        try {
            if (StringUtils.isEmpty(syncTaskDefinition.getId())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            if (syncTaskDefinitionDAO.getById(syncTaskDefinition.getId()) == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务不存在");
            }

            if (syncTaskDefinition.isEnable()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不能修改启用定时的任务");
            }

            checkDuplicateName(syncTaskDefinition.getId(), syncTaskDefinition.getName(), tenantId);
            checkSyncTaskDefinition(syncTaskDefinition);

            return syncTaskDefinitionDAO.update(syncTaskDefinition);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新任务失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateSyncTaskDefinitionEnable(String id, boolean enable, String tenantId) {
        try {
            if (StringUtils.isEmpty(id)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            if (syncTaskDefinitionDAO.getById(id) == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务不存在");
            }

            if (enable) {
                startSyncJob(id);
            } else {
                stopSyncJob(id);
            }

            return syncTaskDefinitionDAO.updateEnable(id, enable);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "更新任务失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteSyncTaskDefinition(List<String> ids, String tenantId) {
        try {
            if (ids != null && !ids.isEmpty()) {
                for (String id : ids) {
                    SyncTaskDefinition syncTaskDefinition = syncTaskDefinitionDAO.getById(id);
                    if (syncTaskDefinition == null || !tenantId.equals(syncTaskDefinition.getTenantId())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务不存在");
                    }
                    if (syncTaskDefinition.isEnable()) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不能删除启用定时的任务");
                    }
                }
            }
            return syncTaskDefinitionDAO.delete(ids);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除任务失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteSyncTaskInstance(List<String> ids, String tenantId) {
        try {
            if (ids != null && !ids.isEmpty()) {
                for (String id : ids) {
                    SyncTaskInstance syncTaskInstance = syncTaskInstanceDAO.getById(id);
                    if (syncTaskInstance == null) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务实例不存在");
                    }
                    if (SyncTaskInstance.Status.RUN.equals(syncTaskInstance.getStatus())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不能删除运行中的任务实例");
                    }
                }
            }
            return syncTaskInstanceDAO.delete(ids);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除任务实例失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PageResult<SyncTaskDefinition> getSyncTaskDefinitionList(Parameters parameters, String tenantId) {
        try {
            PageResult<SyncTaskDefinition> pageResult = new PageResult<>();
            List<SyncTaskDefinition> result = syncTaskDefinitionDAO.pageList(parameters, tenantId);
            pageResult.setCurrentSize(result.size());
            pageResult.setLists(result);
            pageResult.setTotalSize(result.size() == 0 ? 0 : result.get(0).getTotal());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "任务获取失败");
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
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "任务实例获取失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String getSyncTaskInstanceLog(String instanceId) {
        try {
            if (StringUtils.isEmpty(instanceId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            if (syncTaskInstanceDAO.getById(instanceId) == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务实例不存在");
            }

            return syncTaskInstanceDAO.getLog(instanceId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "任务实例获取日志失败");
        }
    }

    /**
     * 启动定时
     */
    public void startSyncJob(String definitionId) {
        try {
            if (StringUtils.isEmpty(definitionId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            SyncTaskDefinition definition = syncTaskDefinitionDAO.getById(definitionId);
            if (definition == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务不存在");
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
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务没有配置定时");
            }
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "启动定时失败");
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
     * 停止定时
     */
    public void stopSyncJob(String definitionId) {
        try {
            if (StringUtils.isEmpty(definitionId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            SyncTaskDefinition definition = syncTaskDefinitionDAO.getById(definitionId);
            if (definition == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务不存在");
            }

            String jobName = buildJobName(definitionId);
            String jobGroupName = buildJobGroupName(definitionId);
            String triggerName = buildTriggerName(definitionId);
            String triggerGroupName = buildTriggerGroupName(definitionId);

            quartzManager.removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "任务停止失败");
        }
    }

    /**
     * 手动执行
     */
    public void startManualJob(String definitionId) {
        try {
            if (StringUtils.isEmpty(definitionId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            SyncTaskDefinition definition = syncTaskDefinitionDAO.getById(definitionId);
            if (definition == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务不存在");
            }

            String now = LocalDateTime.now().toString();
            String jobName = buildJobName(definitionId + now);
            String jobGroupName = buildJobGroupName(definitionId);

            quartzManager.addSimpleJob(jobName, jobGroupName, SyncTaskJob.class, AdminUtils.getUserName());
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "手动触发失败");
        }
    }

    /**
     * 停止任务实例
     */
    public void stopSyncTaskInstance(String instanceId) {
        try {
            if (StringUtils.isEmpty(instanceId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            SyncTaskInstance instance = syncTaskInstanceDAO.getById(instanceId);
            if (instance == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务实例不存在");
            }
            syncTaskInstanceDAO.updateStatusAndAppendLog(instanceId, SyncTaskInstance.Status.FAIL, "手动停止任务实例");
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "停止任务实例失败");
        }
    }
}
