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

import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.dataquality2.DataQualityBasicInfo;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTask;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskObject;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskRule;
import io.zeta.metaspace.model.dataquality2.DataQualityTask;
import io.zeta.metaspace.model.dataquality2.EditionTaskInfo;
import io.zeta.metaspace.model.dataquality2.ExecutionLog;
import io.zeta.metaspace.model.dataquality2.ExecutionLogHeader;
import io.zeta.metaspace.model.dataquality2.ExecutionReportData;
import io.zeta.metaspace.model.dataquality2.HiveNumericType;
import io.zeta.metaspace.model.dataquality2.ObjectType;
import io.zeta.metaspace.model.dataquality2.RuleHeader;
import io.zeta.metaspace.model.dataquality2.RuleTemplateType;
import io.zeta.metaspace.model.dataquality2.TaskExecutionReport;
import io.zeta.metaspace.model.dataquality2.TaskHeader;
import io.zeta.metaspace.model.dataquality2.TaskInfo;
import io.zeta.metaspace.model.dataquality2.TaskRuleExecutionRecord;
import io.zeta.metaspace.model.dataquality2.TaskRuleHeader;
import io.zeta.metaspace.model.dataquality2.TaskWarningHeader;
import io.zeta.metaspace.model.dataquality2.WarningType;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.task.quartz.QuartzJob;
import io.zeta.metaspace.web.task.quartz.QuartzManager;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskManageService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskManageService.class);
    public static String JOB_GROUP_NAME = "METASPACE_JOBGROUP";
    public static String TRIGGER_NAME = "METASPACE_TRIGGER";
    public static String TRIGGER_GROUP_NAME = "METASPACE_TRIGGERGROUP";

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

    public PageResult<TaskHeader> getTaskList(Integer my, Parameters parameters) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            List<TaskHeader> list = taskManageDAO.getTaskList(my, userId, parameters);

            //long totalSize = taskManageDAO.countTaskList(my, userId, parameters);
            long totalSize = 0;
            if (list.size()!=0){
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

    public PageResult getTableList(String databaseId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<Table> pageResult = metaspaceEntityService.getTableByDB(databaseId, true, parameters.getOffset(), parameters.getLimit());

            List<Table> tableList = pageResult.getLists();
            String dbName = null;
            if(Objects.nonNull(tableList) && tableList.size()>0) {
                Table tmpTable = taskManageDAO.getDbAndTableName(tableList.get(0).getTableId());
                dbName = tmpTable.getDatabaseName();
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

    public PageResult getColumnList(String tableGuid, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<Column> pageResult = businessService.getTableColumnList(tableGuid, parameters, null, null);
            List<Column> columnList = pageResult.getLists();
            for (Column column : columnList) {
                Column tmpColumn = taskManageDAO.getDbAndTableAndColumnName(column.getColumnId());
                column.setDatabaseName(tmpColumn.getDatabaseName());
                column.setTableName(tmpColumn.getTableName());
            }
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取字段列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取字段列表失败");
        }
    }

    public void deleteTaskList(List<String> taskIdList) throws AtlasBaseException {
        try {
            if(Objects.nonNull(taskIdList)) {
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

    public List<RuleHeader> getValidRuleList(String groupId, int objType, List<String> objIdList) throws AtlasBaseException {
        try {
            List<RuleHeader> ruleList = taskManageDAO.getRuleListByCategoryId(groupId, objType);
            if(objIdList==null || objIdList.isEmpty() || ruleList==null) {
                return ruleList;
            }
            if(ObjectType.COLUMN == ObjectType.of(objType)) {
                List<String> columnTypeList = taskManageDAO.getColumnTypeList(objIdList);
                boolean allNumericValue = columnTypeList.stream().allMatch(columnType -> HiveNumericType.isNumericType(columnType));
                List<String> numericTypeTempateIdlist = taskManageDAO.getNumericTypeTemplateRuleId();
                return allNumericValue == true ? ruleList
                        :ruleList.stream().filter(rule -> !numericTypeTempateIdlist.contains(rule.getRuleTemplateId())).collect(Collectors.toList());
            }
            return ruleList;
        }  catch (Exception e) {
            LOG.error("获取规则列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则列表失败");
        }
    }

    public List<TaskWarningHeader.WarningGroupHeader> getWarningGroupList(String groupId) throws AtlasBaseException {
        try {
            return taskManageDAO.getWarningGroupList(groupId);
        } catch (Exception e) {
            LOG.error("获取告警组列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警组列表失败");
        }
    }

    public List<TaskWarningHeader.WarningGroupHeader> getAllWarningGroup() throws AtlasBaseException {
        try {
            return taskManageDAO.getAllWarningGroup();
        } catch (Exception e) {
            LOG.error("获取告警组列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警组列表失败");
        }
    }

    public void addTask(TaskInfo taskInfo) throws AtlasBaseException {
        Timestamp currentTime = DateUtils.currentTimestamp();
        addDataQualityTask(currentTime, taskInfo);
    }

    @Transactional
    public void addDataQualityTask(Timestamp currentTime, TaskInfo taskInfo) throws AtlasBaseException {
        try {
            DataQualityTask dataQualityTask = new DataQualityTask();
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
            dataQualityTask.setOrangeWarningTotalCount(0);
            //redWarningCount
            dataQualityTask.setRedWarningTotalCount(0);
            //errorCount
            dataQualityTask.setErrorTotalCount(0);
            //executionCount
            dataQualityTask.setExecutionCount(0);
            //子任务
            List<TaskInfo.SubTask> subTaskList = taskInfo.getTaskList();
            addDataQualitySubTask(guid, currentTime, subTaskList);

            taskManageDAO.addTaskWarningGroup(guid, WarningType.WARNING.code, taskInfo.getContentWarningNotificationIdList());
            taskManageDAO.addTaskWarningGroup(guid, WarningType.ERROR.code, taskInfo.getExecutionWarningNotificationIdList());

            taskManageDAO.addDataQualityTask(dataQualityTask);

            taskManageDAO.updateTaskStatus(guid, 0);
            taskManageDAO.updateTaskFinishedPercent(guid, 0F);

        } catch (Exception e) {
            LOG.error("添加任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加任务失败");
        }
    }

    public void addDataQualitySubTask(String taskId, Timestamp currentTime, List<TaskInfo.SubTask> subTaskList) throws AtlasBaseException {
        try {
            for(int i=0, size=subTaskList.size(); i<size; i++) {
                TaskInfo.SubTask subTask = subTaskList.get(i);
                DataQualitySubTask dataQualitySubTask = new DataQualitySubTask();
                //id
                String guid = UUID.randomUUID().toString();
                dataQualitySubTask.setId(guid);
                //taskId
                dataQualitySubTask.setTaskId(taskId);
                //数据源类型
                dataQualitySubTask.setDataSourceType(subTask.getDataSourceType());
                //序号
                dataQualitySubTask.setSequence(i+1);
                //创建时间
                dataQualitySubTask.setCreateTime(currentTime);
                //更新时间
                dataQualitySubTask.setUpdateTime(currentTime);
                //是否已删除
                dataQualitySubTask.setDelete(false);
                //subTaskRule
                List<TaskInfo.SubTaskRule> subTaskRuleList = subTask.getSubTaskRuleList();
                addDataQualitySubTaskRule(guid, currentTime, subTaskRuleList);
                //object
                List<String> objectIdList = subTask.getObjectIdList();
                addDataQualitySubTaskObject(taskId, guid, currentTime, objectIdList);
                taskManageDAO.addDataQualitySubTask(dataQualitySubTask);
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加子任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加子任务失败");
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
            LOG.error("添加子任务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加子任务对象失败");
        }
    }

    public void addDataQualitySubTaskRule(String subTaskId, Timestamp currentTime, List<TaskInfo.SubTaskRule> subTaskRuleList) throws AtlasBaseException {
        try {
            for (int i=0,size=subTaskRuleList.size(); i<size; i++) {
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
                subTaskRule.setSequence(i+1);

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
                String unit = taskManageDAO.getTaskRuleUnit(rule.getRuleId());
                subTaskRule.setCheckThresholdUnit(unit);
                taskManageDAO.addDataQualitySubTaskRule(subTaskRule);
            }
        } catch (Exception e) {
            LOG.error("添加子任务规则失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加子任务规则失败");
        }
    }

    public void updateTask(DataQualityTask taskInfo) throws AtlasBaseException {
        Timestamp currentTime = DateUtils.currentTimestamp();
        try {
            String userId = AdminUtils.getUserData().getUserId();
            taskInfo.setUpdateTime(currentTime);
            taskInfo.setUpdater(userId);
            taskManageDAO.updateTaskInfo(taskInfo);
        } catch (Exception e) {
            LOG.error("更新任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新任务失败");
        }

    }

    public EditionTaskInfo getTaskInfo(String taskId) throws AtlasBaseException {
        try {
            EditionTaskInfo info = taskManageDAO.getTaskInfo(taskId);
            List<EditionTaskInfo.SubTask> subTaskList = taskManageDAO.getSubTaskInfo(taskId);
            for (EditionTaskInfo.SubTask subTask : subTaskList) {
                String subTaskId = subTask.getSubTaskId();
                List<EditionTaskInfo.ObjectInfo> objectInfoList = taskManageDAO.getSubTaskRelatedObject(subTaskId);
                Integer dataSourceType = subTask.getDataSourceType();
                for (EditionTaskInfo.ObjectInfo objectInfo : objectInfoList) {
                    String objectId = objectInfo.getObjectId();
                    if (0 == dataSourceType) {
                        Table tableInfo = taskManageDAO.getDbAndTableName(objectId);
                        objectInfo.setDbName(tableInfo.getDatabaseName());
                        objectInfo.setTableName(tableInfo.getTableName());
                        objectInfo.setObjectName(tableInfo.getTableName());
                    } else if (1 == dataSourceType) {
                        Column column = taskManageDAO.getDbAndTableAndColumnName(objectId);
                        objectInfo.setDbName(column.getDatabaseName());
                        objectInfo.setTableName(column.getTableName());
                        objectInfo.setObjectName(column.getColumnName());
                    } else {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                    }
                }
                subTask.setObjectIdList(objectInfoList);
                List<EditionTaskInfo.SubTaskRule> subTaskRuleList = taskManageDAO.getSubTaskRule(subTaskId);
                subTask.setSubTaskRuleList(subTaskRuleList);

            }
            info.setTaskList(subTaskList);
            List<EditionTaskInfo.WarningGroup> contentWarningGroup = taskManageDAO.getWarningGroup(taskId, 0);
            List<EditionTaskInfo.WarningGroup> errorWarningGroup = taskManageDAO.getWarningGroup(taskId, 1);
            info.setContentWarningNotificationIdList(contentWarningGroup);
            info.setExecutionWarningNotificationIdList(errorWarningGroup);


            return info;
        } catch (Exception e) {
            LOG.error("获取任务信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务信息失败");
        }
    }

    public DataQualityBasicInfo getTaskBasicInfo(String guid) throws AtlasBaseException {
        try {
            DataQualityBasicInfo basicInfo = taskManageDAO.getTaskBasicInfo(guid);
            String qrtzName = taskManageDAO.getQrtzJobByTaskId(guid);
            if(Objects.nonNull(qrtzName)) {
                String triggerName = TRIGGER_NAME + qrtzName;
                String triggerGroupName = TRIGGER_GROUP_NAME + qrtzName;

                Date lastExecuteTime = quartzManager.getJobLastExecuteTime(triggerName, triggerGroupName);
                Date nextExecuteTime = quartzManager.getJobNextExecuteTime(triggerName, triggerGroupName);
                if(Objects.nonNull(lastExecuteTime)) {
                    basicInfo.setLastExecuteTime(new Timestamp(lastExecuteTime.getTime()));
                }
                if(Objects.nonNull(nextExecuteTime)) {
                    basicInfo.setNextExecuteTime(new Timestamp(nextExecuteTime.getTime()));
                }
            }
            return basicInfo;
        } catch (Exception e) {
            LOG.error("获取任务信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务信息失败");
        }

    }


    public void startTask(String taskId) throws AtlasBaseException {
        try {
            String qrtzName = taskManageDAO.getQrtzJobByTaskId(taskId);
            if (Objects.isNull(qrtzName) || qrtzName.trim().length() == 0) {
                addQuartzJob(taskId);
            } else {
                String jobGroupName = JOB_GROUP_NAME + qrtzName;
                quartzManager.resumeJob(qrtzName, jobGroupName);
            }
            //设置任务状态为【启用】
            taskManageDAO.updateTaskEnableStatus(taskId, true);
        } catch (Exception e) {
            LOG.error("开启任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "开启任务失败");
        }
    }

    public void startTaskNow(String taskId) throws AtlasBaseException {
        try {
            String jobName = taskManageDAO.getJobName(taskId);
            String jobGroupName = JOB_GROUP_NAME + System.currentTimeMillis();
            quartzManager.addSimpleJob(jobName, jobGroupName, QuartzJob.class);
        } catch (Exception e) {
            LOG.error("开启任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "开启任务失败");
        }
    }

    public void stopTask(String taskId) throws AtlasBaseException {
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


    public void addQuartzJob(String taskId) throws AtlasBaseException {
        try {
            long currentTime = System.currentTimeMillis();
            String jobName = String.valueOf(currentTime);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            String triggerName = TRIGGER_NAME + jobName;
            String triggerGroupName = TRIGGER_GROUP_NAME + jobName;
            DataQualityTask task = taskManageDAO.getQrtzInfoByTemplateId(taskId);
            String cron = task.getCronExpression();
            Timestamp startTime = task.getStartTime();
            Timestamp endTime = task.getEndTime();
            Integer level = task.getLevel();
            if(Objects.nonNull(cron)) {
                quartzManager.addCronJobWithTimeRange(jobName, jobGroupName, triggerName, triggerGroupName, QuartzJob.class, cron, level, startTime, endTime);
            } else {
                quartzManager.addSimpleJob(jobName, jobGroupName, QuartzJob.class);
            }
            //添加qrtzName
            taskManageDAO.updateTaskQrtzName(taskId, jobName);

        } catch (Exception e) {
            LOG.error("添加任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加任务失败");
        }
    }

    public PageResult<TaskRuleHeader> getRuleList(String taskId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult();
            List<TaskRuleHeader> lists = taskManageDAO.getRuleList(taskId, parameters);
            Map<Integer, String> ruleTemplateCategoryMap = new HashMap();
            RuleTemplateType.all().stream().forEach(ruleTemplateType -> {
                ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
            });
            for (TaskRuleHeader rule : lists) {
                Integer categoryId = taskManageDAO.getRuleTypeCodeByRuleId(rule.getRuleId());
                String typeName = ruleTemplateCategoryMap.get(categoryId);
                rule.setTypeName(typeName);
            }
            long totalSize = taskManageDAO.countRuleList(taskId);
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
            taskExecutionInfo.setExecutionRecordList(executionRecordList);
            return taskExecutionInfo;
        } catch (Exception e) {
            LOG.error("获取报告详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告详情失败");
        }
    }

    public ExecutionReportData getTaskReportData(String taskId, String taskExecuteId) throws AtlasBaseException {
        try {
            ExecutionReportData resultData = new ExecutionReportData();
            //basicInfo
            ExecutionReportData.TaskBasicInfo basicInfo =  taskManageDAO.getTaskExecutionBasicInfo(taskId, taskExecuteId);
            resultData.setBasicInfo(basicInfo);
            //checkStatus
            ExecutionReportData.TaskCheckResultCount checkResultCount = new ExecutionReportData.TaskCheckResultCount();
            long tableRulePassedNumber =  taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 0, 0);
            long tableRuleNoPassedNumber =  taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 0, 1);
            long columnRulePassedNumber =  taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 1, 0);
            long columnRuleNoPassedNumber =  taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 1, 1);
            checkResultCount.setTableRulePassedNumber(tableRulePassedNumber);
            checkResultCount.setTableRuleNoPassedNumber(tableRuleNoPassedNumber);
            checkResultCount.setColumnRulePassedNumber(columnRulePassedNumber);
            checkResultCount.setColumnRuleNoPassedNumber(columnRuleNoPassedNumber);
            //error
            List<Integer> errorList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 0);
            if(null != errorList && errorList.size()>0) {
                long errorNumber = errorList.stream().filter(status -> status==2).count();
                checkResultCount.setErrorRuleNumber(errorNumber);
                checkResultCount.setTotalRuleNumber(errorList.size());
            } else {
                checkResultCount.setErrorRuleNumber(0);
                checkResultCount.setTotalRuleNumber(0);
            }
            //orangeWarning
            List<Integer> checkOrangeWarningStatusList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 1);
            if(null != checkOrangeWarningStatusList && checkOrangeWarningStatusList.size()>0) {
                long warningNumber = checkOrangeWarningStatusList.stream().filter(status -> status==1).count();
                checkResultCount.setOrangeWarningNumber(warningNumber);
                checkResultCount.setTotalOrangeWarningRuleNumber(checkOrangeWarningStatusList.size());
            } else {
                checkResultCount.setOrangeWarningNumber(0);
                checkResultCount.setTotalOrangeWarningRuleNumber(0);
            }
            //redWarning
            List<Integer> checkRedWarningStatusList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 2);
            if(null != checkRedWarningStatusList && checkRedWarningStatusList.size()>0) {
                long warningNumber = checkRedWarningStatusList.stream().filter(status -> status==1).count();
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
            List<TaskRuleExecutionRecord> executeResult = getTaskRuleExecutionRecordList(taskExecuteId);
            for (TaskRuleExecutionRecord record : executeResult) {
                if(0==record.getObjectType() && 1==record.getCheckStatus()) {
                    tableRuleSuggestion.add(record.getDescription() + suffix);
                } else if(1==record.getObjectType() && 1==record.getCheckStatus()) {
                    columnRuleSuggestion.add(record.getDescription() + suffix);
                }
                Integer sequence = taskManageDAO.getSubTaskSequence(record.getSubtaskId());
                record.setSubTaskSequence(sequence);
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

    public List<TaskRuleExecutionRecord> getTaskRuleExecutionRecordList(String executionId) throws AtlasBaseException {
        try {
            List<TaskRuleExecutionRecord> list = taskManageDAO.getTaskRuleExecutionRecordList(executionId);
            for (TaskRuleExecutionRecord record : list) {
                if(0 == record.getObjectType()) {
                    String objectId = record.getObjectId();
                    Table table = taskManageDAO.getDbAndTableName(objectId);
                    if(table == null) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到表信息，当前表已被删除!");
                    }
                    record.setDbName(table.getDatabaseName());
                    record.setTableName(table.getTableName());
                    record.setObjectName(table.getTableName());
                } else if(1 == record.getObjectType()) {
                    String objectId = record.getObjectId();
                    Column column = taskManageDAO.getDbAndTableAndColumnName(objectId);
                    if(column == null) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到字段信息，当前字段已被删除!");
                    }
                    record.setDbName(column.getDatabaseName());
                    record.setTableName(column.getTableName());
                    record.setObjectName(column.getColumnName());
                }
                Boolean filing = taskManageDAO.getFilingStatus(executionId)==0?false:true;
                record.setFiling(filing);
            }
            return list;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取报告规则详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告规则详情失败");
        }
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


}
