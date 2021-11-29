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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/7/24 10:29
 */
package io.zeta.metaspace.web.service.dataquality;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 10:29
 */

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.dataquality.Schedule;
import io.zeta.metaspace.model.dataquality.TaskType;
import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.datasource.DataSourceHead;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.ColumnParameters;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.Pool;
import io.zeta.metaspace.model.security.Queue;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.utils.GsonUtils;
import io.zeta.metaspace.web.dao.DataSourceDAO;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.task.quartz.QuartzJob;
import io.zeta.metaspace.web.task.quartz.QuartzManager;
import io.zeta.metaspace.web.task.util.LivyTaskSubmitHelper;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HdfsUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.QualityEngine;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class TaskManageService {
    
    private static final Logger LOG = LoggerFactory.getLogger(TaskManageService.class);
    private static final String JOB_GROUP_NAME = "METASPACE_JOBGROUP";
    private static final String TRIGGER_NAME = "METASPACE_TRIGGER";
    private static final String TRIGGER_GROUP_NAME = "METASPACE_TRIGGERGROUP";
    private static String engine;
    private static Configuration conf;
    private static Cache<String, List<String>> errorDataCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(30, TimeUnit.MINUTES).build();
    
    static {
        try {
            conf = ApplicationProperties.get();
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    private static int errorDataSize = 2000;

    @Autowired
    TaskManageDAO taskManageDAO;
    @Autowired
    RuleService ruleService;
    @Autowired
    QuartzManager quartzManager;
    @Autowired
    UsersService usersService;
    @Autowired
    MetaspaceGremlinQueryService metaspaceEntityService;
    @Autowired
    BusinessService businessService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private DataShareService dataShareService;
    @Autowired
    private DataSourceDAO dataSourceDAO;

    public PageResult<TaskHeader> getTaskList(Integer my, Parameters parameters, String tenantId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            String query = parameters.getQuery();
            if (Objects.nonNull(query)) {
                parameters.setQuery(query.replaceAll("_", "/_").replaceAll("%", "/%"));
            }
            List<TaskHeader> list;
            try {
                list = taskManageDAO.getTaskList(my, userId, parameters, tenantId);
            } catch (SQLException e) {
                LOG.error("SQL执行异常", e);
                list = new ArrayList<>();
            }
            long totalSize = 0;
            if (list.size() != 0) {
                totalSize = list.get(0).getTotal();
            }
            PageResult<TaskHeader> pageResult = new PageResult<>();
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取任务列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务列表失败");
        }
    }

    public PageResult getTableList(String dbName, Parameters parameters) throws AtlasBaseException {
        try {
            String databaseId = taskManageDAO.getDbIdByDbName(dbName);
            PageResult<Table> pageResult = metaspaceEntityService.getTableByDB(databaseId, true, parameters.getOffset(), parameters.getLimit());

            List<Table> tableList = pageResult.getLists();
            if (Objects.nonNull(tableList) && tableList.size() > 0) {
                Table tmpTable = taskManageDAO.getDbAndTableName(tableList.get(0).getTableId());
                if (tmpTable != null) {
                    dbName = tmpTable.getDatabaseName();
                }
            }
            for (Table table : tableList) {
                table.setDatabaseName(dbName);
            }
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表失败");
        }
    }

    public PageResult getColumnList(String dbName, String tableName, ColumnParameters parameters, String tenantId) throws AtlasBaseException {
        try {
            String tableGuid = taskManageDAO.getTableId(dbName, tableName);
            List<String> ruleIds = parameters.getRuleIds();
            //判断规则是否还有数值型规则
            int count = 0;
            if (ruleIds != null && ruleIds.size() != 0) {
                count = taskManageDAO.getNumericTypeTemplateRuleIdCount(ruleIds, tenantId);
            }
            boolean isNum = count != 0;
            PageResult<Column> pageResult = businessService.getTableColumnList(tableGuid, parameters, null, null, isNum);
            List<Column> columnList = pageResult.getLists();
            if (columnList != null && columnList.size() > 0) {  //JanusGraph中无column信息
                for (Column column : columnList) {
                    Column tmpColumn = taskManageDAO.getDbAndTableAndColumnName(column.getColumnId());
                    column.setDatabaseName(tmpColumn.getDatabaseName());
                    column.setTableName(tmpColumn.getTableName());
                }
            }
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取字段列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取字段列表失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskList(List<String> taskIdList) throws AtlasBaseException {
        try {
            if (Objects.nonNull(taskIdList)) {
                for (String taskId : taskIdList) {
                    String jobName = taskManageDAO.getQrtzJobByTaskId(taskId);
                    if (jobName != null && jobName.length() != 0) {
                        String jobGroupName = JOB_GROUP_NAME + jobName;
                        String triggerName = TRIGGER_NAME + jobName;
                        String triggerGroupName = TRIGGER_GROUP_NAME + jobName;
                        quartzManager.removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
                    }
                }
                taskManageDAO.deleteTaskList(taskIdList);
                taskManageDAO.deleteSubTaskList(taskIdList);
                taskManageDAO.deleteSubTaskObjectList(taskIdList);
                taskManageDAO.deleteSubTaskRuleList(taskIdList);
                taskManageDAO.deleteWarningGroupUsed(taskIdList);
            }
        } catch (Exception e) {
            LOG.error("删除任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除任务失败");
        }
    }

    public List<RuleHeader> getValidRuleList(String groupId, String scope, String tenantId) throws AtlasBaseException {
        try {
            Integer objType = null;
            if (!"all".equals(scope)) {
                objType = Integer.valueOf(scope);
            }
            List<RuleHeader> ruleList = taskManageDAO.getRuleListByCategoryId(groupId, objType, tenantId);
            return ruleList;
        } catch (Exception e) {
            LOG.error("获取规则列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则列表失败");
        }
    }

    public List<TaskWarningHeader.WarningGroupHeader> getWarningGroupList(String groupId, String tenantId) throws AtlasBaseException {
        try {
            return taskManageDAO.getWarningGroupList(groupId, tenantId);
        } catch (Exception e) {
            LOG.error("获取告警组列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警组列表失败");
        }
    }
    
    public List<TaskWarningHeader.WarningGroupHeader> getAllWarningGroup(String tenantId) throws AtlasBaseException {
        try {
            return taskManageDAO.getAllWarningGroup(tenantId);
        } catch (Exception e) {
            LOG.error("获取告警组列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警组列表失败");
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void addDataQualityTask(TaskInfo taskInfo, String tenantId) throws AtlasBaseException {
        try {
            Timestamp currentTime = DateUtils.currentTimestamp();
            DataQualityTask dataQualityTask = new DataQualityTask();
            
            List<String> dataQualityTaskByName = taskManageDAO.getDataQualityTaskByName(taskInfo.getTaskName(), tenantId);
            if (dataQualityTaskByName != null && dataQualityTaskByName.size() > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务名称已经存在");
            }
            
            //id
            String guid = UUID.randomUUID().toString();
            dataQualityTask.setId(guid);
            //任务名称
            dataQualityTask.setName(taskInfo.getTaskName());
            //任务级别
            dataQualityTask.setLevel(taskInfo.getLevel());
            //描述
            dataQualityTask.setDescription(taskInfo.getDescription());
            //cron表达式
            dataQualityTask.setCronExpression(taskInfo.getCronExpression());
            //执行起始时间
            dataQualityTask.setStartTime(taskInfo.getStartTime());
            //执行截止时间
            dataQualityTask.setEndTime(taskInfo.getEndTime());

            //创建时间
            dataQualityTask.setCreateTime(currentTime);
            //更新时间
            dataQualityTask.setUpdateTime(currentTime);
            //创建者
            String userId = AdminUtils.getUserData().getUserId();
            dataQualityTask.setCreator(userId);
            dataQualityTask.setUpdater(userId);
            //是否已删除
            dataQualityTask.setDelete(false);
            //是否开启
            dataQualityTask.setEnable(false);
            //orangeWarningCount
            dataQualityTask.setGeneralWarningTotalCount(0);
            //orangeWarningCount
            dataQualityTask.setOrangeWarningTotalCount(0);
            //redWarningCount
            dataQualityTask.setRedWarningTotalCount(0);
            //errorCount
            dataQualityTask.setErrorTotalCount(0);
            //executionCount
            dataQualityTask.setExecutionCount(0);
            //子任务
            List<TaskInfo.SubTask> subTaskList = taskInfo.getTaskList();
            addDataQualitySubTask(guid, currentTime, subTaskList, tenantId);

            if (taskInfo.getContentWarningNotificationIdList() != null && taskInfo.getContentWarningNotificationIdList().size() != 0) {
                taskManageDAO.addTaskWarningGroup(guid, WarningType.WARNING.code, taskInfo.getContentWarningNotificationIdList());
            }
            if (taskInfo.getExecutionWarningNotificationIdList() != null && taskInfo.getExecutionWarningNotificationIdList().size() != 0) {
                taskManageDAO.addTaskWarningGroup(guid, WarningType.ERROR.code, taskInfo.getExecutionWarningNotificationIdList());
            }


            taskManageDAO.addDataQualityTask(dataQualityTask, tenantId);

            taskManageDAO.updateTaskStatus(guid, 0);
            taskManageDAO.updateTaskFinishedPercent(guid, 0F);

        } catch (AtlasBaseException e) {
            LOG.error("添加任务失败", e);
            throw e;
        } catch (Exception e) {
            LOG.error("添加任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加任务失败");
        }
    }

    public void addDataQualitySubTask(String taskId, Timestamp currentTime, List<TaskInfo.SubTask> subTaskList, String tenantId) throws AtlasBaseException {
        try {
            for (int i = 0, size = subTaskList.size(); i < size; i++) {
                TaskInfo.SubTask subTask = subTaskList.get(i);
                Gson gson = new Gson();
                DataQualitySubTask dataQualitySubTask = new DataQualitySubTask();
                //id
                String guid = UUID.randomUUID().toString();
                dataQualitySubTask.setId(guid);
                //taskId
                dataQualitySubTask.setTaskId(taskId);
                //数据源类型
                dataQualitySubTask.setDataSourceType(subTask.getDataSourceType());
                //序号
                dataQualitySubTask.setSequence(i + 1);
                //创建时间
                dataQualitySubTask.setCreateTime(currentTime);
                //更新时间
                dataQualitySubTask.setUpdateTime(currentTime);
                //是否已删除
                dataQualitySubTask.setDelete(false);
                // 资源池
                dataQualitySubTask.setPool(subTask.getPool());
                // spark配置
                String config = null;
                if (subTask.getConfig() != null) {
                    config = GsonUtils.getInstance().toJson(subTask.getConfig());
                }
                dataQualitySubTask.setConfig(config);
                //subTaskRule
                List<TaskInfo.SubTaskRule> subTaskRuleList = subTask.getSubTaskRuleList();
                addDataQualitySubTaskRule(guid, currentTime, subTaskRuleList, tenantId);
                //object
                List<String> objectIdList = subTask.getObjectIdList().stream().map(obj -> GsonUtils.getInstance().toJson(obj)).collect(Collectors.toList());
                addDataQualitySubTaskObject(taskId, guid, currentTime, objectIdList);
                taskManageDAO.addDataQualitySubTask(dataQualitySubTask);
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加任务失败");
        }
    }

    public void addDataQualitySubTaskObject(String taskId, String subTaskId, Timestamp currentTime, List<String> objectIdList) throws AtlasBaseException {
        try {
            for (int i = 0, size = objectIdList.size(); i < size; i++) {
                String objectId = objectIdList.get(i);
                DataQualitySubTaskObject dataQualitySubTaskObject = new DataQualitySubTaskObject();
                //id
                String guid = UUID.randomUUID().toString();
                dataQualitySubTaskObject.setId(guid);
                //taskId
                dataQualitySubTaskObject.setTaskId(taskId);
                //子任务Id
                dataQualitySubTaskObject.setSubTaskId(subTaskId);
                //对象Id
                dataQualitySubTaskObject.setObjectId(objectId);
                //序号
                dataQualitySubTaskObject.setSequence(i + 1);
                //创建时间
                dataQualitySubTaskObject.setCreateTime(currentTime);
                //更新时间
                dataQualitySubTaskObject.setUpdateTime(currentTime);
                //是否已删除
                dataQualitySubTaskObject.setDelete(false);
                taskManageDAO.addDataQualitySubTaskObject(dataQualitySubTaskObject);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加任务失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDataQualitySubTaskRule(String subTaskId, Timestamp currentTime, List<TaskInfo.SubTaskRule> subTaskRuleList, String tenantId) throws AtlasBaseException {
        try {
            for (int i = 0, size = subTaskRuleList.size(); i < size; i++) {
                TaskInfo.SubTaskRule rule = subTaskRuleList.get(i);
                DataQualitySubTaskRule subTaskRule = new DataQualitySubTaskRule();
                //id
                String guid = UUID.randomUUID().toString();
                subTaskRule.setId(guid);
                //子任务Id
                subTaskRule.setSubTaskId(subTaskId);
                //规则Id
                subTaskRule.setRuleId(rule.getRuleId());
                //校验类型
                subTaskRule.setCheckType(rule.getCheckType());
                //校验表达式
                subTaskRule.setCheckExpression(rule.getCheckExpression());
                //校验阈值
                subTaskRule.setCheckThresholdMinValue(rule.getCheckThresholdMinValue());
                subTaskRule.setCheckThresholdMaxValue(rule.getCheckThresholdMaxValue());
                //order
                subTaskRule.setSequence(i + 1);

                List<TaskInfo.Warning> warningList = rule.getWarnings();
                if (Objects.nonNull(warningList)) {
                    for (TaskInfo.Warning warning : warningList) {
                        Integer warningType = warning.getWarningType();
                        if (1 == warningType) {
                            //橙色校验类型
                            Integer warningCheckType = warning.getWarningCheckType();
                            subTaskRule.setOrangeCheckType(warningCheckType);
                            //橙色校验表达式
                            Integer warningCheckExpression = warning.getWarningCheckExpression();
                            subTaskRule.setOrangeCheckExpression(warningCheckExpression);
                            //橙色阈值
                            subTaskRule.setOrangeThresholdMinValue(warning.getWarningCheckThresholdMinValue());
                            subTaskRule.setOrangeThresholdMaxValue(warning.getWarningCheckThresholdMaxValue());
                        } else if (2 == warningType) {
                            //红色校验类型
                            Integer warningCheckType = warning.getWarningCheckType();
                            subTaskRule.setRedCheckType(warningCheckType);
                            //红色校验表达式
                            Integer warningCheckExpression = warning.getWarningCheckExpression();
                            subTaskRule.setRedCheckExpression(warningCheckExpression);
                            //红色阈值
                            subTaskRule.setRedThresholdMinValue(warning.getWarningCheckThresholdMinValue());
                            subTaskRule.setRedThresholdMaxValue(warning.getWarningCheckThresholdMaxValue());
                        }
                    }
                }
                //创建时间
                subTaskRule.setCreateTime(currentTime);
                //更新时间
                subTaskRule.setUpdateTime(currentTime);
                //是否已删除
                subTaskRule.setDelete(false);
                String unit = taskManageDAO.getTaskRuleUnit(rule.getRuleId(), tenantId);
                subTaskRule.setCheckThresholdUnit(unit);
                taskManageDAO.addDataQualitySubTaskRule(subTaskRule);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加子任务规则失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTask(DataQualityTask taskInfo) throws AtlasBaseException {
        Timestamp currentTime = DateUtils.currentTimestamp();
        try {
            String userId = AdminUtils.getUserData().getUserId();
            taskInfo.setUpdateTime(currentTime);
            taskInfo.setUpdater(userId);
            taskManageDAO.updateTaskInfo(taskInfo);
            taskManageDAO.deleteWarningGroupUsedByTaskId(taskInfo.getId());
            if (taskInfo.getContentWarningNotificationIdList() != null && taskInfo.getContentWarningNotificationIdList().size() != 0) {
                taskManageDAO.addTaskWarningGroup(taskInfo.getId(), WarningType.WARNING.code, taskInfo.getContentWarningNotificationIdList());
            }
            if (taskInfo.getExecutionWarningNotificationIdList() != null && taskInfo.getExecutionWarningNotificationIdList().size() != 0) {
                taskManageDAO.addTaskWarningGroup(taskInfo.getId(), WarningType.ERROR.code, taskInfo.getExecutionWarningNotificationIdList());
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新任务失败");
        }

    }

    public EditionTaskInfo getTaskInfo(String taskId, String tenantId) throws AtlasBaseException {
        try {
            EditionTaskInfo info = taskManageDAO.getTaskInfo(taskId);
            List<EditionTaskInfo.SubTask> subTaskList = taskManageDAO.getSubTaskInfo(taskId);
            for (EditionTaskInfo.SubTask subTask : subTaskList) {
                String subTaskId = subTask.getSubTaskId();
                List<EditionTaskInfo.ObjectInfo> objectInfoList = taskManageDAO.getSubTaskRelatedObject(subTaskId);
                List<EditionTaskInfo.SubTaskRule> subTaskRuleList = taskManageDAO.getSubTaskRule(subTaskId, tenantId);
                String sparkConfig = taskManageDAO.geSparkConfig(subTaskId);
                if (sparkConfig != null && sparkConfig.length() != 0) {
                    Map<String, Integer> configMap = GsonUtils.getInstance().fromJson(sparkConfig, new TypeToken<Map<String, Integer>>() {
                    }.getType());
                    subTask.setConfig(configMap);
                }
                subTask.setSubTaskRuleList(subTaskRuleList);

                //获取规则类型
                EditionTaskInfo.SubTaskRule subTaskRule = subTask.getSubTaskRuleList().get(0);
                Rule rule = ruleService.getById(subTaskRule.getRuleId(), tenantId);
                subTask.setScope(rule.getScope());
                subTask.setType(rule.getType());
                int dataSourceType = rule.getScope();
                for (EditionTaskInfo.ObjectInfo objectInfo : objectInfoList) {
                    String objectId = objectInfo.getObjectId().toString();
                    if (0 == dataSourceType) {
                        //添加数据源名字
                        CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                        String dataSourceId = paramInfo.getDataSourceId();
                        String dataSourceName = getDataSourceName(dataSourceId);
                        paramInfo.setDataSourceName(dataSourceName);
                        objectInfo.setObjectId(paramInfo);
                    } else if (1 == dataSourceType) {
                        //添加数据源名字
                        CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                        String dataSourceId = paramInfo.getDataSourceId();
                        String dataSourceName = getDataSourceName(dataSourceId);
                        paramInfo.setDataSourceName(dataSourceName);
                        objectInfo.setObjectId(paramInfo);
                    } else if (2 == dataSourceType) {
                        TaskType taskType = TaskType.getTaskByCode(rule.getType());
                        if (TaskType.CONSISTENCY.equals(taskType)) {
                            List<ConsistencyParam> lists = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                            }.getType());
                            lists.forEach(param -> param.setDataSourceName(getDataSourceName(param.getDataSourceId())));
                            objectInfo.setObjectId(lists);
                        } else if (TaskType.CUSTOMIZE.equals(taskType)) {
                            List<CustomizeParam> lists = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<CustomizeParam>>() {
                            }.getType());
                            lists.forEach(param -> param.setDataSourceName(getDataSourceName(param.getDataSourceId())));
                            objectInfo.setObjectId(lists);
                        } else {
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                        }
                    } else {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                    }
                }
                subTask.setObjectIdList(objectInfoList);

            }
            info.setTaskList(subTaskList);
            List<EditionTaskInfo.WarningGroup> contentWarningGroup = taskManageDAO.getWarningGroup(taskId, 0);
            List<EditionTaskInfo.WarningGroup> errorWarningGroup = taskManageDAO.getWarningGroup(taskId, 1);
            info.setContentWarningNotificationIdList(contentWarningGroup);
            info.setExecutionWarningNotificationIdList(errorWarningGroup);


            return info;
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务信息失败");
        }
    }

    public String getDataSourceName(String dataSourceId) {
        if (dataSourceId == null || dataSourceId.length() == 0 || QuartzJob.hiveId.equals(dataSourceId)) {
            return QuartzJob.hiveId;
        } else {
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(dataSourceId);
            if (dataSourceInfo == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源不存在或已删除，请修改任务");
            }
            return dataSourceInfo.getSourceName();
        }
    }

    public DataQualityBasicInfo getTaskBasicInfo(String guid) throws AtlasBaseException {
        try {
            DataQualityBasicInfo basicInfo = taskManageDAO.getTaskBasicInfo(guid);
            String qrtzName = taskManageDAO.getQrtzJobByTaskId(guid);
            if (Objects.nonNull(qrtzName)) {
                String triggerName = TRIGGER_NAME + qrtzName;
                String triggerGroupName = TRIGGER_GROUP_NAME + qrtzName;

                Date lastExecuteTime = quartzManager.getJobLastExecuteTime(triggerName, triggerGroupName);
                Date nextExecuteTime = quartzManager.getJobNextExecuteTime(triggerName, triggerGroupName);
                if (Objects.nonNull(lastExecuteTime)) {
                    basicInfo.setLastExecuteTime(new Timestamp(lastExecuteTime.getTime()));
                }
                if (Objects.nonNull(nextExecuteTime)) {
                    basicInfo.setNextExecuteTime(new Timestamp(nextExecuteTime.getTime()));
                }
            }
            return basicInfo;
        } catch (Exception e) {
            LOG.error("获取任务信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务信息失败");
        }
    
    }
    
    public void enableTask(String taskId) throws AtlasBaseException {
        try {
            String qrtzName = taskManageDAO.getQrtzJobByTaskId(taskId);
            if (Objects.isNull(qrtzName) || qrtzName.trim().length() == 0) {
                addQuartzJob(taskId);
            } else {
                String jobGroupName = JOB_GROUP_NAME + qrtzName;
                String triggerName = TRIGGER_NAME + qrtzName;
                String triggerGroupName = TRIGGER_GROUP_NAME + qrtzName;
                if (quartzManager.checkExists(triggerName, triggerGroupName)) {
                    DataQualityTask task = taskManageDAO.getQrtzInfoByTemplateId(taskId);
                    String cron = task.getCronExpression();
                    quartzManager.modifyJobTime(qrtzName, jobGroupName, triggerName, triggerGroupName, cron);  //任务重新开启后检查cron是否有变化，如果有需要重新刷新trigger
                    quartzManager.resumeJob(qrtzName, jobGroupName);
                } else {
                    addQuartzJob(taskId, qrtzName);
                }
                
            }
            //设置任务状态为【启用】
            taskManageDAO.updateTaskEnableStatus(taskId, true);
        } catch (Exception e) {
            LOG.error("开启任务失败", e);
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "开启任务失败");
        }
    }
    
    public void disableTask(String taskId) throws AtlasBaseException {
        try {
            String jobName = taskManageDAO.getQrtzJobByTaskId(taskId);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            quartzManager.pauseJob(jobName, jobGroupName);
            //设置模板状态为【暂停】
            taskManageDAO.updateTaskEnableStatus(taskId, false);
        } catch (Exception e) {
            LOG.error("关闭模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭模板失败");
        }
    }


    public void startTaskNow(String taskId) throws AtlasBaseException {
        if (QuartzJob.STATE_MAP.containsKey(taskId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务正在执行中");
        }
        try {
            String jobName = taskManageDAO.getQrtzJobByTaskId(taskId);
            String jobGroupName = JOB_GROUP_NAME + System.currentTimeMillis();
            quartzManager.addSimpleJob(jobName, jobGroupName, QuartzJob.class);
            long waitTime = 0L;
            long taskStartExpire = conf.getLong("task_start_expire", 10) * 1000;
            long step = 50;
            while (true) {
                waitTime += step;
                Thread.sleep(step);
                if (QuartzJob.STATE_MAP.containsKey(taskId)) {
                    // TODO
                    break;
                }
                if (waitTime > taskStartExpire) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "启动任务超时,请重新尝试");
                }
            }
        } catch (AtlasBaseException e) {
            LOG.error("启动任务失败", e);
            throw e;
        } catch (Exception e) {
            LOG.error("启动任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "启动任务失败");
        }
    }

    public void stopTaskNow(String taskId) throws AtlasBaseException, InterruptedException {

        if (!QuartzJob.STATE_MAP.containsKey(taskId)) {
            Integer taskStatus = taskManageDAO.getTaskStatus(taskId);
            if (taskStatus == null || taskStatus != 1) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "启动没有运行");
            }
            taskManageDAO.updateTaskStatus(taskId, 4);
        } else {
            //强制停止任务
            QuartzJob.STATE_MAP.put(taskId, true);

            long waitTime = 0L;
            long taskStartExpire = conf.getLong("task_cancel_expire", 15) * 1000;
            long step = 10;
            while (QuartzJob.STATE_MAP.containsKey(taskId)) {
                waitTime += step;
                if (taskStartExpire < waitTime) {
                    QuartzJob.STATE_MAP.put(taskId, false);
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "退出任务超时");
                }
                Thread.sleep(step);
            }
        }
    }

    public void addQuartzJob(String taskId) throws AtlasBaseException {
        long currentTime = System.currentTimeMillis();
        String jobName = String.valueOf(currentTime);
        addQuartzJob(taskId, jobName);
        //添加qrtzName
        taskManageDAO.updateTaskQrtzName(taskId, jobName);
    }


    public void addQuartzJob(String taskId, String jobName) throws AtlasBaseException {
        try {
            String jobGroupName = JOB_GROUP_NAME + jobName;
            String triggerName = TRIGGER_NAME + jobName;
            String triggerGroupName = TRIGGER_GROUP_NAME + jobName;
            DataQualityTask task = taskManageDAO.getQrtzInfoByTemplateId(taskId);
            String cron = task.getCronExpression();
            Timestamp startTime = task.getStartTime();
            Timestamp endTime = task.getEndTime();
            Integer level = task.getLevel();
            if (Objects.nonNull(cron)) {
                CronExpression cronExpression = new CronExpression(cron);
                if (cronExpression.getNextValidTimeAfter(startTime).after(endTime)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "执行时间段内任务永远不会触发");
                }
                quartzManager.addCronJobWithTimeRange(jobName, jobGroupName, triggerName, triggerGroupName, QuartzJob.class, cron, level, startTime, endTime);
            } else {
                quartzManager.addSimpleJob(jobName, jobGroupName, QuartzJob.class);
            }

        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加任务失败");
        }
    }

    public PageResult<TaskRuleHeader> getRuleList(String taskId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult();
            EditionTaskInfo taskInfo = taskManageDAO.getTaskInfo(taskId);
            List<TaskRuleHeader> lists = taskManageDAO.getRuleList(taskId, parameters, taskInfo.getTenantId());
            Map<String, String> ruleTemplateCategoryMap = new HashMap();
            RuleTemplateType.all().stream().forEach(ruleTemplateType -> {
                ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
            });
            for (TaskRuleHeader rule : lists) {
                String categoryId = taskManageDAO.getRuleTypeCodeByRuleId(rule.getRuleId(), taskInfo.getTenantId());
                String typeName = ruleTemplateCategoryMap.get(categoryId);
                rule.setTypeName(typeName);
            }
            long totalSize = taskManageDAO.countRuleList(taskId, taskInfo.getTenantId());
            pageResult.setLists(lists);
            pageResult.setCurrentSize(lists.size());
            pageResult.setTotalSize(totalSize);

            return pageResult;
        } catch (Exception e) {
            LOG.error("获取任务规则列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务规则列表失败");
        }
    }

    public TaskExecutionReport getTaskExecutionReport(String taskId) throws AtlasBaseException {
        try {
            TaskExecutionReport taskExecutionInfo = taskManageDAO.getTaskExecutionInfo(taskId);
            List<TaskExecutionReport.ExecutionRecord> executionRecordList = taskManageDAO.getTaskExecutionRecord(taskId);
            if (!CollectionUtils.isEmpty(executionRecordList)) {
                executionRecordList.stream().forEach(r -> {
                    if (r.getErrorCount() != null && r.getErrorCount() != 0) {
                        r.setCheckResult("异常");
                    } else if (r.getGeneralWarningCount() != null && r.getGeneralWarningCount() != 0) {
                        r.setCheckResult("不合格");
                    } else {
                        r.setCheckResult("合格");
                    }
                });
            }
            taskExecutionInfo.setExecutionRecordList(executionRecordList);
            return taskExecutionInfo;
        } catch (Exception e) {
            LOG.error("获取报告详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告详情失败");
        }
    }

    public ExecutionReportData getTaskReportData(String taskId, String taskExecuteId, String subtaskId, String tenantId) throws AtlasBaseException {
        try {
            ExecutionReportData resultData = new ExecutionReportData();
            //basicInfo
            ExecutionReportData.TaskBasicInfo basicInfo = taskManageDAO.getTaskExecutionBasicInfo(taskId, taskExecuteId);
            resultData.setBasicInfo(basicInfo);
            //checkStatus
            ExecutionReportData.TaskCheckResultCount checkResultCount = new ExecutionReportData.TaskCheckResultCount();
            //查询一个子任务或者All
            long tableRulePassedNumber = taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 0, 0, tenantId, subtaskId);
            long tableRuleNoPassedNumber = taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 0, 1, tenantId, subtaskId);
            long columnRulePassedNumber = taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 1, 0, tenantId, subtaskId);
            long columnRuleNoPassedNumber = taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 1, 1, tenantId, subtaskId);
            checkResultCount.setTableRulePassedNumber(tableRulePassedNumber);
            checkResultCount.setTableRuleNoPassedNumber(tableRuleNoPassedNumber);
            checkResultCount.setColumnRulePassedNumber(columnRulePassedNumber);
            checkResultCount.setColumnRuleNoPassedNumber(columnRuleNoPassedNumber);
            //error
            List<Integer> errorList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 0, subtaskId);
            if (null != errorList && errorList.size() > 0) {
                long errorNumber = errorList.stream().filter(status -> status == 2).count();
                checkResultCount.setErrorRuleNumber(errorNumber);
                checkResultCount.setTotalRuleNumber(errorList.size());
            } else {
                checkResultCount.setErrorRuleNumber(0);
                checkResultCount.setTotalRuleNumber(0);
            }
            //orangeWarning
            List<Integer> checkOrangeWarningStatusList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 1, subtaskId);
            if (null != checkOrangeWarningStatusList && checkOrangeWarningStatusList.size() > 0) {
                long warningNumber = checkOrangeWarningStatusList.stream().filter(status -> status == 1).count();
                checkResultCount.setOrangeWarningNumber(warningNumber);
                checkResultCount.setTotalOrangeWarningRuleNumber(checkOrangeWarningStatusList.size());
            } else {
                checkResultCount.setOrangeWarningNumber(0);
                checkResultCount.setTotalOrangeWarningRuleNumber(0);
            }
            //redWarning
            List<Integer> checkRedWarningStatusList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 2, subtaskId);
            if (null != checkRedWarningStatusList && checkRedWarningStatusList.size() > 0) {
                long warningNumber = checkRedWarningStatusList.stream().filter(status -> status == 1).count();
                checkResultCount.setRedWarningNumber(warningNumber);
                checkResultCount.setTotalRedWarningRuleNumber(checkRedWarningStatusList.size());
            } else {
                checkResultCount.setRedWarningNumber(0);
                checkResultCount.setTotalRedWarningRuleNumber(0);
            }
            resultData.setCheckResultCount(checkResultCount);

            //result && suggestion
            ExecutionReportData.ImprovingSuggestion suggestion = new ExecutionReportData.ImprovingSuggestion();
            List<String> tableRuleSuggestion = new ArrayList<>();
            List<String> columnRuleSuggestion = new ArrayList<>();
            String suffix = "的结果不符合预期，请及时处理";
            String noEnd = "未执行完毕，请稍候再次进行下载";
            List<SubTaskRecord> executeResult = getTaskRuleExecutionRecordList(taskExecuteId, subtaskId, tenantId);
            for (SubTaskRecord subTaskRecord : executeResult) {
                for (TaskRuleExecutionRecord record : subTaskRecord.getTaskRuleExecutionRecords()) {
                    if (0 == record.getScope() && record.getCheckStatus() == null) {
                        tableRuleSuggestion.add(record.getObjectName() + noEnd);
                    } else if (1 == record.getScope() && record.getCheckStatus() == null) {
                        columnRuleSuggestion.add(record.getObjectName() + noEnd);
                    } else if (0 == record.getScope() && 1 == record.getCheckStatus()) {
                        tableRuleSuggestion.add(record.getObjectName() + suffix);
                    } else if (1 == record.getScope() && 1 == record.getCheckStatus()) {
                        columnRuleSuggestion.add(record.getObjectName() + suffix);
                    }
                    Integer sequence = taskManageDAO.getSubTaskSequence(record.getSubtaskId());
                    record.setSubTaskSequence(sequence);
                }
            }
            resultData.setRuleCheckResult(executeResult);
            //suggestion
            suggestion.setTableQuestion(tableRuleSuggestion);
            suggestion.setColumnQuestion(columnRuleSuggestion);
            resultData.setSuggestion(suggestion);
            return resultData;
        } catch (Exception e) {
            LOG.error("获取报告详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告详情失败");
        }
    }

    public List<SubTaskRecord> getTaskRuleExecutionRecordList(String executionId, String subtaskId, String tenantId) throws AtlasBaseException {
        try {
            Map<String, SubTaskRecord> map = new LinkedHashMap<>(); //配置的子任务要满足顺序一致，使用LinkedHashMap保证，且查询sql要保证有序性
            List<TaskRuleExecutionRecord> list = taskManageDAO.getTaskRuleExecutionRecordList(executionId, subtaskId, tenantId);
            Boolean filing = taskManageDAO.getFilingStatus(executionId) == 0 ? false : true;
            for (TaskRuleExecutionRecord record : list) {
                record.setFiling(filing);

                setResultString(record);

                if (map.containsKey(record.getSubtaskId())) {
                    map.get(record.getSubtaskId()).getTaskRuleExecutionRecords().add(record);
                } else {
                    SubTaskRecord subTaskRecord = new SubTaskRecord(record);
                    subTaskRecord.setSequence(taskManageDAO.getSubTaskSequence(record.getSubtaskId()));
                    subTaskRecord.getTaskRuleExecutionRecords().add(record);
                    map.put(record.getSubtaskId(), subTaskRecord);
                }
                String objectId = record.getObjectId();
                if (0 == record.getScope()) {
                    CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                    String dataSourceName = getDataSourceName(paramInfo.getDataSourceId());
                    record.setDataSourceName(dataSourceName);
                    record.setDbName(paramInfo.getSchema());
                    record.setTableName(paramInfo.getTable());
                    record.setObjectName(paramInfo.getTable());
                    record.setTableId(paramInfo.getTable());
                } else if (1 == record.getScope()) {
                    CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                    String dataSourceName = getDataSourceName(paramInfo.getDataSourceId());
                    record.setDataSourceName(dataSourceName);
                    record.setDbName(paramInfo.getSchema());
                    record.setTableName(paramInfo.getTable());
                    record.setObjectName(paramInfo.getColumn());
                    record.setTableId(paramInfo.getTable());
                } else if (2 == record.getScope()) {
                    TaskType taskType = TaskType.getTaskByCode(record.getTaskType());
                    if (TaskType.CONSISTENCY.equals(taskType)) {
                        //一致性规则展示目标表结果，对比几张表就展示几个处理行为
                        List<ConsistencyParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                        }.getType());
                        List<TaskRuleExecutionRecord> records = params
                                .stream()
                                .filter(p -> !p.isStandard())   //过滤掉基础表
                                .map(param -> {
                                    String dataSourceName = getDataSourceName(param.getDataSourceId()); //获取数据源
                                    String tableId = param.getId(); //对比表ID
                                    TaskRuleExecutionRecord taskRuleExecutionRecord = new TaskRuleExecutionRecord(record);
                                    taskRuleExecutionRecord.setDbName(param.getSchema());
                                    taskRuleExecutionRecord.setTableName(param.getTable());
                                    taskRuleExecutionRecord.setDataSourceName(dataSourceName);
                                    taskRuleExecutionRecord.setTableId(tableId);
                                    String fileName = LivyTaskSubmitHelper.getOutName(tableId);
                                    String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(taskRuleExecutionRecord.getRuleExecutionId(), taskRuleExecutionRecord.getCreateTime().getTime(), fileName);
                                    HdfsUtils hdfsUtils = new HdfsUtils();
                                    int fileLine = 0;
                                    try {
                                        fileLine = hdfsUtils.getFileLine(hdfsOutPath);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    taskRuleExecutionRecord.setResult(Float.valueOf(fileLine));
                                    return taskRuleExecutionRecord;
                                }).collect(Collectors.toList());
                        map.get(record.getSubtaskId()).setTaskRuleExecutionRecords(records);
                    } else if (TaskType.CUSTOMIZE.equals(taskType)) {
                        List<CustomizeParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<CustomizeParam>>() {
                        }.getType());
                        List<TaskRuleExecutionRecord> resultList = new ArrayList<>();
                        //取出自定义规则的数据源(保存在其参数字符串中)
                        if (params != null && params.size() > 0) {
                            for (CustomizeParam customizeParam : params) {
                                //自定义规则，只展示规则名称等基础信息，且只有一条记录
                                TaskRuleExecutionRecord taskRuleExecutionRecord = new TaskRuleExecutionRecord(record);
                                String dataSourceName = getDataSourceName(customizeParam.getDataSourceId()); //获取数据源
                                taskRuleExecutionRecord.setDataSourceName(dataSourceName);
                                taskRuleExecutionRecord.setDbName(customizeParam.getSchema());
                                taskRuleExecutionRecord.setTableName(customizeParam.getTable());
                                taskRuleExecutionRecord.setObjectName(customizeParam.getColumn());
                                taskRuleExecutionRecord.setTableId(customizeParam.getTable());
                                resultList.add(taskRuleExecutionRecord);
                            }
                        }
                        map.get(record.getSubtaskId()).setTaskRuleExecutionRecords(resultList);
                    }
                }
            }
            return new ArrayList<>(map.values());
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取报告规则详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告规则详情失败");
        }
    }

    private void setResultString(TaskRuleExecutionRecord record) {
        String result = "";
        if (record.getResult() != null) {
            if ("%".equals(record.getCheckThresholdUnit())) {
                result = String.format("%.2f", record.getResult() / 100f);
            } else {
                result = String.format("%.0f", record.getResult());
            }
            if (null != record.getCheckThresholdUnit()) {
                result = result + record.getCheckThresholdUnit();
            }
        }
        record.setResultString(result);
    }

    public PageResult<ExecutionLogHeader> getExecutionLogList(String taskId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult();
            List<ExecutionLogHeader> lists = taskManageDAO.getExecutionLogList(taskId, parameters);
            Long count = taskManageDAO.countExecutionLogList(taskId, parameters);
            pageResult.setLists(lists);
            pageResult.setCurrentSize(lists.size());
            pageResult.setTotalSize(count);
            return pageResult;

        } catch (Exception e) {
            LOG.error("获取任务列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务列表失败");
        }
    }

    public ExecutionLog getExecutionLogList(String ruleExecutionId) throws AtlasBaseException {
        try {
            ExecutionLog logInfo = taskManageDAO.getExecutionInfo(ruleExecutionId);
            List<String> errorList = taskManageDAO.getRuleExecutionLog(ruleExecutionId);
            logInfo.setMsg(errorList);
            return logInfo;

        } catch (Exception e) {
            LOG.error("获取报告详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告详情失败");
        }
    }

    public List<Queue> getPools(String tenantId) throws AtlasBaseException {
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
        Pool pools = tenantService.getPools(tenantId);
        if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
            return pools.getImpala();
        } else {
            return pools.getHive();
        }
    }

    public PageResult getDataSourceList(Parameters parameters, String tenantId) throws AtlasBaseException {
        PageResult dataSourceList = dataShareService.getDataSourceList(parameters, null, tenantId);
        DataSourceHead hive = new DataSourceHead();
        if (parameters.getOffset() == 0) {
            hive.setSourceType("hive");
            hive.setSourceName("hive");
            hive.setSourceId("hive");
            dataSourceList.getLists().add(hive);
        }
        return dataSourceList;
    }

    public List<RuleHeader> searchRules(Parameters parameters, String scope, String tenantId) throws AtlasBaseException {
        Integer objType = null;
        if (!"all".equals(scope)) {
            objType = Integer.valueOf(scope);
        }
        String query = parameters.getQuery();
        if (query != null) {
            parameters.setQuery(query.replaceAll("%", "/%").replaceAll("_", "/_"));
        }
        List<RuleHeader> ruleList = taskManageDAO.searchRuleList(parameters, objType, tenantId);
        return ruleList;
    }

    public List<String> schedulePreview(Schedule schedule) throws AtlasBaseException {
        return quartzManager.schedulePreview(schedule);
    }

    public boolean checkPreview(Schedule schedule) throws AtlasBaseException {
        return quartzManager.checkSchedule(schedule);
    }

    public ErrorData getErrorData(Parameters parameters, String ruleExecutionId, String tableId, String tenantId) throws IOException {
        ErrorData errorData = new ErrorData();
        TaskRuleExecutionRecord record = taskManageDAO.getTaskRuleExecutionRecord(ruleExecutionId, tenantId);
        if (record == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "执行结果不存在");
        }
        String objectId = record.getObjectId();
        HdfsUtils hdfsUtils = new HdfsUtils();
        if (0 == record.getScope()) {
            CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
            String dataSourceName = getDataSourceName(paramInfo.getDataSourceId());
            errorData.setDataSourceName(dataSourceName);
            errorData.setDbName(paramInfo.getSchema());
            errorData.setTableName(paramInfo.getTable());
            errorData.setDataSourceId(paramInfo.getDataSourceId());
        } else if (1 == record.getScope()) {
            CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
            String dataSourceName = getDataSourceName(paramInfo.getDataSourceId());
            errorData.setDataSourceName(dataSourceName);
            errorData.setDbName(paramInfo.getSchema());
            errorData.setTableName(paramInfo.getTable());
            errorData.setDataSourceId(paramInfo.getDataSourceId());
            errorData.setColumnName(paramInfo.getColumn());
        } else if (2 == record.getScope()) {
            TaskType taskType = TaskType.getTaskByCode(record.getTaskType());
            if (TaskType.CONSISTENCY.equals(taskType)) {
                List<ConsistencyParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                }.getType());
                params.forEach(param -> {
                    if (param.getId().equals(tableId)) {
                        String dataSourceName = getDataSourceName(param.getDataSourceId());
                        errorData.setDataSourceName(dataSourceName);
                        errorData.setDbName(param.getSchema());
                        errorData.setTableName(param.getTable());
                        errorData.setDataSourceId(param.getDataSourceId());
                    }
                });
                String fileName = LivyTaskSubmitHelper.getOutName(tableId);
                String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(ruleExecutionId, record.getCreateTime().getTime(), fileName);
                List<String> list = errorDataCache.getIfPresent(hdfsOutPath);
                if (list == null) {
                    list = hdfsUtils.exists(hdfsOutPath) ? hdfsUtils.catFile(hdfsOutPath, errorDataSize) : new ArrayList<>(); //无输出的规则或者执行异常的任务HDFS上是不会有输出结果的，返回空数据
                }
                errorDataCache.put(hdfsOutPath, list);
                getDataByList(list, parameters, errorData);
                return errorData;
            }
        }
        String fileName = LivyTaskSubmitHelper.getOutName("data");
        String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(ruleExecutionId, record.getCreateTime().getTime(), fileName);
        List<String> list = errorDataCache.getIfPresent(hdfsOutPath);
        if (list == null) {
            list = hdfsUtils.exists(hdfsOutPath) ? hdfsUtils.catFile(hdfsOutPath, errorDataSize) : new ArrayList<>(); //无输出的规则或者执行异常的任务HDFS上是不会有输出结果的，返回空数据
        }
        errorDataCache.put(hdfsOutPath, list);
        getDataByList(list, parameters, errorData);
        return errorData;
    }

    public File exportExcelErrorData(String executionId, String subTaskId, String tenantId) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        List<TaskRuleExecutionRecord> records = taskManageDAO.getTaskRuleExecutionRecords(executionId, subTaskId, tenantId);
        Integer subTaskSequence = taskManageDAO.getSubTaskSequence(subTaskId);
        File tmpFile = new File("/tmp/metaspace/" + executionId, "子任务" + subTaskSequence + ".xlsx");

        if (tmpFile.exists()) {
            return tmpFile;
        }

        for (TaskRuleExecutionRecord record : records) {
            String dataSourceName = "hive";
            String sheetName;
            TaskType taskType = TaskType.getTaskByCode(record.getTaskType());
            List<String> list;
            String objectId = record.getObjectId();
            HdfsUtils hdfsUtils = new HdfsUtils();
            if (record.getScope() != 2) { //内置规则
                String fileName = LivyTaskSubmitHelper.getOutName("data");
                String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(record.getRuleExecutionId(), record.getCreateTime().getTime(), fileName);
                list = hdfsUtils.exists(hdfsOutPath) ? hdfsUtils.catFile(hdfsOutPath, -1) : null;
                CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);

                if (paramInfo.getDataSourceId() != null && !"hive".equals(paramInfo.getDataSourceId())) {
                    dataSourceName = dataSourceDAO.getDataSourceInfo(paramInfo.getDataSourceId()).getSourceName();
                }

                //内置规则的sheetName应该以   规则名称+数据源名称 + schemaName + tableName + columnName 确保唯一性（sheetName唯一）
                sheetName = record.getRuleName() + "-" + dataSourceName + "-" + paramInfo.getSchema() + "-" + paramInfo.getTable() + "-" + paramInfo.getColumn();
                if (list == null || list.size() == 0) {
                    continue;
                }

            } else if (!TaskType.CONSISTENCY.equals(taskType)) { // 自定义
                String fileName = LivyTaskSubmitHelper.getOutName("data");
                String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(record.getRuleExecutionId(), record.getCreateTime().getTime(), fileName);
                list = hdfsUtils.exists(hdfsOutPath) ? hdfsUtils.catFile(hdfsOutPath, -1) : null;
                List<CustomizeParam> paramInfo = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<CustomizeParam>>() {
                }.getType());
                sheetName = record.getRuleName() + "-" + paramInfo != null ? paramInfo.get(0).getDataSourceName() : "";
                if (list == null || list.size() == 0) {
                    continue;
                }
            } else { //一致性规则,基于tableID(对标表)下载对比结果
                List<ConsistencyParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                }.getType());
                for (ConsistencyParam param : params) {
                    String tableId = param.getId();
                    String fileName = LivyTaskSubmitHelper.getOutName(tableId);
                    if (param.getDataSourceId() != null && !"hive".equals(param.getDataSourceId())) {  //查询数据源名称，接口传输内容需要优化
                        dataSourceName = dataSourceDAO.getDataSourceInfo(param.getDataSourceId()).getSourceName();
                    }
                    String name = dataSourceName + "-" + param.getSchema() + "-" + param.getTable() + "-" + param.getCompareFields().stream().collect(Collectors.joining(","));
                    String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(record.getRuleExecutionId(), record.getCreateTime().getTime(), fileName);
                    try {
                        List<String> lineList = hdfsUtils.exists(hdfsOutPath) ? hdfsUtils.catFile(hdfsOutPath, -1) : null;
                        List<Map<String, Object>> data = null;
                        if (lineList != null && lineList.size() > 0) { // 有数据，生成execl数据
                            data = lineList.stream().map(line -> {
                                Map<String, Object> map = GsonUtils.getInstance().fromJson(line, new TypeToken<Map<String, Object>>() {
                                }.getType());
                                return map;
                            }).collect(Collectors.toList());
                        }
                        if (data != null && data.size() > 0) {
                            List<List<String>> values = data.stream().map(map -> {
                                List<Object> arrayList = new ArrayList();
                                arrayList.addAll(map.values());
                                List<String> valueList = arrayList.stream().map(object -> object.toString()).collect(Collectors.toList());
                                return valueList;
                            }).collect(Collectors.toList());
                            PoiExcelUtils.createSheet(workbook, name, new ArrayList<>(data.get(0).keySet()), values);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                break;
            }

            List<Map<String, Object>> data = null;
            if (!CollectionUtils.isEmpty(list)) {
                data = list.stream().map(line -> {
                    Map<String, Object> map = GsonUtils.getInstance().fromJson(line, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    return map;
                }).collect(Collectors.toList());
            }
            if (!CollectionUtils.isEmpty(data)) {
                List<List<String>> values = data.stream().map(map -> {
                    List<Object> arrayList = new ArrayList();
                    arrayList.addAll(map.values());
                    List<String> valueList = arrayList.stream().map(object -> object.toString()).collect(Collectors.toList());
                    return valueList;
                }).collect(Collectors.toList());
                PoiExcelUtils.createSheet(workbook, sheetName, new ArrayList<>(data.get(0).keySet()), values);
            }
        }
        //无数据结果，添加一个空的sheet页，增强健壮性
        if (workbook.getNumberOfSheets() == 0) {
            workbook.createSheet();
        }
        return workbook2file(workbook, "子任务" + subTaskSequence, executionId);
    }

    private File workbook2file(Workbook workbook, String name, String id) throws IOException {
        File tmpFile = new File("/tmp/metaspace", id);
        tmpFile.mkdirs();
        File dataFile = new File(tmpFile, name + ".xlsx");
        try (FileOutputStream output = new FileOutputStream(dataFile)) {
            workbook.write(output);
            output.flush();
            output.close();
        }
        return dataFile;
    }

    public ErrorData getDataByList(List<String> list, Parameters parameters, ErrorData errorData) {
        if (parameters.getOffset() > errorDataSize) {
            errorData.setTotalSize(list.size());
            errorData.setCurrentSize(0);
            errorData.setData(new ArrayList<>());
            return errorData;
        }
        int limit = Math.min(parameters.getLimit(), errorDataSize - parameters.getOffset());
        if (limit == -1) {
            limit = errorDataSize - parameters.getOffset();
        }
        List<Map<String, Object>> data = list.stream().skip(parameters.getOffset()).limit(limit).map(line -> {
            Map<String, Object> map = GsonUtils.getInstance().fromJson(line, new TypeToken<Map<String, Object>>() {
            }.getType());
            return map;
        }).collect(Collectors.toList());
        errorData.setTotalSize(list.size());
        errorData.setCurrentSize(data.size());
        errorData.setData(data);
        return errorData;
    }

    public File downTaskReportData(String executionId, String subTaskId, String tenantId, InputStream fileInputStream, String fileName) throws Exception {
        File tmpFile = new File("/tmp/metaspace", executionId);
        tmpFile.mkdirs();
        File dataFile = new File(tmpFile, fileName);
        FileUtils.copyInputStreamToFile(fileInputStream, dataFile);
        String subTaskName;
        if ("all".equals(subTaskId)) {
            List<String> subTaskIds = taskManageDAO.getSubTaskId(executionId);
            for (String subId : subTaskIds) {
                exportExcelErrorData(executionId, subId, tenantId);
            }
            subTaskName = "all";
        } else {
            exportExcelErrorData(executionId, subTaskId, tenantId);
            Integer subTaskSequence = taskManageDAO.getSubTaskSequence(subTaskId);
            subTaskName = "子任务" + subTaskSequence + ".xlsx";
        }
        File zipFile = new File("/tmp/metaspace", executionId + subTaskId);
        zipFile.mkdirs();
        File file = new File(zipFile, "任务执行结果.zip");
        FileOutputStream fos1 = new FileOutputStream(file);
        toZip(tmpFile.getPath(), fos1, subTaskName, fileName);
        return file;
    }


    /**
     * 递归压缩方法
     *
     * @param sourceFile  源文件
     * @param zos         zip输出流
     * @param subTaskName 压缩后的名称
     *                    false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */

    private void compress(File sourceFile, ZipOutputStream zos,
                          String subTaskName, String fileName) throws Exception {
        byte[] buf = new byte[100 * 1024];
        File[] listFiles = sourceFile.listFiles();
        for (File file : listFiles) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            if (!"all".equals(subTaskName) && !file.getName().contains(subTaskName) && !file.getName().contains(fileName)) {
                continue;
            }
            zos.putNextEntry(new ZipEntry(file.getName()));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(file);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        }
    }

    public void toZip(String srcDir, OutputStream out, String subTaskName, String fileName)
            throws RuntimeException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, subTaskName, fileName);
            long end = System.currentTimeMillis();
        } catch (Exception e) {
            throw new RuntimeException("zip error ", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
